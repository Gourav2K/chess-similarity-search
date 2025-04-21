package com.example.chess.app.repository;

import com.example.chess.app.model.Position;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PositionRepository extends ReactiveCrudRepository<Position, UUID>{
}
