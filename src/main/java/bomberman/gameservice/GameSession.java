package bomberman.gameservice;


import bomberman.model.Message;
import bomberman.model.DataContainer;
import bomberman.model.Topic;
import bomberman.model.Wood;
import bomberman.model.Fire;
import bomberman.model.Bonus;
import bomberman.model.Direction;
import bomberman.model.Character;
import bomberman.util.JsonHelper;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


public class GameSession implements Runnable {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GameSession.class);
    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS;
    private long tickNumber = 0;
    public static int id = 0;
    private Long gameId;
    private BlockingQueue<Message> inputQueue;
    private Queue<Message> buffer;
    private int numOfPlayers;
    private int alivePlayersNum;
    private HashMap<Integer, Character> charList;
    private int connectionsNum;
    private ConnectionPool pool;
    private DataContainer container;
    private final GameService gameService;
    private static final String WIN_MESSAGE = "{\"topic\":\"GAME_OVER\",\"data\":\"Congratulations! You won!\"}";
    private static final String LOSE_MESSAGE = "{\"topic\":\"GAME_OVER\",\"data\":\"Game over. You lost!\"}";

    public boolean isReady() {
        return numOfPlayers == connectionsNum;
    }

    public DataContainer getContainer() {
        return container;
    }

    public GameSession(GameService gameService, long id, BlockingQueue queue, int numberOfPlayers) {
        this.gameService = gameService;
        this.inputQueue = queue;
        this.gameId = id;
        this.numOfPlayers = numberOfPlayers;
        this.charList = new HashMap<>();
        this.connectionsNum = 0;
        this.pool = new ConnectionPool();
        this.buffer = new LinkedList<>();
        this.container = new DataContainer();
        container.setObjsToSend(container.getField().getObjects());
    }

    public synchronized int addCharacter(WebSocketSession session, String owner) {
        Integer newId = id++;
        Character character;
        switch (connectionsNum++) {
            case 0:
                character = new Character(32, 32, owner, id++, container);
                pool.setSession(character, session);
                charList.put(newId, character);
                cornerLd();
                break;
            case 1:
                character = new Character(480, 32, owner, id++, container);
                pool.setSession(character, session);
                charList.put(newId, character);
                cornerRd();
                break;
            case 2:
                character = new Character(480, 352, owner, id++, container);
                pool.setSession(character, session);
                charList.put(newId, character);
                cornerRu();
                break;
            case 3:
                character = new Character(32, 352, owner, id++, container);
                pool.setSession(character, session);
                charList.put(newId, character);
                cornerLu();
                break;
            default:
                break;
        }
        container.getObjsToSend().add(charList.get(newId));

        if (isReady()) {
            new Thread(this).start();
            log.info("Game # {} started", gameId);
        }
        return newId;
    }

    public void run() {
        try {
            Message replicaMsg = new Message(Topic.REPLICA, JsonHelper.toJson(container.getObjsToSend()));
            pool.broadcast(JsonHelper.toJson(replicaMsg));
        } catch (IOException e) {
            log.error("Error while sending message: {} {}", e.getMessage(), e.getStackTrace());
        }
        container.getObjsToSend().clear();
        for (Character c: charList.values())
            container.getObjsToSend().add(c);
        alivePlayersNum = numOfPlayers;
        gameLoop();
        finish();
    }

    private void gameLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            long started = System.currentTimeMillis();
            act(FRAME_TIME);

            try {
                Message replicaMsg = new Message(Topic.REPLICA, JsonHelper.toJson(container.getObjsToSend()));
                pool.broadcast(JsonHelper.toJson(replicaMsg));

                for (Object o: container.getObjsToSend()) {
                    if (o.getClass() == Character.class) {
                        Character character = (Character) o;
                        if (!character.isAlive()) {
                            container.getObjsToSend().remove(o);
                            alivePlayersNum--;
                        }
                    }
                    if (o.getClass() == Wood.class) {
                        container.getObjsToSend().remove(o);
                    }
                    if (o.getClass() == Fire.class) {
                        container.getObjsToSend().remove(o);
                    }
                    if (o.getClass() == Bonus.class) {
                        container.getObjsToSend().remove(o);
                    }
                }

                for (Integer key: charList.keySet())
                    charList.get(key).setDirection(Direction.DEFAULT);
            } catch (IOException e) {
                log.error("Error while sending message: {} {}", e.getMessage(), e.getStackTrace());
                tickNumber++;
                continue;
            }

            if (alivePlayersNum < 2) {
                log.info("Game {}: only {} players left", gameId, alivePlayersNum);
                break;
            }

            long elapsed = System.currentTimeMillis() - started;
            if (elapsed < FRAME_TIME) {
                log.debug("All tick finish at {} ms", elapsed);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(FRAME_TIME - elapsed));
            } else {
                log.warn("tick lag {} ms", elapsed - FRAME_TIME);
            }
            tickNumber++;
        }
    }

    private void act(long frametime) {
        synchronized (inputQueue) {
            while (!inputQueue.isEmpty())
                buffer.add(inputQueue.poll());
        }
        Message tmp;
        while (!buffer.isEmpty()) {
            tmp = buffer.poll();
            if (tmp.getTopic() == Topic.PLANT_BOMB)
                charList.get(tmp.getPlayerId()).plant();
            else
                charList.get(tmp.getPlayerId()).move(tmp.getData(), frametime);
        }
        container.getObjsToTick().stream().forEach(e -> e.tick(frametime));
    }

    public void cornerLd() {
        container.getObjsToSend().remove(container.getField().getBar(1, 1).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(1, 2).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(2, 1).getPlug());
        container.getField().clearBar(1,1);
        container.getField().clearBar(1,2);
        container.getField().clearBar(2,1);
    }

    public void cornerRd() {
        container.getObjsToSend().remove(container.getField().getBar(15, 1).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(15, 2).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(14, 1).getPlug());
        container.getField().clearBar(15,1);
        container.getField().clearBar(15,2);
        container.getField().clearBar(14,1);
    }

    private void cornerRu() {
        container.getObjsToSend().remove(container.getField().getBar(15, 11).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(15, 10).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(14, 11).getPlug());
        container.getField().clearBar(15,11);
        container.getField().clearBar(15,10);
        container.getField().clearBar(14,11);
    }

    private void cornerLu() {
        container.getObjsToSend().remove(container.getField().getBar(1, 11).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(1, 10).getPlug());
        container.getObjsToSend().remove(container.getField().getBar(2, 11).getPlug());
        container.getField().clearBar(1,11);
        container.getField().clearBar(1,10);
        container.getField().clearBar(2,11);
    }

    private void finish() {
        log.info("Game {} is finishing", gameId);
        Character[] chars = new Character[4];
        int ctr = 0;
        for (Character character : charList.values()) {
            try {
                if (character.isAlive()) {
                    pool.sendMessage(character, WIN_MESSAGE);
                } else {
                    pool.sendMessage(character, LOSE_MESSAGE);
                }
            } catch (IOException e) {
                log.error("Error while sending message: {} {}", e.getMessage(), e.getStackTrace());
            }
            chars[ctr++] = character;
        }
        pool.closeAllConnections();
        gameService.deleteSession(gameId,chars);
        log.info("Game {} finished", gameId);
    }
}
