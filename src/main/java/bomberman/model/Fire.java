package bomberman.model;

import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Fire {
    private final Point position;
    private final int id;
    private final String type = "Fire";

    @JsonIgnore
    private Bar bar;

    private static final int size = 28;

    public Fire(int id,Bar bar) {
        this.id = id;
        this.bar = bar;
        this.position = bar.getPosition();
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}
