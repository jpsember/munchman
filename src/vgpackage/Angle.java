package vgpackage;

import mytools.*;

public final class Angle
    implements VidGameGlobals {
  private static float sinTable[];

  private int r;

  static {
    constructSinTable();
  }

  public String toString() {
    return "Angle:"+Utils.toHex(getInt(),2);
  }

  // Calculate the arcSin of a value.
  // Precondition:
  //	val = double, the value to test
  // Postcondition:
  //	returns false if value was out of range (|val| >= 1.0),
  //  else returns true and has set angle to the arcSine of the value.
  public boolean arcSine(double val) {
    if (val <= -1.0 || val >= 1.0)
      return false;
    set( (int) (Math.asin(val) * RADTODEG));
    return true;
  }

  public boolean equals(Angle compare) {
    return r == compare.r;
  }

  public Angle() {
    this(0);
  }

  public static float sin(Angle a) {
    return sinTable[a.getInt()];
  }

  public static float cos(Angle a) {
    int r = ( (a.getInt()) + 64) & 0xff;
    return sinTable[r];
  }

/*  public static int scaleUp(int val) {
    return val << TRIGBITS;
  }
*/
  public static void createRay(float magnitude, Angle direction, FPt p) {
    createRay(FPt.zero, magnitude, direction, p);
    //p.x = (cos(direction) * magnitude) >> Angle.TRIGBITS;
    //p.y = (sin(direction) * magnitude) >> Angle.TRIGBITS;
  }

  public static void createRay(int magnitude, Angle direction, Pt p) {
    p.x = (int)((cos(direction) * magnitude));
    p.y = (int)((sin(direction) * magnitude));
  }

  /**
     Calculate the location of a point a certain distance
     and angle from a starting point

     @param origin : starting point
     @param magnitude : distance from origin
     @param direction : rotation around point
     @param p : where to store calculated point
   */
  public static void createRay(FPt origin, float magnitude, Angle direction, FPt p) {
    p.x = origin.x + (cos(direction) * magnitude);
    p.y = origin.y + (sin(direction) * magnitude);
  }

/*  // Calculate a ray in a particular direction
  // Precondition:
  //	magnitude = magnitude of ray
  //	direction = direction of rotation from origin
  // Postcondition:
  //	ray endpoint returned
  public static Pt createRay(int magnitude, Angle direction) {
    Pt p = new Pt();
    createRay(magnitude, direction, p);
    return p;
  }
*/

  private static void constructSinTable() {
    sinTable = new float[256];
    for (int i = 0; i < 256; i++) {
      sinTable[i] =  (float)Math.sin( (i * 2 * Math.PI) / 256.0);
    }
  }

  public Angle(int n) {
    this(n, false);
  }

  public Angle(int n, boolean intFlag) {
    if (intFlag)
      setInt(n);
    else
      set(n);
  }

  public Angle(float x, float y) {
    calc(x, y);
  }

  public void calc(float x, float y) {
      double val = Math.atan2(y, x);
      set( (int) (Math.atan2(y, x) * RADTODEG));
  }

  public void set(int r) {
    this.r = (r & (MAX - 1));
  }

  public void random() {
    r = MyMath.rnd(MAX);
  }

  public int get() {
    return r;
  }

  public int getInt() {
    return (r >> TRIGBITS) & 0xff;
  }

  public void setInt(int r) {
    set(r << TRIGBITS);
  }

  public void copyTo(Angle dest) {
    dest.r = r;
  }

  public void adjust(int add) {
    r = (r + add) & (MAX - 1);
  }

  public void adjustInt(int add) {
    r = (r + (add << TRIGBITS)) & (MAX - 1);
  }

  // Calculate the distance of a point from a ray
  // Precondition:
  //	point's location, relative to origin of ray, in px,py
  //	ray endpoint in rx,ry
  // Postcondition:
  //	returns the distance between the point and the closest point on the ray
  public static float calcPtDistFromRay(float px, float py, float rx, float ry) {

    // Calculate the dot product
    float dotPS = px * rx + py * ry;

    if (dotPS <= 0) {
      return FPt.magnitude(px, py);
    }

    float sSquared = rx * rx + ry * ry;

    if (dotPS >= sSquared)
      return FPt.magnitude(px - rx, py - ry);

    float crossPS = Math.abs(px * ry - py * rx);

    float sRoot = (float) Math.sqrt(sSquared);

    return crossPS / sRoot;
  }

  // Make angle approach a desired value by a maximum velocity
  public void approach(Angle desired, int speed) {
    approach(desired.r, speed);
  }

  public void approach(int desired, int speed) {
    int diff = desired - r;
    if (diff <= - (MAX >> 1))
      diff += MAX;
    else if (diff >= (MAX >> 1))
      diff -= MAX;

    if (diff < 0) {
      speed = -speed;
      if (speed < diff)
        speed = diff;
    }
    else
    if (speed > diff)
      speed = diff;
    r += speed;
  }

}