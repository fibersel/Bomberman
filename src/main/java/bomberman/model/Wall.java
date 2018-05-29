package bomberman.model;

import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by imakarychev on 22.04.18.
 */
public class Wall implements Tickable,Pathless {
    private final Point position;
    private final int id;
    private final String type = "Wall";


    @JsonIgnore
    private Bar bar;


    private static final int size = 32;

    public Wall(int id,Bar bar) {
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


}
