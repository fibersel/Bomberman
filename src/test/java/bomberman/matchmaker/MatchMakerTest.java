package bomberman.matchmaker;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;




@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MatchMakerTest {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConcurrentHashMap<Long,AtomicInteger> mapCounter;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchMakerTest.class);

    private static final int NUMBER_OF_REQESTS_TEST_1 = 16;

    private static final int NUMBER_OF_REQESTS_TEST_2 = 9;

    private static final int RANK_THRESHOLD_TEST_1 = 4;

    private static final int RANK_THRESHOLD_TEST_2 = 3;

    @Test
    public void matchMakerTest() throws InterruptedException {
        mapCounter.clear();
        Collection<Thread> list = new LinkedList<>();
        int rank = 5;
        for (int i = 0;i < NUMBER_OF_REQESTS_TEST_1;i++) {
            if (i % RANK_THRESHOLD_TEST_1 == 0 && i > 0)
                rank += 10;
            TestClient client = ctx.getBean(TestClient.class);
            client.setRank(rank);
            Thread thread = new Thread(client);
            list.add(thread);
            thread.start();
        }

        for (Thread thread:list) {
            thread.join();
        }

        for (Long key: mapCounter.keySet()) {
            Assert.assertTrue(mapCounter.get(key).intValue() == RANK_THRESHOLD_TEST_1);
        }

    }


    @Test
    public void matchMakerIncompleteSessionTest() throws InterruptedException {
        mapCounter.clear();
        Collection<Thread> list = new LinkedList<>();
        int rank = 5;
        for (int i = 0;i < NUMBER_OF_REQESTS_TEST_2;i++) {
            if (i % RANK_THRESHOLD_TEST_2 == 0 && i > 0)
                Thread.sleep(12_000);
            TestClient client = ctx.getBean(TestClient.class);
            client.setRank(rank);
            Thread thread = new Thread(client);
            list.add(thread);
            thread.start();
        }
        for (Thread thread:list) {
            thread.join();
        }

        for (Long key: mapCounter.keySet()) {
            Assert.assertTrue(mapCounter.get(key).intValue() == RANK_THRESHOLD_TEST_2);
        }
    }





}
