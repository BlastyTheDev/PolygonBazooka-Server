package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import lombok.Getter;
import me.blasty.polygonbazookaserver.api.multiplayer.Player;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.security.user.User;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

@Getter
public class RankedMatch {
    
    private final UUID matchId;

    private final Player player1;
    private final Player player2;

    private final User user1;
    private final User user2;

    private final WebSocketSession session1;
    private final WebSocketSession session2;

    private final int scoreGoal;

    private int player1Score;
    private int player2Score;
    
    private volatile RankedMatchState state;

    public RankedMatch(User user, User player2, WebSocketSession session1, WebSocketSession session2, UUID matchId) {
        this.matchId = matchId;
        
        player1 = new Player();
        this.player2 = new Player();

        this.user1 = user;
        this.user2 = player2;

        this.session1 = session1;
        this.session2 = session2;

        // first to 3 for now
        scoreGoal = 3;
        
        state = RankedMatchState.MatchStart;
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
            
            case Const.MoveDown -> {
//                if (session == session1) player1.moveDown();
//                else player2.moveDown();
            }

            case Const.MoveLeftFully -> {
                if (session == session1) player1.moveLeftFully();
                else player2.moveLeftFully();
            }

            case Const.MoveRightFully -> {
                if (session == session1) player1.moveRightFully();
                else player2.moveRightFully();
            }

            case Const.HardDrop -> {
                if (session == session1) player1.hardDrop();
                else player2.hardDrop();
            }

            case Const.RotateCw -> {
                if (session == session1) player1.rotateCw();
                else player2.rotateCw();
            }

            case Const.RotateCcw -> {
                if (session == session1) player1.rotateCcw();
                else player2.rotateCcw();
            }

            default -> {
                // Modified Client
            }
        }

        // TODO: implement rounds
        
        if (player1.isToppedOut() && state == RankedMatchState.Playing) {
            state = RankedMatchState.RoundEnd;
            
            player2Score++;
        }
        
        if (player2.isToppedOut() && state == RankedMatchState.Playing) {
            state = RankedMatchState.RoundEnd;
            
            player1Score++;
        }
    }

    public void chat(WebSocketSession session, String message) throws IOException {
        var chatMessage = Const.ChatPrefix + message;

        if (session == session1) session2.sendMessage(new TextMessage(chatMessage));
        else session1.sendMessage(new TextMessage(chatMessage));
    }

}
