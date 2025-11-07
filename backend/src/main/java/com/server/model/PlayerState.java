package com.game.server.model;

public class PlayerState {
    public String playerId;
    public int x, y, hp, score;

    public PlayerState() {} // 기본 생성자 필요 (JSON 직렬화용)

    public PlayerState(String playerId, int x, int y, int hp, int score) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.score = score;
    }
}