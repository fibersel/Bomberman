package bomberman.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataContainer {
    private CopyOnWriteArrayList<Tickable> objsToTick;
    private CopyOnWriteArrayList<Object> objsToSend;
    private Field field;

    public DataContainer() {
        this.objsToTick = new CopyOnWriteArrayList<>();
        this.objsToSend = new CopyOnWriteArrayList<>();
        this.field = new Field();
        for (Object o: field.getObjects())
            objsToSend.add(o);
    }


    public Field getField() {
        return field;
    }

    public void setObjsToSend(CopyOnWriteArrayList list) {
        objsToSend = list;
    }

    public List getObjsToSend() {
        return objsToSend;
    }

    public CopyOnWriteArrayList<Tickable> getObjsToTick() {
        return objsToTick;
    }
}
