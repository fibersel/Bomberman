package bomberman.gameservice;

import bomberman.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class GameServiceConfig implements WebSocketConfigurer {

    @Autowired
    private ApplicationContext ctx;

    @Bean(name = "queues")
    public ConcurrentHashMap<Long,BlockingQueue<Message>> getGamesQueueMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "games")
    public ConcurrentHashMap<Long,GameSession> getGamesMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ctx.getBean(ConnectionHandler.class),"/events/connect")
                .setAllowedOrigins("*");
    }

}
