from typing import Dict
from typing_extensions import Annotated
from langchain_core.tools import tool
import chess

@tool
def fen_validator_tool(state: Annotated[Dict, "state"]) -> dict:
    """
    Validates a FEN string and returns chess board details such as side to move,
    piece placement, check status, and halfmove clock.
    """
    fen = state.get("fen")
    if not fen:
        print("FEN not passed — skipping FEN validation.")
        return {"state": state}

    try:
        board = chess.Board(state["fen"])
        state["side"] = "white" if board.turn else "black"
        state["board_summary"] = {
            "piece_map": {square: str(board.piece_at(square)) for square in board.piece_map()},
            "is_check": board.is_check(),
            "halfmove_clock": board.halfmove_clock,
        }
        print("State after FEN Validator: ", state)
        return {"state": state}
    except Exception as e:
        raise ValueError(f"Invalid FEN: {e}")

## Testing
# if __name__ == "__main__":
#     print(fen_validator_tool.invoke({"state": {"fen": "2kr3r/pp5p/2Pb2pQ/3p4/4pPq1/2N5/P1P3PP/R3R1K1 b - - 0 21"}}))
