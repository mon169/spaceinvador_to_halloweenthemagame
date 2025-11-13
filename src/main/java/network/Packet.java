package network;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L; // 🔥 반드시 필요!

    public String playerId;
    public int x, y;
    public boolean shoot;
    public int hp;
    public int score;

    public Packet(String playerId, int x, int y, boolean shoot, int hp, int score) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.shoot = shoot;
        this.hp = hp;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "id='" + playerId + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", shoot=" + shoot +
                ", hp=" + hp +
                ", score=" + score +
                '}';
    }
}