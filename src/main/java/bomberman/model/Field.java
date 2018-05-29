package bomberman.model;

import bomberman.gameservice.GameSession;
import bomberman.model.geometry.Bar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Field {
    private static final int height = 13;
    private static final int width = 17;
    private static final int BONUSES_NUMBER = 15;

    private Bar[][] field;
    private CopyOnWriteArrayList<Object> objects = new CopyOnWriteArrayList<>();

    public Field() {
        field = new Bar[height][width];
        int bonusesNumber = BONUSES_NUMBER;
        int bonusTypesNumber = BonusType.values().length;
        int woodNumber = 118;
        Set<Integer> bonusSet = new HashSet<>();
        Random random = ThreadLocalRandom.current();

        for (int i = 0; i < bonusesNumber; i++) {
            int woodToPlaceBonus;
            do {
                woodToPlaceBonus = random.nextInt(woodNumber);
            } while (bonusSet.contains(woodToPlaceBonus));
            bonusSet.add(woodToPlaceBonus);
            System.out.println(woodToPlaceBonus);
        }

        for (int i = 0;i < height;i++)
            for (int j = 0;j < width;j++) {
                field[i][j] = new Bar(j * Bar.getSize(), i * Bar.getSize(), j, i);
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
                    Wall wall = new Wall(GameSession.id++, field[i][j]);
                    field[i][j].setWall(wall);
                    objects.add(wall);
                } else if (j % 2 == 1 || i % 2 == 1) {
                    Wood wood = new Wood(GameSession.id++, field[i][j]);
                    woodNumber--;
                    field[i][j].setWood(wood);
                    if (bonusSet.contains(woodNumber)) {
                        woodNumber--;
                        Bonus bonus = new Bonus(GameSession.id++, BonusType.values()[bonusesNumber % bonusTypesNumber],
                                field[i][j]);
                        field[i][j].setBonus(bonus);
                        bonusesNumber--;
                    }
                    objects.add(wood);
                } else {
                    Wall wall = new Wall(GameSession.id++, field[i][j]);
                    field[i][j].setWall(wall);
                    objects.add(wall);
                }
            }
    }

    public CopyOnWriteArrayList<Object> getObjects() {
        return objects;
    }

    public Bar getBar(int x, int y) {
        return field[y][x];
    }

    ArrayList<Bar> getBarsAround(int centerX, int centerY) {
        ArrayList<Bar> bars = new ArrayList<>();
        if ((centerX >= 0) && (centerX < width) && (centerY >= 0) && (centerY < height))
            bars.add(getBar(centerX, centerY));
        if ((centerX - 1 >= 0) && (centerX - 1 < width) && (centerY >= 0) && (centerY < height))
            bars.add(getBar(centerX - 1, centerY));
        if ((centerX >= 0) && (centerX < width) && (centerY - 1 >= 0) && (centerY - 1 < height))
            bars.add(getBar(centerX, centerY - 1));
        if ((centerX + 1 >= 0) && (centerX + 1 < width) && (centerY >= 0) && (centerY < height))
            bars.add(getBar(centerX + 1, centerY));
        if ((centerX >= 0) && (centerX < width) && (centerY + 1 >= 0) && (centerY + 1 < height))
            bars.add(getBar(centerX, centerY + 1));

        return bars;
    }

    public void clearBar(int x, int y) {
        Bar bar = getBar(x,y);
        bar.clearBar();
        if (bar.hasBonus()) {
            Bonus bonus = bar.getBonus();
            bar.removeBonus();
            Bar newBar = getRandomBarWithWoodAndNoBonus();
            newBar.setBonus(bonus);
            bonus.relocate(newBar);
        }
    }

    private Bar getRandomBarWithWoodAndNoBonus() {
        Random random = ThreadLocalRandom.current();
        int xpos = 1 + random.nextInt(width - 2);
        int ypos = 1 + random.nextInt(height - 2);
        Bar result = field[ypos][xpos];
        while (!result.isWood() || result.hasBonus()) {
            xpos = 1 + random.nextInt(width - 2);
            ypos = 1 + random.nextInt(height - 2);
            result = field[ypos][xpos];
        }
        return result;
    }

}
