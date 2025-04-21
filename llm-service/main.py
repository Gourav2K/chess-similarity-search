from fastapi import FastAPI
from models import StrategyRequest
from strategy_generator import generate_per_game_summaries
from aggregator import aggregate_strategies

app = FastAPI()

@app.post("/analyze-strategy")
async def analyze_strategy(request: StrategyRequest):
    summaries = await generate_per_game_summaries(request)
    agg_summary = await aggregate_strategies([s["summary"] for s in summaries])
    return {
        "aggregated_summary": agg_summary,
        "per_game_summaries": summaries
    }