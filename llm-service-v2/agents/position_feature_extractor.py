from langchain_core.tools import tool
from typing import Dict
from typing_extensions import Annotated
import chess
from pydantic import BaseModel, Field

class PositionFeaturesOutput(BaseModel):
    position_features: Dict

@tool
def position_feature_extractor_tool(state: Dict) ->  Annotated[Dict, "position_features"]:
    """
    Analyzes the given FEN and extracts position-specific features:
    - king safety
    - center control
    - pawn structure
    - open/semi-open files
    - bishop pair
    Adds these insights to state["position_features"]
    """
    fen = state.get("fen")
    if not fen:
        print("FEN not passed — skipping FEN validation.")
        return PositionFeaturesOutput(position_features={})

    board = chess.Board(fen)
    features = {}

    # 1. King Safety
    def king_safety(color):
        king_square = board.king(color)
        rank = chess.square_rank(king_square)
        file = chess.square_file(king_square)
        print("KIng rank:", rank, "king File: ", file)
        castled = (file in [6, 2])  # kingside or queenside

        pawn_shield = 0
        directions = [-1, 0, 1]
        for df in directions:
            sq = chess.square(file + df, rank + 1 if color == chess.WHITE else rank - 1)
            if board.piece_at(sq) == chess.Piece(chess.PAWN, color):
                pawn_shield += 1

        if castled and pawn_shield == 3:
            return "castled_with_shield"
        elif castled and pawn_shield >= 1:
            return "castled_partial_shield"
        elif not castled:
            return "uncastled"
        else:
            return "exposed"

    features["white_king_safety"] = king_safety(chess.WHITE)
    features["black_king_safety"] = king_safety(chess.BLACK)

    # 2. Center Control
    central_squares = [chess.D4, chess.E4, chess.D5, chess.E5]
    features["center_control"] = {"white": [], "black": []}
    for sq in central_squares:
        attackers_w = board.attackers(chess.WHITE, sq)
        attackers_b = board.attackers(chess.BLACK, sq)
        if attackers_w:
            features["center_control"]["white"].append(chess.square_name(sq))
        if attackers_b:
            features["center_control"]["black"].append(chess.square_name(sq))

    # 3. Bishop Pair
    def has_bishop_pair(color):
        bishops = [sq for sq in board.pieces(chess.BISHOP, color)]
        return len(bishops) == 2

    features["white_has_bishop_pair"] = has_bishop_pair(chess.WHITE)
    features["black_has_bishop_pair"] = has_bishop_pair(chess.BLACK)

    # 4. Open and Semi-Open Files
    def file_status(color):
        open_files = []
        semi_open = []
        for file_index in range(8):
            file_squares = [chess.square(file_index, r) for r in range(8)]
            own_pawns = any(
                board.piece_at(sq) == chess.Piece(chess.PAWN, color) for sq in file_squares
            )
            opp_pawns = any(
                board.piece_at(sq) == chess.Piece(chess.PAWN, not color) for sq in file_squares
            )
            file_letter = chr(file_index + ord("a"))
            if not own_pawns and not opp_pawns:
                open_files.append(file_letter)
            elif not own_pawns:
                semi_open.append(file_letter)
        return open_files, semi_open

    wf_open, wf_semi = file_status(chess.WHITE)
    bf_open, bf_semi = file_status(chess.BLACK)
    features["open_files"] = list(set(wf_open) & set(bf_open))
    features["white_semi_open_files"] = wf_semi
    features["black_semi_open_files"] = bf_semi

    #state["position_features"] = features

    print("Position Feature Extractor : ", features)
    return PositionFeaturesOutput(position_features=features)
