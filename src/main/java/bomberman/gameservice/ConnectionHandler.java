package bomberman.gameservice;

import bomberman.model.Economer;
import bomberman.model.Message;
import bomberman.model.Topic;
import bomberman.util.JsonHelper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConnectionHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static Pattern gameId = Pattern.compile("gameId=(\\d+)");
    private static Pattern name = Pattern.compile("name=(.+)");

    @Autowired
    private ApplicationContext ctx;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConnectionHandler.class);


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established. Got query: {}", session.getUri().getQuery());
        Matcher idMatcher = gameId.matcher(session.getUri().getQuery());
        Matcher nameMatcher = name.matcher(session.getUri().getQuery());
        Long gameId = 0L;
        int playerId;
        String name = "";

        if (idMatcher.find()) {
            gameId = Long.parseLong(idMatcher.group(1));
        } else {
            log.error("Cannot find gameId in query parameters");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        if (nameMatcher.find()) {
            name = nameMatcher.group(1);
        } else {
            log.error("Cannot find name in query parameters");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        session.getAttributes().put("msgQueue",ctx.getBean(GameService.class).getQueue(gameId));
        playerId = ctx.getBean(GameService.class).addPlayer(gameId,session,name);
        session.getAttributes().put("playerId",playerId);
        log.info("Connection with gameID: {}, name: {}", gameId, name);

        String possessMsg = "{\"topic\":\"POSSESS\",\"data\":" + playerId + "}";
        session.sendMessage(new TextMessage(possessMsg));
        log.info("Sent message: " + possessMsg);
        session.getAttributes().put("economer",new Economer());
    }


    private void push(Economer e,Topic topic,BlockingQueue<Message> queue,Message msg) throws  Exception{
        if(e.getTopic() == topic){
            if(e.getCtr() == 0){
                queue.offer(msg, 1_000, TimeUnit.MILLISECONDS);
                e.setCtr(1);
            } else
                e.setCtr(0);
        } else {
            e.setTopic(topic);
            queue.offer(msg, 1_000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Got message: {}", message.getPayload());
        Message msg = JsonHelper.fromJson(message.getPayload(), Message.class).setPlayerId(
                (int) session.getAttributes().get("playerId"));
        BlockingQueue<Message> queue = (BlockingQueue<Message>) session.getAttributes().get("msgQueue");
        if (msg.getTopic() == Topic.PLANT_BOMB) {
            queue.offer(msg, 1_000, TimeUnit.MILLISECONDS);
        } else {
            switch (msg.getData()){
                case "{\"direction\":\"UP\"}":
                    push((Economer)session.getAttributes().get("economer"),Topic.UP,queue,msg);
                    break;
                case "{\"direction\":\"DOWN\"}":
                    push((Economer)session.getAttributes().get("economer"),Topic.DOWN,queue,msg);
                    break;
                case "{\"direction\":\"LEFT\"}":
                    push((Economer)session.getAttributes().get("economer"),Topic.LEFT,queue,msg);
                    break;
                case "{\"direction\":\"RIGHT\"}":
                    push((Economer)session.getAttributes().get("economer"),Topic.RIGHT,queue,msg);
                    break;
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Connection closed. Reason: {}", closeStatus.getReason());
    }
}
