from typing import List
from models.game_summary_request import GameSummaryRequest, StrategyRequest
from graph_builder import build_chess_strategy_graph
from langchain_openai import ChatOpenAI
import os
import dotenv

dotenv.load_dotenv()
llm = ChatOpenAI(model=os.getenv("OPENAI_MODEL"), temperature=0.5)
verifier_llm = ChatOpenAI(model=os.getenv("VERIFIER_OPENAI_MODEL"), temperature=0.5)
strategy_graph_app = build_chess_strategy_graph(llm,verifier_llm)

async def generate_per_game_summaries(request: StrategyRequest) -> List[dict]:
    summaries = []

    for position in request.positions:
        result = strategy_graph_app.invoke({
            "fen": position.fen,
            "moves": position.moves,
            "side": position.side
        })



        summaries.append({
            "game_id": position.gameId,
            "summary": result.get("formatted_strategy", "(No strategy returned)")
        })

    return summaries


async def generate_single_game_summary(position: GameSummaryRequest) -> str:
    cleaned_moves = extract_moves_from_pgn(position.moves)
    result = strategy_graph_app.invoke({
            "fen": position.fen,
            "moves": cleaned_moves,
            "side": position.side
        })

    return result.get("formatted_strategy", "(No strategy returned)")

def extract_moves_from_pgn(pgn_text: str) -> str:
    if pgn_text.strip().startswith("["):
        parts = pgn_text.strip().split("\n\n", 1)
        return parts[1].strip() if len(parts) > 1 else ""
    return pgn_text.strip()

