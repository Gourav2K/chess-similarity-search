package com.example.chess.app.repository;

import com.example.chess.app.model.Game;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GameRepository extends ReactiveCrudRepository<Game, String>{
}

