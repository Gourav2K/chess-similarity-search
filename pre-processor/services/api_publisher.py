import requests
import json

class APIPublisher:
    def __init__(self, base_url='http://localhost:8080/api/games/batch'):
        """
        Initialize API publisher.
        
        :param base_url: Base URL for API endpoint
        """
        self.base_url = base_url
    
    def publish_game_data(self, game_data):
        """
        Publish game data via REST API.
        
        :param game_data: Game data dictionary
        :return: Whether publication was successful
        """
        try:
            headers = {'Content-Type': 'application/json'}
            response = requests.post(
                self.base_url, 
                data=json.dumps(game_data), 
                headers=headers
            )
            
            if response.status_code == 200:
                print(f"Successfully published game to API: {response.text}")
                return True
            else:
                print(f"Failed to publish game. Status code: {response.status_code}, Response: {response.text}")
                return False
        except requests.exceptions.RequestException as e:
            print(f"Error publishing to API: {e}")
            return False