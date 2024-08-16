package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import lombok.RequiredArgsConstructor;
import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
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
    private final HashMap<WebSocketSession, User> users = new HashMap<>();
    
    private final List<User> usersInQueue = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));

        if (cookies == null || !cookies.contains("token")) {
            session.close();
            return;
        }
        
        // TODO: not tested
        String token = cookies.split("token=")[1].split(";")[0];
        String username = jwtService.getSubject(token);
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            session.close();
            return;
        }
        
        users.put(session, user);
        
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        String message = msg.getPayload();

        if (message.equals(Const.JoinQueue)) {
        }

        if (message.equals(Const.LeaveQueue)) {
        }

        if (message.equals(Const.ForfeitGame)) {
        }
    }

}
