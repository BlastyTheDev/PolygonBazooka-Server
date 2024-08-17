package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import lombok.RequiredArgsConstructor;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.multiplayer.util.RNG;
import me.blasty.polygonbazookaserver.api.security.jwt.JWTService;
import me.blasty.polygonbazookaserver.api.security.user.User;
import me.blasty.polygonbazookaserver.api.security.user.UserRepository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RankedMatchHandler extends TextWebSocketHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final HashMap<User, WebSocketSession> users = new HashMap<>();
    private final HashMap<User, UUID> usersInMatch = new HashMap<>();

    private final List<User> usersInQueue = new ArrayList<>();
    
    private final List<RankedMatch> matches = new ArrayList<>();
    private final List<UUID> usedMatchIds = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));

        if (cookies == null || !cookies.contains("token")) {
            session.sendMessage(new TextMessage(Const.ConnectionRejected));
            session.close();
            return;
        }

        String token = cookies.split("token=")[1].split(";")[0];
        String username = jwtService.getSubject(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            session.sendMessage(new TextMessage(Const.ConnectionRejected));
            session.close();
            return;
        }

        if (users.get(user) != null) {
            session.sendMessage(new TextMessage(Const.ConnectionRejected));
            session.close();
            return;
        }

        users.put(user, session);

        sessions.add(session);
        
        session.sendMessage(new TextMessage(Const.ConnectionInitialized));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        if (!sessions.contains(session))
            return;

        var user = users.keySet().stream().filter(u -> users.get(u).equals(session)).findFirst().orElse(null);
        
        if (user == null)
            return;
        
        String message = msg.getPayload();

        System.out.println("received: " + message);

        if (message.equals(Const.JoinQueue)) {
            usersInQueue.add(user);
            
            // TODO: implement matchmaking based on ranks. will be just 2 random players for now, games unranked
            if (usersInQueue.size() >= 2) {
                User player2 = null;
                
                while (player2 == null || player2.equals(user)) {
                    if (usersInQueue.size() < 2)
                        break;
                    
                    player2 = usersInQueue.get(RNG.next(usersInQueue.size() - 1));
                }
                
                var session2 = users.get(player2);
                
                UUID matchId = null;
                
                while (matchId == null || usedMatchIds.contains(matchId))
                    matchId = UUID.randomUUID();
                
                usedMatchIds.add(matchId);
                
                var match = new RankedMatch(user, player2, session, session2, matchId);
                matches.add(match);
                
                session.sendMessage(new TextMessage(Const.MatchFound + matchId));
                session2.sendMessage(new TextMessage(Const.MatchFound + matchId));
                
                usersInQueue.remove(user);
                usersInQueue.remove(player2);
            }
            
            return;
        }

        if (message.equals(Const.LeaveQueue)) {
            usersInQueue.remove(user);
            
            return;
        }

        if (message.equals(Const.ForfeitGame)) {
            return;
        }
        
        if (message.startsWith(Const.ChatPrefix)) {
            var chatMessage = message.substring(Const.ChatPrefix.length());
            // broadcast if in match
            
            return;
        }
    }

}
