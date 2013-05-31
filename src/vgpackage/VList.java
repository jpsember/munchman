package vgpackage;

import java.awt.*;
import mytools.*;
/**
   VList class
   Defines the appearance and properties of a 2D vector mesh
*/
public class VList implements VidGameGlobals {

  private static void pr(String s) {System.out.println(s);}

  private static final int cmdLengths[] = {
      1, 3, 3, 3, 3, 1, 1
  };

  /**
     Determine the length of a command

     @param cmd : command
     @return the length of the command
   */
  public static int commandLength(short cmd) {
    return cmdLengths[cmd];
  }

  /**
     Determine the list of commands

     @return the list of commands
   */
  public short[] getCommands() {
    return commands;
  }

  /**
     Set the shimmer effect for the VList

     @param weight : weight of line; 0 = solid (no shimmer) to
                        4 (mostly gaps)
   */
  public void setShimmer(int weight) {
    shimmerEffect = weight;
  }

  // Color translation:
  private byte colorTransTbl[];

  /**
     Set the color offset to use for this VList

     @param startColor : color to use as color #0
   */
  public void setColorOffset(int startColor) {
    for (int i = 0; i < MyColors.maxColors(); i++) {
      colorTransTbl[i * 2] = (byte) (startColor + i);
      colorTransTbl[i * 2 + 1] = 0;
    }
  }

  /**
     Scale the brightness of all the colors

     @param brightAdjust : amount to scale colors by;
        MyColors.COLOR_LEVELS is the range of the colors, and
        the scaled colors are clamped to 0...n-1
   */
  public void scaleColors(int brightAdjust) {
    for (int i = 0; i < MyColors.maxColors(); i++)
      colorTransTbl[i * 2 + 1] = (byte) brightAdjust;
  }

  /**
     Set the color translation for a particular color index

     @param index : index to modify (0...n-1)
     @param hue : actual color to apply
     @param brightAdjust : amount to add to level
   */
  public void setColorTrans(int index, int hue, int brightAdjust) {
    colorTransTbl[index * 2] = (byte) hue;
    colorTransTbl[index * 2 + 1] = (byte) brightAdjust;
  }

  /**
     Constructor

     @param cmds : array of commands, or null if mesh is not specified
        yet
  */
  public VList(short cmds[]) {


    origin = new FPt();
    FPt[] polyPts = null;

    for (int pass = 0; pass < 2; pass++) {
boolean f = displayFlag && pass == 1;
      commands = cmds;
      int polyPtTotal = 0;
      int colPtTotal = 0;

      float x = 0;
      float y = 0;

      if (commands != null)
        for (int i = 0; i < commands.length; i += cmdLengths[commands[i]]) {
          switch (commands[i]) {
            case CENTER:
            case MOVETO:
            case LINETO:
              x = commands[i + 1];
              y = commands[i + 2];
              if (commands[i] == CENTER && pass == 0)
                origin = new FPt(x, y);
  //            if (f) pr("cmd "+commands[i]+", x="+x+" y="+y);
              x = (x - origin.x);
              y = (y - origin.y);
//              if (f) pr(" scaled, trans="+x+" "+y);
              break;

            case COLPT:
              if (pass == 1) {
                colPts[colPtTotal] = new FPt(x,y);
              }
              colPtTotal++;
              break;
            case POLYPT:
              if (pass == 1) {
                polyPts[polyPtTotal] = new FPt(x,y);
              }
              polyPtTotal++;
              break;
          }
        }
        if (pass == 0) {
          polyPts = new FPt[polyPtTotal];
          colPts = new FPt[colPtTotal];
         // if (displayFlag) pr("colPts array is "+colPts);
        }
    }

    constructPoly(polyPts);

    // Construct a color translation table.
    colorTransTbl = new byte[MyColors.maxColors() * 2];
    setColorOffset(0);
  }

  /**
     Copy the color translation table from this VList to
     another.

     @param dest : where to copy translation table to
   */
  public void copyColorTransTblTo(VList dest) {
    for (int i = 0; i < MyColors.maxColors() * 2; i++)
      dest.colorTransTbl[i] = colorTransTbl[i];
  }

  /**
     Plot a script using the object -> screen matrix last used

     @param g : Graphics object
     @param script : commands comprising script
   */
  public void plotOverlay(Graphics g, short script[]) {

    short cmd = script[0];
    for (int i = 0; i < script.length; i += cmdLengths[cmd]) {
      cmd = script[i];
      switch (cmd) {
        case COLOR:
          int colorIndex = script[i + 1] * 2;
          MyColors.set(g, colorTransTbl[colorIndex],
                       script[i + 2] + colorTransTbl[colorIndex + 1]);
          break;
        case MOVETO:
          VEngine.ptObjToScreen((script[i + 1] - origin.x),
              (script[i + 2] - origin.y),
              workPt0);
          break;
        case LINETO:
          VEngine.ptObjToScreen((script[i + 1] - origin.x),
                                (script[i + 2] - origin.y), workPt1);
          if (shimmerEffect != 0)
            ShimmerLine.plot(shimmerEffect - 1, g,
             (int)workPt0.x, (int)workPt0.y,
                             (int)workPt1.x, (int)workPt1.y);
          else
            g.drawLine((int)workPt0.x, (int)workPt0.y, (int)workPt1.x, (int)workPt1.y);
          workPt1.copyTo(workPt0);
          break;
      }
    }
  }

  /**
     Plot VList

     @param g : Graphics object
     @param objToWorld : transformation matrix for object to world
       space
   */
  public void plot(Graphics g, Mat2 objToWorld) {
    VEngine.calcObjToScreen(objToWorld);
    plotOverlay(g, commands);
  }

  /**
     Calculate the minimum enclosing radius for the VList.

     @return radius guaranteed to enclose the current VList
   */
  public float calcRadius() {
    float longestDist = 0;
    short cmd = commands[0];
    for (int i = 0; i < commands.length; i += cmdLengths[cmd]) {
      cmd = commands[i];
      if (cmd > LINETO)
        continue;
      float dx = commands[i + 1] - origin.x;
      float dy = commands[i + 2] - origin.y;

      float dist = (dx * dx) + (dy * dy);
      if (longestDist < dist)
        longestDist = dist;
    }

    return 1 + (float)Math.sqrt(longestDist);
  }

  /**
     Test if a point is within the perimeter of the VList

     @param p : point to test (world coordinates)
     @param radius : radius of minimum enclosing circle of mesh,
        centered at origin of mesh
   */
  public boolean testCollision(FPt p, float radius) {

    // The POLYPT points define an open-ended polygon that we
    // will test to see if p is within.

    // Take the line segment that starts at (px-radius, py) and ends at (px,py)
    // and compare to see how many polygon segments it intersects.  If it intersects
    // an odd number, the point is within the mesh.

    float p0x = ( -radius);
 //   int testX = p.x;
 //   int testY = p.y;

    int crossCount = 0;

    for (int i = 0; i < polyPts.length; i++) {//, last = next) {
        PolyPt edge = polyPts[i];

        // Test if the line segment bounding boxes overlap at all.
        // If the y-coordinates match, no intersection possible.
        if (  (p.x < edge.xMin
               || p0x >= edge.xMax
               || p.y < edge.yMin
               || p.y >= edge.yMax
               || edge.yMax < edge.yMin
               )) continue;

        float iX;
        iX = (edge.k * (p.y - edge.y))
         + edge.x;

        if (iX > p0x && iX <= p.x) {
          crossCount++;
        }
        }
    return ( (crossCount & 1) != 0);
  }

  private static String s(int x, int y) {
    return s(x,y);
  }
  private static String s(float x, float y) {
    return Float.toString(x)+","+Float.toString(y);
  }

  public static boolean testCollision(
      Mat2 aWorldToObj,
      VList aList,
      Mat2 bObjToWorld,
      VList bList) {

    boolean collide = false;

iloop:
    for (int i = 0; i < bList.polyPts.length; i++) {

      PolyPt po = bList.polyPts[i];

      boolean f = (displayFlag && DEBUG);

      if (f) pr("b edge = "+s(po.x,po.y)+"  length  "+s(po.dx,po.dy));

      // Send the edge through b's obj->world matrix.
      bObjToWorld.transform(po.x,po.y,workPt0);
      // Send this point through a's world->obj matrix.
      aWorldToObj.transform(workPt0.x,workPt0.y,workPt0);

      // Send other endpoint.
      bObjToWorld.transform(po.x+po.dx,po.y+po.dy,workPt1);
      // Send this point through a's world->obj matrix.
      aWorldToObj.transform(workPt1.x,workPt1.y,workPt1);

      if (f) pr(" in a obj space = "+workPt0.toStringScale()+" to "+workPt1.toStringScale());

      float px = (workPt0.x);
      float py = (workPt0.y);
      float vx = (workPt1.x - workPt0.x);
      float vy = (workPt1.y - workPt0.y);

      // Test this edge against every edge in a.
      for (int j = 0; j < aList.polyPts.length; j++) {

        PolyPt pj = aList.polyPts[j];


        float qx = pj.x; float qy = pj.y;
        float rx = pj.dx; float ry = pj.dy;

        if (f)
          pr("p=" + s(px, py));
        if (f)
          pr("v=" + s(vx, vy));
        if (f)
          pr("q=" + s(qx, qy));
        if (f)
          pr("r=" + s(rx, ry));

        float denom = (rx * vy - ry * vx);
        if (f)
          pr("denom=" + denom);
        if (denom == 0)
          continue;

        float numer = qy * vx - py * vx - qx * vy + px * vy;
        float t = numer / denom;
        if (f)
          pr("numer=" + numer);
        if (f)
          pr("t = " + t);

        if (t < 0f || t > 1.0f)
          continue;

        float s = 0;
        if (Math.abs(vx) > Math.abs(vy)) {
          s = ((qx - px) + t * rx) / vx;

        }
        else {
          s = ((qy - py) + t * ry) / vy;
        }
        if (f)
          pr("s = " + s);
        if (s < 0f || s > 1.0f)
          continue;
        collide = true;
        break iloop;
      }
    }
    return collide;

  }

  public static boolean testCollision(
      float objRadius,
      Mat2 aWorldToObj,
      VList aList,
      Mat2 bObjToWorld,
      VList bList) {

    boolean collide = false;

    for (int i = 0; i < bList.colPts.length; i++) {

      FPt po = bList.colPts[i];

      boolean f = (displayFlag && DEBUG);
//if (f && i == 0)
  //      pr("colPts array is "+bList.colPts);

      // Send the point through ob's obj->world matrix.
      if (f)
        pr(" next ob point is " + po.toStringScale());

      bObjToWorld.transform(po.x, po.y, workPt0);

      if (f)
        pr(" in world coords =" + workPt0.toStringScale());

        // Send this point through this object's world->obj matrix.

      if (f && false)
        aWorldToObj.print(false, "transforming through world->obj: ");

      aWorldToObj.transform(workPt0.x,workPt0.y,workPt0);

      if (f)
        pr(" to test obj crds=" + workPt0.toStringScale());

      boolean save = VList.displayFlag;
      VList.displayFlag = false;

      if (f) pr(" objRadius="+objRadius);

      boolean fl = aList.testCollision(workPt0, objRadius);
      VList.displayFlag = save;

      if (fl) {
        collide = true;
        break;
      }
    }
    return collide;
  }

  /**
     Determine the coordinates of a particular POLYPT

     @param vertIndex : index of POLYPT (0:first ... n-1)
     @param w : where to store the (object) coordinates
   */
  public void getPolyPt(int vertIndex, FPt w) {
    int ox = 0;
    int oy = 0;

    short cmd = commands[0];
    for (int i = 0; ; cmd = commands[i += cmdLengths[cmd]]) {
      if (cmd == POLYPT)
        if (vertIndex-- == 0)
          break;
      if (cmd > LINETO)
        continue;
      w.x = (commands[i + 1] - origin.x);
      w.y = (commands[i + 2] - origin.y);
    }
  }

  /**
     Determine the origin of the mesh

     @return a Pt (read only) representing the origin
   */
  public FPt getOrigin() {
    return origin;
  }

  /**
     Set the commands for the VList

     @param cmds : new commands for VList
   */
  public void setCommands(short cmds[]) {
    commands = cmds;
  }

  /** Debug flag:  displays information during collision testing if true. */
  public static boolean displayFlag;

  private static FPt
   workPt0 = new FPt(),
   workPt1 = new FPt();

  private short commands[];
  private FPt origin;
  private int shimmerEffect;
  private PolyPt[] polyPts;
  private FPt[] colPts;

  static class PolyPt {
    float x;
    float y;
//    int x2,y2;
    float k;
    float yMin;
    float yMax;
    float xMin,xMax;
    float dx,dy;
  }

  static class Pt2 extends FPt {
    public Pt2(FPt src) {
      super(src);
    }
    boolean down;
  }

  private void constructPoly(FPt[] list0) {
    Pt2[] list = new Pt2[list0.length];
    for (int i = 0; i < list0.length; i++)
      list[i] = new Pt2(list0[i]);

      // Fill in the 'down' flags.
    {
      int proc = 0;
      boolean down = false;
      for (int i = 0; proc < list.length; i++) {
        Pt2 p1 = list[MyMath.mod(i, list.length)];
        Pt2 p2 = list[MyMath.mod(i + 1, list.length)];
        if (p1.y != p2.y) {
          down = (p1.y > p2.y);
          proc++;
        }
        p1.down = (p1.y > p2.y);
      }
    }

    for (int pass = 0; pass < 2; pass++) {
      int count = 0;
      boolean down = false;

      for (int i = 0; i < list.length; i++) {

        int curr = MyMath.mod(i,list.length);
        Pt2 p0 = list[MyMath.mod(i-1,list.length)];
        Pt2 p1 = list[curr];
        Pt2 p2 = list[MyMath.mod(i+1,list.length)];
        Pt2 p3 = list[MyMath.mod(i+2,list.length)];
 //       if (p2.y == p1.y) continue;

        float ymin = 0;
        float ymax = 0;
        if (!p1.down) {
          ymin = (p0.down ? p1.y : p1.y + 1);
          ymax = p2.y - 1;
        } else {
          ymin = p2.y + 1;
          ymax = !p0.down ? p1.y : p1.y - 1;
        }
   //     if (ymin > ymax) continue;
        if (pass == 0) {
        } else {
          PolyPt p = new PolyPt();
          p.x = p1.x;
          p.y = p1.y;
          //int x2 = p2.x;
        //  int y2 = p2.y;

          p.dx = p2.x - p1.x;
          p.dy = p2.y - p1.y;

          //p.y2 = p2.y;
          p.yMin = ymin;
          p.yMax = ymax;
          p.xMin = Math.min(p1.x,p2.x);
          p.xMax = Math.max(p1.x,p2.x);
          if (p2.y != p1.y)
            p.k = ((p2.x - p1.x)) / (p2.y - p1.y);
          polyPts[count] = p;
        }
        count++;
      }
      if (pass == 0) {
        polyPts = new PolyPt[count];
      }
    }
  }
}