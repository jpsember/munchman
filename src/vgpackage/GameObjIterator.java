package vgpackage;
import mytools.*;

public final class GameObjIterator {

    public GameObj get(int i) {
        return list[i];
    }

    public void store(int position, GameObj obj) {
        list[position] = obj;
    }

    public boolean isRoom() {
        return (iterIndex < maxObjects);
    }

    public void store(GameObj obj) {
        store(iterIndex++, obj);
    }

    public void toFirst() {
        iterIndex = 0;
    }

    public boolean isNext() {
        return (iterIndex < maxObjects);
    }

    public GameObj getNext() {
        return list[iterIndex++];
    }

    /**
     * Return the next active object, or null.
     * Returns the next object whose status is not S_VACANT, or null
     * if none are found.
     */
    public GameObj getNextActive() {
        while (isNext()) {
            GameObj g = getNext();
            if (g.getStatus() != GameObj.S_VACANT)
                return g;
        }
        return null;
    }

    public GameObjIterator(int max) {
        maxObjects = max;
        list = new GameObj[maxObjects];
    }

    private GameObjIterator() {}

    /**
     * Returns a new iterator based on this one, but with the index
     * reset to zero.
     */
    public GameObjIterator getNewUNUSED() {
        GameObjIterator iter = new GameObjIterator();
        iter.list = list;
        iter.maxObjects = maxObjects;
        return iter;
    }

    private GameObj[] list;
    private int maxObjects;
    private int iterIndex;
}
