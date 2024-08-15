package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import me.blasty.polygonbazookaserver.api.multiplayer.Player;
import me.blasty.polygonbazookaserver.api.multiplayer.TileType;

public class RankedMatch {

    private final Player player1;
    private final Player player2;

    private int scoreGoal;

    private int player1Score;
    private int player2Score;

    public RankedMatch() {
        player1 = new Player();
        player2 = new Player();

        // first to 3 for now
        scoreGoal = 3;
    }

    public TileType[][] updateBoard() {
        throw new RuntimeException("not implemented");
    }

}
