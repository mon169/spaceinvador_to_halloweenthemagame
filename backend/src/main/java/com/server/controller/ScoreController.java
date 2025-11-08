package com.game.server.controller;

import com.game.server.model.PlayerState;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/score")
@CrossOrigin("*")
public class ScoreController {
    private final List<PlayerState> results = new ArrayList<>();

    @PostMapping("/save")
    public String saveScore(@RequestBody PlayerState result) {
        results.add(result);
        System.out.println("💾 Score saved: " + result.playerId + " | Score: " + result.score);
        return "✅ Score saved for " + result.playerId;
    }

    @GetMapping("/ranking")
    public List<PlayerState> ranking() {
        results.sort((a, b) -> b.score - a.score);
        return results;
    }
}