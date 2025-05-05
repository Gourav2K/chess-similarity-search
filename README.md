# ♟️ Chess Similarity Search Engine + Middlegame Insight Builder

> ✨ Live Demo; Try the app here: [chess-similarity-frontend.vercel.app](https://chess-similarity-frontend.vercel.app)

Demo Video I - Position-similarity-search Tab

https://github.com/user-attachments/assets/7b232c77-f1d0-4f23-b6cb-9f3beb93034b

Demo Video II - With New Insights Tab

https://github.com/user-attachments/assets/d401c061-ee0b-4155-bec5-85bbb026e1f2

## 🌐 Hosted Demo (Vercel + Render)

This is a **live, miniature hosted version** of the application, showcasing the GraphQL-based similarity search between chess positions via a fully deployed stack:

- **Frontend (React + Vite)**: hosted on [Vercel](https://vercel.com/)
- **Backend (Spring WebFlux + GraphQL)**: deployed via [Render](https://render.com/)
- **PostgreSQL DB**: running on a Render free-tier instance (results + query performance is limited for demo purposes ~ 7 lakh positions)

> **Note**: The **data ingestion pipeline** (Python + Kafka + Redis) is **not active** in this live demo.  
> The backend uses a preloaded snapshot of ~0.7M positions from Lichess PGN data.

Try it here → ***[chess-similarity-frontend.vercel.app](https://chess-similarity-frontend.vercel.app)***

---

## 📖 Project Objective
A full-stack, containerized application (built in a way that it can be easily scaled as well) to explore and analyze chess positions based on structural similarity — built to help players discover recurring human ideas rather than rely solely on engine precision.

Yes, engines exist — and yes, they can tell us the best move in any given position. But anyone who has passionately played chess knows that while the opening phase is often well-studied, the middlegame can feel like a wilderness. Players may be familiar with the first 10–15 moves of an opening, but once theory ends, they’re often left improvising.

This project aims to bridge that gap — by surfacing patterns, structures, and motifs that arise across thousands of human games. Middlegame ideas aren’t just position-specific — many emerge from structural features. A certain pawn chain might demand a pawn break at a typical moment. Specific exchanges may lead to critical outposts. Recognizing these recurring structures can help players build intuition and formulate practical plans.

The core idea here is to make the ***middlegame*** more studyable. Human brains are remarkably good at learning from patterns — and this tool brings that pattern recognition to the study of real chess games. It's not about finding the best move, it's about finding familiar territory.

---

## 🚀 Features

- 📦 **Python preprocessor** to parse PGN files and publish to Kafka
- 🧠 **Reactive Spring Webflux backend** with Kafka consumer, PostgreSQL persistence and querying logic
- 💡 **Array, bitboard, and integer-based matching logic with GIN-accelerated SQL queries** for position similarity
- 🔎 **GraphQL-based resolver** for similarity search application for chess positions
- 🖥️ **React + Vite frontend** to input FENs or create position on chess board and visualize results
- 🐳 **Fully containerized using Docker and Docker Compose**
- 🎯 **Elo-aware filtering** to show relevant human games within a target rating range
- 🔄 **Deduplication** ensures no repeated games show up in results
- 🧠 **LLM-powered strategy summarization** with similar positions fetched from database, users can generate a strategic breakdown using LLMs, showing key ideas and typical plans based on similar historical positions.


---

## 🧰 Tech Stack

| Layer        | Technology                                       |
|--------------|--------------------------------------------------|
| Frontend     | React, Vite, GraphQL Client                      |
| Backend      | Java 17, Spring Boot WebFlux, GraphQL            |
| Preprocessor | Python 3.10, kafka-python, redis                 |
| Messaging    | Kafka, Zookeeper                                 |
| Database     | PostgreSQL (R2DBC)                               |
| Cache        | Redis                                            |
| DevOps       | Docker, Docker Compose                           |
| LLM-Service  | OpenAI, FastAPI, Langgraph, Langchain            |

---

## 🧱 System Architecture

This project follows a modular, event-driven design pattern that makes it easy to scale each component independently. Here's a high-level overview of how it works:

![Architecture Diagram](./resources/images/Architecture_Diagram_Updated.png)

- **PGN Preprocessor (Python):** Reads PGN files, extracts FEN positions, and publishes structured data in batches via Kafka. Uses Redis to track progress of how many games have been read from the PGN file so that subsequent runs can continue processing from the next game in the PGN file.
- **Backend Application (Spring WebFlux):** Acts as the core search engine. It consumes game data from Kafka, stores positions and games in PostgreSQL, and handles incoming GraphQL queries. It includes the logicto perform structure-based similarity search using array + bitboard + integer matching logic, pre-filtering positions with PostgreSQL GIN indexes, scoring via dynamic SQL expressions, and ranking top results efficiently in-memory with deduplication based on provided criteria (like pawns, pieces, and color). 
- **Frontend Application (React + Vite):** Provides a user-friendly interface to input FEN strings or use a chessboard to search for similar positions based on structural features. Each position output also has a direct link to the game played on Lichess which can be used for further evaluation.
- **LLM Pipeline (Python + FastAPI + Langgraph + Langchain)**: A dedicated microservice built using FastAPI interacting with a multi-agent framework which is explained in detail below.


## LLM-Service : Agentic Pipeline

![Agentic Pipeline](./resources/images/Agentic_Pipeline.png)

A single prompt based API call has been broken into tiny thinking steps with LangGraph:
1. FEN Validator: Validates FEN if passed (Langchain tool) → 
2. Move Simulator: simulate moves and store metadata (Langchain tool) → 
3. Structure Extractor: extracts structure details for each move simulated above  & 
4. Position features Extractor: Extracts ideas like king-safety, presence of bishop pair, etc (Langchain tool) → 
5. Ideas Synthesizer: synthesize ideas using all the information passed via the above (llm call) → 
6. Verifier: verify synthesized ideas & make corrections based on highlighted incoherent ideas (llm call; but a smarter llm reviews the ideas) → 
7. Formatter : formats the final strategy (Langchain tool). 

---

## ⚙️ Scalability

This project is designed with scalability in mind, making it ideal for future growth, cloud deployment, or large dataset processing.

- **Kafka-based ingestion** decouples parsing from storage, enabling horizontal scaling of consumers and producers
- **Stateless backend (Spring WebFlux)** handles concurrent GraphQL queries efficiently and is ready for Kubernetes, load balancers, etc.
- **PostgreSQL schema with bitboard optimizations** ensures fast lookups and indexing for pawn-search based queries
- **CTE-based prefiltering + bitboard & array GIN indexing** reduces full-table scans for similarity queries
- **Redis-backed preprocessor batching** allows checkpointing and distributed ingestion
- **Frontend is built on Vite/React**, making it lightweight and easy to deploy on any static CDN or edge server

With each component running independently via Docker, the entire system is modular and ready for:

- Distributed processing  
- Cloud-native scaling  
- Integration with advanced vector search or LLM-based game insights in the future (Will start work on this soon)

---

## 📁 Project Structure

```
chess-similarity-project/
├── chess-app/              # Spring Boot WebFlux backend
├── chess-app-frontend/     # React + Vite frontend
├── llm-service-v1          # Service interacting with LLMs via OpenAI API Call to generate roadmap, strategies
├── llm-service-v2          # Service interacting with LLMs via agentic workflow built using Langgraph
├── pre-processor/          # Python PGN preprocessor with Kafka & Redis
├── db-init/                # init.sql for preloaded games & schema
├── docker-compose.yml      # Service orchestration
├── data/                   # Mount PGN files here (not tracked)
├── images/                 # Images used in readme
└── README.md
```

---

## ⚙️ Running the Application (Docker)

1. Clone the repository:

```bash
git clone https://github.com/gourav2k/chess-similarity-search.git
cd chess-similarity-search
```
2. How to Build the Database
There are two ways to set up the PostgreSQL database with chess positions:
#### Option 1: Parse Your Own PGN File

1. Download any `.pgn.zst` file from [https://database.lichess.org](https://database.lichess.org)
2. Extract it and place it inside the `data/` directory
3. Run the preprocessor like this:

```bash
docker-compose run -v $(pwd)/data:/data preprocessor /data/{your_file.pgn} --max-games 2500000
```
💡 This step is safe to run in multiple passes, since Redis tracks progress. However, reading millions of games may take time and use significant disk space.

#### Option 2: Use Preloaded SQL Dump (Recommended for Demo)

1. Download the .sql file I’ve shared : [Database Dump Link](https://www.dropbox.com/scl/fi/24uqbh2pow3pz22gff53z/chess_app_db.sql?rlkey=aftaj862o7oaztney9d031ijn&st=0jlthfo2&dl=0) - contains 250K games + ~3M positions data.
2. Place the file inside the db-init/ directory
3. Proceed to the next step (docker-compose up --build)

The application will auto-load 200K+ positions and be immediately ready to query!

3. Start all services:

```bash
docker-compose up --build
```
> Note: The postgres container might take some time to import the dump as its big - so if the backend container stops, rerun it once you see that the postgres container is ready to accept connections.

4. Access the app:
- Frontend: [http://localhost:3000](http://localhost:3000)
- GraphQL Playground: [http://localhost:8080/graphql](http://localhost:8080/graphql)

---

## 🧪 Sample GraphQL Query

```graphql
query FindSimilarPositionsByFen($fen: String!, $request: SimilarityRequestInput!) {
  findSimilarPositionsByFen(fen: $fen, request: $request) {
    similarityScore
    moveNumber
    position {
      fen
    }
    game {
      whiteName
      blackName
      result
      site
      whiteElo
      blackElo
    }
  }
}
```

### 📦 Sample Variables

```json
{
  "fen": "2r1r1k1/5ppp/b3p3/p2p4/5n2/PBP4P/2P3P1/3RR1K1 w - - 0 25",
  "request": {
    "color": "WHITE",
    "selectedPieces": ["PAWN", "BISHOP"],
    "limit": 10,
    "minElo": 1600,
    "maxElo": 2000
  }
}
```
---

## 🔁 Ingesting a New PGN File

```bash
docker-compose run -v $(pwd)/data:/data preprocessor /data/lichess_file.pgn --max-games 100000
```

- Tracks progress using Redis
- Publishes parsed games+positions data to Kafka

---
## 🧪 Local Setup (Non-Docker)

If you prefer to run the project locally without Docker (for development or debugging), follow these steps:

---

### 🛠 Prerequisites

- Java 17+
- Python 3.10+
- Node.js (v18 or higher)
- PostgreSQL 14+ installed locally
- Redis running locally
- Kafka + Zookeeper running locally (Docker images recommended)

---

### 1. Set Up the Database

Ensure your local Postgres DB has the required tables and data. You can either:

- Ingest a `.pgn` file using the Python preprocessor
```bash
cd pre-processor
python main.py /path/to/file.pgn --batch-size 100 --max-games 100000
```
Make sure redis and kafka connections are pointing to local services via .env.local.

- Or restore the `init.sql` dump manually into Postgres from this link - [Database Dump Link](https://tinyurl.com/2amdv3kd):
  
```bash
psql -U postgres -d chess_positions_db -f db-init/chess_app_db.sql
```

### 2. Start the Backend (Spring WebFlux)
```bash
cd chess-app
./gradlew bootRun
```
Make sure application.properties is pointing to your local Postgres, Redis, and Kafka setup.

### 3. Start the Frontend (React + Vite)
```bash
cd chess-app-frontend
npm install
npm run dev
```
Ensure your .env.local contains:
```bash
VITE_WEBFLUX_BACKEND_URL=http://localhost:8080
```

### 4. Start the LLM-Service
```bash
cd llm-service-v2
pip install -r requirements.txt
uvicorn main:app --reload
```
Ensure your .env contains:
```bash
OPENAI_API_KEY="xxx-xx"
OPENAI_MODEL="xxx-xx"
VERIFIER_OPENAI_MODEL="xxx-xx"
```

### ✅ You're good to go!
- Visit frontend at: [http://localhost:5173](http://localhost:5173)
- Access GraphQL: [http://localhost:8080/graphql](http://localhost:8080/graphql)
---
## 🧭 What's Next?

- **UI Features** - the current chessboard needs to be replaced by a board which allows custom setting of position instead of making moves to reach the desired position.
- **Adding more suitable filters** - like opening, result, etc; so that it could help study similar structures out of specific openings, filtering by the side which won.
- **Performance Optimizations** - Database side, application side.
- **Clustering : KNN Embedding + Vector DB integration** to enhance the performance of fetching subset of similar positions via cosine similarity between board embeddings. A faster subset fetch could drastically improve the CTE prefiltering step.
- **CRON Jobs** - Preprocessor logic could potentially be converted into **scheduled jobs** reading pgn files and pushing batches of games to kafka at fixed schedules
- **LLM-powered game summaries and pattern insights for each similar position** - based on the output; another service within this architecture. 
- ✅ **LLM-powered summaries are now integrated!** Users can instantly view a strategic roadmap for the given position across all similar results, powered by OpenAI APIs for now.
- PGNs from Lichess website are very big and compressed - a service which could automate the process of fetching, decompressing the PGN file from Lichess and become an input for the preprocessor jobs

---
## 📜 License

This project is licensed under the [MIT License](LICENSE).

---

## 🙌 Credits

- Built using open PGN data from [Lichess](https://database.lichess.org/)
- Designed & engineered by [@gourav2k](https://github.com/gourav2k)
- Big thanks to [@vishiivivek](https://github.com/vishiivivek) for helping with the ideation and his invaluable inputs
