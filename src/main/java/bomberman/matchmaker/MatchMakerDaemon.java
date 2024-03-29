package bomberman.matchmaker;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class MatchMakerDaemon implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchMakerDaemon.class);

    @Autowired
    private OkHttpClient client;
    @Autowired
    private ConcurrentHashMap<String,Long> playersId;
    @Autowired
    private MatchMakerRepository repository;

    private BlockingQueue<String> playersQueue;
    private int numberOfPlayers;

    private static final String PROTOCOL = "http://";
    private static final String HOST = "localhost";
    private static final String PORT = ":8080";
    private static final int MAX_NUMBER_OF_PLAYERS = 4;
    private static final long WAIT_TIME = 10_000_000_000L;

    public void setPlayersQueue(BlockingQueue<String> playersQueue) {
        this.playersQueue = playersQueue;
    }

    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + " Started!");

        numberOfPlayers = 0;

        while (!Thread.interrupted()) {
            makeSession(addPlayers());
        }
    }

    String[] addPlayers() {
        String[] players = new String[MAX_NUMBER_OF_PLAYERS];
        int index = 0;
        long lastTime = Long.MAX_VALUE;
        long curTime = System.nanoTime();

        while (!((numberOfPlayers == MAX_NUMBER_OF_PLAYERS) || (numberOfPlayers > 1)
                && (((curTime = System.nanoTime()) - lastTime) > WAIT_TIME))) {
            if (!playersQueue.isEmpty()) {
                try {
                    players[index++] = playersQueue.poll(10_000, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    return players;
                }
                numberOfPlayers++;

                if (numberOfPlayers == 1) {
                    lastTime = System.nanoTime();
                }
            }
        }

        String[] finalList = new String[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++)
            finalList[i] = players[i];

        return finalList;
    }

    void makeSession(String[] players) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        Request request;
        Response response;
        Long id;

        request = new Request.Builder()
                .post(RequestBody.create(mediaType , "playerCount=" + numberOfPlayers))
                .url(PROTOCOL + HOST + PORT + "/game/create")
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
        try {
            id = Long.parseLong(response.body().string());
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        for (String names: players)
            playersId.put(names, id);
        repository.saveGameSession(id, players);
        numberOfPlayers = 0;
    }

    int getNumberOfWaitingPlayers() {
        return numberOfPlayers + playersQueue.size();
    }
}
