package com.example.gobang.config;

import com.example.gobang.api.GameAPI;
import com.example.gobang.api.MatchAPI;
import com.example.gobang.api.TestAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpSession;

/**
 * @author qiu
 * @version 1.8.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TestAPI testAPI;

    @Autowired
    private MatchAPI matchAPI;

    @Autowired
    private GameAPI gameAPI;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(testAPI,"/test");
        registry.addHandler(matchAPI,"/findMatch")
                //向WebSocket中的Session中注册HttpSession
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        registry.addHandler(gameAPI,"/game")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
