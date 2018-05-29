package bomberman.model.geometry;

import bomberman.model.Bomb;
import bomberman.model.Point;
import bomberman.model.Pathless;
import bomberman.model.Wall;
import bomberman.model.Wood;
import bomberman.model.Character;
import bomberman.model.Bonus;

import java.util.LinkedList;
import java.util.List;

public class Bar implements Collider {
    private int coordX;
    private int coordY;
    private Point position;
    private Pathless plug;
    private Bomb bomb;
    private Bonus bonus;
    private List<Character> chars;
    private static int size = 32;

    public Bar(int coordX,int coordY, int j, int i) {
        this.plug = null;
        this.bomb = null;
        this.position = new Point(coordX,coordY);
        this.coordX = j;
        this.coordY = i;
        this.chars = new LinkedList<>();
    }

    @Override
    public boolean isColliding(Collider other) {
        return false;
    }

    public static int getSize() {
        return size;
    }

    public int getCoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setBomb(Bomb bomb) {
        this.bomb = bomb;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public boolean bombStands() {
        return this.bomb != null;
    }

    public void removeBomb() {
        if (bomb == null) {
            System.out.println("null");
        }
        bomb = null;
    }

    public boolean isFree()
    {
        return plug == null && bomb == null;
    }

    public List<Character> getChars() {
        return chars;
    }

    public boolean isWall() {
        if (plug == null) {
            return false;
        }
        return plug.getClass() == Wall.class;
    }

    public boolean isWood() {
        if (plug == null) {
            return false;
        }
        return plug.getClass() == Wood.class;
    }

    public void setWall(Wall wall) {
        if (wall != null) {
            plug = wall;
        }
    }

    public void setWood(Wood wood) {
        if (wood != null) {
            plug = wood;
        }
    }

    public Pathless getPlug() {
        return plug;
    }

    public void clearBar() {
        plug = null;
    }

    public Point getPosition() {
        return position;
    }

    public void addChar(Character c) {
        chars.add(c);
    }

    public void removeChar(Character c) {
        chars.remove(c);
    }

    public void removeWood() {
        if (plug == null) {
            return;
        }
        if (plug.getClass() == Wood.class) {
            plug = null;
        }
    }

    public boolean hasBonus() {
        return bonus != null;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public void removeBonus() {
        bonus = null;
    }
}
