from typing import Dict, List, TypedDict, Any, Annotated
from langgraph.graph import StateGraph, END
from langchain_core.runnables import Runnable
from langchain_core.language_models import BaseChatModel

from agents.fen_validator import fen_validator_tool
from agents.move_simulator import move_simulator_tool
from agents.structure_extractor import structure_extractor_tool
from agents.position_feature_extractor import position_feature_extractor_tool
from agents.idea_synthesizer import idea_synthesizer_tool
from agents.strategy_formatter import strategy_formatter_tool
from agents.verifier import strategy_verifier_tool 

def build_chess_strategy_graph(llm: BaseChatModel, verifier_llm: BaseChatModel) -> Runnable:
    # Define the state with typed information using TypedDict
    class GraphState(TypedDict, total=False):
        """State for the chess strategy graph."""
        structure_output: dict
        feature_output: dict
        fen: str
        moves: str
        side: str
        move_analysis: List[Any]
        structure_insights: dict
        position_features: dict
        synthesized_ideas: str
        formatted_strategy: str
        strategy_verification: str
        synthesized_ideas_corrected: str
    
    # Initialize the state graph
    graph = StateGraph(GraphState)
    
    # Define wrapper functions for each tool to handle the state formatting correctly
    def run_fen_validator(input_state):
        print(input_state)
        tool_input = {"state": input_state}
        result = fen_validator_tool.invoke(tool_input)
        return {**input_state, **result.get("state", {})}
    
    def run_move_simulator(input_state):
        tool_input = {"state": input_state}
        result = move_simulator_tool.invoke(tool_input)
        return {**input_state, **result.get("state", {})}
    
    # Use a different approach for parallel nodes to avoid state conflicts
    
    # First parallel branch only returns structure_output
    def run_structure_extractor(input_state):
        # Create a copy of only the necessary state for this tool
        tool_state = {
            "move_analysis": input_state.get("move_analysis", ""),
        }
        tool_input = {"state": tool_state}
        result = structure_extractor_tool.invoke(tool_input)
        # Only return the specific key we're updating
        return {"structure_output": result.structure_insights}
    
    # Second parallel branch only returns feature_output
    def run_position_feature_extractor(input_state):
        # Create a copy of only the necessary state for this tool
        tool_state = {
            "fen": input_state.get("fen", ""),
        }
        tool_input = {"state": tool_state}
        result = position_feature_extractor_tool.invoke(tool_input)
        # Only return the specific key we're updating
        return {"feature_output": result.position_features}
    
    # Join node that combines results from both parallel paths
    def join_results(state):
        # Combine the parallel results with the original state
        structure_output = state.get("structure_output", {})
        feature_output = state.get("feature_output", {})
        
        # Create a new state with the combined results
        return {
            **state,
            "structure_insights": structure_output,
            "position_features": feature_output
        }
    
    # Add the wrapped nodes
    graph.add_node("fen_validator", run_fen_validator)
    graph.add_node("move_simulator", run_move_simulator)
    graph.add_node("structure_extractor", run_structure_extractor)
    graph.add_node("position_feature_extractor", run_position_feature_extractor)
    graph.add_node("join", join_results)
    
    # Wrap the idea synthesizer to handle state format
    def run_idea_synthesizer(input_state):
        tool_input = {
            "state": input_state,
            "llm": llm
        }
        result = idea_synthesizer_tool.invoke(tool_input)
        return {**input_state, **result.get("state", {})}
    
    graph.add_node("idea_synthesizer", run_idea_synthesizer)

    def run_verifier(input_state):
        tool_input = {
            "state": input_state,
            "llm": llm,
            "verifier_llm":verifier_llm
        }
        result = strategy_verifier_tool.invoke(tool_input)
        return {**input_state, **result.get("state", {})}

    graph.add_node("verifier", run_verifier)
    
    # Wrap the strategy formatter
    def run_strategy_formatter(input_state):
        result = strategy_formatter_tool.invoke({"state": input_state})
        return {**input_state, **result.get("state", {})}
    
    graph.add_node("strategy_formatter", run_strategy_formatter)
    
    # Set up the graph edges
    graph.set_entry_point("fen_validator")
    graph.add_edge("fen_validator", "move_simulator")
    
    # Connect to parallel nodes
    graph.add_edge("move_simulator", "structure_extractor")
    graph.add_edge("move_simulator", "position_feature_extractor")
    
    # Join the parallel branches properly
    graph.add_edge("structure_extractor", "join")
    graph.add_edge("position_feature_extractor", "join")
    
    # Connect the rest of the graph
    graph.add_edge("join", "idea_synthesizer")
    graph.add_edge("idea_synthesizer", "verifier")
    graph.add_edge("verifier", "strategy_formatter")
    graph.add_edge("strategy_formatter", END)
    
    return graph.compile()