import argparse
from services.pgn_parser import process_pgn_file
from services.kafka_publisher import KafkaPublisher
from services.api_publisher import APIPublisher
import redis
from dotenv import load_dotenv
import os

# Load the environment file
if os.path.exists(".env.local") and not os.getenv("RUNNING_IN_DOCKER"):
    load_dotenv(dotenv_path=".env.local")
else:
    load_dotenv(dotenv_path=".env")

def main():
    # Set up argument parsing
    parser = argparse.ArgumentParser(description='Process chess game PGN file')
    parser.add_argument('pgn_file', help='Path to the PGN file')
    parser.add_argument('--method', choices=['kafka', 'api'], default='kafka', 
                        help='Publishing method (default: kafka)')
    parser.add_argument('--batch-size', type=int, default=100, 
                        help='Number of games to process in a batch')
    parser.add_argument('--position-freq', type=int, default=5, 
                        help='Extract position every N moves')
    parser.add_argument('--max-games', type=int, default=200000,
                        help="Max number of games that you want it to read from the pgn file")
    
    args = parser.parse_args()
    
    # Choose publisher based on method
    if args.method == 'kafka':
        publisher = KafkaPublisher()
    else:
        publisher = APIPublisher()

    # Redis connect to check games read
    redis_client = redis.Redis(
        host=os.getenv("REDIS_HOST", "localhost"),
        port=int(os.getenv("REDIS_PORT", 6379)),
        db=int(os.getenv("REDIS_DB", 0))
    )
    redis_key = "chess_pgndata:games_published"
    games_already_processed = int(redis_client.get(redis_key) or 0)
    
    # Process games
    try:
        batch = []
        for game_data in process_pgn_file(args.pgn_file, position_frequency= args.position_freq, max_games=args.max_games,
                                           skip_games=games_already_processed, redis_client=redis_client, redis_key=redis_key):
            batch.append(game_data)
            
            # Publish batch when size is reached
            if len(batch) >= args.batch_size:
                for game in batch:
                    publisher.publish_game_data(game)
                batch = []
        
        # Publish any remaining games
        for game in batch:
            publisher.publish_game_data(game)
    
    except Exception as e:
        print(f"Error processing PGN file: {e}")
    
    finally:
        # Cleanup if needed
        if hasattr(publisher, 'close'):
            publisher.close()

if __name__ == "__main__":
    main()