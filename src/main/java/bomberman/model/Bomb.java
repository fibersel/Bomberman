package bomberman.model;

import bomberman.gameservice.GameSession;
import bomberman.model.geometry.Bar;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

/**
 * Created by imakarycheva on 22.04.18.
 */
public class Bomb implements Tickable {
    private final String type = "Bomb";
    private int id;
    private Point position;
    private int strength;

    private static int size = 28;

    @JsonIgnore
    private final long cooldown = 3000;
    @JsonIgnore
    private Bar bar;
    @JsonIgnore
    private long timer;
    @JsonIgnore
    private Character owner;
    @JsonIgnore
    private ArrayList<Bar> barsToBlow;

    public Bomb(int id,Bar bar, int strength, Character owner) {
        this.id = id;
        this.strength = strength;
        this.bar = bar;
        this.bar.setBomb(this);
        this.position = bar.getPosition();
        this.owner = owner;
        this.timer = System.currentTimeMillis();
        barsToBlow = new ArrayList<>();
    }

    @Override
    public void tick(long elapsed) {
        if (System.currentTimeMillis() - timer > cooldown) {
            blow();
        }
    }

    public void dropCooldown() {
        timer -= cooldown;
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

    public int getStrength() {
        return strength;
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }

    public int getSize() {
        return size;
    }

    public void blow() {
        timer = Long.MAX_VALUE;
        bar.removeBomb();
        Fire fire = new Fire(GameSession.id++, bar);
        owner.getContainer().getObjsToSend().add(fire);
        bar.getChars().stream().forEach(Character::kill);

        getBarsInDirections();
        for (Bar b: barsToBlow) {
            fire = new Fire(GameSession.id++, b);
            owner.getContainer().getObjsToSend().add(fire);
            b.getChars().forEach(Character::kill);
            if (b.isWood()) {
                owner.getContainer().getObjsToSend().add(b.getPlug());
                b.removeWood();
            }
            if (b.bombStands()) {
                b.getBomb().dropCooldown();
                b.removeBomb();
            }
            if (b.hasBonus()) {
                owner.getContainer().getObjsToSend().add(b.getBonus());
            }
        }
        barsToBlow.clear();
        owner.getContainer().getObjsToSend().remove(this);
        owner.getContainer().getObjsToTick().remove(this);
        owner.addBomb();
    }

    void getBarsInDirections() {
        Bar bar1;
        for (int i = 0; i < 4; i++) {
            for (int j = 1; strength - j >= 0; j++) {
                switch (i) {
                    case 0:
                        bar1 = owner.getContainer().getField().getBar(bar.getCoordX() + j, bar.getCoordY());
                        break;
                    case 1:
                        bar1 = owner.getContainer().getField().getBar(bar.getCoordX() - j, bar.getCoordY());
                        break;
                    case 2:
                        bar1 = owner.getContainer().getField().getBar(bar.getCoordX(), bar.getCoordY() + j);
                        break;
                    case 3:
                        bar1 = owner.getContainer().getField().getBar(bar.getCoordX(), bar.getCoordY() - j);
                        break;
                    default:
                        bar1 = bar;
                }
                if (bar1.isWall()) {
                    break;
                }
                if (bar1.isWood()) {
                    barsToBlow.add(bar1);
                    break;
                }
                barsToBlow.add(bar1);
            }
        }
    }
}
