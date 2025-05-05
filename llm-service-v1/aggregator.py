from openai import OpenAI
import os
from dotenv import load_dotenv
from typing import List

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

async def aggregate_strategies(summaries: List[str]) -> str:
    joined = "\n".join(summaries)
    prompt = f"""
    Below are summaries of strategies from multiple games. Your task is to synthesize a **tactical roadmap** that captures the most common and actionable ideas shared across games.

    Summaries:
    {joined}

    Output:
    - A short 2–3 line high-level goal
    - 4–6 bullet points capturing recurring patterns: key maneuvers, typical threats, pawn breaks, open file strategies, piece coordination plans
    - Do **not** repeat full sentences from the input. Consolidate and abstract over them.
    """

    response = client.chat.completions.create(
        model=os.getenv("OPENAI_MODEL"),
        messages=[
            {"role": "system", "content": "You are a chess analyst trained to extract common plans across games."},
            {"role": "user", "content": prompt}
        ],
        temperature=0.5
    )

    return response.choices[0].message.content.strip()