from langchain_core.tools import tool
from langchain_core.language_models import BaseChatModel
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import Runnable
from typing import Dict
import json

@tool
def strategy_verifier_tool(state: Dict, llm: BaseChatModel, verifier_llm: BaseChatModel) -> Dict:
    """
    Verifies and optionally corrects hallucinated or invalid strategies.
    If strategy is invalid, asks the LLM to rewrite it based on position and structure.
    """

    print("Inside Strategy Verifier")
    strategy = state.get("synthesized_ideas", "")
    position = state.get("position_features", {})
    structure = state.get("structure_insights", [])
    fen = state.get("fen", "")
    side = state.get("side", "")
    moves = state.get("moves", "")

    if not strategy:
        raise ValueError("Missing synthesized strategy in state")

    # First prompt: Ask LLM to critique the strategy
    critique_prompt = ChatPromptTemplate.from_messages([
        ("system", 
         "You are a strict chess expert and a verifier. Given a strategy, identify if it aligns with the position."),
        ("user", 
         "FEN: {fen}\n"
         "Side to play: {side}\n"
         "Position Features:\n{position}\n"
         "Structure Insights:\n{structure}\n"
         "Moves:\n{moves}\n\n"
         "Strategy to verify:\n{strategy}\n\n"
         "Check the strategy for hallucinations or contradictions.\n"
         "Return JSON:\n"
         "- verdict: 'valid' or 'needs_correction'\n"
         "- issues: list of detected problems"
        )
    ])
    critique_chain: Runnable = critique_prompt | verifier_llm
    critique_response = critique_chain.invoke({
        "fen": fen,
        "side": side,
        "position": position,
        "structure": structure,
        "moves": moves,
        "strategy": strategy
    })

    state["strategy_verification"] = critique_response.content.strip()

    # Try parsing the response
    try:
        feedback = json.loads(critique_response.content)
    except json.JSONDecodeError:
        feedback = {"verdict": "needs_correction", "issues": ["Invalid JSON from LLM"]}

    print("Feedback - ", feedback)
    # If correction is needed, do it
    if feedback.get("verdict") == "needs_correction":
        correction_prompt = ChatPromptTemplate.from_messages([
            ("system", 
             "You are a chess strategist. Rewrite the plan to fix the listed issues."),
            ("user",
             "Original Strategy:\n{strategy}\n\n"
             "Issues:\n{issues}\n\n"
             "Please correct the strategy so it aligns with the FEN and structure insights.\n"
             "New output should ONLY be a corrected strategy with goal + bullet points.")
        ])
        correction_chain: Runnable = correction_prompt | llm
        corrected = correction_chain.invoke({
            "strategy": strategy,
            "issues": "\n".join(feedback.get("issues", []))
        })

        state["synthesized_ideas_corrected"] = corrected.content.strip()
        state["synthesized_ideas"] = corrected.content.strip()
        state["strategy_verification"] += "\n\n[Auto-corrected ✅]"

    return {"state": state}
