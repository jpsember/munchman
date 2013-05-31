package vgpackage;

public class Dot implements VidGameGlobals {
  public FPt position;
  public FPt velocity;
public static void pr(String s) {System.out.println(s);}


  public Dot() {
    position = new FPt();
    velocity = new FPt();
  }

  public void setPosition(FPt position) {
    position.copyTo(this.position);
  }

  public void move() {
    velocity.addTo(position);
    // Wrap at the ends of the world
    VEngine.wrapToWorld(position);
  }

  public FPt getPosition() {
    return position;
  }

  public FPt getVelocity() {
    return velocity;
  }

}