package com.example.chess.app.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;

    @Value("${spring.r2dbc.database:chess_position_db}")
    private String database;

    @PostConstruct
    public void initializeDatabase() {
        log.info("Initializing Postgres database schema...");

        // In PostgreSQL, database creation is typically done outside the application
        // or requires a connection to the 'postgres' database, so we'll focus on table creation
        createBitCountFunction()  // 👈 create this first
                .then(createGamesTable())
                .then(createPositionsTable())
                .doOnSuccess(v -> log.info("Database schema initialized successfully."))
                .doOnError(e -> log.error("Error initializing database schema:", e))
                .block();
    }

    private Mono<Void> createBitCountFunction() {
        return databaseClient.sql("""
        CREATE OR REPLACE FUNCTION bit_count(val bigint)
        RETURNS int AS $$
            SELECT length(replace((val::bit(64))::text, '0', ''))::int;
        $$ LANGUAGE SQL IMMUTABLE STRICT;
    """).then();
    }

    private Mono<Void> createGamesTable() {
        return databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS games (
                id VARCHAR(255) PRIMARY KEY,
                result VARCHAR(10),
                white_elo INTEGER,
                black_elo INTEGER,
                game_type VARCHAR(50),
                date VARCHAR(20),
                white_name VARCHAR(255),
                black_name VARCHAR(255),
                eco VARCHAR(10),
                time_control VARCHAR(50),
                site VARCHAR(255),
                opening VARCHAR(255)
            )
            """)
                .then();
    }

    private Mono<Void> createPositionsTable() {
        return databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS positions (
                id UUID PRIMARY KEY,
                game_id VARCHAR(255) REFERENCES games(id),
                move_number INTEGER,
                white_king INTEGER,
                black_king INTEGER,
                white_queens VARCHAR(100),
                white_rooks VARCHAR(100),
                white_bishops VARCHAR(100),
                white_knights VARCHAR(100),
                black_queens VARCHAR(100),
                black_rooks VARCHAR(100),
                black_bishops VARCHAR(100),
                black_knights VARCHAR(100),
                white_pawns BIGINT,
                black_pawns BIGINT,
                side_to_move VARCHAR(1),
                castling_rights INTEGER,
                en_passant_square INTEGER,
                half_move_clock INTEGER,
                full_move_number INTEGER,
                fen VARCHAR(100)
            )
            """)
                .then();
    }

    // Optional: Create helpful indexes for position searches
    private Mono<Void> createIndexes() {
        return databaseClient.sql("""
            CREATE INDEX IF NOT EXISTS idx_white_pawns ON positions(white_pawns);
            CREATE INDEX IF NOT EXISTS idx_black_pawns ON positions(black_pawns);
            CREATE INDEX IF NOT EXISTS idx_white_bishops ON positions(white_bishops);
            CREATE INDEX IF NOT EXISTS idx_black_bishops ON positions(black_bishops);
            CREATE INDEX IF NOT EXISTS idx_white_rooks ON positions(white_rooks);
            CREATE INDEX IF NOT EXISTS idx_black_rooks ON positions(black_rooks);
            CREATE INDEX IF NOT EXISTS idx_white_knights ON positions(white_knights);
            CREATE INDEX IF NOT EXISTS idx_black_knights ON positions(black_knights);
            """)
                .then();
    }
}