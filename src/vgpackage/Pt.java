package vgpackage;

public class Pt implements VidGameGlobals {
  public int x, y;

  public Pt() {}

  public Pt(int x, int y) {
    set(x, y);
  }

  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void scale(int s) {
    x = (x * s) >> FRACBITS;
    y = (y * s) >> FRACBITS;
  }

  public Pt(Pt source) {
    source.copyTo(this);
  }

  public void swap() {
    int temp = x;
    x = y;
    y = temp;
  }

  public void swapWith(Pt dest) {
    int temp = dest.x;
    dest.x = x;
    x = temp;
    temp = dest.y;
    dest.y = y;
    y = temp;
  }

  public void copyTo(Pt dest) {
    dest.set(x, y);
  }

  public void addTo(Pt dest) {
    dest.x += x;
    dest.y += y;
  }

  public void clear() {
    set(0, 0);
  }

  // Calculate the magnitude of a vector
  public int magnitude() {
    return magnitude(x, y);
  }

  public String toStringScale() {
    return "scl[X:" + Utils.formatDbl(x / (double)ONE, 8, 4)
     +", Y:" + Utils.formatDbl(y / (double)ONE,8,4) + "]";
  }

  public String toString() {
    return "[X:" + Utils.formatInt(x,5)
     +", Y:" + Utils.formatInt(y,5) + "]";
  }

  public static int magnitude(int x, int y) {
    double dx = x;
    double dy = y;
    return (int) Math.sqrt(dx * dx + dy * dy);
  }

  // Reduce vector to maximum magnitude if it currently exceeds it
  public void setMax(int max) {
    int mag = magnitude();
    if (mag > max) {
      // this value may overflow if the magnitude is quite large!
      int scaleDown = (max << VidGame.FRACBITS) / mag;
      set( (x * scaleDown) >> VidGame.FRACBITS,
          (y * scaleDown) >> VidGame.FRACBITS);
    }
  }
}