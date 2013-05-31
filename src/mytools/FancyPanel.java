package mytools;
import java.awt.*;

/**
 * Title:        FancyPanel.java
 * Description:  An extension of java.awt.Panel,
 *                it supports fancy looking bevelled edges,
 *                double buffering, and event thread processing.
 * Copyright:    Copyright (c) 2001
 * Company:      Sember Enterprises
 * @author Jeff Sember
 * @version 1.0
 */

public class FancyPanel extends Panel /*implements PanelMsg*/ {

   public static final int STYLE_PLAIN = 0;
   public static final int STYLE_ETCH = 1;
   public static final int STYLE_SUNKEN = 2;

   public FancyPanel() {
      init(null,STYLE_PLAIN,new Color(0x80,0x80,0xff));
   }

   public FancyPanel(String title, int iStyle, Color c) {
      init(title, iStyle, c);
   }

   /**
    * Make this panel the means for triggering the event thread
    * events.
    *
    * This is necessary for older JDK's, ones that did not support
    * the EventQueue.invokeLater() method.  We simulate this behaviour
    * by triggering a small repaint() event.  When this event occurs,
    * we call the supplied Runnable object's run() method.
    */
   public void attachEventThread(Runnable r) { // used to be called addEv..
      pnlEventThread = this;
      runnable = r;
      iRepaintX = 0;
      iRepaintY = 0;
   }

   /**
    * Process a message.
    *
    * This method is designed to facilitate communication between
    * panels.
    */
/*   public Object processMsg(int iMsg, Object obj) {
      return null;
   }
*/

   /**
    * Flag a portion of the interior of the panel for repainting
    */
   public void repaint(int x, int y, int width, int height) {
      repaint(0,x,y,width,height);
   }

   /**
    * Repaint the entire interior area of the panel
    */
   public void repaint() {
      repaint(0);
   }

   public void repaint(long tm) {
      repaint(tm, 0, 0, iPaintWidth, iPaintHeight);
   }

   // Flag a portion of the interior for repainting, with a
   // delay value
   public void repaint(int iDelay, int x, int y, int width, int height) {
      // Clip bounds into range of interior.
      int iXE = x + width;
      int iYE = y + height;

      x = MyMath.clamp(x, 0, iPaintWidth - insetsInterior.left - insetsInterior.right);
      y = MyMath.clamp(y, 0, iPaintHeight - insetsInterior.top - insetsInterior.bottom);
      iXE = MyMath.clamp(iXE,0,iPaintWidth - insetsInterior.left - insetsInterior.right);
      iYE = MyMath.clamp(iYE,0,iPaintHeight - insetsInterior.top - insetsInterior.bottom);

      width = iXE - x;
      height = iYE - y;

      if (width > 0 && height > 0) {
         if (pnlEventThread == this) {
            if (!fRepainted) {
               synchronized(this) {
                  iRepaintX = x;
                  iRepaintY = y;
                  fRepainted = true;
               }
            }
         }
         x += insetsInterior.left;
         y += insetsInterior.top;
         super.repaint(iDelay, x, y, width,height);
      }
   }

   /**
    * Trigger the event dispatch thread by repainting a single pixel
    * of the panel that has been designated the event panel.
    *
    * When a panel processes repaint() events, it can also process
    * any messages sent to the event thread.  This method determines
    * if any repainting has occurred since this method was last called,
    * and if not, repaints a single pixel to cause the repaint and
    * trigger the message processing.
    */
   public static void processEvents() /*triggerEventDispatchThread()*/ {

      if (ThreadEvent.isEmpty()) return;

      boolean fTriggerNecessary;
      int x,y;

      synchronized(pnlEventThread) {
         fTriggerNecessary = !pnlEventThread.fRepainted;
         x = iRepaintX;
         y = iRepaintY;
         pnlEventThread.fRepainted = false;
      }
      if (!fTriggerNecessary) return;
      pnlEventThread.repaint(x,y,1,1);
   }

   /**
    * Translate a point to be relative to the interior of the panel.
    */
   public Point translatePoint(Point point) {
      return new Point(point.x - insetsInterior.left, point.y - insetsInterior.top);
   }

   /**
    * Return the Insets of the panel
    */
   public Insets insets() {
      return insetsInterior;
   }

   /**
    * Make the panel double-buffered.
    * Should be called right after the panel is created, if double
    * buffering is desired.
    */
   public void makeDoubleBuffered() {
      fDoubleBuffered = true;
      fContentsValid = false;
   }

   /**
    * Draw a bevelled edge within the panel.  The edge is drawn within
    * a rectangle specified by x,y coordinate pairs.  The bottom and
    * right edges are not drawn.
    * @param g the graphics object
    * @param iBevelWidth the width of the bevelled edge
    * @param x0 left edge
    * @param y0 top edge
    * @param x1 right edge
    * @param y1 bottom edge
    * @param sunken true if edge is to be sunken, otherwise raised
    */
   void drawBevelEdge(Graphics g, int iBevelWidth, int x0, int y0, int x1, int y1, boolean sunken) {
      g.setColor(sunken ? colorDarker : colorLighter);
      for (int i = 0; i < iBevelWidth; i++) {
         g.drawLine(x0+i,y0+i,x1 - 1 - i,y0+i);
         g.drawLine(x0+i,y0+i+1,x0 + i,y1 - 2 - i);
      }
      g.setColor(sunken ? colorLighter : colorDarker);
      for (int i = 0; i < iBevelWidth; i++) {
         g.drawLine(x1-1-i,y0+i+1,x1 - 1 - i,y1 - 1 - i);
         g.drawLine(x0+i,y1 - 1 -i,x1 - 2 - i,y1 - 1 -i);
      }
   }

   /**
    * Create an array of Color[] from an array of rgb integers
    * @param size the number of (r,g,b) entries in the array
    * @param rgbArray an array of integers, sets of red, green, and blue
    */
	public static Color[] createColorTable(/*int size,*/ int rgbArray[]) {
      int size = rgbArray.length / 3;
		Color colArray[] = new Color[size];
		for (int i=0; i<size; i++) {
			int j = i*3;
			colArray[i] = new Color(rgbArray[j],rgbArray[j+1],rgbArray[j+2]);
		}
		return colArray;
	}

   /**
    * Overriden update() function, to avoid flickering.  No erasing
    * occurs.
    */
   public void update(Graphics g) {
      paint(g);
   }

   /**
    * Determine if panel is double buffered
    */
   public boolean isDoubleBuffered() {
      return fDoubleBuffered;
   }

   /**
    * Paint the panel.  The frame is drawn if necessary.  To paint
    * the interior of the panel, the paintInterior() method is called.
    */
   public void paint(Graphics g) {
		if (fDoubleBuffered) {
         Graphics og = getOffscreenBuffer();

			// Paint into the offscreen buffer.

			// Use the same clipping rectangle as the input context,
         // unless the offscreen buffer is invalid, in which case
         // we use the entire size.

        Rectangle clip = g.getClipBounds();
        if (!fContentsValid) {
          clip = getBounds();
          clip.x = 0;
          clip.y = 0;
        }
        og.setClip(clip);

         commonPaint(og, fContentsValid);
         og.dispose();

         fContentsValid = true;

			g.drawImage(offscreenImage,0,0,this);

		} else {
			commonPaint(g, false);
		}
   }

   /**
    * Determine the size of the interior of the panel
    */
   public Dimension getInteriorSize() {
    return new Dimension(rInterior.width, rInterior.height);
  }

   /**
    * Paint the dirty part of the panel's interior
    *  g = Graphics object
    *  fBufferValid = true if panel is double buffered and existing contents
    *    may be valid.  If true, calling program can assume no painting is
    *    required unless their own data has changed.  If false, entire
    *    rUpdate requires repaint.
    *
    *  The origin has been adjusted so (0,0) is the top left of the interior
    *  of the panel.
    *
    * The clip region of the panel has been set to the bounding
    * rectangle of the dirty part of the panel's interior.
    *
    * Override this function to perform your own object's updating.
    * This default one fills the interior with colorInterior.
    *
    */
   public void paintInterior(Graphics g, boolean fBufferValid) {
      g.setColor(getBackground());
      MyGraphics.fillRect(g, g.getClipBounds());
   }

	/**
    * Helper method for setting GridBag constraints
    * @param gbc GridBagConstraints object to modify
    * @param gx gridx value
    * @param gy gridy value
    * @param gw gridwidth value
    * @param gh gridheight value
    * @param wx weightx value
    * @param wy weighty value
    */
	public static void setGBC(GridBagConstraints gbc, int gx, int gy,
	 int gw, int gh, int wx, int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

   /**
    * Clear the interior to the background color.
    * @param g Graphics object
    */
   public void clearRect(Graphics g) {
      g.setColor(getBackground());
      MyGraphics.fillRect(g,g.getClipBounds());
   }

  private void beginPaint(Graphics g) {
      calculateInteriorSize();

    rDirty = g.getClipBounds();
    if (rDirty != null && rDirty.isEmpty())
      rDirty = null;
    fPaintPrepared = true;
  }

   private void calculateInteriorSize() {
      iPaintWidth = getSize().width;
      iPaintHeight = getSize().height;

      rInterior = new Rectangle(
         insetsInterior.left,insetsInterior.top,
         iPaintWidth - insetsInterior.left - insetsInterior.right,
         iPaintHeight - insetsInterior.top - insetsInterior.bottom);

      ptInteriorEnd = new Point(iPaintWidth - insetsInterior.right,
         iPaintHeight - insetsInterior.bottom);
   }

  private void endPaint() {
    rDirty = null;
    rInterior = null;
    fPaintPrepared = false;
  }

   public static boolean rectContainsRect(Rectangle rLarge, Rectangle rSmall) {
      return (
         rLarge.contains(rSmall.x,rSmall.y)
       && rLarge.contains(rSmall.x + rSmall.width - 1, rSmall.y + rSmall.height - 1));
   }

  // Determine if any of the frame needs repainting.
  // Returns false if the update region is confined to the interior.
  private boolean frameDirty(Graphics g) {
    return (!rectContainsRect(rInterior, rDirty));
  }

  private void paintFrame(Graphics g) {

    // Fill the border with the bgnd color
    g.setColor(colorBgnd);
    g.fillRect(0,0,iPaintWidth,insetsInterior.top);
    g.fillRect(0,iPaintHeight - insetsInterior.bottom, iPaintWidth, insetsInterior.bottom);
    g.fillRect(0,insetsInterior.top,insetsInterior.left,rInterior.height);
    g.fillRect(iPaintWidth - insetsInterior.right,insetsInterior.top,insetsInterior.right,rInterior.height);

    if (iTitleHeight != 0) {
      g.setColor(colorTitle);
      g.fillRect(iOutsideMarginSize,iOutsideMarginSize,
         iPaintWidth - iOutsideMarginSize * 2, iTitleHeight);
      FontMetrics fm = g.getFontMetrics(titleFont);
      int iHeight = fm.getAscent();
      g.setColor(Color.black);
      int tx = (iPaintWidth - fm.stringWidth(sTitle)) / 2;
      int ty = iOutsideMarginSize + iHeight +
         (TITLE_HEIGHT - iHeight) / 2;

      g.drawString(sTitle,tx,ty);
    }

    drawBevelEdge(g, iOutsideMarginSize, 0,0, iPaintWidth, iPaintHeight, false);

    switch (iStyle) {
    case STYLE_SUNKEN:
      {
        // Draw a sunken border one pixel wide
        int iEtchWidth = iPaintWidth - insetsEtch.left - insetsEtch.right;
        int iEtchHeight = iPaintHeight - insetsEtch.top - insetsEtch.bottom;

        drawBevelEdge(g,1,
          insetsEtch.left, insetsEtch.top,
          insetsEtch.left + iEtchWidth, insetsEtch.top + iEtchHeight,
          true);
       }
      break;

    case STYLE_ETCH:
      {
        // Draw an etched border a few pixels inside

        int iEtchWidth = iPaintWidth - (1 + insetsEtch.left + insetsEtch.right);
        int iEtchHeight = iPaintHeight - (1 + insetsEtch.top + insetsEtch.bottom);

        g.setColor(colorLighter);
        g.drawRect(insetsEtch.left + 1, insetsEtch.top + 1,iEtchWidth,iEtchHeight);
        g.setColor(colorDarker);
        g.drawRect(insetsEtch.left, insetsEtch.top,iEtchWidth,iEtchHeight);
      }
      break;
    }
  }

  private Graphics getOffscreenBuffer() {
   if (needNewOffscreenBuffer()) {
      fContentsValid = false;
      offscreenSize = new Dimension(getSize().width, getSize().height);
      offscreenImage = createImage(offscreenSize.width, offscreenSize.height);
   }
   return offscreenImage.getGraphics();
  }


  private void commonPaint(Graphics g, boolean fBufferValid) {
    beginPaint(g);

    if (rDirty != null) {

      if (frameDirty(g))
        paintFrame(g);

      Rectangle rUpdate = prepareInterior(g);
      if (!rUpdate.isEmpty()) {
        paintInterior(g,fBufferValid);
      }
      restoreOrigin(g);
   /*
      if (this == pnlEventThread) {
         iCount++;
         g.setColor((iCount & 1) != 0 ? Color.black : Color.yellow);
         g.drawRect(rDirty.x,rDirty.y,rDirty.width-1,rDirty.height-1);
      }
   */
    }
    endPaint();

    if (this == pnlEventThread) {
      runnable.run();
    }
  }

  private void restoreOrigin(Graphics g) {
    g.translate(-rInterior.x,-rInterior.y);
  }

  // Move the origin and adjust the clipping rectangle to fit the interior
  // of the panel.  The origin is moved so (0,0) is in the top left of the
  // interior of the panel.
  //
  // Returns a Rectangle that contains the portion of the interior that needs
  // redrawing.
  private Rectangle prepareInterior(Graphics g) {
    Rectangle r = rDirty.intersection(rInterior);
    g.translate(rInterior.x,rInterior.y);
    if (!r.isEmpty()) {
      r.translate(-rInterior.x,-rInterior.y);
      g.setClip(r.x,r.y,r.width,r.height);
    }
    return r;
  }

   private void init(String title, int iStyle, Color c) {

      if (title != null) {
         sTitle = title;
         iTitleHeight = TITLE_HEIGHT;

         if (titleFont == null) {
            titleFont = new Font("Courier",Font.PLAIN,iTitleHeight - 2);
         }
      }
      fPaintPrepared = false;
      fDoubleBuffered = false;

      switch (iStyle) {
      case STYLE_SUNKEN:
         iOutsideMarginSize = 3;
         insetsEtch = new Insets(6 + iTitleHeight,6,6,6);
         insetsInterior = new Insets(7 + iTitleHeight,7,7,7);
         break;

      case STYLE_ETCH:
         iOutsideMarginSize = 2;
         insetsEtch = new Insets(5 + iTitleHeight,5,5,5);
         insetsInterior = new Insets(10 + iTitleHeight,10,10,10);
         break;

      default:
         // In case the caller specified an unsupported style,
         // change style to PLAIN.
         iStyle = STYLE_PLAIN;
         iOutsideMarginSize = 2;
         insetsInterior = new Insets(5 + iTitleHeight,5,5,5);
         break;
      }

      this.iStyle = iStyle;
      setColor(c);

      calculateInteriorSize();
   }

   private static Color colorAdjust(Color c, int iFactor) {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();

      r = (r * iFactor) >> 8;
      g = (g * iFactor) >> 8;
      b = (b * iFactor) >> 8;

      return new Color(r,g,b);
   }

   private void setColor(Color c) {

//      colorBgnd = new Color(r,g,b);
//      colorLighter = c.brighter();
//      colorDarker = c.darker();

      colorBgnd = colorAdjust(c, 0x100);
      colorTitle = colorAdjust(c, 0xe0);
      colorLighter = colorAdjust(c, 0x140);
      colorDarker = colorAdjust(c, 0xC0);

      switch (iStyle) {
      case STYLE_SUNKEN:
         setBackground(colorDarker);
         break;
      default:
         setBackground(colorBgnd);
         break;
      }
   }

  private boolean needNewOffscreenBuffer() {
		return (
			offscreenImage == null
		 || getSize().width != offscreenSize.width
		 || getSize().height != offscreenSize.height
		);
  }

  private int iStyle;

  private int iOutsideMarginSize;   // # pixels of bevelling on the outside borders
  private Insets insetsEtch;         // insets for etched border
  private Insets insetsInterior;    // insets for interior

   private Color colorBgnd;
   private Color colorLighter;
   private Color colorDarker;
   private Color colorTitle;

  // These are used within beginPaint()...endPaint() calls:

  private boolean fPaintPrepared;
  private int iPaintWidth;
  private int iPaintHeight;
  private Rectangle rInterior;
  private Rectangle rDirty;
  private boolean fDoubleBuffered;

	// double buffering:
	private Image offscreenImage;
	private Dimension offscreenSize;
   private boolean fContentsValid;

   // For keeping track of whether repainting occurred, for
   // triggering the event dispatch thread:
   private static int iRepaintX, iRepaintY;
   private static boolean fRepainted;
   private static Runnable runnable;
   private static FancyPanel pnlEventThread = null;
   private static int iCount = 0;   // debug only.

   private static final int TITLE_HEIGHT = 13;
   private String sTitle = null;
   private int iTitleHeight = 0;
   private Font titleFont;
   private Point ptInteriorEnd;
}