package vgpackage;
import java.awt.*;
import mytools.*;

public class SpriteExp implements VidGameGlobals
{
    private static final int TOTAL = 128;	// should be a power of 2.
    private static final int SIZES = 4;
    private static SpriteExp list[];
    private static int nextFree;
	private static byte nextGroup;
    private static final int BRICK_W = 6;
    private static final int BRICK_H = 5;

    public static void init() {
        nextFree = 0;
    	nextGroup = 0;
        list = new SpriteExp[TOTAL];
        for(int i = 0; i < TOTAL; i++)
            list[i] = new SpriteExp();
    }

	protected static final int EXP_VEL = 40;
	protected static final int EXP_VEL_RND = 750;

	public static void add(Sprite source, Pt pt, int lifeSpeed, int gravX, int gravY) {
		add(source, pt, lifeSpeed, gravX, gravY, EXP_VEL, EXP_VEL_RND, null);
	}

    public static void add(Sprite source,
        Pt pt,
        int lifeSpeed,
        int gravX, int gravY,
        int velBase, int velRand, Pt vel) {

        int baseVelX = 0;
        int baseVelY = 0;
        if (vel != null) {
            baseVelX = vel.x;
            baseVelY = vel.y;
        }

		velBase *= DASH;
		velRand *= DASH;
		gravX *= BEngine.TICK;
		gravY *= BEngine.TICK;

        int centerX = source.w >> 1;
        int centerY = source.h >> 1;
        int startX = 0;

		nextGroup++;

        for (int yFrag = 0; yFrag <= source.h - BRICK_H; yFrag += BRICK_H) {
            startX++;

            for (int xFrag = xOffsets[startX & 0x3]; xFrag <= source.w - BRICK_W; xFrag += BRICK_W) {
                SpriteExp exp = list[nextFree];

				nextFree = (nextFree + 1) & (TOTAL - 1);

                exp.sprite.copyFrom(source);
                exp.ox = xFrag + source.ox;
                exp.oy = yFrag + source.oy;

                exp.size = -1;	// make it undefined to force a recalc the first time.

                exp.lifeSpan = 1024;
                exp.lifeSpeed = (lifeSpeed + MyMath.rnd(lifeSpeed)) / VidGame.FPS;
				exp.accel.x = gravX;
				exp.accel.y = gravY;

                exp.pos.x = pt.x + ((xFrag - source.cx) << BEngine.FRACBITS);
                exp.pos.y = pt.y + ((yFrag - source.cy) << BEngine.FRACBITS);

                exp.vel.x = baseVelX + (xFrag - centerX) * velBase + MyMath.rndCtr(velRand);
                exp.vel.y = baseVelY + (yFrag - centerY) * velBase + MyMath.rndCtr(velRand);

				exp.group = nextGroup;
            }

        }

    }

	private final static int xOffsets[] = {4,2,0,2};

    public static void add(Sprite source, Pt pt, int lifeSpeed, int gravX, int gravY, int velBase, int velRand) {
        add(source,pt,lifeSpeed,gravX,gravY,velBase,velRand,null);
    }

    public static void move()
    {
        for(int i = 0; i < TOTAL; i++)
            list[i].moveOne();

    }

    private final static int imgInsets[] = {
        6, 5, 0, 0,
		4, 3, 1, 1,
		2, 2, 2, 1,
		2, 1, 2, 2
    };

	protected void processGravity() {
		vel.y += accel.y;
		vel.x += accel.x;
	}

    protected void moveOne()
    {
        lifeSpan -= lifeSpeed;
        if (lifeSpan <= 0) {
            lifeSpan = 0;
            return;
        }

		processGravity();

        vel.addTo(pos);

		// Determine if we need to recalculate the size of the fragment.

        int i = Math.min(SIZES-1, lifeSpan >> 8);

        if (i != size) {
            size = i;
            int j = ((SIZES-1) - size) * 4;
            sprite.w = imgInsets[j];
            sprite.h = imgInsets[j + 1];
            sprite.ox = ox + imgInsets[j + 2];
            sprite.oy = oy + imgInsets[j + 3];
            sprite.cx = 0;
            sprite.cy = 0;
        }
    }

	private final static boolean SHOW_UPDATES = (VidGame.DEBUG && false);

	// Plot each active explosion sprite
    public static void draw()
    {
    	final Pt workPt = new Pt();

		// We avoid updating each sprite individually.  Instead,
		// calculate on-the-fly the minimum enclosing rectangle of each group
		// of sprites.

		boolean prevDefined = false;		// True if we are working on a group
		int x0=0,y0=0,x1=0,y1=0;			// The enclosing coordinates
		byte prevGroup = 0;					// The id of the current group

		if (SHOW_UPDATES)
            BEngine.setColor(Color.red);

		// Turn off updates.
		// We will turn it back on temporarily when we update each group.
		BEngine.disableUpdate(1);

		// Start just beyond the last explosion object added.  This ensures that
		// we will not break up a group into two pieces.

		int j = nextFree;

        for (int i = 0; i < TOTAL; i++, j++) {
			SpriteExp e = list[j & (TOTAL-1)];
			if (e.lifeSpan == 0) continue;

            BEngine.ptWorldToView(e.pos.x, e.pos.y, workPt);

			// Calculate the right and bottom extent of the sprite.
			int ex = workPt.x + e.sprite.w;
			int ey = workPt.y + e.sprite.h;

			// Is this the start of a new explosion group?  If so, update the old one
			// and set flag to start a new group.
			if (prevDefined && e.group != prevGroup) {
				BEngine.disableUpdate(-1);
				BEngine.updateRect(x0,y0,x1-x0,y1-y0);
				if (SHOW_UPDATES) {
                    Graphics g = BEngine.getGraphics();
					g.drawRect(x0+BEngine.viewR.x,
                        y0+BEngine.viewR.y, x1-x0-1, y1-y0-1);
                }
				BEngine.disableUpdate(1);
				prevDefined = false;
			}

			// If we are starting a new group, initialize the bounding coordinates.
			if (!prevDefined) {
				prevDefined = true;
				x0 = workPt.x;
				x1 = ex;
				y0 = workPt.y;
				y1 = ey;
				prevGroup = e.group;
			}

			// Accumulate this explosion into the current group.
			// We don't need to adjust for centerpoints, since we set them to 0.

			if (x0 > workPt.x)
				x0 = workPt.x;
			if (x1 < ex)
				x1 = ex;
			if (y0 > workPt.y)
				y0 = workPt.y;
			if (y1 < ey)
				y1 = ey;

			// Plot the explosion sprite, without adding to the update region.
            BEngine.drawSprite(e.sprite, workPt.x, workPt.y);
		}

		// Turn updates back on.
		BEngine.disableUpdate(-1);

		// If we finished with a current group that hasn't been updated, do so.
		if (prevDefined) {
			BEngine.updateRect(x0,y0,x1-x0,y1-y0);
			if (SHOW_UPDATES) {
                Graphics g = BEngine.getGraphics();
				g.drawRect(x0+BEngine.viewR.x, y0+BEngine.viewR.y,
                    x1-x0-1, y1-y0-1);
            }
		}
    }

    public SpriteExp()
    {
        pos = new Pt();
        vel = new Pt();
		accel = new Pt();
        sprite = new Sprite();
    }

    private Sprite sprite;
    private int ox;
    private int oy;
	private byte group;
    private int size;

    protected int lifeSpan;
    protected Pt pos;
    protected Pt vel;
    protected int lifeSpeed;
	protected Pt accel;
}
