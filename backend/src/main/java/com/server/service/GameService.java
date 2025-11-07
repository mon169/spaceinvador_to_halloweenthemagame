package com.game.server.service;

import org.springframework.stereotype.Service;

@Service
public class GameService {
    public String getStatus() {
        return "✅ Game service running";
    }
}