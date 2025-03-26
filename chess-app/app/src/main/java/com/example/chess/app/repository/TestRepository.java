package com.example.chess.app.repository;

import com.example.chess.app.model.Position;
import com.example.chess.app.model.TestModel;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends ReactiveCrudRepository<TestModel, String> {
}
