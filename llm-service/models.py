from pydantic import BaseModel
from typing import List, Literal

class GameSummaryRequest(BaseModel):
    gameId: str
    fen: str
    moves: str
    side: Literal["white", "black"]

class StrategyRequest(BaseModel):
    positions: List[GameSummaryRequest]