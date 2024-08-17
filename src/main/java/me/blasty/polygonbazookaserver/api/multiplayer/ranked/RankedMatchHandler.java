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

@RequiredArgsConstructor
public class RankedMatchHandler extends TextWebSocketHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final HashMap<User, WebSocketSession> users = new HashMap<>();

    private final List<User> usersInQueue = new ArrayList<>();
    
    private final List<RankedMatch> matches = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));

        if (cookies == null || !cookies.contains("token")) {
            session.close();
            return;
        }

        String token = cookies.split("token=")[1].split(";")[0];
        String username = jwtService.getSubject(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            session.close();
            return;
        }

        if (users.get(user) != null) {
            session.close();
            return;
        }

        users.put(user, session);

        sessions.add(session);
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
                
                var session1 = users.get(user);
                var session2 = users.get(player2);
                
                var match = new RankedMatch(user, player2, session1, session2);
                // TODO: send players a match id to reroute socket traffic to the match
                
                usersInQueue.remove(user);
                usersInQueue.remove(player2);
            }
        }

        if (message.equals(Const.LeaveQueue)) {
            usersInQueue.remove(user);
        }

        if (message.equals(Const.ForfeitGame)) {
        }
    }

}
