import io
import chess
import chess.pgn
from utils.bitboard_converter import convert_position_to_dto

def extract_positions_from_game(pgn_text, position_frequency=5):
    """
    Extract positions from a chess game with specific PGN format.
    
    :param pgn_text: Raw PGN text
    :param position_frequency: Extract a position every N moves
    :return: Dictionary with game metadata and positions or None
    """
    # Split the input into headers and move text
    parts = pgn_text.rsplit('\n', 1)
    min_move = 6
    max_move = 80
    
    if len(parts) < 2:
        print(f"Incomplete PGN: {pgn_text}")
        return None
    
    headers_text, moves_text = parts
    
    # Parse headers
    headers = {}
    for line in headers_text.split('\n'):
        if line.startswith('[') and line.endswith(']'):
            # Remove brackets and split
            header_parts = line[1:-1].split(' ', 1)
            if len(header_parts) == 2:
                key = header_parts[0]
                # Remove quotes from value
                value = header_parts[1].strip('"')
                headers[key] = value
    
    # Extract move text and result
    moves_result = moves_text.strip()
    result = moves_result[-3:]  # Last 3 characters are the result
    moves = moves_result[:-3].strip()
    
    # Prepare full PGN
    full_pgn = f"{headers_text}\n\n{moves} {result}"
    
    # Create StringIO for parsing
    pgn = io.StringIO(full_pgn)
    
    try:
        # Read game
        game = chess.pgn.read_game(pgn)
        
        if game is None:
            print(f"Could not parse game from PGN:\n{full_pgn}")
            return None

        MAX_PGN_LENGTH = 650
        raw_pgn = f"{moves} {result}"

        # Smart trimming: don't cut mid-move
        if len(raw_pgn) > MAX_PGN_LENGTH:
            trimmed_pgn = raw_pgn[:MAX_PGN_LENGTH].rsplit(' ', 1)[0]
        else:
            trimmed_pgn = raw_pgn
        
        # Extract game metadata
        game_data = {
            "result": result,
            "whiteElo": int(headers.get("WhiteElo", "0")),
            "blackElo": int(headers.get("BlackElo", "0")),
            "gameType": "unknown",
            "date": headers.get("UTCDate", headers.get("Date", "")),
            "whiteName": headers.get("White", ""),
            "blackName": headers.get("Black", ""),
            "eco": headers.get("ECO", ""),
            "timeControl": headers.get("TimeControl", ""),
            "opening":headers.get("Opening", ""),
            "site":headers.get("Site", ""),
            "pgn":trimmed_pgn
        }
        
        # Determine game type
        event = headers.get("Event", "")
        for word in event.split():
            if word.lower() in ["rapid", "blitz", "bullet", "classical"]:
                game_data["gameType"] = word.lower()
                break
        
        # Skip abandoned or incomplete games
        if (headers.get("Termination", "") == "Abandoned" or headers.get("Termination", "") == "Time forfeit" or
            not any(game.mainline_moves())):
            print(f"Skipping abandoned or incomplete game")
            return None
        
        # Extract positions
        positions = []
        board = game.board()
        move_count = 0
        
        # Add positions at regular intervals
        for move in game.mainline_moves():
            board.push(move)
            move_count += 1

            # Only extract positions between min_move and max_move
            if min_move <= move_count <= max_move:
                if (move_count - min_move) % position_frequency == 0:
                    positions.append({
                        "moveNumber": move_count,
                        **convert_position_to_dto(board)
                    })
            
            # Stop if we've reached the maximum move
            if move_count >= max_move:
                break
        
        return {
            "gameMetadata": game_data,
            "positions": positions
        }
    
    except Exception as e:
        print(f"Error parsing PGN: {e}")
        print(f"Problematic PGN:\n{full_pgn}")
        return None

def process_pgn_file(pgn_file_path, max_games=200000, position_frequency=5, skip_games = 0, redis_client = None, redis_key = None):
    """
    Process entire PGN file line by line and yield game data.
    
    :param pgn_file_path: Path to PGN file
    :param max_games: Maximum number of games to process
    :param position_frequency: Extract a position every N moves
    :yield: Processed game data
    """
    with open(pgn_file_path, 'r', encoding='utf-8') as file:
        current_game = []
        games_processed = 0
        line_breaks = 0
        
        for line in file:
            # Strip whitespace
            line = line.strip()
            
            # Check for empty line
            if not line:
                line_breaks += 1
                
                # Second line break indicates complete game
                if line_breaks == 2:
                    # Process the current game
                    if current_game:

                        if games_processed < skip_games:
                            games_processed += 1
                            current_game = []
                            line_breaks = 0
                            continue

                        # Join the game lines and process
                        game_text = '\n'.join(current_game)
                        print("Game Text : ", game_text)
                        game_data = extract_positions_from_game(game_text, position_frequency)
                        
                        if game_data:
                            games_processed += 1

                            # Yield the game data
                            yield game_data
                            
                            # Print some info about each processed game
                            print(f"Processed Game {games_processed}:")
                            print(f"  White: {game_data['gameMetadata']['whiteName']}")
                            print(f"  Black: {game_data['gameMetadata']['blackName']}")
                            print(f"  Result: {game_data['gameMetadata']['result']}")
                            print(f"  Positions Extracted: {len(game_data['positions'])}")
                            print("-" * 40)
                            
                            if redis_client and redis_key:
                                redis_client.set(redis_key, games_processed)

                            # Stop processing if we've reached max_games
                            if games_processed >= max_games:
                                break
                    
                    # Reset for next game
                    current_game = []
                    line_breaks = 0
                    continue
            else:
                current_game.append(line)
        
        print(f"Total games processed: {games_processed}")