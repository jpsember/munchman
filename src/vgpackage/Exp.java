package vgpackage;

import mytools.*;
import java.awt.*;
import java.util.*;

public class Exp extends Obj {
  // Class variables:
  private static final int MAX_OBJ = 100;
  private static final int EXP_SPEED = VidGame.TICK * 600;
  private static final int MAX_SPEED = VidGame.TICK * 4500;

  private static Exp list[];

  // Object members:
  protected Angle rotVel; // rotational velocity
  protected int lifeLeft; // # cycles of life left

  // Initialize the class
  public static void init() {
    //ve = veParm;
    list = new Exp[MAX_OBJ];
  }

  public Exp() {
    super();
    rotVel = new Angle();
  }

  public boolean alive() {
    return lifeLeft != 0;
  }

  public static void moveAll() {
    for (int i = 0; i < MAX_OBJ; i++) {
      if (VidGame.initFlag())
        list[i] = null;
      Exp obj = list[i];
      if (obj == null)
        continue;

      if (obj.lifeLeft-- == 0) {
        list[i] = null;
        continue;
      }

      obj.rotation.adjust(obj.rotVel.get());
      obj.move();
    }
  }

  // Plot all objects
  public static void plot(Graphics g) {
    g.setColor(Color.white);
    for (int i = 0; i < MAX_OBJ; i++) {
      Exp obj = list[i];
      if (obj == null)
        continue;
      obj.vl.plot(g, obj.objToWorld);
    }
  }

  protected static Exp findFree() {
    for (int i = 0; i < MAX_OBJ; i++) {
      if (list[i] == null) {
        list[i] = new Exp();
        return list[i];
      }
    }
    return null;
  }

  // Create an explosion from another object.
  public static void create(Obj obj, int time, int rotVel) {

    // Construct a new explosion object for each segment in the obj's VList.

    short commands[] = obj.vl.getCommands();
    short cmd = commands[0];
    int i = 0;
    int colorInd = -1;
    FPt origin = obj.vl.getOrigin();

    FPt p0 = new FPt();
    FPt p1 = new FPt();

    for (; i < commands.length; i += VList.commandLength(cmd)) {
      cmd = commands[i];

      if (cmd == COLOR)
        colorInd = i;

      if (cmd == MOVETO) {
        p0.x = (commands[i + 1] - origin.x);
        p0.y = (commands[i + 2] - origin.y);
      }

      if (cmd == LINETO) {
        p1.x = (commands[i + 1] - origin.x);
        p1.y = (commands[i + 2] - origin.y);

        // Create a new VList from this segment.

        Exp exp = findFree();
        if (exp == null)
          break;

        //.db.a(colorInd >= 0, "Exp.create : color undefined");

        exp.createSegment(obj, p0, p1, (float)commands[colorInd + 1],
                          (float)commands[colorInd + 2], time, rotVel);

        // Update start position for next segment.
        p1.copyTo(p0);
      }
    }
  }

  // Construct a single segment in this object
  protected void createSegment(Obj orig, FPt p0, FPt p1, float c0, float c1,
                               int time, int rv) {

    // Determine midpoint of segments, make it the new origin.

    FPt origin = new FPt( (p0.x + p1.x) / 2, (p0.y + p1.y) / 2);

    short list[] = new short[9];

    list[0] = COLOR;
    list[1] = (short)c0;
    list[2] = (short)c1;

    list[3] = MOVETO;
    list[4] = (short) (p0.x - origin.x);
    list[5] = (short) (p0.y - origin.y);

    list[6] = LINETO;
    list[7] = (short) (p1.x - origin.x);
    list[8] = (short) (p1.y - origin.y);

    vl = new VList(list);
    orig.vl.copyColorTransTblTo(vl);

    scale = orig.scale;
    inverseNeeded = false;
    orig.rotation.copyTo(rotation);

    // Determine where the new segment's midpoint is in world space coords, and
    // make this the segment's starting position.
    orig.objToWorld.transform(origin.x,origin.y,position);
// VEngine.ptTransformFwd(origin.x, origin.y, orig.objToWorldMatrix, position);

    // Set up the velocity to be outward from the original object's center.

//      Random r = VidGame.getRandom();
    Angle a = new Angle(position.x - orig.position.x,
                        position.y - orig.position.y);
    Angle.createRay(MyMath.rnd(EXP_SPEED), a, velocity);

    velocity.x += orig.velocity.x / 2;
    velocity.y += orig.velocity.y / 2;

    // Give each segment a random rotation speed.

    rotVel.setInt((int)MyMath.rndCtr((int)rv)); //VidGame.rnd(32) - 16);

    //velocity.clear();
    //rotVel.set(0);

    // Give each segment a random lifespan.

    lifeLeft = MyMath.rnd((int)time) + time / 4;

  }
}