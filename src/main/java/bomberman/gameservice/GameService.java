package bomberman.gameservice;

import bomberman.matchmaker.MatchMakerRepository;
import bomberman.model.Character;
import bomberman.model.Message;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping("game")
public class GameService {

    @Autowired
    MatchMakerRepository repository;

    @Resource(name = "games")
    private ConcurrentHashMap<Long,GameSession> games;

    @Resource(name = "queues")
    private ConcurrentHashMap<Long,BlockingQueue<Message>> gameQueues;

    private static AtomicLong numOfGame = new AtomicLong(0);

    @PostConstruct
    private void init() {
        numOfGame = new AtomicLong(repository.getLastSessionId());
    }

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GameService.class);


    /*
     *  curl -X POST -i http://localhost:8080/game/create -d "playerCount=4"
     * */

    @RequestMapping(
            path = "create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity create(@RequestParam("playerCount") int playerCount) {
        Long id = numOfGame.incrementAndGet();
        BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
        GameSession session = new GameSession(this,id,queue,playerCount);
        gameQueues.put(id,queue);
        games.put(id,session);
        return ResponseEntity.ok(id.toString());
    }


    public int addPlayer(Long gameId, WebSocketSession session,String owner) {
        int playerNum = games.get(gameId).addCharacter(session,owner);
        return playerNum;
    }

    public BlockingQueue<Message> getQueue(Long gameId) {
        return gameQueues.get(gameId);
    }

    public void deleteSession(Long gameId, Character[] players) {
        int rank;
        String login;
        for (Character c: players) {
            if (c != null) {
                login = c.getOwner();
                rank = repository.getUserRank(login);
                if (c.isAlive()) {
                    repository.setUserRank(login,Math.min(rank + 3,39));
                } else {
                    repository.setUserRank(login,Math.max(rank - 1,0));
                }
            }
        }
        games.remove(gameId);
        gameQueues.remove(gameId);
        log.info("Session {} deleted", gameId);
    }
}
