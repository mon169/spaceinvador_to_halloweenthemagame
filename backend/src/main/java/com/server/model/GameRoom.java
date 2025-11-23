package com.game.server.model;

import java.util.*;

public class GameRoom {
    public String roomId;
    public Map<String, PlayerState> players = new HashMap<>();

    public GameRoom(String roomId) {
        this.roomId = roomId;
    }
}