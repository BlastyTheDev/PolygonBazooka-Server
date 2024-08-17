package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import lombok.Getter;
import me.blasty.polygonbazookaserver.api.multiplayer.Player;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.security.user.User;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class RankedMatch {

    private final Player player1;
    private final Player player2;

    private final User user1;
    private final User user2;

    private final WebSocketSession session1;
    private final WebSocketSession session2;

    private final int scoreGoal;

    private int player1Score;
    private int player2Score;

    public RankedMatch(User user, User player2, WebSocketSession session1, WebSocketSession session2) {
        player1 = new Player();
        this.player2 = new Player();

        this.user1 = user;
        this.user2 = player2;

        this.session1 = session1;
        this.session2 = session2;

        // first to 3 for now
        scoreGoal = 3;
    }

    public synchronized void update(WebSocketSession session, String message) {
        var command = message.charAt(0);
        
        switch (command) {
            case Const.MoveLeft -> {
                if (session == session1) player1.moveLeft();
                else player2.moveLeft();
            }
            
            case Const.MoveRight -> {
                if (session == session1) player1.moveRight();
                else player2.moveRight();
            }
            
            case Const.HardDrop -> {
                if (session == session1) player1.hardDrop();
                else player2.hardDrop();
            }
            
            case Const.RotateCw -> {
            }
            
            case Const.RotateCcw -> {
            }
            
            default -> {
                // Modified Client
            }
        }
    }

}
