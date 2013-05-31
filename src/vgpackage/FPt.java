package vgpackage;

public class FPt implements VidGameGlobals {
  public float x, y;

  public FPt() {}

  public FPt(float x, float y) {
    set(x, y);
  }

  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void scale(float s) {
    x *= s;
    y *= s;
  }

  public FPt(FPt source) {
    source.copyTo(this);
  }

  public void swap() {
    float temp = x;
    x = y;
    y = temp;
  }

  public void swapWith(FPt dest) {
    float temp = dest.x;
    dest.x = x;
    x = temp;
    temp = dest.y;
    dest.y = y;
    y = temp;
  }

  public void copyTo(FPt dest) {
    dest.set(x, y);
  }

  public void addTo(FPt dest) {
    dest.x += x;
    dest.y += y;
  }

  public static final FPt zero = new FPt();

  public void clear() {
    set(0, 0);
  }

  // Calculate the magnitude of a vector
  public float magnitude() {
    return magnitude(x, y);
  }

  public String toStringScale() {
    return "scl[X:" + Utils.formatDbl(x / (double)ONE, 8, 4)
     +", Y:" + Utils.formatDbl(y / (double)ONE,8,4) + "]";
  }

  public String toString() {
    return "[X:" + Utils.formatDbl(x,6,3)
     +", Y:" + Utils.formatDbl(y,6,3) + "]";
  }

  public static float magnitude(float x, float y) {
    return (float)Math.sqrt(x * x + y * y);
  }

  // Reduce vector to maximum magnitude if it currently exceeds it
  public void setMax(float max) {
    float mag = magnitude();
    if (mag > max) {
      float scaleDown = max / mag;
      set( x * scaleDown, y * scaleDown);
    }
  }
}