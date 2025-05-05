from typing import List, Dict
import chess
import re

from langchain_core.tools import tool

@tool
def move_simulator_tool(state: Dict) -> Dict:
    """
    Simulates SAN moves from a given FEN position. 
    Requires `fen` and `moves` keys in the input state.
    Returns structured metadata for each move under `move_analysis`.
    """
    fen = state.get("fen") or chess.STARTING_FEN
    print(fen)
    move_sequence = state.get("moves")

    if not move_sequence:
        raise ValueError("'moves' must be provided in the state.")

    board = chess.Board(fen)
    move_data = []

    # Prepare move parsing
    move_sequence_cleaned = re.sub(r"\d+\.(\.\.)?", "", move_sequence)
    move_sequence_cleaned = re.sub(r"1-0|0-1|1/2-1/2|\\*", "", move_sequence_cleaned)
    san_moves = move_sequence_cleaned.strip().split()

    fields = fen.strip().split()
    starting_move_number = int(fields[-1]) if fen != chess.STARTING_FEN else 1               # fullmove number from FEN
    side_to_move = fields[1] if fen != chess.STARTING_FEN else "w"                           # 'w' or 'b'
    halfmove = 0 if side_to_move == 'w' else 1           # offset start

    for san in san_moves:
        try:
            move = board.parse_san(san)
        except ValueError:
            raise ValueError(f"Illegal move '{san}' on board: {board.fen()}")

        uci = move.uci()
        source_square = uci[:2]
        target_square = uci[2:4]
        captured = board.is_capture(move)
        captured_piece = board.piece_at(chess.parse_square(target_square)) if captured else None
        captured_str = captured_piece.symbol() if captured_piece else None

        move_data.append({
            "move_number": (halfmove // 2) + starting_move_number,
            "player": "White" if halfmove % 2 == 0 else "Black",
            "san": san,
            "uci": uci,
            "from": source_square,
            "to": target_square,
            "captured": captured,
            "captured_piece": captured_str,
        })

        board.push(move)
        halfmove += 1

    state["move_analysis"] = move_data
    print("State after Move Simulator: ", state)
    return {"state": state}

# # Testing the tool
# if __name__ == "__main__":
#     result = move_simulator_tool.invoke({
#     "state": {
#         "fen": "2kr3r/pp5p/2Pb2pQ/3p4/4pPq1/2N5/P1P3PP/R3R1K1 b - - 0 21",
#         "moves": "bxc6 22. Ne2 Rdf8 23. Rab1 Bxf4 24. Nxf4 Qxf4 25. Qg7 Qf2+ 26. Kh1 Qf7 27. Qe5 Qc7 28. Qe6+ Kd8 29. h3 Re8 30. Qf6+ Qe7 31. Qxc6 Qc7 32. Qxd5+ Qd7 33. Rb8+ Kc7 34. Rb6 Qxd5"
#         }
#     })
#     from pprint import pprint
#     pprint(result["state"]["move_analysis"])