package bomberman.model;

import bomberman.gameservice.GameSession;
import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Character {
    private final String type = "Pawn";
    private final int id;
    private Point position;
    private Direction direction;
    private boolean alive;

    @JsonIgnore
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Character.class);

    @JsonIgnore
    private final String owner;
    @JsonIgnore
    private DataContainer container;
    @JsonIgnore
    private List<Bar> bars;
    @JsonIgnore
    private Bar mainBar;
    @JsonIgnore
    private long velocity = 1;
    @JsonIgnore
    private int bombsCtr;
    @JsonIgnore
    private int bombStrength;
    public static int width = 20;
    public static int height = 24;


    public Character(int x, int y, String owner, int id,DataContainer container) {
        this.position = new Point(x, y);
        this.alive = true;
        this.owner = owner;
        this.id = id;
        this.direction = Direction.DOWN;
        this.container = container;
        this.bars = getBarsByPosition(position);
        this.mainBar = bars.get(0);
        this.bombStrength = 1;
        this.bombsCtr = 1;
    }

    public void plant() {
        if (!alive)
            return;
        if (bombsCtr > 0 && !mainBar.bombStands()) {
            bombsCtr--;
            Bomb bomb = new Bomb(GameSession.id++, mainBar, bombStrength,this);
            container.getObjsToTick().add(bomb);
            container.getObjsToSend().add(bomb);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void move(String direction, long frametime) {
        if (!alive)
            return;
        long distance = velocity;
        long allowed;
        int delta;
        int xpos = position.getX() / Bar.getSize();
        int ypos = position.getY() / Bar.getSize();
        System.out.println(direction);
        switch (direction) {


            case "{\"direction\":\"UP\"}":
                this.direction = Direction.UP;

                allowed = Bar.getSize() - 1 - (position.getY() + height) % Bar.getSize();

                for (int i = (position.getY() + height) / Bar.getSize() + 1;
                     container.getField().getBar(xpos,i).isFree()
                             && (position.getX() % Bar.getSize() + width) < Bar.getSize(); i--) {
                    System.out.println(i + "  " + xpos);
                    allowed += Bar.getSize();
                }
                delta = (int)Math.min(allowed,distance);
                position.setY(position.getY() + delta);
                System.out.println(position);
                break;


            case "{\"direction\":\"RIGHT\"}":
                this.direction = Direction.RIGHT;

                allowed = Bar.getSize() - 1 -  (position.getX() + width) % Bar.getSize();

                for (int i = (position.getX() + width) / Bar.getSize() + 1; container.getField().getBar(i,ypos).isFree()
                        && (position.getY() % Bar.getSize() + height) < Bar.getSize();i++) {
                    System.out.println(i + "  " + ypos);
                    allowed += Bar.getSize();
                }
                delta = (int)Math.min(allowed,distance);
                position.setX(position.getX() + delta);
                System.out.println(position);
                break;


            case "{\"direction\":\"DOWN\"}":
                this.direction = Direction.DOWN;

                allowed = position.getY() % Bar.getSize();

                for (int i = ypos - 1; container.getField().getBar(xpos,i).isFree()
                        && (position.getX() % Bar.getSize() + width) < Bar.getSize(); i++) {
                    System.out.println(i + "  " + xpos);
                    allowed += Bar.getSize();
                }
                delta = (int)Math.min(allowed,distance);
                position.setY(position.getY() - delta);
                System.out.println(position);
                break;


            case "{\"direction\":\"LEFT\"}":
                this.direction = Direction.LEFT;

                allowed = position.getX() % Bar.getSize();

                for (int i = xpos - 1; container.getField().getBar(i,ypos).isFree()
                        && (position.getY() % Bar.getSize() + height) < Bar.getSize(); i--) {
                    System.out.println(i + "  " + ypos);
                    allowed += Bar.getSize();
                }
                delta = (int)Math.min(allowed,distance);
                position.setX(position.getX() - delta);
                System.out.println(position);
                break;

            default:
                break;
        }
        bars.stream().forEach(e -> e.removeChar(this));
        bars = getBarsByPosition(position);
        for (Bar bar: bars)
            checkAndApplyBonus(bar.getCoordX(),bar.getCoordY());
    }

    public void addBomb() {
        bombsCtr++;
    }

    public String getType() {
        return type;
    }

    public List<Bar> getBarsByPosition(Point position) {
        List<Bar> tmp = new LinkedList<>();
        int xpos = position.getX() / Bar.getSize();
        int ypos = position.getY() / Bar.getSize();
        System.out.println(xpos);
        System.out.println(ypos);
        tmp.add(container.getField().getBar(xpos,ypos));
        if (position.getX() % Bar.getSize() + width > Bar.getSize()) {
            tmp.add(container.getField().getBar(xpos + 1,ypos));
            if ((position.getX() % Bar.getSize() + width) % Bar.getSize() > Bar.getSize() / 2)
                mainBar = container.getField().getBar(xpos + 1,ypos);
            else
                mainBar = container.getField().getBar(xpos,ypos);
        } else if (position.getY() % Bar.getSize() + height > Bar.getSize()) {
            tmp.add(container.getField().getBar(xpos,ypos + 1));
            if ((position.getY() % Bar.getSize() + height) % Bar.getSize() > Bar.getSize() / 2)
                mainBar = container.getField().getBar(xpos,ypos + 1);
            else
                mainBar = container.getField().getBar(xpos,ypos);
        } else mainBar = tmp.get(0);
        tmp.forEach(e -> e.addChar(this));
        return tmp;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void kill() {
        alive = false;
    }

    public DataContainer getContainer() {
        return container;
    }

    private void checkAndApplyBonus(int x, int y) {
        Bar bar = container.getField().getBar(x,y);
        if (bar.hasBonus()) {
            Bonus bonus = bar.getBonus();
            bar.removeBonus();
            applyBonus(bonus);
            container.getObjsToSend().add(bonus);
            log.info("Character# {} got bonus {}", id, bonus.getBonusType());
        }
    }

    private void applyBonus(Bonus bonus) {
        switch (bonus.getBonusType()) {
            case SPEED:
                velocity++;
                break;
            case RANGE:
                bombStrength++;
                break;
            case BOMBS:
                bombsCtr++;
                break;
            default:
                break;
        }
    }

    public String getOwner() {
        return owner;
    }
}
