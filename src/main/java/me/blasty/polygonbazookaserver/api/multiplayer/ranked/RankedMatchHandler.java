package me.blasty.polygonbazookaserver.api.multiplayer.ranked;

import me.blasty.polygonbazookaserver.api.multiplayer.util.Const;
import me.blasty.polygonbazookaserver.api.security.user.User;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RankedMatchHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final HashMap<WebSocketSession, User> users = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));

        if (cookies == null || !cookies.contains("token")) {
            session.close();
            return;
        }
        
        // TODO: reject connection if user is already connected
        
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
