# Test file to test the Agentic Pipeline

import argparse
from graph_builder import build_chess_strategy_graph
from langchain_openai import ChatOpenAI
import os
import json
import dotenv

# Load your OpenAI key from environment or set manually
dotenv.load_dotenv()
os.environ["OPENAI_API_KEY"] = os.getenv("OPENAI_API_KEY")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--fen", type=str, required=True, help="FEN position")
    parser.add_argument("--moves", type=str, required=True, help="Post-FEN move sequence")
    parser.add_argument("--side", type=str, choices=["white", "black"], required=True, help="Which side to analyze")
    args = parser.parse_args()

    llm = ChatOpenAI(model=os.getenv("OPENAI_MODEL"), temperature=0.5)
    verifier_llm = ChatOpenAI(model=os.getenv("VERIFIER_OPENAI_MODEL"), temperature=0.5)
    app = build_chess_strategy_graph(llm, verifier_llm)

    print(args.fen)

    result = app.invoke({
        "fen": args.fen,
        "moves": args.moves,
        "side": args.side
    })

    print("\n🔍 Position Features:\n")
    print(json.dumps(result.get("position_features", {}), indent=2))
    print("\n📦 Structure Insights:\n")
    print(json.dumps(result.get("structure_insights", []), indent=2))
    print("\n✅ Final Output:\n")
    print(result.get("formatted_strategy", "[No strategy generated]"))
    print("\n🧠 Synthesized Ideas (Raw):\n")
    print(result.get("synthesized_ideas", "[None]"))
    
