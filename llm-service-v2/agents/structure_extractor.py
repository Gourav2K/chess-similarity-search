from langchain_core.tools import tool
from typing import Dict, List
from typing_extensions import Annotated
from pydantic import BaseModel, Field

class StructureInsightsOutput(BaseModel):
    structure_insights: List[Dict]

@tool
def structure_extractor_tool(state: Dict) -> Annotated[Dict, "structure_insights"]:
    """
    Extracts piece-specific structural patterns from move_analysis.
    For each move, determine:
    - Was a pawn pushed?
    - Was a file opened or closed?
    - Was a capture central, flank, or backward?
    - Did a rook/queen enter a file?
    - Provide human-readable capture narration
    Updates state with `structure_insights` list.
    """

    move_analysis: List[Dict] = state.get("move_analysis", [])
    if not move_analysis:
        raise ValueError("Missing move_analysis in state")

    structure_insights = []

    symbol_map = {
        "p": "pawn",
        "n": "knight",
        "b": "bishop",
        "r": "rook",
        "q": "queen",
        "k": "king",
    }

    for move in move_analysis:
        insight = {
            "move_number": move["move_number"],
            "player": move["player"],
            "san": move["san"],
            "uci": move["uci"],
            "insights": []
        }

        from_sq = move["from"]
        to_sq = move["to"]
        piece = move["san"][0] if move["san"][0].isupper() else "P"
        file_diff = abs(ord(from_sq[0]) - ord(to_sq[0]))
        rank_diff = abs(int(from_sq[1]) - int(to_sq[1]))

        # Detect pawn push
        if piece == "P":
            if rank_diff >= 1:
                insight["insights"].append("pawn_push")
                if file_diff > 0:
                    insight["insights"].append("pawn_diagonal_push")

        # Detect central or flank movement
        piece_full = symbol_map.get(piece.lower(), "pawn").capitalize()
        movement_desc = f"{piece_full} moved from {from_sq} to {to_sq}"

        if to_sq[0] in "decf":
            insight["insights"].append(f"central_file movement - {movement_desc}")
        elif to_sq[0] in "abhg":
            insight["insights"].append(f"flank_file movement - {movement_desc}")
            
        # Detect rook/queen file entry
        if piece in ["R", "Q"]:
            insight["insights"].append(f"{piece.lower()}_file_activation")

        # Detect capture logic
        if move["captured"]:
            if to_sq[0] in "abgh":
                insight["insights"].append("flank_capture")
            elif to_sq[0] in "cdef":
                insight["insights"].append("central_capture")

            # Determine attacker piece more accurately
            if move["san"][0].islower():  # e.g., 'bxc6' means pawn capture from file b
                attacker_piece = "pawn"
            else:
                attacker_piece = symbol_map.get(move["san"][0].lower(), "unknown piece")
            captured_piece = symbol_map.get((move["captured_piece"] or '').lower(), "unknown piece")
            insight["capture_narration"] = (
                f"{attacker_piece.capitalize()} on {from_sq} captures {captured_piece} on {to_sq}."
            )

        structure_insights.append(insight)

    #state["structure_insights"] = structure_insights
    print("Structure Insights : ", structure_insights)
    return StructureInsightsOutput(structure_insights=structure_insights)


# def main():
#     dummy_moves = [
#         {'captured': True, 'captured_piece': 'P', 'from': 'b7', 'move_number': 21, 'player': 'Black', 'san': 'bxc6', 'to': 'c6', 'uci': 'b7c6'},
#         {'captured': False, 'captured_piece': None, 'from': 'c3', 'move_number': 22, 'player': 'White', 'san': 'Ne2', 'to': 'e2', 'uci': 'c3e2'},
#         {'captured': False, 'captured_piece': None, 'from': 'd8', 'move_number': 22, 'player': 'Black', 'san': 'Rdf8', 'to': 'f8', 'uci': 'd8f8'},
#         {'captured': False, 'captured_piece': None, 'from': 'a1', 'move_number': 23, 'player': 'White', 'san': 'Rab1', 'to': 'b1', 'uci': 'a1b1'},
#         {'captured': True, 'captured_piece': 'P', 'from': 'd6', 'move_number': 23, 'player': 'Black', 'san': 'Bxf4', 'to': 'f4', 'uci': 'd6f4'},
#         {'captured': True, 'captured_piece': 'b', 'from': 'e2', 'move_number': 24, 'player': 'White', 'san': 'Nxf4', 'to': 'f4', 'uci': 'e2f4'},
#         {'captured': True, 'captured_piece': 'N', 'from': 'g4', 'move_number': 24, 'player': 'Black', 'san': 'Qxf4', 'to': 'f4', 'uci': 'g4f4'},
#         {'captured': False, 'captured_piece': None, 'from': 'h6', 'move_number': 25, 'player': 'White', 'san': 'Qg7', 'to': 'g7', 'uci': 'h6g7'},
#         {'captured': False, 'captured_piece': None, 'from': 'f4', 'move_number': 25, 'player': 'Black', 'san': 'Qf2+', 'to': 'f2', 'uci': 'f4f2'}
#     ]

#     state = {"move_analysis": dummy_moves}
#     result = structure_extractor_tool.invoke({"state": state})
#     print("\nFinal Structure Insights Output:")
#     for item in result.structure_insights:
#         print(item)

# if __name__ == "__main__":
#     main()