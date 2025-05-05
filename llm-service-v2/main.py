from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from models.game_summary_request import GameSummaryRequest, StrategyRequest
from strategy_generator import generate_per_game_summaries, generate_single_game_summary
from aggregator import aggregate_strategies
import dotenv
import os

dotenv.load_dotenv()
os.environ["OPENAI_API_KEY"] = os.getenv("OPENAI_API_KEY")
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # or ["*"] for all origins (not recommended in prod)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/analyze-strategy")
async def analyze_strategy(request: StrategyRequest):
    summaries = await generate_per_game_summaries(request)
    agg_summary = await aggregate_strategies([s["summary"] for s in summaries])
    return {
        "aggregated_summary": agg_summary,
        "per_game_summaries": summaries
    }

@app.post("/analyze-single-strategy")
async def analyze_single_strategy(request: GameSummaryRequest):
    summary = await generate_single_game_summary(request)
    return {
        "summary": summary
    }