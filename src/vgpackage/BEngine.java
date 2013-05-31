package vgpackage;

import java.awt.*;
import java.util.*;
import java.net.*;
import java.applet.Applet;
import mytools.*;

public final class BEngine {

  // Make the default constructor private so nobody tries to
  // instantiate any objects.
  private BEngine() {}

  // Prepare for modifying a layer
  // Precondition:
  //	layer = L_xxx
  // Postcondition:
  //	Returns Graphics object.  This graphics object must be
  //	disposed of when calling function is done with it!

  // Prepare for modifying a layer.  Must be balanced by a call to
  // closeLayer().
  // Precondition:
  //	layer = L_xxx
  public static void openLayer(int layer) {
    Debug.ASSERT(gLayer == null, "Unbalanced openLayer()...closeLayer() calls");

    switch (layer) {
      case L_BGND:
        paintBGND();
        break;

      case L_SPRITE:
        if (workLayer != L_BGND) {
          openLayer(L_BGND);
          closeLayer();
        }
        paintSPRITE();
        break;
    }

    // Set previous layer valid.

    if (workLayer >= 0) {
      layerValidFlags |= (1 << workLayer);
    }

    workLayer = layer;
    gLayer = getGraphics(layer);

    // Select default, full-screen view initially.
    selectView(0);
  }

  public static void closeLayer() {
    gLayer.dispose();
    gLayer = null;
  }

  private static Graphics gLayer;

  public final static Graphics getGraphics() {
    return gLayer;
  }

  /**
   * Mark current layer as invalid
   */
  public static void invalidate() {
    layerValidFlags &= ~ (1 << workLayer);
  }

  // Determine if current layer is valid
  public static boolean layerValid() {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.layerValid() open");
      Debug.ASSERT(workLayer >= 0 && workLayer<L_TOTAL, "BEngine.layerValid()");
    }
    return (layerValidFlags & (1 << workLayer)) != 0;
  }

  public static void updateScreen(Graphics g) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.updateScreen() open");
    }
    if (workLayer != L_SPRITE) {
      openLayer(L_SPRITE); //paintLayer(L_SPRITE);
      closeLayer();
//			g2.dispose();
    }
    // Copy the entire os image every time.

    if (VidGame.DEBUG) {
      Debug.ASSERT(osImages[L_SPRITE] != null, "L_SPRITE is null");
    }

    g.drawImage(osImages[L_SPRITE], 0, 0, component);

    // Mark sprite layer as valid, although this has no real use.
    layerValidFlags |= (1 << L_SPRITE);

    workLayer = -1;
//        db.pr("clearing workLayer to "+workLayer);
  }

  /*
   public static void drawLineWorld(Graphics g, int x0, int y0, int x1, int y1) {
          x0 = (x0 >> VidGame.FRACBITS);
          x1 = (x1 >> VidGame.FRACBITS);
          y0 = (y0 >> VidGame.FRACBITS);
          y1 = (y1 >> VidGame.FRACBITS);
          g.drawLine(x0+viewR.x,y0+viewR.y,x1+viewR.x,y1+viewR.y);
          int w = x1 + 1 - x0;
          if (x0 > x1) {
              w = x0 + 1 - x1;
              x0 = x1;
          }
          int h = y1 + 1 - y0;
          if (y0 > y1) {
              h = y0 + 1 - y1;
              y0 = y1;
          }
          updateRect(x0,y0,w,h);
      }
   */

  // Fill rectangle in view space to current color
  public static void fillRect( /*Graphics g,*/int x, int y, int w, int h) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.fillRect() open");
    }

    gLayer.fillRect(x + viewR.x, y + viewR.y, w, h);
    updateRect(x, y, w, h);
  }

  /*	public BEngine() {
//		this.applet = applet;
    component = VidGame.getApplet();

    Sprite.init(this);
   }
   */
  public static void prepareUpdate() {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.prepareUpdate() open");
    }
    if (needNewOffscreenBuffer()) {
      constructOffscreenBuffer();
    }
  }

  // Clear the current view to black (Is graphics required, or can we use gLayer?)
  public static void clearView() { //Graphics g) {
    clearView(Color.black);
  }

  // Is graphics required, or can we use gLayer?
  public static void clearView( /*Graphics g,*/Color c) {
//        Graphics g = getGraphics();
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.clearView() open");
    }
    gLayer.setColor(c);
    fillRect(0, 0, viewR.width, viewR.height);
  }

  public static void setColor(Color c) {
    gLayer.setColor(c);
  }

  /*
   public Applet getApplet() {
    return applet;
   }
   */
  /*
// Transform a point from world space to screen space
   public static void ptWorldToScreen(int x, int y, Pt p) {
    if (VidGame.DEBUG)
     Debug.ASSERT(opened(),"BEngine.ptWorldToScreen() open");
    p.x = viewR.x + (x >> FRACBITS);
    p.y = viewR.y - (y >> FRACBITS);
   }
   */

  // Transform a point from world space to view space
  public static void ptWorldToView(int x, int y, Pt p) {
    p.x = (x >> FRACBITS);
    p.y = (y >> FRACBITS);
  }

  public static void drawSprite(Sprite sprite, Pt loc) {
    drawSprite(sprite, loc.x, loc.y);
  }

  public static void drawSpriteWorld(Sprite sprite, Pt worldLoc) {
    drawSpriteWorld(sprite, worldLoc.x, worldLoc.y);
//        drawSprite(sprite, worldLoc.x >> VidGame.FRACBITS, worldLoc.y >> VidGame.FRACBITS);
  }

  public static void drawSpriteWorld(Sprite sprite, int wx, int wy) {
    drawSprite(sprite, wx >> VidGame.FRACBITS, wy >> VidGame.FRACBITS);
  }

  public static void drawSprite(Sprite sprite, int x, int y) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.drawSprite() open");
    }

    int px = x - sprite.cx;
    int py = y - sprite.cy;

    // If clipped entirely out of the view, return.
    if (px >= viewR.width || px + sprite.w <= 0
        || py >= viewR.height || py + sprite.h <= 0
        ) {
      return;
    }

    sprite.draw(getGraphics(), x, y);
    if (updateDisabled == 0) {
      updateRect(px, py, sprite.w, sprite.h);
    }
  }

  public static void disableUpdate(int adjust) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.disableUpdate() open");
    }
    updateDisabled += adjust;
    if (VidGame.DEBUG) {
      Debug.ASSERT(updateDisabled >= 0, "BEngine:updateDisabled underflow");
    }
  }

  // Make sure a rectangular region of the screen gets displayed.
  // This has a different function depending upon which layer is being manipulated.
  // If it's the L_BGND, it flags the appropriate bgnd squares for updating.
  // If L_SPRITE, it ensures that the region gets erased next frame.
  public static void updateRect(int x, int y, int w, int h) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.updateRect() open");
    }

    if (updateDisabled > 0) {
      return;
    }

    if (workLayer == L_BGND) {
      flagBGNDSquare(x + viewR.x, y + viewR.y, w, h);
      return;
    }
    if (VidGame.DEBUG) {
      Debug.ASSERT(workLayer == L_SPRITE,
                   "BEngine:updateRect called with workLayer not valid");
    }

    addDirtyRect(x, y, w, h);
  }

  /**
   * Determines the mouse coordinates, in world space, relative to a
   * view.
   * @param view the view
   */
  public static Point getMousePoint(int view) {
    Rectangle r = new Rectangle();
    selectView(view, r);
    Point pt = VidGame.getMousePoint();
    return new Point(
        (pt.x - r.x) << VidGame.FRACBITS,
        (pt.y - r.y) << VidGame.FRACBITS);
  }

  public static Rectangle getViewWorldRect(int view) {
    Rectangle r = new Rectangle();
    selectView(view, r);
    r.x = 0;
    r.y = 0;
    r.width <<= VidGame.FRACBITS;
    r.height <<= VidGame.FRACBITS;
    return r;
  }

  private static void selectView(int index, Rectangle r) {
    int i = index * 4;
    r.setBounds(viewBounds[i + 0], viewBounds[i + 1], viewBounds[i + 2],
                viewBounds[i + 3]);
  }

  public static void selectView( /*Graphics g,*/int index) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.selectView() open");
      Debug.ASSERT(index >= 0 && index < MAX_VIEWS &&
                   (viewsDefined & (1 << index)) != 0,
                   "BEngine.selectView bad arg");
    }
    selectView(index, viewR);
    gLayer.setClip(viewR.x, viewR.y, viewR.width, viewR.height);

//		db.pr("selectView "+index+", viewR = "+db.rString(viewR) );

  }

  // Define a view
  // Preconditions:
  //	index = index of view (1...n)
  //	x, y, width, height = bounds
  //	Note that index = 0 is illegal, as this view is predefined to be the full screen
  public static void defineView(int index, int x, int y, int width, int height) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.defineView() open");
      Debug.ASSERT(index > 0 && index<MAX_VIEWS,
                   "BEngine.defineView illegal index");
    }
    int i = index * 4;

    viewBounds[i + 0] = x;
    viewBounds[i + 1] = y;
    viewBounds[i + 2] = width;
    viewBounds[i + 3] = height;
//		db.pr("defineView index="+index+", bounds = "+db.rString(
//			viewBounds[i+0],viewBounds[i+1],viewBounds[i+2],viewBounds[i+3]) );

    viewsDefined |= (1 << index);
  }

  public static void open() {
    if (VidGame.DEBUG) {
      Debug.ASSERT(!opened(), "BEngine.open() called while already open!");
    }

    open = true;

    component = VidGame.getApplet();
    Sprite.init( /* this */);

    osImages = new Image[L_TOTAL];
    viewR = new Rectangle();
    viewBounds = new int[MAX_VIEWS * 4];

    viewsDefined = (1 << 0);
    viewBounds[0] = 0;
    viewBounds[1] = 0;
    viewBounds[2] = component.getSize().width;
    viewBounds[3] = component.getSize().height;

    maxDirtyRects = 60;
  }

  public static void close() {
    if (VidGame.DEBUG) {
      Debug.ASSERT(opened(), "BEngine.close() called while not open!");
    }

    open = false;

    osImages = null;
    viewR = null;
    viewBounds = null;
    updateDisabled = 0;
    workLayer = -1;
    layerValidFlags = 0;
    layersInitFlags = 0;
    displayFlags = null;
    dirtyRects = null;
  }

  public static boolean opened() {
    return open;
  }

  private static Graphics getGraphics(int layer) {
    if (VidGame.DEBUG) {
      Debug.ASSERT(layer >= 0 && layer<L_TOTAL,
                   "BEngine.getGraphics() layer is " + layer);
      Debug.ASSERT(osImages[layer] != null, "osImages " + layer + " is null");
    }
    return osImages[layer].getGraphics();
  }

  // Determine if a new off-screen buffer is required by comparing
  // the size of the applet with the size of the last prepared off-screen buffer.
  private static boolean needNewOffscreenBuffer() {
    // Note: the views that have been defined will NOT be affected by changing the
    // size of the applet.  With this in mind, we won't support re-sizing the applet
    // window.  It won't be changable in a browser context anyways.
    return (
        osImages[L_SPRITE] == null
        );
  }

  // Construct new off-screen buffer(s)
  private static void constructOffscreenBuffer() {
    osSize = new Dimension(viewBounds[0 * 4 + 2], viewBounds[0 * 4 + 3]);
    for (int i = 0; i < L_TOTAL; i++) {
      osImages[i] = component.createImage(osSize.width, osSize.height);
    }

    layersInitFlags = 0;
    layerValidFlags = 0;
  }

  private static void paintBGND() {
    if ( (layersInitFlags & (1 << L_BGND)) == 0) {

      ckWidth = (osSize.width + SQR_PIXELS - 1) >> SQR_POWER;
      // We want to store refresh flags in an array of ints, so make sure it's not too big:
      // Debug.ASSERT(ckWidth <= 31, "BEngine.paintBGND() osBuffer is too big");
      ckHeight = (osSize.height + SQR_PIXELS - 1) >> SQR_POWER;

      displayFlags = new int[ckHeight];

      // Mark every square for updating.

      for (int y = 0; y < ckHeight; y++) {
        for (int x = 0; x < ckWidth; x++) {
          displayFlags[y] |= (1 << x);
        }
      }

      layersInitFlags |= (1 << L_BGND);
    }
    else {
      // Clear all the checkerboard display flags.
      for (int y = 0; y < ckHeight; y++) {
        displayFlags[y] = 0;
      }
    }
  }

  // We need to keep track of dirty areas of the screen, or areas that will
  // need to be erased later by copying the appropriate background squares.
  // The dirty areas may have been created by a drawing sprite, or by some other
  // graphic operation, such as plotting text.
  // All we need to store is the minimum enclosing rectangle of the dirty area.
  // These rectangles are stored in screen space, not view space.

  private static void paintSPRITE() {
    if ( (layersInitFlags & (1 << L_SPRITE)) == 0) {

      // Allocate an array of dirty rectangles

      dirtyRects = new short[maxDirtyRects<<2];
      dirtyRectCount = 0;

      layersInitFlags |= (1 << L_SPRITE);
    }

    // Flag any background squares that overlap the old sprites.

    if (VidGame.DEBUG) {
      if (dirtyRectCount > maxDirtyRects) {
        Debug.print("BEngine dirtyRectCount = " + dirtyRectCount + ", max is " +
                    maxDirtyRects);
      }
    }

    for (int i = (Math.min(dirtyRectCount, maxDirtyRects) - 1) << 2; i >= 0;
         i -= 4) {
      flagBGNDSquare(dirtyRects[i + 0], dirtyRects[i + 1], dirtyRects[i + 2],
                     dirtyRects[i + 3]);
    }

    dirtyRectCount = 0;
  }

  // Copy flagged BGND squares to SPRITE buffer
  public static void erase() {
    int xStart = 0;
    int xTotal = 0;
    int yStart = 0;

//        if (true) return;

    xTotal = 0;

//		Graphics g = getGraphics();//L_SPRITE);

    Rectangle oldClipRect = gLayer.getClipBounds();

    for (int y = 0; y < ckHeight; y++) {
      int flags = displayFlags[y];
      if (flags == 0) {
        continue;
      }

      for (int x = 0; x < ckWidth; x++) {
        boolean flushFlag = ( (flags & (1 << x)) == 0);
        if (!flushFlag) {
          if (xTotal++ == 0) {
            xStart = x;
            yStart = y;
          }
          if (x + 1 == ckWidth) {
            flushFlag = true;
          }
        }

        if (!flushFlag) {
          continue;
        }

        if (xTotal == 0) {
          continue;
        }

        // See how high this rectangle is.
        int testFlags = 0;
        for (int i = xStart + xTotal - 1; i >= xStart; i--) {
          testFlags |= (1 << i);
        }
        int yEnd = yStart;
        do {
          if ( (displayFlags[yEnd] & testFlags) != testFlags) {
            break;
          }
          displayFlags[yEnd] &= ~testFlags;
          yEnd++;
        }
        while (yEnd < ckHeight);

        // Copy a rectangle with position (xStart, yStart) and size (xTotal, yEnd-yStart)

        int cx = xStart << SQR_POWER;
        int cw = Math.min(xTotal<<SQR_POWER, osSize.width - cx);
        int cy = yStart << SQR_POWER;
        int ch = Math.min( (yEnd - yStart) << SQR_POWER, osSize.height - cy);

        gLayer.setClip(cx, cy, cw, ch);
        if (VidGame.DEBUG) {
          Debug.ASSERT(osImages[L_BGND] != null, "L_BGND image null");
        }
        gLayer.drawImage(osImages[L_BGND], 0, 0, component);

        if (SHOW_BGND_UPDATES) {
          updateTicker++;
          if (updateTicker > SQR_PIXELS / 4) {
            updateTicker = 0;
          }
          gLayer.setColor(Color.green);
          gLayer.drawRect(cx + updateTicker, cy + updateTicker,
                          cw - updateTicker * 2 - 1,
                          ch - updateTicker * 2 - 1);
        }

        xTotal = 0;
      }
      displayFlags[y] = 0;
    }
    gLayer.setClip(oldClipRect.x, oldClipRect.y, oldClipRect.width,
                   oldClipRect.height);
  }

  // Flag for display any L_BGND squares that overlap a rectangle.
  // Precondition:
  //	x,y,w,h : bounds of rectangle, in screen space
  private static void flagBGNDSquare(int x, int y, int w, int h) {
    int ex = x + w;
    int ey = y + h;
    if (x < 0) {
      x = 0;
    }
    if (y < 0) {
      y = 0;
    }
    if (ex > osSize.width) {
      ex = osSize.width;
    }
    if (ey > osSize.height) {
      ey = osSize.height;
    }
    if (ex <= x || ey <= y) {
      return;
    }

    int flags = 0;
    for (int i = (x >> SQR_POWER); i <= ( (ex - 1) >> SQR_POWER); i++) {
      flags |= (1 << i);
    }

    for (int i = (y >> SQR_POWER); i <= ( (ey - 1) >> SQR_POWER); i++) {
      displayFlags[i] |= flags;
    }
  }

  private static void addDirtyRect(int x, int y, int w, int h) {
    if (dirtyRectCount < maxDirtyRects) {
      int i = dirtyRectCount << 2;

      dirtyRects[i + 0] = (short) (x + viewR.x);
      dirtyRects[i + 1] = (short) (y + viewR.y);
      dirtyRects[i + 2] = (short) w;
      dirtyRects[i + 3] = (short) h;
    }
    dirtyRectCount++;
  }

  public static int getLayer() {
    return workLayer;
  }

  public static boolean isSpriteLayer() {
    return (workLayer == L_SPRITE);
  }

  public static boolean isBgndLayer() {
    return (workLayer == L_BGND);
  }

  // layers:
  public static final int L_SPRITE = 0;
  public static final int L_BGND = 1;
  public static final int L_TOTAL = 2;

  public static final int FRACBITS = 11;
  // # fractional bits in worldSpace

  public static final int ONE = (1<<FRACBITS);
  // one world space pixel

  //public static final int DASH = (ONE / 256);
  // 1/256th of a world space pixel

  public static final int TICK = ( (ONE * 16) / (256 * VidGame.FPS));
  // 1/16th world pixel/second, adjusted for frame rate

  public static Rectangle viewR; // bounds of current view, in screen space

  // Private class members:

  // Views:
  private final static int MAX_VIEWS = 16;
  private static int viewsDefined; // Flags for which views have been defined
  private static int viewBounds[]; // Position & size of each view

  private static Component component; // the component containing the engine

  // Double buffering:
  private static Dimension osSize; // size of off-screen image
  private static Image osImages[]; // array of images for each layer

  private static int workLayer; // layer currently being worked on, or -1
  private static int layerValidFlags; // (1 << L_xxx) set if its contents are valid

  private static int layersInitFlags; // (1 << L_xxx) set if layer has been initialized

  private static int ckWidth, ckHeight; // # squares across and down
  private static int displayFlags[];

  private static short dirtyRects[];

  private static int maxDirtyRects;
  private static int dirtyRectCount;

  private static int updateDisabled; // If > 0, don't add primitive bounds to update

  private static boolean open;

  // Debug-only variables:
  private static int updateTicker;
  private static int debCount;

  // Width/height of checkerboard squares:
  private static final int SQR_POWER = 5;
  private static final int SQR_PIXELS = (1<<SQR_POWER);

  private static final boolean SHOW_BGND_UPDATES =
      (false && VidGame.DEBUG);

}

/*
 How the Bitmap Engine works
 ---------------------------

 All output to the screen is double buffered.  Changes are plotted to an offscreen (os) buffer
 that exactly mirrors the main screen (or applet window) in size.

 When paint() is called, changes are plotted to the os buffer and then the entire os buffer
 is plotted to the screen.

 The areas of the os buffer that have changed are defined by a minimum enclosing rectangle.
 When the entire os buffer is plotted, it actually only plots this 'changes rect'.

 There is a second offscreen buffer, the background (bg) buffer.  Again, it mirrors the
 size of the applet.  It is constructed and disposed of at the same time as the primary
 os buffer.

 The bg buffer includes a 'checkerboard' data structure to organize areas that are being
 modified.  Anytime pixels change in the bg buffer, all the overlapping squares on the
 checkerboard (bg squares) are flagged for displaying.  When the paint() occurs, all of
 the flagged bg squares are copied from the bg buffer to the os buffer, and their bounds
 are accumulated into the os changes rect.

 Sprites, or temporary images, are implemented in this way:

 When update() is processed, any old sprites are erased by having their enclosing
 bg squares copied from the bg buffer -> os buffer, and the bg squares added to the
 os changes rect.

 New sprites are plotted to the os buffer, and their bounding rectangles are added to the
 os changes rect.

 The update can be thought of as operating on two layers, the background and the
 sprites.  The 'L_xxx' equates are provided.  The update occurs in these steps:

 ---- bg layer -------
 [] changes to background plotted to bg buffer, bg squares flagged for display
 [] overlapping bg squares of old sprites flagged for display
 [] all flagged bg squares are copied from bg buffer to os buffer
 [] os rect initialized to minimum enclosing rectangle of flagged bg squares
 ---- sprite layer -------
 [] new sprites plotted to os buffer, bounding rectangles accumulated into os rect
 ---- copy to screen -----
 [] os rect copied from os buffer to screen

 Observations
 ------------
 1.
 The bg buffer can be eliminated if a solid color or pattern is to be used instead.
 The bg squares structure is still required, but no image needs to be allocated.

 2.
 Things are simplified if we assume we are updating the entire screen every frame.
 Then repaint can be called with the entire bounds of the applet, but the os and bg
 buffers still only need to update what has changed.  If the os/bg buffers were newly
 created, this means they are entirely invalid.  Otherwise, the various plot() functions
 can compare what was drawn with what is now required to determine what needs redrawing.

 The VEngine method was to determine the minimum enclosing rectangle to pass to
 repaint(), but it's inefficient to calculate this rectangle before the actual plotting
 is occurring.

 The logic/draw thread will follow these steps:
 [] modify logic variables
 [] call repaint()

 When processing an update() command, the applet will then follow the steps outlined above.

 */
