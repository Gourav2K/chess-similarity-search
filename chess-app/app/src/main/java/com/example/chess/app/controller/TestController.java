package com.example.chess.app.controller;

import com.example.chess.app.model.TestModel;
import com.example.chess.app.repository.TestRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoop;

@RestController
@RequestMapping("/test")
public class TestController {
    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @PostMapping
    public Mono<TestModel> testEndpoint(@RequestBody String testString,
                                        @RequestParam String id){
        TestModel testModel = new TestModel(id, testString);
        return testRepository.save(testModel);
    }

}
