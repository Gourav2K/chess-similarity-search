from langchain_core.tools import tool
from typing import Dict
import re

@tool
def strategy_formatter_tool(state: Dict) -> Dict:
    """
    Formats the synthesized strategy into clean markdown-style output.
    Supports corrected or raw LLM output and handles Markdown-style headings.
    """
    strategy_text = state.get("synthesized_ideas_corrected") or state.get("synthesized_ideas")
    if not strategy_text:
        raise ValueError("Missing synthesized strategy in state")

    lines = strategy_text.strip().split("\n")

    goal_text = None
    bullet_points = []

    # Look for the line after the "Strategic Goal" heading
    for i, line in enumerate(lines):
        lower_line = line.lower()
        if "strategic goal" in lower_line:
            # Find next non-empty line for goal
            for j in range(i + 1, len(lines)):
                if lines[j].strip():
                    goal_text = lines[j].strip()
                    break
            break

    if not goal_text:
        goal_text = "(Strategic goal not found)"

    # Get all bullet points
    bullet_points = [line.strip() for line in lines if line.strip().startswith(("-", "•"))]

    # Compose formatted markdown
    formatted = f"""🧠 **Strategic Goal:**
{goal_text}

🗺️ **Roadmap of Specific Ideas:**"""
    for point in bullet_points:
        formatted += f"\n{point}"

    state["formatted_strategy"] = formatted.strip()
    print("State After Strategy Formatter - ", state)
    return {"state": state}
