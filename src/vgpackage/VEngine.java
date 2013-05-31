package vgpackage;

// Is layer useful in this library?

import java.awt.*;
import mytools.*;

public final class VEngine implements VidGameGlobals {

public static void pr(String s) {System.out.println(s);}

  /**
     Clear the current window to black
     @param g Graphics object
   */
  public static void clearWindow(Graphics g) {
    clearWindow(g, Color.black);
  }

  /**
     Clear the current window to the specified color
     @param g Graphics object
     @param c Color to clear to
   */
  public static void clearWindow(Graphics g, Color c) {
    g.setColor(c);
    MyGraphics.fillRect(g, windowR);
  }

  /**
     Select the window to draw into
     @param g Graphics object
     @param index the index of the window
   */
  public static void selectWindow(Graphics g, int index) {
    selectWindow(index);
    g.setClip(windowR.x, windowR.y, windowR.width, windowR.height);
  }

  /**
     Class for storing window structure
   */
  static class Window {
    public float x,y,width,height,scale;
    public Window(float x, float y, float width, float height, float scale) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.scale = scale;
    }
  }

  /**
     Define a window.
     @param index index of window (0...n)
     @param x the left edge of the window
     @param y the top edge of the window
     @param width the width of the window
     @param height the height of the window
     @param scale the scale factor; ONE is unscaled
   */
  public static void defineWindow(int index, float x, float y,
                                  float width, float height, float scale) {

    windows[index] = new Window(x,y,width,height,scale);
  }

  /**
     Open the VEngine class.  Define the base window (WINDOW_BASE)
     to encompass the component.

     @param maxWindows the maximum number of windows to be defined
   */
  public static void open(int maxWindows) {
    windows = new Window[maxWindows];
    defineWindow(WINDOW_BASE,
                 0, 0,
                 component.getSize().width, component.getSize().height,
                 VidGame.ONE);
  }

  /**
     Open the VEngine class.  Define the base window (WINDOW_BASE)
     to encompass the component.  Sets up a maximum of 8 user windows.
   */
  public static void open() {
    open(8 + WINDOW_USER);
  }

  /**
     Close the VEngine class
   */
  public static void close() {
    osImage = null;
    workLayer = 0;
    layerValidFlags = 0;
    layersInitFlags = 0;
  }

  /**
     Initialize the VEngine class

     @param c component containing the windows
   */
  public static void init(Component c) {
    component = c;
  }

  /**
     Specify the window that contains the world

     @param window index of window
   */
  public static void setWorldWindow(int window) {
    selectWindow(window);
    // Make the world a little bigger than the display area so wraparound objects
    // don't tend to appear instantly.
    // We have to be careful though that things don't get stuck in the buffer zone
    // offscreen where the player can't see them.

    FPt ptMisc = new FPt();
    ptViewToScreen(BUFFER_PIXELS, BUFFER_PIXELS, ptMisc);
    setWorldSize(new FPt(
        (windowR.width + ptMisc.x) * windowScaleInv,
        (windowR.height + ptMisc.y) * windowScaleInv
        ));
  }

  /**
     Adjust a position so it's within the world; wrap at edges
     if necessary

     @param position position to wrap
   */
  public static void wrapToWorld(FPt position) {
    if (position.x < worldClipMin.x || position.x > worldClipMax.x)
      position.x = MyMath.mod((int)(position.x - worldClipMin.x), (int)worldSize.x) +
          worldClipMin.x;
    if (position.y < worldClipMin.y || position.y > worldClipMax.y)
      position.y = MyMath.mod((int)(position.y - worldClipMin.y), (int)worldSize.y) +
          worldClipMin.y;
  }

  /**
     Adjust a position so it's within the world; clamp to edges
     if necessary

     @param position position to clamp
   */
  public static void clampToWorld(FPt position) {
    if (position.x < worldClipMin.x)
      position.x = worldClipMin.x;
    if (position.x > worldClipMax.x)
      position.x = worldClipMax.x;

    if (position.y < worldClipMin.y)
      position.y = worldClipMin.y;
    if (position.y > worldClipMax.y)
      position.y = worldClipMax.y;
  }

  /**
     Set the size of the world

     @param size size of world
   */
  public static void setWorldSize(FPt size) {
    worldSize = new FPt(size);
    worldClipMax = new FPt(worldSize.x / 2, worldSize.y / 2);
    worldClipMin = new FPt( -worldClipMax.x, -worldClipMax.y);
  }

  /**
     Determine the size of the world

     @return world size
   */
  public static FPt getWorldSize() {
    return worldSize;
  }

  /** Determine if a new off-screen buffer is required by comparing
      the size of the applet with the size of the last prepared
      off-screen buffer.

      @return true if a new buffer is needed
    */
  private static boolean needNewOffscreenBuffer() {
    return (osImage == null);
  }

  /**
     Construct a new offscreen buffer sufficient to hold the
     current window size
   */
  private static void constructOffscreenBuffer() {
    Window w = windows[WINDOW_BASE];
    osSize = new Dimension((int)w.width,(int)w.height);
    osImage = component.createImage(osSize.width, osSize.height);
    layersInitFlags = 0;
    layerValidFlags = 0;
  }

  /**
    Determine if current layer is valid

    @return true if current layer is valid
  */
  public static boolean layerValid() {
    return (layerValidFlags & (1 << (workLayer - 1))) != 0;
  }

  /**
     Prepare to update the graphics; constructs buffers if necessary
   */
  public static void prepareUpdate() {
    if (needNewOffscreenBuffer())
      constructOffscreenBuffer();
  }

  /**
     Prepare for modifying a layer.  Must be balanced by a call to
     closeLayer().

     @param layer layer to modify (L_xxx)
     @return the Graphics object to be manipulated
  */
  public static Graphics openLayer(int layer) {
    Debug.ASSERT (gLayer == null, "Unbalanced openLayer()...closeLayer() calls");

    Graphics g = osImage.getGraphics();
    gLayer = g;

    workLayer = layer + 1;

    return g;
  }

  /**
     Close layer that was opened by openLayer(...)
   */
  public static void closeLayer() {
    gLayer.dispose();
    gLayer = null;
  }

  /**
     Update the screen

     @param g Graphics object to be updated
   */
  public static void updateScreen(Graphics g) {
    // Copy the entire os image every time.
    g.drawImage(osImage, 0, 0, component);
    layerValidFlags |= (1 << L_BGND);
    workLayer = 0;
  }


  /**
     Calculate a matrix for scaling, shearing, rotating, and translating
     a series of points from object space to world space.

     @param position position of object in world space
     @param rotation angle of rotation of object
     @param scale scale factor to apply (ONE = unscaled)
     @param shearX x shear factor; if 0, no shearing performed;
        otherwise, ONE = shear by 1.0
     @param shearY y shear factor; if 0, no shearing performed;
         otherwise, ONE = shear by 1.0
     @param objToWorld matrix to calculate
 */
  public static void calcObjToWorld(FPt position, Angle rotation,
      float scale, float shearX, float shearY, Mat2 objToWorld) {
    objToWorld.identity();
    objToWorld.translate(position);
    /*
    if (shearX != 0)
      objToWorld.shearX(shearX);
    if (shearY != 0)
      objToWorld.shearY(shearY);
      */
    objToWorld.scale(scale);
    objToWorld.rotate(rotation);
  }

  /**
     Calculate inverse of object to world matrix

     @param position position of object in world space
     @param rotation angle of rotation of object
     @param scale scale factor to apply (ONE = unscaled)
     @param shearX x shear factor; if 0, no shearing performed;
        otherwise, ONE = shear by 1.0
     @param shearY y shear factor; if 0, no shearing performed;
         otherwise, ONE = shear by 1.0
     @param worldToObj matrix to calculate
 */
  public static void calcWorldToObj(FPt position, Angle rotation,
      float scale, float shearX, float shearY, Mat2 worldToObj) {
    Mat2 m = worldToObj;
    m.identity();
    m.scale(1 / scale);
    m.rotate(new Angle(-rotation.get()));
    /*
    if (shearY != 0)
      worldToObj.shearY(-shearY);
    if (shearX != 0)
      worldToObj.shearX(-shearX);
      */
    worldToObj.translate(new FPt(-position.x,-position.y));
  }

  /**
     Calculate object to screen matrix from existing object to world
     matrix; object-->screen matrix will be available in objToScreen()

     @param objToWorld matrix for object to world transformation
 */
  public static void calcObjToScreen(Mat2 objToWorld) {
    Mat2.multiply(worldToScreen,objToWorld,objToScreen);
  }

  /**
     Construct a matrix to transform from world -> screen.
   */
  private static void calcWorldToScreen() {
    Mat2 w = worldToScreen;
    w.identity();
    w.translate(origin);

    // Construct matrix to scale from world to screen.  Take
    // into account the window scaling factor.
    Mat2 w2 = new Mat2();
    w2.set(0,(int)windowScale);
    w2.set(4,(int)-windowScale);
    w.multiply(w2);
  }

  /**
     Determine origin of current view
     @return origin; read-only!
   */
  public static FPt origin() {
    return origin;
  }

  /**
     Transform a point from view space to screen space

     @param x xcoordinate to transform
     @param y ycoordinate to transform
     @param p screen space coordinates returned here
   */
  public static void ptViewToScreen(float x, float y, FPt p) {
    p.x = x * windowScale;
    p.y = y * windowScale;
  }

  /**
     Transform a point from screen space to view space

     @param x xcoordinate to transform
     @param y ycoordinate to transform
     @param p view space coordinates returned here
   */
  public static void ptScreenToView(float x, float y, FPt p) {
    p.x = x * windowScaleInv;
    p.y = y * windowScaleInv;
  }

  /**
     Transform a point from object space to screen space

     @param x xcoordinate to transform
     @param y ycoordinate to transform
     @param p screen space coordinates returned here
   */
  public static void ptObjToScreen(float x, float y, FPt p) {
    objToScreen.transform(x,y,p);
  }

  /**
     Transform a point from world space to screen space

     @param x xcoordinate to transform
     @param y ycoordinate to transform
     @param p screen space coordinates returned here
   */
  public static void ptWorldToScreen(float x, float y, FPt p) {
    // We can avoid using the transformation matrix for this
    // transform.
    p.x = x * windowScale + origin.x;
    p.y = origin.y - y * windowScale;
  }

  /**
     Transform a point from screen space to world space

     @param x xcoordinate to transform
     @param y ycoordinate to transform
     @param p screen space coordinates returned here
   */
  public static void ptScreenToWorld(float x, float y, FPt p) {
    p.x = (x - origin.x) * windowScaleInv;
    p.y = (origin.y - y) * windowScaleInv;
  }


  /**
     Select active window

     @param index window to select
   */
  private static void selectWindow(int index) {
    Window w = windows[index];
    windowR.setBounds((int)w.x,(int)w.y,(int)w.width,(int)w.height);
    windowScale = w.scale;

    windowScaleInv = 1 / windowScale;
    origin.set(windowR.x + (windowR.width / 2),
               windowR.y + (windowR.height / 2));
    calcWorldToScreen();
  }

  /**
     Determine object to screen transformation matrix
     @return object->screen transform matrix; read only!
   */
  public static Mat2 objToScreen() {
    return objToScreen;
  }

  /**
     Determine bounds of current window
     @return bounds of current window; read-only!
   */

  public static Rectangle windowR() {
    return windowR;
  }

  private static Rectangle windowR = new Rectangle();
  private static Graphics gLayer = null;

  private static Window[] windows;

  // 1 + the layer currently being worked on
  private static int workLayer;
  // (1 << L_xxx) set if its contents are valid
  private static int layerValidFlags;

  // (1 << L_xxx) set if layer has been initialized
  private static int layersInitFlags;

  private static final int BUFFER_PIXELS = 10;

  // layers:
  public static final int L_BGND = 0;
  public static final int L_TOTAL = 1;
  // Windows:
  public static final int WINDOW_BASE = 0;
  public static final int WINDOW_USER = 1;

  // the component containing the engine
  private static Component component;

  private static Image osImage;
  private static Dimension osSize;
  private static FPt origin = new FPt();
  private static FPt worldSize, worldClipMin, worldClipMax;

  private static float windowScale;
  private static float windowScaleInv;

  private static Mat2 worldToScreen = new Mat2();
  private static Mat2 objToScreen = new Mat2();

  public static void drawLine(Graphics g, float x1, float y1, float x2, float y2) {
    g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
  }

}