package bomberman.model;

import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by imakarycheva on 22.04.18.
 */
public class Bonus implements Tickable {
    private final String type = "Bonus";
    private final int id;
    private Point position;
    private final BonusType bonusType;
    @JsonIgnore
    private Bar bar;

    public Bonus(int id, BonusType bonusType, Bar bar) {
        this.id = id;
        this.bonusType = bonusType;
        this.bar = bar;
        position = bar.getPosition();
    }

    public int getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void relocate(Bar newBar) {
        bar = newBar;
        position = newBar.getPosition();
    }

    @Override
    public void tick(long elapsed) {
        
    }
}
