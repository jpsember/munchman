package vgpackage;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class AnimScriptBuilder implements AnimScriptConst {

    public AnimScriptBuilder() {
        array = new short[200];
        used = 0;
    }

    private void addEnd() {
        final short[] end = {END};
        if (!endExists) {
            add(end,0,1);
            endExists = true;
        }
    }

    private static short[] workField = new short[2];

    /**
     * Appends a script array to this one
     */
    public void add(short[] a, int time) {

        int addLength = a.length;

        // If end exists, get rid of it.
        if (endExists) {
            endExists = false;
            used -= 1;
        }

        // Copy fields until END encountered.  Don't include the
        // final END.

        int offset = 0;
        int typeOpen = -1;
        boolean startDefined = false;
        int fieldStartTime = 0;

        while (true) {
            int len = AnimScript.fieldLength(a, offset);
            short code = AnimScript.END;
            if (len != 0)
                code = a[offset];

            if (code <= AnimScript.TYPES_END) {
                // If a type is already open, see if we need to
                // add a START field.
                if (typeOpen >= 0) {
                    if (!startDefined && time > 0) {
                        startDefined = true;
                        workField[0] = START;
                        workField[1] = (short)fieldStartTime;
                        add(workField,0,2);
                    }
                }

                typeOpen = code;
                startDefined = false;
                fieldStartTime = time;
            }

            if (code == START) {
                fieldStartTime = time + a[offset+1];
                continue;   // skip this field
            }

            if (code == END) break;
            add(a,offset,len);
            offset += len;
        }

//        System.arraycopy(a,0,array,used,addLength);
//        used = requiredLength;
    }

    private void add(short[] a, int offset, int length) {
        int requiredLength = length + used;
        if (requiredLength > array.length) {
            resize(requiredLength << 1);
        }
        System.arraycopy(a,offset,array,used,length);
        used += length;
    }

    /**
     * Return script array
     */
    public short[] getScript() {
        addEnd();
        return array;
    }

    private void resize(int newLen) {
        short[] aNew = new short[newLen];
        System.arraycopy(array,0,aNew,0,used);
        array = aNew;
    }

    private short[] array;
    private int used = 0;
    private boolean endExists = false;
//    private int endOffset;
}