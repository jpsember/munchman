package vgpackage;
// Sprite class
import java.io.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.applet.Applet;
import mytools.*;

public class Sprite {

	// ----------------------------------------------------
	// Class members
	// ----------------------------------------------------
	private static Component component;
	private static MediaTracker tracker;

	// Initialize the sprite engine
	// Precondition:
	//	ge = BEngine (bitmap engine)
	public static void init(/*BEngine ge*/) {
//		Sprite.ge = ge;
//		Sprite.applet = ge.getApplet();
      component = VidGame.getApplet();
//		Sprite.component = applet;
		tracker = new MediaTracker(component);
	}

	// ----------------------------------------------------

	// ----------------------------------------------------
	// Instance members
	// ----------------------------------------------------

	public int w,h;				// width, height of the sprite
	public int cx,cy;			// centerpoint, relative to the sprite
	public Image image;		// the image containing the sprite
	private boolean embedded;	// true if this sprite is embedded within another
	private int colRects[];
	private int totalColRects;
	private final static int MAX_COL_RECTS = 5;
	public int ox,oy;			// embedding offset (0,0 if not embedded)
	private Sprite parent;	    // sprite containing embedded; null if not

	public void addColRect(int a[]) {

/* From Microsoft's website:
http://support.microsoft.com/default.aspx?scid=kb;[LN];Q265889

Fixed a problem with the just-in-time (JIT) compiler that
causes an illegal instruction exception to occur in native
code that generates for some loops with a constant number
of iterations and a cast of the induction variable in the loop body.
*/

    if (true) { // New version; use non-constant loop iterator
        int k = a.length;
		for (int j = 0; j < k; j += 4) {
			addColRect(a[j+0],a[j+1],a[j+2],a[j+3]);
		}
    } else  // Old version that crashes with IE:
    {
		for (int j = 0; j < a.length; j += 4) {
			addColRect(a[j+0],a[j+1],a[j+2],a[j+3]);
		}
    }
	}

	// Add a collision rectangle to the sprite.
	// Precondition:
	//	x0,y0 = pixel coordinates of top left pixel in the rectangle, relative to top left
	//				of sprite (i.e. not relative to the centerpoint)
	//	x1,y1 = pixel coordinates of bottom right pixel in the rectangle
	public void addColRect(int x0, int y0, int x1, int y1) {

		x0 -= cx;
		y0 -= cy;
		x1 -= (cx - 1);	// have x1,y1 extend 1 pixel beyond the collision range
		y1 -= (cy - 1);

		x0 <<= BEngine.FRACBITS;
		y0 <<= BEngine.FRACBITS;
		x1 <<= BEngine.FRACBITS;
		y1 <<= BEngine.FRACBITS;

		if (totalColRects == 0)
			colRects = new int[MAX_COL_RECTS * 4];
		if (totalColRects == 1) {
			for (int j = 0; j < 4; j++)
				colRects[4+j] = colRects[0+j];
			totalColRects = 2;
		}

		int j = totalColRects << 2;
		colRects[j+0] = x0;
		colRects[j+1] = y0;
		colRects[j+2] = x1;
		colRects[j+3] = y1;
		totalColRects++;

		if (totalColRects > 1) {
			// Accumulate this new rectangle to the bounding rect.
			if (colRects[0] > x0)
				colRects[0] = x0;
			if (colRects[2] < x1)
				colRects[2] = x1;
			if (colRects[1] > y0)
				colRects[1] = y0;
			if (colRects[3] < y1)
				colRects[3] = y1;
		}
	}

	public boolean collided(int x, int y, int tx, int ty, Sprite test) {

//		db.pr("collided (x,y)=" + (x >> BEngine.FRACBITS) + ","+(y >> BEngine.FRACBITS));
//		db.pr(" (tx,ty)=" + (tx >> BEngine.FRACBITS) + ","+(ty >> BEngine.FRACBITS));

		int jFlags = 0;
		int jFlag = 1;

		int iTotal = totalColRects << 2;
		int jTotal = test.totalColRects << 2;

		int iMin = (iTotal > 4) ? 0 : -1;
		int jMin = (jTotal > 4) ? 0 : -1;

//		db.pr(" iTotal="+iTotal+", jTotal="+jTotal);

		// Make collisions relative to x,y.

		tx -= x;
		ty -= y;

		for (int i = 0; i < iTotal; i+= 4) {
//			db.pr(" i="+i+", colRects="+(colRects[i]>>BEngine.FRACBITS)+","+(colRects[i+1]>>BEngine.FRACBITS)+","+(colRects[i+2]>>BEngine.FRACBITS)+","+(colRects[i+3]>>BEngine.FRACBITS));
			for (int j = 0; j < jTotal; j+= 4, jFlag <<= 1) {
				if ((jFlags & jFlag) != 0) continue;

				boolean result = !(
					colRects[i] >= test.colRects[j+2] + tx
				 || colRects[i+1] >= test.colRects[j+3] + ty
				 || colRects[i+2] <= test.colRects[j] + tx
				 || colRects[i+3] <= test.colRects[j+1] + ty);

//				db.pr("  j="+j+", colRects="+(test.colRects[j]>>BEngine.FRACBITS)+","+(test.colRects[j+1]>>BEngine.FRACBITS)+","+(test.colRects[j+2]>>BEngine.FRACBITS)+","+(test.colRects[j+3]>>BEngine.FRACBITS));

//				db.pr("   result="+result);

				if (!result) {
 					if (i != 0 && j != 0) continue;
					if (i == 0 && j == 0) return false;
					if (j == 0) break;
					jFlags |= jFlag;
					continue;
				}

				if (i > iMin && j > jMin) return true;
			}
		}
		return false;
	}

	public void copyFrom(Sprite base) {
		w = base.w;
		h = base.h;
		cx = base.cx;
		cy = base.cy;
		image = base.image;
		embedded = base.embedded;
		ox = base.ox;
		oy = base.oy;
		parent = base.parent;
	}

	// Constructors

	public Sprite() {}

	// Construct for non-embedded sprite
	// Precondition:
	//	name = name of .gif file to load, not including the .gif extension
	//	cx,cy = centerpoint of the sprite
	public Sprite(String name, int cx, int cy) {
//		db.pr("Sprite name="+name);
//		db.pr(" code base = "+applet.getCodeBase() );

//		if (VidGame.DEBUG)
//			Debug.ASSERT(applet != null,"Sprite class not initialized");

		// Determine path for image.

		String path = name + ".gif";

		boolean errFlag = false;

      // From Core Java Vol. 1, p. 629
      // -----------------------------------------------

      URL url = VidGame.getApplet().getClass().getResource(path);
      image = VidGame.getApplet().getImage(url);
      // -----------------------------------------------

//		image = applet.getImage(applet.getCodeBase(), path);

		tracker.addImage(image, 0);

		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			errFlag = true;
      }

		if (!errFlag)
			errFlag |= tracker.isErrorAny();

		if (VidGame.DEBUG) Debug.ASSERT(!errFlag,"Error loading image "+path);

		w = image.getWidth(component);
		h = image.getHeight(component);

		if (VidGame.DEBUG)
			Debug.ASSERT(w>0 && h>0, "Image "+path+" w, h = "+w+","+h);

		this.cx = cx;
		this.cy = cy;

	}

	public Sprite(String name) {
		this(name, 0, 0);
	}

	public Sprite(Sprite parent, int x, int y, int w, int h) {
		this(parent, x,y,w,h, 0,0);
	}

	// Constructor for sprite embedded in another sprite
	// Precondition:
	//	parent = Sprite containing this sprite in its image
	//	x,y,w,h = bounds of this sprite within parent's image
	//	cx,cy = centerpoint of the sprite, relative to the sprite bounds
	public Sprite(Sprite parent, int x, int y, int w, int h, int cx, int cy) {
//		if (VidGame.DEBUG)
//			Debug.ASSERT(applet != null,"Sprite class not initialized");

//		db.pr("Constructing embedded sprite, parent="+parent);

		embedded = true;

		// Determine the offset relative to the parent.  If the parent is itself
		// an embedded sprite, look at its parent, and so on.

		while (true) {
			image = parent.image;
			if (!parent.embedded) break;
			x += parent.ox;
			y += parent.oy;
			parent = parent.parent;
		}
		this.parent = parent;

		this.cx = cx;
		this.cy = cy;
		this.w = w;
		this.h = h;
		this.ox = x;
		this.oy = y;

		if (VidGame.DEBUG)
			Debug.ASSERT(ox >= 0 && oy >= 0 && ox + this.w <= parent.w && oy + this.h <= parent.h,
				"embedded sprite overflow, bounds="
					+ox+","+oy+","+this.w+","+this.h+", parent size "+parent.w+","+parent.h);

	}

	// Draw a sprite to the current layer
	// Precondition:
	//	g = Graphics object
	//	x,y = coordinates, relative to current view
	public void draw(Graphics g, int x, int y) {
		if (VidGame.DEBUG)
			Debug.ASSERT(BEngine.opened(),"Sprite.draw BEngine.opened");

		// Convert from view to screen coordinates.  Also, adjust for centerpoint.

		x += BEngine.viewR.x - cx;
		y += BEngine.viewR.y - cy;

		// Is this an embedded sprite?  If so, we must manipulate the clipping
		// area.
		if (!embedded) {
			g.drawImage(image, x, y, component);
			return;
		}

		// Set the clip region to the intersection of the sprite and the
		// current view.

		int clipXMin = Math.max(x, BEngine.viewR.x);
		int clipWidth = Math.min(x + w, BEngine.viewR.x + BEngine.viewR.width) - clipXMin;
		if (clipWidth <= 0) return;
		int clipYMin = Math.max(y, BEngine.viewR.y);
		int clipHeight = Math.min(y + h, BEngine.viewR.y + BEngine.viewR.height) - clipYMin;
		if (clipHeight <= 0) return;

		g.setClip(clipXMin, clipYMin, clipWidth, clipHeight);

		g.drawImage(image, x - ox, y - oy, component);

		// Restore clip region to the view

		g.setClip(BEngine.viewR.x, BEngine.viewR.y, BEngine.viewR.width, BEngine.viewR.height);
	}

}
