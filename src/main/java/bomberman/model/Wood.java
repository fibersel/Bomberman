package bomberman.model;

import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by imakarycheva on 22.04.18.
 */
public class Wood implements Tickable,Pathless {
    private final Point position;
    private final int id;
    private final String type = "Wood";


    @JsonIgnore
    private Bar bar;


    private static int size = 32;

    public Wood(int id, Bar bar) {
        this.id = id;
        this.bar = bar;
        this.position = bar.getPosition();
    }

    @Override
    public void tick(long elapsed) {

    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }
}
