package vgpackage;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class AnimScript implements AnimScriptConst {

    private static final int TIMESCALE = 1000/100;

    private AnimScript() {}

    /**
     * Create a new animation script.
     * @param script the script, consisting of any number of fields,
     * the last one of which must be END.
     * @param handler an object that implements IAnimScript, to return
     * String and Sprite objects.
     */
    public AnimScript(short[] script, CharSet cs, IAnimScript handler) {
        this.script = script;
        scriptHandler = handler;
        charSet = cs;
        constructElements();
        startTime = (int)VidGame.getSystemTime();
    }

    public CharSet getCharSet() {
        return charSet;
    }

    /**
     * Calculates the length of an AnimScript field.
     * @param array the array of fields
     * @param offset the location of the field in the array
     * @return the length of the field, or zero if no more exist
     */
    public static int fieldLength(short[] array, int offset) {

        int startOffset = offset;

        if (offset == array.length)
            return 0;

        int length = 0;

        short command = array[offset + 0];
        switch (command) {
        case END:
            length = 1;
            break;
        case LOC:
            length = 3;
            break;
        default:
            length = 2;
        }
        return length;
    }

    private CharSet charSet;
    private AnimElement[] elements;
    private static final int MAX_ELEMENTS = 50;
    private int totalElements;
    private AnimElement workElement;
    private int startTime;

    private static final int FLAGS_PER_BYTE = 7;

    private byte visibleFlags[];

    private void constructElements() {
        elements = new AnimElement[MAX_ELEMENTS + 1];
        totalElements = 0;

        cursor = 0;
        workElement = new AnimElement();
        elements[totalElements] = workElement;

//        db.pr("constructElements()");
        workElement.type = read();
//        db.pr(" initial type read = "+workElement.type);
        while (workElement.type != END) {
            if (workElement.id == AnimElement.ID_UNDEFINED) {
//                db.pr(" reading id for type");
                workElement.id = read();
                workElement.setDefaults(this);
            }

//            db.pr("reading command");
            int command = read();
            if (command <= TYPES_END) {
                if (workElement.id != AnimElement.ID_UNDEFINED)
                    addElement();
//                db.pr(" read new type = "+command);
                workElement.type = command;
                workElement.id = AnimElement.ID_UNDEFINED;
                continue;
            }
            switch (command) {
            case LOC:
                workElement.loc.x = read();
                workElement.loc.y = read();
                break;
            case Y:
                workElement.loc.y = read();
                break;
            case X:
                workElement.loc.x = read();
                break;
            case START:
                workElement.startTime = read() * TIMESCALE;
                break;
            case DURATION:
                workElement.duration = read() * TIMESCALE;
                break;
            case SIGNAL:
                workElement.signalFlag = true;
                workElement.signalCode = read();
                break;
            }
        }

        visibleFlags = new byte[(totalElements + FLAGS_PER_BYTE - 1) / FLAGS_PER_BYTE];
    }

    private void addElement() {
        switch(workElement.type) {
        case STRING:
        case CSTRING:
            processString(workElement);
            break;
        case SPRITE:
            processSprite(workElement);
            break;
        }

        if (totalElements == MAX_ELEMENTS) {
            Debug.print("TOO MANY ANIM ELEMENTS!");
            return;
        }
        workElement = new AnimElement();
        elements[++totalElements] = workElement;
    }

    private void processString(AnimElement e) {
//        String s = (String)objArray[e.id];
        String s = (String)scriptHandler.getObject(this, e.id);

        // Determine bounding rectangle of string, relative to
        // its plot location.

        Point pt = charSet.stringBounds(s);
        e.bounds.setBounds(
            e.type == CSTRING ? -(pt.x >> 1) : 0,
            -charSet.getSizeY(),pt.x,pt.y);
        e.string = s;
    }

    private void processSprite(AnimElement e) {
        Sprite s = (Sprite)scriptHandler.getObject(this, e.id);
        // Determine the bounding rectangle of the sprite, relative
        // to its plot location.
        e.bounds.setBounds(-s.cx,-s.cy, s.w,s.h);
        e.sprite = s;

    }

    private short[] script;
    private IAnimScript scriptHandler;

    private int currentTime;
    private int cursor;

    private short[] signalArray = new short[16];
    private int signalTotal = 0;

    private int read() {
        return script[cursor++];
    }

//    private Graphics graphics;

    public void stop() {//Graphics g, boolean valid) {
        update(true);//g, valid, true);
    }

    /**
     * Update the script animation
     */
    public void update() {//Graphics g, boolean valid) {
        update(false);//g, valid, false);
    }

    // This is not used at present, so I made it private.
    private boolean itemVisible(int id) {
        for (int i = totalElements-1; i >= 0; i--) {
            if (elements[i].id == id) {
                return getVisibleFlag(i);
            }
        }
        return false;
    }

    private boolean getVisibleFlag(int i) {
        int shift = (i % FLAGS_PER_BYTE);
        byte flag = (byte)(visibleFlags[i / FLAGS_PER_BYTE] >> shift);
        return (flag & 1) != 0;
    }

    private void setVisibleFlag(int i, boolean flag) {
        int shift = (i % FLAGS_PER_BYTE);
        int index = i / FLAGS_PER_BYTE;
//        byte data = visibleFlags[i / FLAGS_PER_BYTE];
        byte bit = (byte)(1 << shift);
        byte mask = (byte)~bit;
        if (!flag)
            bit = 0;

        visibleFlags[index] = (byte)((visibleFlags[index] & mask) | bit);
    }

    private void update(/*Graphics g, boolean valid,*/ boolean stop) {

//        graphics = BEngine.getGraphics();
        boolean valid = BEngine.layerValid();
        currentTime = ((int)VidGame.getSystemTime()) - startTime;
        boolean spriteLayer = (BEngine.getLayer() == BEngine.L_SPRITE);

        for (int i = 0; i < totalElements; i++) {
            AnimElement e = elements[i];

            int timePos = currentTime - e.startTime;
            boolean newVisible = (!stop && timePos >= 0
                && (e.duration == 0 || timePos < e.duration));
            boolean oldVisible = getVisibleFlag(i);

            // If it was visible and is no longer, erase it if not
            // plotting to the sprite layer.

            if (!valid)
                oldVisible = false;
//                e.visible = false;

            if (oldVisible && !newVisible && !spriteLayer) {
                eraseElement(e);
                oldVisible = false;
            }
            // If now visible and wasn't before, or we're plotting
            // to the sprite layer, draw it.

            if (newVisible) {
                if (!oldVisible || spriteLayer) {
                    plotElement(e);
                    oldVisible = true;
                }
            }
            setVisibleFlag(i, oldVisible);

            if (newVisible && e.signalFlag) {
                e.signalFlag = false;
                addSignal(e.signalCode);
            }
        }
    }

    private void addSignal(int code) {
        if (signalTotal < signalArray.length) {
            signalArray[signalTotal++] = (short)code;
        }
    }

    public int readSignal() {
        if (signalTotal == 0) return 0;
        return signalArray[--signalTotal];
    }

    private void eraseElement(AnimElement e) {
        BEngine.fillRect(/*graphics,*/ e.bounds.x + e.loc.x,
            e.bounds.y + e.loc.y, e.bounds.width, e.bounds.height);
//        e.visible = false;
    }

    private void plotElement(AnimElement e) {
        if (e.type == SPRITE) {
            BEngine.drawSprite(/*graphics,*/ e.sprite,
                e.loc.x,e.loc.y);
        } else {
            charSet.plotString(/*graphics,*/ e.string,
                e.bounds.x + e.loc.x, e.bounds.y + e.loc.y + charSet.getSizeY());
        }
//        e.visible = true;
    }

}

class AnimElement {
    public int type;
    public Point loc = new Point();
    public int startTime;
    public int duration;
    public int id;
//    public boolean visible;
    public Rectangle bounds = new Rectangle();
    public String string;
    public Sprite sprite;
    public static final int ID_UNDEFINED = -1;
    public boolean signalFlag;
    public int signalCode;

    public AnimElement() {
        id = ID_UNDEFINED;
    }

    public void setDefaults(AnimScript scr) {
        if (type == AnimScript.CSTRING) {
            loc.setLocation(
                BEngine.viewR.width >> 1,
                (BEngine.viewR.height + scr.getCharSet().getSizeY() ) >> 1
            );
        } else {
            loc.setLocation(0,0);
        }
        startTime = 0;
        duration = 0;
        signalFlag = false;
    }
}
