import chess

def convert_position_to_dto(board):
    """
    Convert a chess.Board position to the format expected by the Spring DTO.
    
    :param board: chess.Board object
    :param game_id: Optional game ID
    :param move_number: Optional move number
    :param position_id: Optional position ID
    :return: Dictionary with position information in DTO format
    """
    # Kings are represented as individual square indices
    white_king_squares = list(board.pieces(chess.KING, chess.WHITE))
    black_king_squares = list(board.pieces(chess.KING, chess.BLACK))
    
    # Major pieces are represented as comma-separated square indices
    white_queens = comma_separated_squares(board.pieces(chess.QUEEN, chess.WHITE))
    white_rooks = comma_separated_squares(board.pieces(chess.ROOK, chess.WHITE))
    white_bishops = comma_separated_squares(board.pieces(chess.BISHOP, chess.WHITE))
    white_knights = comma_separated_squares(board.pieces(chess.KNIGHT, chess.WHITE))
    
    black_queens = comma_separated_squares(board.pieces(chess.QUEEN, chess.BLACK))
    black_rooks = comma_separated_squares(board.pieces(chess.ROOK, chess.BLACK))
    black_bishops = comma_separated_squares(board.pieces(chess.BISHOP, chess.BLACK))
    black_knights = comma_separated_squares(board.pieces(chess.KNIGHT, chess.BLACK))
    
    # Pawns are represented as bitboards (long integers)
    white_pawns_bitboard = bitboard_to_int(board.pieces(chess.PAWN, chess.WHITE))
    black_pawns_bitboard = bitboard_to_int(board.pieces(chess.PAWN, chess.BLACK))
    
    # Castling rights as an integer (0-15)
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

def comma_separated_squares(square_set):
    """
    Convert a chess.SquareSet to a comma-separated string of square indices
    
    :param square_set: chess.SquareSet of pieces
    :return: Comma-separated string of square indices, or empty string if no pieces
    """
    if not square_set:
        return ""
    return ",".join(str(square) for square in square_set)

def bitboard_to_int(bitboard):
    """
    Convert a chess.SquareSet to its integer (bitboard) representation
    
    :param bitboard: chess.SquareSet of pieces
    :return: Integer representation of the bitboard
    """
    return int(bitboard)