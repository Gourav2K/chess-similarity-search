import json
from config.kafka_config import create_kafka_producer

def delivery_report(err, msg):
    """
    Callback to handle message delivery reports.

    :param err: Delivery error (if any)
    :param msg: Delivered message
    """
    if err is not None:
        print(f'Message delivery failed: {err}')
    else:
        print(f'Message delivered to {msg.topic()} [{msg.partition()}]')

class KafkaPublisher:
    def __init__(self):
        """
        Initialize Kafka publisher.
        """
        self.producer, self.topic = create_kafka_producer()

    def publish_game_data(self, game_data):
        """
        Publish game data to Kafka topic.

        :param game_data: Game data dictionary
        """
        try:
            # Generate a unique key (you might want to use game metadata for this)
            key = game_data['gameMetadata'].get('date', '') + '_' + \
                  game_data['gameMetadata'].get('whiteName', '') + '_' + \
                  game_data['gameMetadata'].get('blackName', '')

            # Convert data to JSON string
            value = json.dumps(game_data)

            # Publish message
            self.producer.produce(
                self.topic,
                key=key,
                value=value,
                callback=delivery_report
            )

            # Flush to ensure message is sent
            self.producer.flush()

            return True
        except Exception as e:
            print(f"Error publishing to Kafka: {e}")
            return False
