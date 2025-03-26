# ♟️ Chess Similarity Search Engine

A full-stack, containerized application to explore and analyze chess positions based on structural similarity — built to help players find real-game patterns, not just engine lines.

This project ingests millions of Lichess games, breaks them down into position data, and enables similarity search based on configurations like pawn structures, piece combinations, and more.

---

## 🚀 Features

- 🔎 **GraphQL-based similarity search** for chess positions
- 📦 **Python preprocessor** to parse PGN files and publish to Kafka
- 🧠 **Reactive Spring Boot backend** with Kafka consumer and PostgreSQL persistence
- 💡 **Bitboard-based matching logic** for position similarity
- 🖥️ **React + Vite frontend** to input FENs and visualize results
- 🐳 Fully containerized using Docker and Docker Compose

---

## 🧰 Tech Stack

| Layer      | Technology                             |
|------------|-----------------------------------------|
| Frontend   | React, Vite, GraphQL Client             |
| Backend    | Java 17, Spring Boot WebFlux, GraphQL   |
| Preprocessor | Python 3.10, kafka-python, redis      |
| Messaging  | Kafka, Zookeeper                        |
| Database   | PostgreSQL (R2DBC)                      |
| Cache      | Redis                                   |
| DevOps     | Docker, Docker Compose                  |

---

## 📁 Project Structure

```
chess-similarity-project/
├── chess-app/              # Spring Boot WebFlux backend
├── chess-app-frontend/     # React + Vite frontend
├── pre-processor/          # Python PGN preprocessor with Kafka & Redis
├── db-init/                # init.sql for preloaded games & schema
├── docker-compose.yml      # Service orchestration
├── data/                   # Mount PGN files here (not tracked)
└── README.md
```

---

## ⚙️ Running the Application (Docker)

1. Clone the repository:

```bash
git clone https://github.com/gourav2k/chess-similarity-search.git
cd chess-similarity-search
```

2. Start all services:

```bash
docker-compose up --build
```

3. Access the app:
- Frontend: [http://localhost:3000](http://localhost:3000)
- GraphQL Playground: [http://localhost:8080/graphql](http://localhost:8080/graphql)

> The database is auto-initialized with 200,000+ positions via `init.sql`

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
    "limit": 10
  }
}
```
---

## 🔁 Ingesting a New PGN File

```bash
docker-compose run -v $(pwd)/data:/data preprocessor /data/lichess_file.pgn --batch-size 100
```

- Tracks progress using Redis
- Publishes parsed data to Kafka

---

## 🧠 Use Cases

- Search past games with similar structures
- Study openings based on actual human play
- Build datasets of specific pawn formations or piece configurations

---

## 📌 Notes

- PGN dumps from Lichess (e.g., 10GB files) are supported
- PostgreSQL is pre-populated with real data for demo-ready usage
- Uses bitwise operations for pawn structure similarity (bitboards)

---

## 🛠 Local Dev (Optional)

| Component     | How to Run                         |
|---------------|-------------------------------------|
| Backend       | `./gradlew bootRun` in `chess-app/` |
| Frontend      | `npm run dev` in `chess-app-frontend/` |
| Preprocessor  | `python main.py file.pgn` in `pre-processor/` |

---

## 📜 License

MIT License

---

## 🙌 Credits

- Built using open PGN data from [Lichess](https://database.lichess.org/)
- Designed & engineered by [@gourav2k](https://github.com/gourav2k)
