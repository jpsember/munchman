package MunchMan;

import mytools.*;
import java.awt.*;
//import java.applet.Applet;
//import java.net.*;
import vgpackage.*;

public class Ghost extends Obj {

	private final boolean VERY_SMART = false;
	private final boolean INVINCIBLE = false;

	private final static int MAX_GHOSTS = 4;

	private final static int ACCEL = BEngine.TICK * 60;

	private final static int BOUNCE_LIMIT = Board.WORLD_CELL_SIZE/2;

	private static boolean caughtThisCycle;
	private static Board board;
//	private static BEngine ge;
	private static MunchMan parent;
	private static Player player;
	private static int ghostsCaptured;
	private static int sfxFlags;

	private static int reverseDelay;
	private static boolean reverseFlag;
	private static void calcReverseDelay() {
		reverseDelay = MyMath.rnd(7000) + 6000;
	}

	private int power;
	private byte status;	// GHS_x
	private short flags;	// GHF_nnn
	private int maxSpeed;

	private int moveOutDelay;
	private int indivReverseDelay;	//	For ghost type #1, allows him to reverse direction.

	private byte type;
	private byte number;

	private final static byte GHF_ANIMFRAME = 0x03;
	private final static byte GHF_SCORESHIF = 2;
	private final static byte GHF_SCOREMASK = (0x7<<GHF_SCORESHIF);
	private final static byte GHF_MOVINGOUT = 0x20;

	private final static byte GHS_UNUSED = 0;
	private final static byte GHS_WAITING = 1;
	private final static byte GHS_MOVINGOUT = 2;
	private final static byte GHS_ACTIVE = 3;
	private final static byte GHS_CAUGHT = 4;
	private final static byte GHS_EYES = 5;
	private final static byte GHS_EYES2 = 6;

	private static Pt outsidePit;
	private static Pt centerPit;
	private static int bounceMinY, bounceMaxY;

	private static Ghost list[];
	public static int cb;
	private static Sprite ims[];

	private final static int S_EYES = 0;				// one for each direction, plus power up
	private final static int S_BODY = S_EYES+5;		// main body
	private final static int S_FEET = S_BODY + 1;	// 6 frames
	private final static int S_TOTAL = S_BODY+(7 * 6);

	private void calcStartLoc(Pt p) {
		final int ghostStartLocs[] = {
			0,-3,
			-2,0,
			0,0,
			2,0,
		};

		p.set(
			ghostStartLocs[number*2+0] * Board.WORLD_CELL_SIZE + centerPit.x,
			ghostStartLocs[number*2+1] * Board.WORLD_CELL_SIZE + centerPit.y
		);
	}

	public Ghost(int index) {
		super(board, parent);
		number = (byte)index;
	}

	public static void init(Board board, MunchMan parent, Player player) {
		Ghost.board = board;
		Ghost.parent = parent;
//		Ghost.vg = vg;
		Ghost.player = player;

		outsidePit = new Pt();
		centerPit = new Pt();
		list = new Ghost[MAX_GHOSTS];
		for (int i = 0; i < MAX_GHOSTS; i++)
			list[i] = new Ghost(i);

		for (int i = 0; i < MAX_GHOSTS; i++) {
			Ghost g = list[i];

        }
	/*
		final String paths[] = {
			"eyesu","eyesr","eyesd","eyesl","eyesp",
			"g0b","g0f0","g0f1","g0f2","g0f3","g0f4","g0f5",
			"g1b","g1f0","g1f1","g1f2","g1f3","g1f4","g1f5",
			"g2b","g2f0","g2f1","g2f2","g2f3","g2f4","g2f5",
			"g3b","g3f0","g3f1","g3f2","g3f3","g3f4","g3f5",
			"gpb","gpf0","gpf1","gpf2","gpf3","gpf4","gpf5",
			"gqb","gqf0","gqf1","gqf2","gqf3","gqf4","gqf5",
		};
	*/

		ims = new Sprite[S_TOTAL];
		for (int i = 0; i < S_TOTAL; i++) {
			int j = i + (i >= S_BODY ? 2 : 0);
			int x = (j % 7) * 30;
			int y = (j / 7) * 30;
			int ht = 28;
			int cy = 14;

			if (i >= S_BODY) {
				if ((i - S_BODY) % 7 != 0) {
					ht -= 23;
					y += 23;
					cy -= 23;
				}
			}

			ims[i] = new Sprite(parent.sprite, x, y, 28, ht, 14, cy);

			// Set collision parameters for the first S_BODY sprite only.
			if (i == S_BODY) {
				setCollision(ims[i]);
			}
		}
/*
		// Calculate the checksum of the current document base to
		// make sure we are running the applet from our own URL,
		// to prevent someone from stealing our applet.

		if (db.COPYPROT) {
			URL u = ((Applet)parent).getDocumentBase();

			String str = u.toString().toUpperCase();

			int start = Math.max(0, str.length() - 30);
			int sum = 0;
			for (int i = start; i < str.length(); i++) {
				sum ^= (((int)str.charAt(i)) << (i & 7));
			}

			// !!!!! Enable this line to find out the secret
			// checksum.  It will be displayed as the high score.
                        if (VidGame.DEBUG)
    			  VidGame.setHighScore(sum);

			cb = sum;
		}
*/

	}

	public static int sfxFlags() {
		return sfxFlags;
	}

	private void setOnBoard() {
		type = (byte)(number & 3);
		dir = (number & 1) == 0 ? Board.UP : Board.DOWN;
		status = (number == 0) ? GHS_ACTIVE : GHS_WAITING;
		moveOutDelay = number * 500;

		if (status == GHS_ACTIVE)
			dir = Board.RIGHT;
		calcStartLoc(position);

		final int maxSpeeds[] = {
			BEngine.TICK * 1900,
			BEngine.TICK * 1800,
			BEngine.TICK * 1650,
			BEngine.TICK * 1400,
		};
		int level = VidGame.getLevel();
		if (level >= 8)
			level = ((level - 8) % 3) + 5;

		final int speedScales[] = {
			96,105,110,115,
			118,120,124,128,
			96,115,120,128,
			138,144,150,157
		};

      /*
         if (db.COPYPROT) {
    		  if (Ghost.cb != MunchMan.CSUM)
			level += 8;
                }
      */

		maxSpeed = (maxSpeeds[type] * speedScales[level]) >> 7;
		speed = maxSpeed;
		power = 0;
		onBoard = true;
	}

	public static void start() {
		for (int i = 0; i < MAX_GHOSTS; i++) {
			list[i].onBoard = false;
		}
	}

	public static void move() {
		caughtThisCycle = false;

		if (VidGame.getStage() == MunchMan.GS_STARTING && VidGame.getStageTime() == 0)
         {
			Pt den = new Pt();
			board.getDen(den);

			outsidePit.set(den.x + (Board.WORLD_CELL_SIZE * 7)/2,
				den.y - Board.WORLD_CELL_SIZE);

			centerPit.set(outsidePit.x, outsidePit.y + Board.WORLD_CELL_SIZE * 3);

			bounceMinY = centerPit.y - BOUNCE_LIMIT;
			bounceMaxY = centerPit.y + BOUNCE_LIMIT;

			calcReverseDelay();
			ghostsCaptured = 0;
		}

		if (prevStage != parent.GS_CAUGHTONE) {
			reverseFlag = false;
			reverseDelay -= 1024 / VidGame.FPS;
			if (reverseDelay <= 0) {
				reverseFlag = true;
				calcReverseDelay();
			}
		}

		sfxFlags = 0;
		for (int i = 0; i < MAX_GHOSTS; i++) {
			list[i].moveOne();
		}
        prevStage = VidGame.getStage();
	}

    private static int prevStage;

	private void moveOne() {

		if (indivReverseDelay <= 0)
			indivReverseDelay = MyMath.rnd(6000) + 4000;
		indivReverseDelay -= 1024 / VidGame.FPS;

		switch (VidGame.getStage()) {
		case MunchMan.GS_STARTING:
			if (VidGame.getStageTime() == 0) {
//				if (number == 0)
					setOnBoard();
			}
			break;

		case MunchMan.GS_DYING:
			incAnim();
            break;

		case MunchMan.GS_RUNNING:
		case MunchMan.GS_CAUGHTONE:
//			db.a(onBoard,"moveOne() not on board!");
			if (!onBoard) return;
			boolean skip = (prevStage == parent.GS_CAUGHTONE);

			if (!skip && VidGame.getStage() == parent.GS_RUNNING) {
				power -= 1024 / VidGame.FPS;
				if (power < 0)
					power = 0;
			}

			switch (status) {
			 case GHS_WAITING:
			 	if (skip) break;
			    if (VidGame.getStage() == parent.GS_RUNNING) {
			        incAnim();
			        /*  Continue to move up or down, reversing if hit edge. */
			        adjustSpeed(true);
			        moveInDir();
			        if (position.y < bounceMinY)
			            dir = Board.DOWN;
			        else if (position.y >= bounceMaxY)
						dir = Board.UP;

			        /*  Decrement the delay counter, and if it
			            is zero, move out when no longer powered.   */
			        if (moveOutDelay > 0) {
						moveOutDelay -= 1024/VidGame.FPS;
						break;
					}
					if (power != 0) break;

					if (VidGame.getMode() == VidGame.MODE_PREGAME) {
						if (VidGame.getStageTime() < number * 600) break;
					} else {
						if (player.dotsEaten() < number * 10) break;
					}
		            setGhostStatus(GHS_MOVINGOUT);
			    }
			    break;

			 case GHS_MOVINGOUT:
			    if (VidGame.getStage() == parent.GS_RUNNING) {
			        /*  If not in center, move to center.   */
			        if (position.x != outsidePit.x) {
						if (skip) break;
			            dir = position.x < outsidePit.x ? Board.RIGHT : Board.LEFT;
			            moveInDir();
			            /*  If we've now reached the center, start moving up.   */
			            int test = outsidePit.x - position.x;
			            if (dir == Board.LEFT)
			                test = -test;
			            if (test <= 0)
			                position.x = outsidePit.x;
			        } else {
						if (!skip) {
				            /*  Continue moving up. */
				            dir = Board.UP;
				            moveInDir();
				            /*  If we're approaching the outside, switch status.    */
				            if (position.y <= outsidePit.y) {
				                position.y = outsidePit.y;
				                setGhostStatus(GHS_ACTIVE);
								dir = (MyMath.rnd(2) == 0) ? Board.LEFT : Board.RIGHT;
				            }
						}
			            testCatch();
			        }
					if (!skip)
				        incAnim();
			    }
			    break;

			 case GHS_ACTIVE:
			    if (VidGame.getStage() == parent.GS_RUNNING) {
					if (!skip)
			            moveNormalGhost();
			        testCatch();
			    }
			    break;
			 case GHS_EYES:
//			 	Sfx.play(parent.E_EYES);
			    moveEyes();
			    break;
			 case GHS_EYES2:
			    incAnim();
			    /*  If not in center, move to center.   */
			    if (position.y < centerPit.y) {
					dir = Board.DOWN;
			        moveInDir();
			        /*  If we've now reached the center, start moving left
			            or right. */
			        if (position.y >= centerPit.y)
						position.y = centerPit.y;
			    } else {
			        /*  Move left/right to desired starting point.  */
					Pt testPoint = new Pt();
					calcStartLoc(testPoint);
					testPoint.y = centerPit.y;

			        dir = (testPoint.x > position.x) ? Board.RIGHT : Board.LEFT;
					Pt prevLoc = new Pt(position);
			        moveInDir();
			        /*  If now exceeded seek point, reset status.   */
			        if (reachedPosition(prevLoc, testPoint)) {
			            setGhostStatus(GHS_WAITING);
			            moveOutDelay = 800;
						dir = Board.UP;
			        }
			    }
			    break;

			 case GHS_CAUGHT:
			    /*  If no longer doing CAUGHTONE stage, inc status
			        to eyes.    */
			    if (VidGame.getStage() != parent.GS_CAUGHTONE) {
			        if ((flags & GHF_MOVINGOUT) != 0) {
			            setGhostStatus(GHS_EYES2);
			            flags &= ~GHF_MOVINGOUT;
			        } else
			            setGhostStatus(GHS_EYES);
			    }
			    break;
			}
			break;
        }
		if (power != 0)
			sfxFlags |= 1;
		if (status >= GHS_EYES)
			sfxFlags |= 2;
	}


	private void testCatch() {
        if (VidGame.getStage() != parent.GS_RUNNING) return;
		if (VidGame.getMode() != VidGame.MODE_PLAYING) return;
		if (caughtThisCycle) return;

//		if (testCatch(player))

		if (ims[S_BODY].collided(position.x,position.y,player.position.x,player.position.y,player.icon() ))
		{
			caughtThisCycle = true;
			if (power > 0 || INVINCIBLE) {
				Sfx.play(parent.E_EAT);
				VidGame.setStage(parent.GS_CAUGHTONE);
				// If we were moving out, make a note of this so we start at the EYES2
				// stage.
				if (status == GHS_MOVINGOUT)
					flags |= GHF_MOVINGOUT;
				setGhostStatus(GHS_CAUGHT);
				power = 0;
				VidGame.adjScore(200 << ghostsCaptured);
				flags = (short)( (flags & ~GHF_SCOREMASK) | (ghostsCaptured << GHF_SCORESHIF));
				if (ghostsCaptured < 7)
					ghostsCaptured++;
			} else {
				VidGame.setStage(parent.GS_DYING);
			}
		}
	}

	/****************************************************************/
	/*                                                              */
	/*  Adjust speed of ghost                                       */
	/*                                                              */
	/*  The wall_flags are only used to see if ghost is in tunnel.  */
	/*  Pass 0 if assume not in tunnel is ok.                       */
	/*                                                              */
	/****************************************************************/
	private void adjustSpeed(boolean slowInTunnel) {
	    int desiredSpeed;
	    int delta;

        /*  Determine desired speed.  Slow down if in tunnel.   */

        switch (status) {
         case GHS_WAITING:
            speed = VidGame.TICK * 1000;
            break;

         case GHS_EYES:
         case GHS_EYES2:
		 	speed = VidGame.TICK * 5000;
            break;

         default:
            desiredSpeed = (power != 0) ? VidGame.TICK * 900 : VidGame.TICK * 10000;
            if (slowInTunnel) {
				if (board.inTunnel(position, 0))
					desiredSpeed = Math.min(desiredSpeed, VidGame.TICK * 700);
			}
            delta = desiredSpeed - speed;
			speed += MyMath.clamp(delta, -ACCEL, ACCEL);
			speed = MyMath.clamp(speed, 0, maxSpeed);
            break;
        }
	}

	private void incAnim() {
		flags ^= ((flags & 1) != 0) ? 3 : 1;
	}

	private void setGhostStatus(byte status) {
		this.status = status;
	}


	private void moveNormalGhost() {
//		print = (number == 0);

        incAnim();

        /*  Adjust speed according to tunnel flag.  */
        adjustSpeed(true);


        /*  Filter out reversing unless
            a) it's a reversing ghost and it's
                time for him to do a possible
                reverse
            or
            b) the global reverse flag is set
                                                */
        boolean allowRev = false;

        if (
            power == 0
         && (
	     		reverseFlag
			||	(
                    type == 1
                 && indivReverseDelay <= 0
                )
            )
        )
            allowRev = true;

        /*
            Depending upon the ghost's intelligence,
            seek the player every so often.         */

		final int defaultSmarts[] = {
	        100,    /*  Red: smart almost all of the time.  */
	        40,     /*  Blue: pretty dumb                   */
	        75,     /*  Green: smart most of the time       */
	        55,     /*  Purple not too smart                */
		};
        int smarts = defaultSmarts[type];
		int level = Math.min(VidGame.getLevel(), 8);
		smarts += level * 3;

        /*  Add to his smarts if we've been on this level a while.  */

        if (VidGame.getStageTime() > 1024 * 28)
            smarts += 32;
		if (VidGame.getStageTime() > 1024 * 38)
			smarts += 32;

        int rval = MyMath.rnd(128);

        int dist = speed;
		while (dist > 0) {
			if (VERY_SMART)
				allowRev = true;
			int moves = board.detMoves(position);
            if (!allowRev)
                moves = filterReverse(moves, dir);

            if (
				VidGame.getMode() == VidGame.MODE_PLAYING
			 && (smarts > rval || VERY_SMART)
			)
                moves = seek(player.position,moves,0);

			desiredDir = chooseRandomDir(moves);
			dist = super.move(dist);
            allowRev = false;
        }
		animate(12);

	}

	private void plotOne() {
		if (!onBoard) return;
//		db.a(board.defined(),"Player.plot board not defined");
		Pt sLoc = new Pt();
		board.worldToView(position.x,position.y,sLoc);

		// Draw body and feet

		final byte animFrames[] = {
			0,1,2,1,2,1,
			0,1,2,3,4,5,
			0,5,4,5,4,5,
			0,5,4,3,2,1,
		};

		if (status <= GHS_ACTIVE) {
			int colorOffset = type * 7;
			if (power > 0) {
				colorOffset = 4 * 7;
				if (power < 3000 && (power & 0x080) > 0)
					colorOffset = 5 * 7;
			}
			BEngine.drawSprite(ims[colorOffset + S_BODY], sLoc);
			BEngine.drawSprite(ims[colorOffset + S_FEET + animFrames[(frame/2) + dir * 6]],
			 sLoc);
		}

		// Draw the eyes

		if (status  == GHS_CAUGHT) {
			int scoreData = (flags & GHF_SCOREMASK) >> GHF_SCORESHIF;
			int points = 200 << scoreData;
			String str = Integer.toString(points);
			parent.charSet0.centerString(str, sLoc.x, sLoc.y);
		} else
			BEngine.drawSprite(ims[S_EYES + (power > 0 ? 4 : dir)], sLoc);

	}

	public static void plot() {
		switch (VidGame.getStage()) {
		case MunchMan.GS_COMPLETED2:
		case MunchMan.GS_DYING2:
			return;
		}

		for (int i = 0; i<MAX_GHOSTS; i++)
			list[i].plotOne();

	}

	// Apply power up to every ghost (power pill has been eaten)
	public static void powerUp() {

		final int powers[] = {
			13000,
			8000,
			6000,
			4500,
			3400,
			4200,
			2800,
			2000
		};
		int level = VidGame.getLevel();
		if (level >= 8)
			level = (level % 4) + 4;
		int power = powers[level];

		ghostsCaptured = 0;
		for (int i = 0; i < MAX_GHOSTS; i++) {
			Ghost g = list[i];
            if (
                g.status >= GHS_WAITING
             && g.status <= GHS_ACTIVE
            ) {
                g.power = power;
				g.dir ^= 2;
            }
		}

	}

	private void moveEyes() {
		incAnim();
		adjustSpeed(false);
		boolean seekFlag = true;

		Pt prevLoc = new Pt(position);

		int s = speed;
		while (s > 0) {
			int moves = board.detMoves(position);
			moves = filterReverse(moves, dir);
			moves = seek(outsidePit, moves, MyMath.rnd(16) < 10 ? 1 : 0);

			desiredDir = chooseRandomDir(moves);
			s = super.move(s);

	        /*  If he's reached the pit, increment status.  */
	        if (reachedPosition(prevLoc, outsidePit)) {
	            setGhostStatus(GHS_EYES2);
				break;
			}
		}
	}
}
