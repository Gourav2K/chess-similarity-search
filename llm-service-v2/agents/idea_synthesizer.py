from langchain_core.tools import tool
from typing import Dict
from langchain_core.runnables import Runnable
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.language_models import BaseChatModel

@tool
def idea_synthesizer_tool(state: Dict, llm: BaseChatModel) -> Dict:
    """
    Synthesizes high-level positional ideas and motifs from structure_insights + position_features.
    Returns a ranked list of strategic ideas for the given side (white/black).
    """
    structure = state.get("structure_insights", [])
    position = state.get("position_features", {})
    side = state.get("side")  # 'white' or 'black'
    fen = state.get("fen")
    pgn = state.get("moves")

    if not structure or not side:
        raise ValueError("Missing one or more of: structure_insights, position_features, side")

    prompt = ChatPromptTemplate.from_messages([
    ("system", 
     "You are a chess strategist. Your task is to analyze the chess game shared with you with the help of the structural and positional insights below and generate a strategic plan for {side}."),
     
    ("user", 
     "FEN of current position - {fen}\n"
     "POSITION FEATURES of above position:\n{position}\n\n"
     "PGN sequence of moves after the above position - {pgn}\n"
     "STRUCTURE INSIGHTS for moves:\n{structure}\n\n"
     "Carefully go through and analyze the game above move by move. Take help of the insights provided.\n"
     "Please produce the following:\n"
     "- A short summary of the strategic goal for {side}\n"
     "- A bullet-point roadmap of **specific strategic ideas**, such as:\n"
     "  - pawn breaks\n"
     "  - piece placements\n"
     "  - open files\n"
     "  - targets or weaknesses to attack or defend\n"
     "- Each bullet should be **concrete, actionable, and free of vague advice**\n"
     "- Do NOT include conclusions, closing remarks, or phrases like 'In summary' or 'Overall'."
    )
])

    chain: Runnable = prompt | llm

    formatted = chain.invoke({
        "side": side,
        "structure": structure,
        "position": position,
        "fen":fen,
        "pgn": pgn
    })

    state["synthesized_ideas"] = formatted.content.strip()
    print("State after Idea Generator: ", state)
    return {"state": state}
