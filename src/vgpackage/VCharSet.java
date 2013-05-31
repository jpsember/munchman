package vgpackage;

import java.awt.*;
import java.util.*;

public class VCharSet
    implements VidGameGlobals {
  public final static int C_COLOR = 1;
  public final static int C_BOLD = 2;
  private final static int C_TOTAL = 3;

  private FPt ptMisc = new FPt();
  protected int colorHue, colorBrightness;
  protected float sizeX, sizeY;
  protected float spacingX, spacingY;
  protected boolean boldStatus;
  protected Obj obj;
  protected Vector meshes; // CharMesh objects

  protected final static int STD_BRIGHTNESS = 16;

  public VCharSet() {
    //this.ve = ve;
    meshes = new Vector(50);
    obj = new Obj(false);
    colorHue = 0;
    colorBrightness = STD_BRIGHTNESS;
  }

  // Find a set of commands for character, or null if char is not in set
  public short[] findCharScript(char c) {
    Enumeration en = meshes.elements();
    while (en.hasMoreElements()) {
      CharMesh cm = (CharMesh) en.nextElement();
      if (cm.c == c)
        return cm.commands;
    }
    return null;
  }

  public static final byte basicCharSetCoords[] = {
      (byte) 4, (byte) 8, // X, Y size
      (byte) 2, (byte) 2, // X, Y spacing
      (byte) '0', 1, 0, 3, 0, 4, 2, 4, 6, 3, 8, 1, 8, 0, 6, 0, 2, 1, 0, -9,
      (byte) '1', 1, 0, 3, 0, -1, 2, 0, 2, 8, 1, 8, -9,
      (byte) '2', 0, 7, 1, 8, 3, 8, 4, 7, 4, 4, 0, 0, 4, 0, -9,
      (byte) '3', 0, 1, 1, 0, 3, 0, 4, 1, 4, 3, 3, 4, 4, 5, 4, 7, 3, 8, 1, 8, 0,
      7, -1, 1, 4, 3, 4, -9,
      (byte) '4', 4, 0, 4, 8, 0, 4, 4, 4, -9,
      (byte) '5', 0, 0, 3, 0, 4, 1, 4, 3, 3, 4, 0, 4, 0, 8, 4, 8, -9,
      (byte) '6', 4, 7, 3, 8, 1, 8, 0, 7, 0, 1, 1, 0, 3, 0, 4, 1, 4, 3, 3, 4, 0,
      4, -9,
      (byte) '7', 0, 0, 4, 8, 0, 8, -9,
      (byte) '8', 1, 4, 0, 3, 0, 1, 1, 0, 3, 0, 4, 1, 4, 3, 3, 4, 1, 4, 0, 5, 0,
      7, 1, 8, 3, 8, 4, 7, 4, 5, 3, 4, -9,
      (byte) '9', 0, 1, 1, 0, 3, 0, 4, 1, 4, 7, 3, 8, 1, 8, 0, 7, 0, 5, 1, 4, 4,
      4, -9,
      (byte) 'A', 0, 0, 2, 8, 4, 0, -1, 1, 4, 3, 4, -9,
      (byte) 'B', 0, 4, 3, 4, 4, 3, 4, 1, 3, 0, 0, 0, 0, 8, 3, 8, 4, 7, 4, 5, 3,
      4, -9,
      (byte) 'C', 4, 2, 3, 0, 1, 0, 0, 2, 0, 6, 1, 8, 3, 8, 4, 6, -9,
      (byte) 'D', 0, 0, 0, 8, 3, 8, 4, 6, 4, 2, 3, 0, 0, 0, -9,
      (byte) 'E', 4, 8, 0, 8, 0, 0, 4, 0, -1, 0, 4, 3, 4, -9,
      (byte) 'F', 4, 8, 0, 8, 0, 0, -1, 0, 4, 3, 4, -9,
      (byte) 'G', 4, 6, 3, 8, 1, 8, 0, 6, 0, 2, 1, 0, 3, 0, 4, 2, 4, 4, 2, 4,
      -9,
      (byte) 'H', 0, 0, 0, 8, -1, 4, 0, 4, 8, -1, 0, 4, 4, 4, -9,
      (byte) 'I', 1, 0, 3, 0, -1, 1, 8, 3, 8, -1, 2, 0, 2, 8, -9,
      (byte) 'J', 0, 2, 1, 0, 3, 0, 4, 2, 4, 8, -9,
      (byte) 'K', 0, 0, 0, 8, -1, 4, 8, 0, 4, 4, 0, -9,
      (byte) 'L', 0, 8, 0, 0, 4, 0, -9,
      (byte) 'M', 0, 0, 0, 8, 2, 5, 4, 8, 4, 0, -9,
      (byte) 'N', 0, 0, 0, 8, 4, 0, 4, 8, -9,
      (byte) 'O', 1, 0, 3, 0, 4, 2, 4, 6, 3, 8, 1, 8, 0, 6, 0, 2, 1, 0, -9,
      (byte) 'P', 0, 0, 0, 8, 3, 8, 4, 7, 4, 5, 3, 4, 0, 4, -9,
      (byte) 'Q', 1, 0, 3, 0, 4, 2, 4, 6, 3, 8, 1, 8, 0, 6, 0, 2, 1, 0, -1, 3,
      2, 4, 0, -9,
      (byte) 'R', 0, 0, 0, 8, 3, 8, 4, 7, 4, 5, 3, 4, 0, 4, 4, 0, -9,
      (byte) 'S', 0, 1, 1, 0, 3, 0, 4, 1, 4, 3, 3, 4, 1, 4, 0, 5, 0, 7, 1, 8, 3,
      8, 4, 7, -9,
      (byte) 'T', 0, 8, 4, 8, -1, 2, 8, 2, 0, -9,
      (byte) 'U', 0, 8, 0, 2, 1, 0, 3, 0, 4, 2, 4, 8, -9,
      (byte) 'V', 0, 8, 2, 0, 4, 8, -9,
      (byte) 'W', 0, 8, 0, 0, 2, 3, 4, 0, 4, 8, -9,
      (byte) 'X', 0, 0, 4, 8, -1, 0, 8, 4, 0, -9,
      (byte) 'Y', 0, 8, 2, 5, 4, 8, -1, 2, 5, 2, 0, -9,
      (byte) 'Z', 0, 8, 4, 8, 0, 0, 4, 0, -9,
      (byte) ':', 2, 2, 2, 3, -1, 2, 5, 2, 6, -9,
      (byte) '!', 2, 0, 2, 2, -1, 2, 3, 2, 8, -9,
      (byte) '>', 0, 4, 4, 4, -1, 2, 6, 4, 4, 2, 2, -9,
      (byte) '<', 4, 4, 0, 4, -1, 2, 6, 0, 4, 2, 2, -9,
      (byte) '^', 2, 2, 2, 6, -1, 0, 4, 2, 6, 4, 4, -9,
      (byte) ',', 1, 0, 2, 1, 2, 2, -9,
      (byte) '.', 2, 0, 2, 1, -9,
      (byte) '\'', 2, 8, 2, 7, -9,
      (byte) '(', 3, 0, 2, 1, 1, 3, 1, 5, 2, 7, 3, 8, -9,
      (byte) ')', 2, 0, 3, 1, 3, 3, 3, 5, 2, 7, 1, 8, -9,
      (byte) '/', 0, 0, 4, 8, -9,
      (byte) '\\', 4, 0, 0, 8, -9,
      0
  };

  // Construct short[] meshes for the basic character set
  private final static short header[] = {
      COLOR, 0, (short) STD_BRIGHTNESS};

  public static VCharSet construct(byte coords[]) {

    VCharSet set = new VCharSet();
    short[] workCmds = new short[80];

    // Store the CharMesh objects in a Vector.

    int i = 0;
    set.sizeX = coords[i++];
    set.sizeY = coords[i++];
    set.spacingX = coords[i++];
    set.spacingY = coords[i++];
    while (true) {
      int c = coords[i++];

      if (c == 0)
        break; // We're done all the characters
      CharMesh work = new CharMesh();
      work.c = (char) c;

      int j;
      int k = header.length; // IE bug
      for (j = 0; j < k; j++) {
        workCmds[j] = header[j];
      }

      boolean startDefined = false;

      while (coords[i] != -9) {
        if (coords[i] == -1) {
          startDefined = false;
          i++;
          continue;
        }

        if (startDefined)
          workCmds[j++] = LINETO;
        else
          workCmds[j++] = MOVETO;

        workCmds[j++] = coords[i++];
        workCmds[j++] = coords[i++];

        startDefined = true;
      }
      i++;

//      workCmds[j++] = END;

      work.commands = new short[j];
      while (j > 0) {
        j--;
        work.commands[j] = workCmds[j];
      }

      set.meshes.addElement(work);
    }

    set.obj.vl = new VList(null);
    set.obj.vl.setColorOffset(0);
    set.obj.setScale(1);

    set.meshes.trimToSize();

    return set;
  }

  public void plotChar(Graphics g, char c, float sx, float sy) {
    FPt position = new FPt();
    //int objToWorldMatrix[] = new int[6];

    // Calculate the world coordinates.
    // Coordinates were given to us in view space.

    VEngine.ptScreenToWorld(sx, sy, position);
    obj.setPosition(position);
    obj.calcMatrix();

    short[] cmds = findCharScript(c);
    if (cmds == null)
      return;

    obj.vl.setCommands(cmds);
    obj.vl.plot(g,obj.objToWorld);

    if (boldStatus) {
      VEngine.ptScreenToView(DASH * 0x100, 0, position);
      Mat2 m = obj.objToWorld;
      m.set(2,m.get(2)+position.x);
      //obj.objToWorld.objToWorldMatrix[2] += position.x;
      obj.vl.plot(g,m);
     }
  }

  private static int cmdArgs[] = {
      2, // C_COLOR
      1, // C_BOLD
  };

  public int stringLength(String s) {
    int len = 0;
    int k = s.length();
    for (int i = 0; i < k; i++) {
      char c = s.charAt(i);
      if (c < C_TOTAL) {
        i += cmdArgs[c - C_COLOR];
      }
      else
        len++;
    }
    return len;
  }

  public void centerString(Graphics g, String s, FPt pt) {
    centerString(g, s, pt.x, pt.y);
  }

  public void centerString(Graphics g, String s) {
    centerString(g, s, VEngine.origin());
  }

  public void centerString(Graphics g, String s, float sx, float sy) {
    int strLen = stringLength(s);

    float pixelsWide = getAdvX() * strLen - getSpacingX();
    float pixelsTall = getSizeY();

    plotString(g, s, sx - (pixelsWide / 2), sy + (pixelsTall / 2));
  }

  public void plotString(Graphics g, String s, float sx, float sy) {

    int saveColorHue = colorHue;
    int saveColorBrightness = colorBrightness;
    boolean saveBold = boldStatus;

    float advX = getAdvX();

    int k = s.length();
    for (int i = 0; i < k; i++) {
      char c = s.charAt(i);

      // Is it a special control character (change color, etc)?

      if (c < C_TOTAL) {
        switch (c) {
          case C_COLOR: {
            int h = s.charAt(i + 1);
            int b = s.charAt(i + 2);
            setColor(h, b);
          }
          break;
          case C_BOLD: {
            int b = s.charAt(i + 1);
            setBold(b != 0);
          }
          break;
        }
        i += cmdArgs[c - 1];
        continue;
      }

      plotChar(g, c, sx, sy);
      sx += advX;
    }

    if (saveColorHue != colorHue || saveColorBrightness != colorBrightness)
      setColor(saveColorHue, saveColorBrightness);
    if (saveBold != boldStatus)
      setBold(saveBold);
  }

  public void setScale(float scale) {
    obj.setScale(scale);
  }

  public void setColor(int hue, int brightness) {
    colorHue = hue;
    colorBrightness = brightness;
    obj.vl.setColorTrans(0, hue, brightness - STD_BRIGHTNESS);
  }

  public void setSkew(float xFactor, float yFactor) {
    obj.setShear(xFactor, yFactor);
  }

  public void setBold(boolean active) {
    boldStatus = active;
  }

  public static String boldString(boolean active) {
    byte b[] = new byte[2];
    b[0] = (byte) C_BOLD;
    b[1] = (byte) (active ? 1 : 0);
    return new String(b);
  }

  public static String colorString(int hue, int bright) {
    byte b[] = new byte[3];
    b[0] = (byte) C_COLOR;
    b[1] = (byte) hue;
    b[2] = (byte) bright;
    return new String(b);
  }

  public float getSizeX() {
//      if (VEngine.SCALED) {
//         Pt pt = new Pt();
    VEngine.ptViewToScreen(sizeX * obj.getScale(), 0,
                           ptMisc);
    return ptMisc.x;
//   		return VEngine.transformViewToScreen((sizeX * obj.getScale()) >> VidGame.FRACBITS) >> VidGame.FRACBITS;
//      } else
//	   	return (sizeX * obj.getScale()) >> VidGame.FRACBITS;
  }

  public float getAdvX() {
//      if (VEngine.SCALED) {
    VEngine.ptViewToScreen( (sizeX + spacingX) * obj.getScale(), 0, ptMisc);
    return ptMisc.x;
//      }
//   		return VEngine.transformViewToScreen((sizeX + spacingX) * obj.getScale() >> VidGame.FRACBITS);
//      else
//   		return ((sizeX + spacingX) * obj.getScale()) >> VidGame.FRACBITS;
  }

  public float getSizeY() {
//      if (VEngine.SCALED) {
    VEngine.ptViewToScreen(0, sizeY * obj.getScale() ,
                           ptMisc);
    return ptMisc.y;
//      }
//      else
//   		return (sizeY * obj.getScale()) >> VidGame.FRACBITS;
  }

  public float getSpacingX() {
//      if (VEngine.SCALED) {
    VEngine.ptViewToScreen(spacingX * obj.getScale(), 0,
                           ptMisc);
    return ptMisc.x;
//      }
//      else
//   		return (spacingX * obj.getScale()) >> VidGame.FRACBITS;
  }

}