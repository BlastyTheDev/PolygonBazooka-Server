package me.blasty.polygonbazookaserver.config;

import lombok.RequiredArgsConstructor;
import me.blasty.polygonbazookaserver.api.multiplayer.ranked.RankedMatchHandler;
import me.blasty.polygonbazookaserver.api.security.jwt.JWTService;
import me.blasty.polygonbazookaserver.api.security.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final JWTService jwtService;
    private final UserRepository userRepository;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rankedMatchHandler(), "/api/ws/ranked").setAllowedOrigins("*");
    }
    
    @Bean
    public RankedMatchHandler rankedMatchHandler() {
        return new RankedMatchHandler(jwtService, userRepository);
    }
    
}
