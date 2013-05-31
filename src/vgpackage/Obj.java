package vgpackage;

import java.awt.*;

public class Obj extends Dot {
  protected Mat2 objToWorld;
  protected boolean delayWrap;

  public Obj() {
    this(true);
  }

  public boolean alive() {
    return false;
  }
  public void move() {
    velocity.addTo(position);
    testWrap();
    calcMatrix();
  }

  protected void testWrap() {
    if (!delayWrap) {
      // Wrap at the ends of the world
      VEngine.wrapToWorld(position);
    } else {
      if (withinWorld(0)) {
        delayWrap = false;
      }
    }
  }

  public Obj(boolean inverseNeeded) {
    super();
    objToWorld = new Mat2();
    rotation = new Angle();
    this.inverseNeeded = inverseNeeded;
    scale = 1.0f; //ONE;
    calcMatrix();
  }

  public float getWorldRadius() {
    return worldRadius;
  }

  protected boolean withinWorld(float overlap) {
    float sx = VEngine.getWorldSize().x / 2 + overlap;
    float sy = VEngine.getWorldSize().y / 2 + overlap;
    return (position.x > -sx && position.x < sx &&
     position.y > -sy && position.y < sy);
  }

  public void setPosition(FPt position, Angle rotation) {
    setPosition(position);
    rotation.copyTo(this.rotation);
  }

  // Calculate new object space -> world space transformation matrix
  // This is required whenever we change the position or rotation of the object.
  public void calcMatrix() {
    VEngine.calcObjToWorld(position,rotation,scale,shearX,shearY, objToWorld);
    if (inverseNeeded)
    {
      if (worldToObj == null)
        worldToObj = new Mat2();
      VEngine.calcWorldToObj(position,rotation,scale,shearX,shearY,worldToObj);
    }
  }

//  public void move() {
//    super.move();
//    calcMatrix();
//  }

  public void setRadius(float objRadius) {
    this.objRadius = objRadius;
    scale = 0;
  }

  public void setScale(float scale) {
    this.scale = scale;
    if (objRadius != 0) {
      worldRadius = objRadius * scale;
      if (Utils.DEBUG) Utils.ASSERT(worldRadius != 0);
    }
  }

  public void setShear(float x, float y) {
    shearX = x;
    shearY = y;
  }

  public float getScale() {
    return scale;
  }

  // Plot a green box at object's location to indicate its minimum enclosing radius.

  public void displayBoundingBox(Graphics g, Color value) {
    if (DEBUG) Utils.ASSERT(worldRadius != 0, "Obj.displayBoundingBox radius undefined");
    g.setColor(value);

    // Calculate center of object, in screen space.
    FPt screenPt = new FPt();
    VEngine.ptObjToScreen(0,0,screenPt);
    // Determine extent of bounding radius in screen space.
    float rx,ry,rw,rh;
    {
      final FPt ps0 = new FPt();
      final FPt ps1 = new FPt();
      VEngine.ptWorldToScreen(position.x - worldRadius, position.y - worldRadius, ps0);
      VEngine.ptWorldToScreen(position.x + worldRadius, position.y + worldRadius, ps1);
      rx = Math.min(ps0.x,ps1.x);
      ry = Math.min(ps0.y,ps1.y);
      rw = Math.abs(ps1.x-ps0.x);
      rh = Math.abs(ps1.y-ps0.y);
    }
    g.drawRect((int)rx,(int)ry,(int)rw,(int)rh);
  }

  /**
     Test if object has collided with another

     @param ob : object to test collision with
     @param polyWithPoly : true to perform polygon vs. polygon test

     @return true if any of the COLPT's of ob were within this object's
       mesh
   */
  public boolean testCollision(Obj ob, boolean polyWithPoly) {
    if (!polyWithPoly) return testCollision(ob);

      boolean f = DEBUG && VList.displayFlag;
if (f) pr("---- testCollision ------------------------------");
if (f) pr(" this pos="+position.toStringScale()+" ob="+ob.position.toStringScale());
    // Do the bounding squares not overlap at all?

    if (
           Math.abs(ob.position.x - position.x) >= worldRadius + ob.worldRadius
        || Math.abs(ob.position.y - position.y) >= worldRadius + ob.worldRadius
        )
      return false;

    return VList.testCollision(worldToObj,vl,ob.objToWorld,ob.vl);
  }

  /**
     Test if object has collided with another

     @param ob object to test collision with
     @return true if any of the COLPT's of ob were within this object's
       mesh
   */
  public boolean testCollision(Obj ob) {
      boolean f = DEBUG && VList.displayFlag;
if (f) pr("---- testCollision ------------------------------");
if (f) pr(" this pos="+position.toStringScale()+" ob="+ob.position.toStringScale());
if (f) pr(" dir = "+this.rotation +", ob="+ob.rotation);
    // Do the bounding squares not overlap at all?

    if (
           Math.abs(ob.position.x - position.x) >= worldRadius + ob.worldRadius
        || Math.abs(ob.position.y - position.y) >= worldRadius + ob.worldRadius
        )
      return false;

    // For each COLPT in ob, convert to world coordinates and test that point.

    int meshIndex = 0;
    Pt po = new Pt();

    boolean collide = VList.testCollision(
     objRadius,
     worldToObj,
     vl,
     ob.objToWorld,
     ob.vl);
    /*
    boolean collide = true;
    while (true) {
      meshIndex = ob.vl.getNextCOLPT(meshIndex, po);
      po.x = VEngine.scaleUp(po.x);
      po.y = VEngine.scaleUp(po.y);
      if (meshIndex < 0) {
        collide = false;
        break;
      }
      // Send the point through ob's obj->world matrix.
if (f) pr(" next ob point is "+po.toStringScale());
      ob.objToWorld.transform(po.x,po.y,po);
if (f) pr(" in world coords ="+po.toStringScale());

      // Send this point through this object's world->obj matrix.

if (f) worldToObj.print(false,"transforming through world->obj: ");
      worldToObj.transform(po.x,po.y,po);
if (f) pr(" to test obj crds="+po.toStringScale());
boolean save =VList.displayFlag;
VList.displayFlag = false;
      boolean fl = vl.testCollision(po, objRadius);
VList.displayFlag = save;
if (fl) break;
    }
    */
    return collide;
  }

  // Test if an object has collided with a point
  // Precondition:
  //	p = point to test, in world space
  //	object's radius is defined
  //	object's transformation matrix is valid
  // Postcondition:
  //	returns true if point p was within the POLYPT polygon of this object
  public boolean testCollision(FPt p) {
    if (DEBUG)
      Utils.ASSERT(worldRadius != 0, "Obj.testCollision radius undefined");

    boolean flag = false;
    do {
      // Is point outside of the world space bounding box?  Use the
      // worldRadius value to determine this.

      if (
          p.x <= position.x - worldRadius
          || p.x >= position.x + worldRadius
          || p.y <= position.y - worldRadius
          || p.y >= position.y + worldRadius
          )
        break;

      // Send the point through the object's world->obj matrix.
      boolean f = DEBUG && VList.displayFlag;
if (f) pr("-------------------------------------------------");
if (f) pr("TestCollision, pt = "+p.toStringScale());
if (f) worldToObj.print(false,"World to object matrix:");
if (f) objToWorld.print(false,"Obj to World matrix:");
      FPt po = new FPt();
      worldToObj.transform(p.x, p.y, po);
if (f) pr("transformed to object = "+po.toString());

      // We have the point po in the obj's space.
      if (vl.testCollision(po, objRadius))
        flag = true;
    }
    while (false);
    return flag;
  }
  public String toString() {
    String s = null;
    if (DEBUG) {
      s = "pos="+this.position.toStringScale()+", rot="+rotation;
    }
    return s;
  }

  protected Angle rotation;
  protected VList vl;
  // scale factor to convert from mesh to world; 0 if not defined
  protected float scale;
  // shear factors (0 if no shearing)
  protected float shearX, shearY;
  protected float objRadius; // minimum enclosing radius for collision
  //  detection, object space; 0 if not defined
  protected float worldRadius; // enclosing radius, world space

  protected boolean inverseNeeded; // true if we need to maintain a world->object matrix
  protected Mat2 worldToObj;

}