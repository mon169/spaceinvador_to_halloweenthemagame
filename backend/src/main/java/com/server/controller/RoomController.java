package com.game.server.controller;

import com.game.server.model.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/room")
@CrossOrigin("*")
public class RoomController {
    private final Map<String, GameRoom> rooms = new HashMap<>();

    @PostMapping("/create")
    public GameRoom createRoom() {
        String id = UUID.randomUUID().toString().substring(0, 6);
        GameRoom room = new GameRoom(id);
        rooms.put(id, room);
        System.out.println("🟢 Room created: " + id);
        return room;
    }

    @PostMapping("/{roomId}/join")
    public GameRoom joinRoom(@PathVariable String roomId, @RequestBody PlayerState player) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.players.put(player.playerId, player);
            System.out.println("👥 Player joined room " + roomId + ": " + player.playerId);
        }
        return room;
    }

    @GetMapping("/{roomId}")
    public GameRoom getRoom(@PathVariable String roomId) {
        return rooms.get(roomId);
    }
}