package bomberman.gameservice;

import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import bomberman.model.Character;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPool {

    //Set<WebSocketSession> sessions = new HashSet<>();
    Map<Character,WebSocketSession> sessionMap = new HashMap<>();

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    public void setSession(Character player, WebSocketSession session) {
        this.sessionMap.put(player, session);
    }

    public void broadcast(String msg) throws IOException {
        for (WebSocketSession session : sessionMap.values()) {
            if(session.isOpen())
                session.sendMessage(new TextMessage(msg));
        }
        log.debug("Sent message: " + msg);
    }

    public void sendMessage(Character player, String msg) throws IOException {
        if(sessionMap.get(player).isOpen()) {
            sessionMap.get(player).sendMessage(new TextMessage(msg));
            log.info("Sent message to {}: {}", player.getOwner(), msg);
        }
    }

    public void closeAllConnections() {
        for (WebSocketSession session : sessionMap.values()) {
            try {
                session.close(CloseStatus.GOING_AWAY);
            } catch (IOException ignored) {
            }
        }
    }
}