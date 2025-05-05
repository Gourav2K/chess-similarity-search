from openai import OpenAI
import os
from models import StrategyRequest
from dotenv import load_dotenv

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

async def generate_per_game_summaries(request: StrategyRequest) -> list[dict]:
    summaries = []
    i = 1
    for position in request.positions:
        prompt = f"""
        You are a chess strategist. Your task is to analyze the following position and move sequence, and create a clear, objective strategy that the player should follow.

        FEN (starting position): {position.fen}  
        Following moves: {position.moves}

        Please outline the short-term strategic plan for {position.side} in the next 10–15 moves. Your output should be:
        - A short summary of the strategic goal
        - A bullet-point roadmap of **specific ideas**, such as piece maneuvers, open files, key squares, and weaknesses
        - Keep the suggestions **concrete and actionable**, not generic advice
        - Do NOT include summaries, or closing remarks
        """

        response = client.chat.completions.create(
            model=os.getenv("OPENAI_MODEL"),
            messages=[
                {"role": "system", "content": "You are a chess assistant that analyzes strategies from a given position and move sequence."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.7
        )

        print(i,response)
        i+=1

        content = response.choices[0].message.content.strip()

        summaries.append({
            "game_id": position.gameId,
            "summary": content
        })


    return summaries