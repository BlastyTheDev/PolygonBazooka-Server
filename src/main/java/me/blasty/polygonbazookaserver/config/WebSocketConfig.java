package me.blasty.polygonbazookaserver.config;

import me.blasty.polygonbazookaserver.api.multiplayer.ranked.RankedMatchHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rankedMatchHandler(), "/api/ws/ranked").setAllowedOrigins("*");
    }
    
    @Bean
    public RankedMatchHandler rankedMatchHandler() {
        return new RankedMatchHandler();
    }
    
}
