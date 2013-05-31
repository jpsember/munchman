package vgpackage;
import mytools.*;
/**
 * This class implements a design pattern for video game objects.
 */
public abstract class GameObj {
    public static final int GAME = 0;
    public static final int LEVEL = 1;

    // Class methods

    /**
     * Prepares for a new game or level.  Override this method; the
     * default method does nothing.
     * @param type GAME, LEVEL, or user-defined value
     */
    public static void prepare(int type) {}

    /**
     * Determines if a game level has been completed.
     * Override this method as required.  The default method always
     * returns true.
     */
    public static boolean levelCompleted() {
        return true;
    }

    /**
     * Move all the objects.  Also, process any other logic.
     */
    public static void move() {}

    /**
     * Plot all the objects.
     */
    public static void draw() {}

    /**
     * Draws the objects by using a GameObjIterator to call moveOne().
     * @param iter all active objects will have their moveOne()
     *  methods called.
     */
    protected static void move(GameObjIterator iter) {
        iter.toFirst();
        while (true) {
            GameObj obj = iter.getNextActive();
            if (obj == null) break;
            obj.moveOne();
        }
    }

    /**
     * Draw all the objects by using a GameObjIterator to call drawOne().
     * @param iter all active objects will have their drawOne(g,valid)
     *  methods called.
     */
     protected static void draw(GameObjIterator iter) {
        iter.toFirst();
        while (true) {
            GameObj obj = iter.getNextActive();
            if (obj == null) break;
            obj.drawOne();
        }
    }

    /**
     * Attempts to find a vacant GameObj (one whose status field is S_VACANT)
     * @param iter iterator to use
     */
    protected static GameObj findVacant(GameObjIterator iter) {
        iter.toFirst();
        while (iter.isNext()) {
            GameObj test = iter.getNext();
            if (test.getStatus() == S_VACANT)
                return test;
        }
        return null;
    }

    // Instance methods

    /**
     * Constructor.  It is protected so no objects can be
     * instantiated except from a subclass.
     */
    protected GameObj() {}

    /**
     * Moves the object.  Override this method to process the movement
     * of individual objects.
     */
    protected void moveOne() {}

    /**
     * Draws the object.  Override this method to process the drawing of
     * individual objects.
     */
    protected void drawOne() {}

    /**
     * Sets the status of the object.  Override this method if additional
     * bookkeeping is required.
     */
    public void setStatus(int s) {
        if (status != s) {
            if (s == S_VACANT)
                adjustActiveCount(-1);
            else if (status == S_VACANT)
                adjustActiveCount(1);
            status = s;
        }
    }

    /**
     * Adjust the number of active objects by a value.
     * Override this method to modify the subclass-specific
     * total.
     * If this is overriden, it is not calling the override method!
     * Is this because it's a static?
     */
    public void adjustActiveCount(int val) {}

    /**
     * Returns the status of the object.
     */
    public final int getStatus() {
        return status;
    }

    /**
     * Returns true if object is not null, and its status is not S_VACANT
     */
    public static final boolean active(GameObj obj) {
        return (obj != null && obj.getStatus() != S_VACANT);
    }

    /**
     * Status value for an unused object.  Subclasses should define
     * their own non-zero status values for objects that are in use.
     */
    protected static final int S_VACANT = 0;

    // Instance fields

    private int status = S_VACANT;
}
