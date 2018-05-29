package bomberman.gameservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Ivan Makarychev on 13.05.18.
 */
@Controller
@RequestMapping("game")
public class GameController {

    @RequestMapping("")
    public String getHomePage() {
        return "forward:/game/index.html/?name=Ivan";
    }
}
