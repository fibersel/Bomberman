package bomberman.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by imakarychev on 22.04.18.
 */
public class Point {
    private int x;
    private int y;


    @JsonCreator
    public Point(@JsonProperty("x")int x,@JsonProperty("y")int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public Point setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public Point setY(int y) {
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }
}
