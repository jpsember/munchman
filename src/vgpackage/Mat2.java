package vgpackage;
public final class Mat2 implements VidGameGlobals {
  private static final int ELEM = 3*2;
  public Mat2() {
    identity();
  }

  public void set(int index, float value) {
    elem[index] = value;
  }
  public float get(int index) {
    return elem[index];
  }

  private static void pr(String s) {System.out.println(s);}

  public void print(String title) {
    print(true,title);
  }

  public void print(boolean scaled) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < ELEM; i++) {
      if (i == 0 || i == 3 || i == 6)
        sb.append("[");
      double val = elem[i];
      sb.append(val+" ");
      sb.append(' ');

      if (i == 2 || i == 5 || i == 8)
        sb.append("]\n");
    }
    System.out.print(sb.toString());
  }

  public void print(boolean scaled, String title) {
    pr(title);
    print(scaled);
  }


  public void print() {
    print(true);
  }

  public void identity() {
    for (int i = 0; i < ELEM; i++)
      elem[i] = 0;
    elem[0] = elem[4] = 1f;
  }

  private static Mat2 w = new Mat2();
  private static Mat2 w2 = new Mat2();

  public void setRotate(Angle r) {
    float c = Angle.cos(r);
    float s = Angle.sin(r);
    float[] e = elem;

    e[0] = c;
    e[1] = -s;
    e[2] = 0;
    e[3] = s;
    e[4] = c;
    e[5] = 0;
  }
  public void translate(FPt p) {
    w.identity();
    w.elem[2] = p.x;
    w.elem[5] = p.y;
    multiply(w);
  }

  public void scale(float s) {
    elem[0] *= s;
    elem[1] *= s;
    elem[3] *= s;
    elem[4] *= s;
  }

  public void rotate(Angle r) {
    w.setRotate(r);
    multiply(w);
  }

  /** Apply matrix to a point
   */
  public void transform(float x, float y, FPt dest) {
    dest.x = (elem[0] * x + elem[1] * y + elem[2]);
    dest.y = (elem[3] * x + elem[4] * y + elem[5]);
  }

  public void shearX(float n) {
    w.identity();
    w.elem[1] = n;
    multiply(w);
  }
  public void shearY(float n) {
    w.identity();
    w.elem[3] = n;
    multiply(w);
  }

  public void multiply(Mat2 m) {
    float[] a = elem;
    float[] b = m.elem;

    float[] d = w2.elem;

    d[0] = (a[0] * b[0] + a[1] * b[3]);
    d[1] = (a[0] * b[1] + a[1] * b[4]);
    d[2] = (a[0] * b[2] + a[1] * b[5]) + a[2];
    d[3] = (a[3] * b[0] + a[4] * b[3]);
    d[4] = (a[3] * b[1] + a[4] * b[4]);
    d[5] = (a[3] * b[2] + a[4] * b[5]) + a[5];
    copy(w2);
  }

  public static void multiply(Mat2 m1, Mat2 m2, Mat2 dest) {
    float[] a = m1.elem;
    float[] b = m2.elem;

    float[] d = dest.elem;

    d[0] = a[0]*b[0] + a[1]*b[3];
    d[1] = a[0]*b[1] + a[1]*b[4];
    d[2] = a[0]*b[2]+a[1]*b[5] + a[2];
    d[3] = a[3]*b[0]+a[4]*b[3];
    d[4] = a[3]*b[1]+a[4]*b[4];
    d[5] = a[3]*b[2]+a[4]*b[5] + a[5];
  }

  public void copy(Mat2 src) {
    for (int i = 0; i < ELEM; i++)
      elem[i] = src.elem[i];
  }

  private float[] elem = new float[ELEM];
}