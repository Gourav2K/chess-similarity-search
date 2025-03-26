import os
from confluent_kafka import Producer

def get_kafka_producer_config_from_env():
    """
    Reads Kafka producer config from environment variables, with sensible defaults.
    """
    return {
        'bootstrap.servers': os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092'),
        'client.id': os.getenv('KAFKA_CLIENT_ID', 'chess-position-processor'),
        'acks': os.getenv('KAFKA_ACKS', 'all')
    }

def get_kafka_topic_from_env():
    """
    Reads Kafka topic name from environment variable, with default fallback.
    """
    return os.getenv('KAFKA_TOPIC', 'chess_positions')

def create_kafka_producer():
    """
    Creates and returns a Kafka Producer and topic using environment-based config.
    """
    config = get_kafka_producer_config_from_env()
    topic = get_kafka_topic_from_env()
    return Producer(config), topic