import chess

def convert_position_to_dto(board):
    """
    Convert a chess.Board position to the format expected by the Spring DTO
    using array format for all multi-piece squares.
    """
    # Kings (single squares)
    white_king_squares = list(board.pieces(chess.KING, chess.WHITE))
    black_king_squares = list(board.pieces(chess.KING, chess.BLACK))

    # Major and minor pieces as arrays of integers
    white_queens = list(board.pieces(chess.QUEEN, chess.WHITE))
    white_rooks = list(board.pieces(chess.ROOK, chess.WHITE))
    white_bishops = list(board.pieces(chess.BISHOP, chess.WHITE))
    white_knights = list(board.pieces(chess.KNIGHT, chess.WHITE))

    black_queens = list(board.pieces(chess.QUEEN, chess.BLACK))
    black_rooks = list(board.pieces(chess.ROOK, chess.BLACK))
    black_bishops = list(board.pieces(chess.BISHOP, chess.BLACK))
    black_knights = list(board.pieces(chess.KNIGHT, chess.BLACK))

    # Pawns as bitboard integers
    white_pawns_bitboard = bitboard_to_int(board.pieces(chess.PAWN, chess.WHITE))
    black_pawns_bitboard = bitboard_to_int(board.pieces(chess.PAWN, chess.BLACK))

    # Castling rights (bitmask)
    castling_rights = (
        (1 if board.has_kingside_castling_rights(chess.WHITE) else 0) +
        (2 if board.has_queenside_castling_rights(chess.WHITE) else 0) +
        (4 if board.has_kingside_castling_rights(chess.BLACK) else 0) +
        (8 if board.has_queenside_castling_rights(chess.BLACK) else 0)
    )

    return {
        "whiteKing": white_king_squares[0] if white_king_squares else None,
        "blackKing": black_king_squares[0] if black_king_squares else None,
        "whiteQueens": white_queens,
        "whiteRooks": white_rooks,
        "whiteBishops": white_bishops,
        "whiteKnights": white_knights,
        "blackQueens": black_queens,
        "blackRooks": black_rooks,
        "blackBishops": black_bishops,
        "blackKnights": black_knights,
        "whitePawns": white_pawns_bitboard,
        "blackPawns": black_pawns_bitboard,
        "sideToMove": "w" if board.turn == chess.WHITE else "b",
        "castlingRights": castling_rights,
        "enPassantSquare": board.ep_square if board.ep_square is not None else 0,
        "fullmoveNumber": board.fullmove_number,
        "fen": board.fen()
    }

def bitboard_to_int(bitboard):
    return int(bitboard)