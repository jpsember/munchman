package MunchMan;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Board {

//	public final static boolean BITMAP_METHOD = true;
	private final static boolean FEW_DOTS = false;
	private final static boolean TESTCLIP = false;

	private	final static int TUNNEL_CELL_EXTENT = 1;
	public final static int UP = 0;
	public final static int RIGHT = 1;
	public final static int DOWN = 2;
	public final static int LEFT = 3;

	// Direction vectors.  0=up 1=right 2=down 3=left
	public final static int yMoves[] = {-1,0,1,0};
	public final static int xMoves[] = {0,1,0,-1};

	protected final static int CELL_PIXELS_X = 14;
	protected final static int CELL_PIXELS_Y = CELL_PIXELS_X;

	public final static int WORLD_CELL_SIZE = CELL_PIXELS_X * BEngine.ONE;
//	public final static int SCREEN_CELL_SIZE = CELL_PIXELS_X;

	protected final static int CELLS_XM = 28;
	protected final static int CELLS_YM = 31;
	protected short cells[];
	protected int dotsRemaining;
	protected final static int MAX_POWER = 4;
	protected byte powerLocs[];
	protected Pt humanStartLoc;
	private static MunchMan parent;
	protected static BEngine ge;
	private static Sprite sprite;
	private boolean defined;
//	private static VidGame vg;
	private int maze, mazeDrawn;
	private boolean notFirstTime;

	protected static final int MAX_TUNNELS = 4;
	protected byte tunnelLocs[];
	protected int totalTunnels;
	protected static final int MAZE_SPR_SET = 14;

	public final static int COLOR_PATH = 0;
	public final static int COLOR_EDGE = 1;
	public final static int COLOR_WALL = 2;
	public final static int COLOR_DOT = 3;
	public final static int COLOR_BGND = 4;

/*
	// !BITMAP_METHOD:
	protected final static int MAX_COLORS = 8;
	protected Color colors[];
	// ------------------
*/
	Sprite cellSprites[];

	private final static int SPR_DOT = 0;
	private final static int SPR_POWERPILL = 1;
	private final static int SPR_GATE = 2;
	private final static int SPR_TOTAL = 3;
	private static Sprite sprites[];

	public boolean defined() {
		return defined;
	}

	public static int width() {
		return CELLS_XM;
	}

	public static int height() {
		return CELLS_YM;
	}

	public static Pt screenSize() {
		return new Pt(CELLS_XM * CELL_PIXELS_X, CELLS_YM * CELL_PIXELS_Y);
	}

	// Adjust object's position to bring him in on the opposite side of the board
	// if he's in a tunnel.
	public static void tunnelWrap(Pt position) {

		if (position.x < -WORLD_CELL_SIZE * TUNNEL_CELL_EXTENT)
			position.x += WORLD_CELL_SIZE * (CELLS_XM + 2*TUNNEL_CELL_EXTENT);
		else if (position.x > WORLD_CELL_SIZE * (CELLS_XM + TUNNEL_CELL_EXTENT))
			position.x -= WORLD_CELL_SIZE * (CELLS_XM + 2*TUNNEL_CELL_EXTENT);
	}
/*
	private boolean deb;
	private void p(String s) {
		if (deb) db.pr(s);
	}
*/
	// Den location & size:
	protected byte denX, denY, denW, denH;

	protected final static short C_PATH	= 0x0010; // path things can move on
	protected final static short C_OUTSIDE	= 0x0020; // wall part of outside
	protected final static short C_DOT		= 0x0040; // normal dot
	protected final static short C_POWER	= 0x0080; // power pill
	protected final static short C_MOVES	= 0x000f; // possible moves

	private final static short sData[] = {
		12,222,4,4,2,2,
		38,219,10,10,5,4,
		70,217,36,4,18,-7,
	};

	public static void init(MunchMan parent) {
		Board.parent = parent;
//		Board.ge = ge;
		sprites = new Sprite[SPR_TOTAL];
		for (int i = 0; i < SPR_TOTAL; i++) {

		/*
			final String sNames[] = {
				"dot",
				"pwrpill",
				"gate",
			};
			final int sCp[] = {
				6,6,
				7,7,
				28,0,
			};
			sprites[i] = new Sprite(sNames[i], sCp[i*2+0], sCp[i*2+1]);
		*/
			int j = i * 6;
			sprites[i] = new Sprite(parent.sprite, sData[j+0], sData[j+1], sData[j+2],
				sData[j+3],sData[j+4],sData[j+5]);
		}

		sprite = new Sprite(parent.sprite,232,192,196,140);	//"mazes");
	}

	// Constructor
	public Board() {
		tunnelLocs = new byte[MAX_TUNNELS * 2];
		cells = new short[CELLS_XM * CELLS_YM];
		powerLocs = new byte[MAX_POWER * 2];
	/*
		if (!BITMAP_METHOD) {
			colors = new Color[MAX_COLORS];
			colors[COLOR_DOT] = new Color(235,235,255);
			colors[COLOR_BGND] = Color.black;
		}
	*/
		humanStartLoc = new Pt();
		defined = false;
	}

	// Plot the board sprites
	public void plot(int animTimer) {
		if (animTimer % 1024 >= 512) return;

		Pt p = new Pt();
		for (int i = 0; i < MAX_POWER; i++) {
			int cx = powerLocs[i*2+0];
			int cy = powerLocs[i*2+1];
			if ((cx|cy) == 0) continue;
			if ((getCell(cx,cy) & C_POWER) == 0) continue;
			cellToView(cx, cy, p);
			BEngine.drawSprite(sprites[SPR_POWERPILL], p);
		}
	}

	// Test for eating a dot.  If so, removes dot from board.
	// Postcondition:
	//	returns type of dot eaten:
	//		0:none
	//		1:normal
	//		2:power
	public int testEatDot(Pt position) {
		if (!atIntersection(position,false))
			return 0;

		Pt cellPt = new Pt();
		worldToCell(position.x,position.y,cellPt);

		if (offScreen(cellPt.x,cellPt.y))
			return 0;

		int contents = getCell(cellPt.x,cellPt.y);
		storeCell(cellPt.x,cellPt.y, contents & ~(C_DOT|C_POWER));
		if ( (contents & (C_POWER | C_DOT)) == 0)
			return 0;

		dotsRemaining--;

		if ((contents & C_POWER) != 0) return 2;
		return 1;
	}

	public boolean dotsRemaining() {
		return (dotsRemaining != 0);
	}

	// Copy human's starting location to dest
	public void getHumanStartLoc(Pt dest) {
//		db.a(defined,"Board.getHumanStartLoc, not defined");
		humanStartLoc.copyTo(dest);
	}

	public void setMaze() {
		final int MAZES_DEFINED = 5;

		int maze = VidGame.getLevel() / 2;
		if (maze >= MAZES_DEFINED) {
			maze = (VidGame.getLevel() - MAZES_DEFINED*2) % MAZES_DEFINED;
		}
      /*
		if (db.COPYPROT) {
                  if (Ghost.cb != MunchMan.CSUM) {
			if (maze >= 3)
				maze = (maze % 3);
                  }
		}
      */
		if (VidGame.getMode() == VidGame.MODE_PREGAME) {
			if (notFirstTime)
				maze = MyMath.rnd(4);
			notFirstTime = true;
		}

		this.maze = maze;

		cellSprites = new Sprite[MAZE_SPR_SET*2];
		for (int i = 0; i < MAZE_SPR_SET*2; i++) {
			cellSprites[i] = new Sprite(
				sprite,
				(i % MAZE_SPR_SET) * CELL_PIXELS_X,
				(maze * (2*CELL_PIXELS_Y)) + (i / MAZE_SPR_SET) * CELL_PIXELS_Y,
				CELL_PIXELS_X,
				CELL_PIXELS_Y
			);
		}

		final byte[] mazes[] = {maze0,maze1,maze2,maze3,maze4};

		storePaths(mazes[maze]);
		examinePaths();
		defined = true;
	}

/*
	// !BITMAP_METHOD:
	public void setColor(int index, int r, int g, int b) {
		db.a(index >= 0 && index < MAX_COLORS,"Board.setColor() index out of range");
		colors[index] = new Color(r,g,b);
	}
*/

	// Clear the cells to empty.  Also clears the power pill locations
	protected void clear() {
		for (int i = 0; i < CELLS_XM * CELLS_YM; i++) {
			cells[i] = 0;
		}
		for (int i = 0; i < MAX_POWER*2; i++) {
			powerLocs[i] = 0;
		}
		dotsRemaining = 0;
	}

	protected final static byte JUMPTO = 101;
	protected final static byte DOTSTO = 102;
	protected final static byte CLEARTO = 103;
	protected final static byte POWER = 104;
	protected final static byte DEN = 105;
	protected final static byte HUMAN = 106;
	protected final static byte END = 119;

	protected int stepValue(int n) {
		if (n == 0) return 0;
		return (n > 0) ? 1 : -1;
	}

	public int getCell(int x, int y) {
		return cells[CELLS_XM * y + x];
	}

	public void storeCell(int x, int y, int value) {
		cells[CELLS_XM * y + x] = (short)value;
	}

	protected void addToCell(int x, int y, int value) {
		cells[CELLS_XM * y + x] |= (short)value;
	}

	// Fill in cells with paths, including dots, from a script
	// Precondition:
	//	script is an array of bytes with the following commands:
	//		JUMPTO	X	Y
	//		DOTSTO	X	Y
	//		CLEARTO X	Y
	//		POWER	X	Y
	//		DEN		X	Y	W	H
	//		HUMAN	X	Y
	//		END
	protected void storePaths(byte script[]) {
		clear();

		int i = 0;
		int sx = 0;
		int sy = 0;
		while (true) {
			byte cmd = script[i++];
			if (cmd == END) break;

			int x = script[i++];
			int y = script[i++];

//			db.pr("cmd="+cmd+", x="+x+", y="+y);

//			db.a(!(x < 0 || x >= CELLS_XM || y < 0 || y > CELLS_YM),"X,Y out of range: "+x+","+y );

			switch (cmd) {
			case JUMPTO:
				sx = x;
				sy = y;
				break;
			case CLEARTO:
			case DOTSTO:
				{
				/*
					if (x != sx && y != sy) {
						db.pr(" invalid!  "+db.p2String(sx,sy)+" to "+db.p2String(x,y));
						sx = x;
						sy = y;
						break;
					}
				*/

					boolean first = true;
					while (sx != x || sy != y) {
						if (!first) {
							sx += stepValue(x - sx);
							sy += stepValue(y - sy);
						}
						first = false;

						if (FEW_DOTS) {
							if (cmd == DOTSTO && (sy < 23 || sx > 14))
								continue;
						}

						addToCell(sx,sy, (cmd == CLEARTO) ?
							C_PATH : (C_PATH | C_DOT) );
					}
				}
				break;

			case POWER:
				if (FEW_DOTS) {
					if (y < 23 || x > 14) break;
				}

				storeCell(x,y,C_PATH|C_POWER|C_DOT);
				break;

			case DEN:
				{
					int w = script[i++];
					int h = script[i++];

					denX = (byte)x;
					denY = (byte)y;
					denW = (byte)w;
					denH = (byte)h;

					/*
					for (int dy = y; dy < y + h; dy++)
						for (int dx = x; dx < x + w; dx++)
							addToCell(dx,dy,C_OUTSIDE);
					*/
				}
				break;
			case HUMAN:
				humanStartLoc.x = ((x * CELL_PIXELS_X) + (CELL_PIXELS_X >> 1)) * BEngine.ONE;
				humanStartLoc.y = (y * CELL_PIXELS_Y) * BEngine.ONE;
				break;
			}
		}
	}

	protected boolean offScreen(int x, int y) {
		return (x < 0 || x >= CELLS_XM || y < 0 || y >= CELLS_YM);
	}

	// Finish constructing the maze.
	// [] fills in movement flags
	// [] marks outside areas as such
	// [] sets initial dot and power pill totals
	protected void examinePaths() {

		// Fill in movement flags and count dots

		int powerTotal = 0;
		totalTunnels = 0;

		int x, y;
		for (y=0; y<CELLS_YM; y++) {
			for (x=0; x<CELLS_XM; x++) {

				int cellFlags = getCell(x,y);

				// Determine directions of movement
				for (int d = 0; d < 4; d++) {
					int nX = x + xMoves[d];
					int nY = y + yMoves[d];

					// If this takes us off the maze, assume it's
					// a tunnel.

					boolean movePossible = offScreen(nX,nY);

					if (!movePossible) {
						if ((getCell(nX,nY) & C_PATH) != 0)
							movePossible = true;
					}

					if (movePossible)
						cellFlags |= (1 << d);

				}
				storeCell(x,y,cellFlags);

				// Add tunnel if this is one.
				if (
					(x == 1 || x == CELLS_XM-2)
				 && ((cellFlags & (2|8)) == (2|8))
				) {
					tunnelLocs[totalTunnels*2+0] = (byte)x;
					tunnelLocs[totalTunnels*2+1] = (byte)y;

					totalTunnels++;
				}

				if ((cellFlags & C_POWER) != 0) {
//					db.a(powerTotal < MAX_POWER, "Board.examinePaths() out of power pills");
					powerLocs[powerTotal * 2] = (byte)x;
					powerLocs[powerTotal * 2 + 1] = (byte)y;
					powerTotal++;
				}

				if ((cellFlags & C_DOT) != 0) {
					dotsRemaining++;
				}

			}
		}

		// Mark the exterior walls as being 'outside'

		for (x = 0; x < CELLS_XM; x++) {
			paintOutside(x,0);
			paintOutside(x,CELLS_YM-1);
		}
		for (y = 0; y < CELLS_YM; y++) {
			paintOutside(0,y);
			paintOutside(CELLS_XM-1,y);
		}
		paintOutside(denX,denY);

	}

	// Mark exterior walls as being 'outside'.  Uses recursion.
	// This may use a lot of stack space!
	protected void paintOutside(int x, int y) {
		if (offScreen(x,y) || (getCell(x,y) & (C_OUTSIDE | C_PATH)) != 0)
			return;

		addToCell(x,y,C_OUTSIDE);

		for (int d = 0; d < 4; d++) {
			paintOutside(x + xMoves[d], y + yMoves[d]);
		}
	}


	// Plot any changes that have occurred with the board
	// Precondition:
	//	drawn = board object containing last drawn state
	//	valid = false if it should be assumed that last drawn state
	//				is completely invalid
	public void plotChanges(Board drawn, boolean valid) {
//		db.a(defined,"Board.plotChanges, not defined");
		if ((maze | 0x80) != mazeDrawn)
			valid = false;

		mazeDrawn = maze | 0x80;
		Rectangle r = new Rectangle();

		for (int y = 0; y < CELLS_YM; y++) {
			for (int x = 0; x < CELLS_XM; x++) {
				int contents = getCell(x,y);
				if (!valid || contents != drawn.getCell(x,y)) {
					// Calculate view bounds of a cell
					r.setBounds(CELL_PIXELS_X * x, CELL_PIXELS_Y * y,
						CELL_PIXELS_X, CELL_PIXELS_Y);

					BEngine.updateRect(r.x,r.y,r.width,r.height);

					BEngine.disableUpdate(1);
					if ((contents & C_PATH) != 0) {

//						if (BITMAP_METHOD)
						{
							BEngine.drawSprite(cellSprites[0], r.x, r.y);
						}
/*
						else {
							g.setColor(colors[COLOR_PATH]);
							g.fillRect(r.x + ge.viewR.x, r.y + ge.viewR.y,
								r.width,r.height);
						}
*/
						// Plot dots if required

						if ((contents & C_POWER) != 0) {
						} else if ((contents & C_DOT) != 0) {
                            BEngine.drawSprite(sprites[SPR_DOT],
								r.x + CELL_PIXELS_X/2,
								r.y + CELL_PIXELS_Y/2);
						}
					} else {
						plotWall(contents, x,y, r);
					}
					BEngine.disableUpdate(-1);

					drawn.storeCell(x,y,contents);
				}
			}
		}

		if (!valid) {
			BEngine.drawSprite(sprites[SPR_GATE],
				(denX + denW/2) * CELL_PIXELS_X, denY * CELL_PIXELS_Y);
		}
	}


	// Determine if a cell is off the board or contains a wall
	private boolean wallAt(int x, int y) {
		return offScreen(x,y) ||
			((getCell(x,y) & C_PATH) == 0);
	}

/*
	private static final byte CMD_COLOR = 30;
	private static final byte CMD_FILL = 31;
	private static final byte CMD_END = 99;

	// Rotate a point through a multiple of 90 degrees
	// Precondition:
	//	x,y = point to rotate
	//	rotFactor = number of 90 degree rotations to perform
	//	translation = this offset is added if rotated coordinate becomes
	//					negative
	//	dest = rotated point returned here
	private void rotatePoint(int x, int y, int rotFactor,
		int translation, Pt dest) {

		final int mat[] = {
			1,0,0,0,1,0,
			0,-1,1,1,0,0,
			-1,0,1,0,-1,1,
			0,1,0,-1,0,1 };

		int m = rotFactor * 6;

		dest.x = mat[m+0] * x + mat[m+1] * y + mat[m+2] * translation;
		dest.y = mat[m+3] * x + mat[m+4] * y + mat[m+5] * translation;

	}

	private final byte vSolid[] = {
		CMD_COLOR, COLOR_BGND, CMD_FILL,
		CMD_END
	};
	private final byte N = 9;
	private final byte vHorz[] = {
		CMD_COLOR,COLOR_PATH,CMD_FILL,
		CMD_COLOR,COLOR_EDGE,
		0,9,15,9,
		0,10,15,10,
		CMD_COLOR,COLOR_WALL,
		0,11,15,11,
		0,12,15,12,
		CMD_COLOR,COLOR_BGND,
		0,13,15,13,
		0,14,15,14,
		0,15,15,15,
		CMD_END
	};
	private final byte vVert[] = {
		CMD_COLOR,COLOR_PATH,CMD_FILL,
		CMD_COLOR,COLOR_EDGE,
		9,0,9,15,
		10,0,10,15,
		CMD_COLOR,COLOR_WALL,
		11,0,11,15,
		12,0,12,15,
		CMD_COLOR,COLOR_BGND,
		13,0,13,15,
		14,0,14,15,
		15,0,15,15,
		CMD_END
	};
	private final byte vConvex[] = {
		CMD_COLOR,COLOR_PATH,CMD_FILL,
		CMD_COLOR,COLOR_EDGE,
		9,14,9,15,
		10,12,10,15,
		11,11,11,13,
		12,10,12,12,
		13,10,13,11,
		14,9,15,9,
		14,10,15,10,
		CMD_COLOR,COLOR_WALL,
		11,14,14,11,
		11,15,15,11,
		12,15,15,12,
		CMD_COLOR,COLOR_BGND,
		13,15,15,15,
		14,14,15,14,
		15,13,15,13,
		CMD_END
	};
	private final byte vConcave[] = {
		CMD_COLOR,COLOR_BGND,CMD_FILL,
		CMD_COLOR,COLOR_PATH,
		0,0,8,0,
		0,1,8,1,
		0,2,7,2,
		0,3,7,3,
		0,4,6,4,
		0,5,5,5,
		0,6,4,6,
		0,7,3,7,
		0,8,1,8,
		CMD_COLOR,COLOR_EDGE,
		1,10,10,1,
		3,9,9,3,0,10,2,8,
		0,9,0,9,
		10,0,8,2,
		9,0,9,0,
		5,8,8,5,
		CMD_COLOR,COLOR_WALL,
		5,10,10,5,
		3,11,11,3,
		1,12,4,9,
		12,1,9,4,
		0,12,2,10,
		12,0,10,2,
		0,11,0,11,
		11,0,11,0,
		CMD_END
	};
*/

	// Plot a wall section.

	protected void plotWall(int contents,
		int x, int y, Rectangle rV) {

//		db.a(defined,"Board.plotWall, maze not defined");
//		db.pr("plotWall g="+g+" rect="+rV);

		Rectangle r = new Rectangle();
//		db.a(ge != null,"642");
//		db.a(ge.viewR != null,"643");
		r.setBounds(rV.x + BEngine.viewR.x, rV.y + BEngine.viewR.y, rV.width, rV.height);

		// Offsets for each neighbor, starting with right,
		// moving clockwise:
		final int xOffset[] = {1,1,0,-1,-1,-1,0,1};
		final int yOffset[] = {0,1,1,1,0,-1,-1,-1};

		int rotFactor;
		Pt p = new Pt();
		Pt p1 = new Pt();

		// Determine which of the 8 neighbors have walls.
		// Set bit 0=bottom left, and so on clockwise to bit 7=bottom.

//		db.pr(" det which have neighbors");
		int wallFlags = 0;
		for (int j = 0; j < 8; j++) {
			if (wallAt(x + xOffset[j], y + yOffset[j]))
				wallFlags |= (1 << ((j+5) & 7));
		}
//		db.pr(" wallFlags = "+wallFlags);

		// Determine the 90 degree rotation that brings us to
		// a point where the bottom, right, and bottom right
		// neighbors are also walls.
		// If the rotated pattern is found in the illegal list,
		// we can't plot a proper wall.
		// If the rotated pattern is found in the rotate list,
		// we should try another rotation.
		// Otherwise, process the rotated pattern.

rot:	for (rotFactor = 0; rotFactor < 4; rotFactor++) {
			if (rotFactor != 0) {
				wallFlags = ((wallFlags << 2) | (wallFlags >> 6)) & 0xff;
			}

			// If we are missing walls in the bottom, right, and bottom
			// right, continue rotating.

			if ((wallFlags & 0xe0) != 0xe0) continue;

			final byte rotFlags[] = {0x02,0x08,0x0a,0x0b,0x0e,
									0x0f,0x12,0x16,0x1a,0x1e};

			for (int j=0; j<rotFlags.length; j++) {
				if ((byte)(wallFlags & 0x1f) == rotFlags[j])
					continue rot;
			}
			break;
		}
//		db.pr(" rotFactor="+rotFactor);


//		db.a(rotFactor != 4, "Board.plotWall: maze is invalid!");

		// Examine the upper and left neighbors of the rotated
		// cell to determine which type of bitmap to plot.


		{
			final int shapeCodes[] = {3,1,3,1,4,2,4,0};
			int code = shapeCodes[(wallFlags >> 1) & 7];

			// We have to rotate in the reverse direction to convert
			// edge space back to screen space.
			int plotRotFactor = (4 - rotFactor) & 3;
			final int sInd[] = {
				1,1,1,1,
				2,3,4,5,
				6,9,8,7,
				10,13,12,11,
				3,4,5,4,
			};
			int sIndex = sInd[code * 4 + plotRotFactor];

			if ((contents & C_OUTSIDE) != 0)
				sIndex += MAZE_SPR_SET;

//			db.pr(" attempt to plot sprite "+sIndex);
			BEngine.drawSprite(cellSprites[sIndex], rV.x, rV.y);
		}
	}

	public static void cellToView(int cx, int cy, Pt loc) {
		loc.x = cx * CELL_PIXELS_X + CELL_PIXELS_X/2;
		loc.y = cy * CELL_PIXELS_Y + CELL_PIXELS_Y/2;
	}

	public static void worldToView(int cx, int cy, Pt loc) {
		loc.x = (cx >> BEngine.FRACBITS) + CELL_PIXELS_X/2;
		loc.y = (cy >> BEngine.FRACBITS) + CELL_PIXELS_Y/2;

		if (TESTCLIP) {
			// !!!!!!! Test that clipping is working correctly by making it relative to 0,0:
			loc.x -= BEngine.viewR.x;
			loc.y -= BEngine.viewR.y;
		}

	}

	// Convert cell to world coordinates
	public static void cellToWorld(int cx, int cy, Pt loc) {
		loc.x = cx * WORLD_CELL_SIZE;
		loc.y = cy * WORLD_CELL_SIZE;
	}

	// Convert world to cell coordinates
	public static void worldToCell(int cx, int cy, Pt loc) {
		loc.x = cx / WORLD_CELL_SIZE;
		loc.y = cy / WORLD_CELL_SIZE;
	}

	// Determine what moves are available from a location
	// Precondition:
	//	pos = location, in world coordinates
	// Postcondition:
	//	int returned, with bits for possible movement:
	//		0:	up
	//		1:	right
	//		2:	down
	//		3:	left
	public int detMoves(Pt pos) {
		int moves = 0;

		// If off the screen, assume we're in a tunnel.

		if (pos.x < 0 || pos.x >= WORLD_CELL_SIZE * CELLS_XM)
 	    	return (1 << 1) | (1 << 3);

		int xRem = MyMath.mod(pos.x, WORLD_CELL_SIZE);
		int yRem = MyMath.mod(pos.y, WORLD_CELL_SIZE);

		if (yRem != 0)
			return (1 << UP) | (1 << DOWN);
		if (xRem != 0)
			return (1 << LEFT) | (1 << RIGHT);

		int cx = pos.x / (WORLD_CELL_SIZE);
		int cy = pos.y / (WORLD_CELL_SIZE);

		return (getCell(cx,cy) & C_MOVES);
	}

	// Determine if we are at an intersection.
	// Precondition:
	//	halfXFlag = true to includes 1/2 cell values in x as an intersection,
	//	 for manipulating the den.
	public static boolean atIntersection(Pt pos, boolean halfXFlag) {
		int xSize = halfXFlag ? WORLD_CELL_SIZE/2 : WORLD_CELL_SIZE;
		return (MyMath.mod(pos.x,xSize) == 0 && MyMath.mod(pos.y,WORLD_CELL_SIZE) == 0);
	}

	public static boolean movePossible(int dir, int dirFlags) {
		return ((1 << dir) & dirFlags) != 0;
	}

	public void getDen(Pt pos) {
		cellToWorld(denX, denY, pos);
	}

	public boolean inTunnel(Pt pos, int depth) {
		int testX = WORLD_CELL_SIZE * 1;

		if (pos.x > testX - depth) {
			testX = WORLD_CELL_SIZE * (CELLS_XM - 1);
			if (pos.x < testX + depth)
				return false;
		}

		for (int i = 0; i < totalTunnels; i++) {
			if ((pos.y / WORLD_CELL_SIZE) != tunnelLocs[i*2+1]) continue;
			if (testX ==  tunnelLocs[i*2+0] * WORLD_CELL_SIZE) return true;
		}
		return false;
	}

	// Calculate coordinates of inside of a tunnel
	// Precondition:
	//	side = 0: emerging from left; 1:from right
	// Postcondition:
	//	location, in world space, returned in pos
	public void chooseRandomTunnel(int side, Pt pos) {
//		db.a(totalTunnels > 0, "Board.chooseRandomTunnel, total = 0");

		int n = MyMath.rnd(totalTunnels);

		cellToWorld(side == 0 ? 0 - TUNNEL_CELL_EXTENT : CELLS_XM - 1 + TUNNEL_CELL_EXTENT,
			tunnelLocs[n*2+1], pos);
	}

	protected final static byte maze0[] = {
		JUMPTO,1,1,
		DOTSTO,12,1,
		DOTSTO,12,5,
		DOTSTO,15,5,
		DOTSTO,15,1,
		DOTSTO,26,1,
		DOTSTO,26,8,
		DOTSTO,21,8,
		DOTSTO,21,20,
		DOTSTO,26,20,
		DOTSTO,26,23,
		DOTSTO,24,23,
		DOTSTO,24,26,
		DOTSTO,26,26,
		DOTSTO,26,29,
		DOTSTO,1,29,
		DOTSTO,1,26,
		DOTSTO,3,26,
		DOTSTO,3,23,
		DOTSTO,1,23,
		DOTSTO,1,20,
		DOTSTO,6,20,
		DOTSTO,6,8,
		DOTSTO,1,8,
		DOTSTO,1,1,

		JUMPTO,1,5,
		DOTSTO,26,5,
		JUMPTO,6,1,
		DOTSTO,6,26,
		JUMPTO,21,1,
		DOTSTO,21,26,
		JUMPTO,9,5,
		DOTSTO,9,8,
		DOTSTO,12,8,
		CLEARTO,12,11,
		CLEARTO,15,11,
		CLEARTO,15,8,
		DOTSTO,18,8,
		DOTSTO,18,5,
		JUMPTO,9,11,
		CLEARTO,18,11,
		CLEARTO,18,17,
		CLEARTO,9,17,
		CLEARTO,9,11,
		JUMPTO,9,14,
		CLEARTO,0,14,
		JUMPTO,18,14,
		CLEARTO,27,14,
		JUMPTO,9,17,
		CLEARTO,9,20,
		JUMPTO,18,17,
		CLEARTO,18,20,

		JUMPTO,6,20,
		DOTSTO,12,20,
		DOTSTO,12,23,
		DOTSTO,6,23,
		JUMPTO,9,23,
		DOTSTO,9,26,
		DOTSTO,12,26,
		DOTSTO,12,29,
		JUMPTO,3,26,
		DOTSTO,6,26,

		JUMPTO,21,20,
		DOTSTO,15,20,
		DOTSTO,15,23,
		DOTSTO,21,23,
		JUMPTO,18,23,
		DOTSTO,18,26,
		DOTSTO,15,26,
		DOTSTO,15,29,
		JUMPTO,24,26,
		DOTSTO,21,26,

		JUMPTO,12,23,
		CLEARTO,15,23,

		DEN, 10,12,8,5,

		HUMAN,13,23,

		POWER,1,3,
		POWER,26,3,
		POWER,1,23,
		POWER,26,23,

		END
	};

	protected final static byte maze1[] = {
		DEN, 10,12,8,5,
		HUMAN,13,23,
		POWER,1,3,POWER,26,3,POWER,1,27,POWER,26,27,

		JUMPTO,1,4,
		DOTSTO,1,1,
		DOTSTO,7,1,
		DOTSTO,7,4,
		JUMPTO,10,4,
		DOTSTO,10,1,DOTSTO,17,1,DOTSTO,17,4,
		JUMPTO,20,4,DOTSTO,20,1,DOTSTO,26,1,DOTSTO,26,4,DOTSTO,1,4,

		JUMPTO,3,4,DOTSTO,3,23,JUMPTO,24,4,DOTSTO,24,23,
		JUMPTO,12,23,DOTSTO,12,26,DOTSTO,10,26,DOTSTO,10,29,DOTSTO,1,29,DOTSTO,1,23,DOTSTO,12,23,
		JUMPTO,7,23,DOTSTO,7,29,

		JUMPTO,12,23,CLEARTO,15,23,DOTSTO,26,23,DOTSTO,26,29,DOTSTO,17,29,DOTSTO,17,26,DOTSTO,15,26,DOTSTO,15,23,
		JUMPTO,20,23,DOTSTO,20,29,DOTSTO,10,29,

		JUMPTO,6,4,DOTSTO,6,8,DOTSTO,12,8,DOTSTO,12,4,
		JUMPTO,15,4,DOTSTO,15,8,DOTSTO,21,8,DOTSTO,21,4,
		JUMPTO,9,8,DOTSTO,9,11,DOTSTO,3,11,
		JUMPTO,18,8,DOTSTO,18,11,DOTSTO,24,11,
		JUMPTO,18,11,CLEARTO,9,11,DOTSTO,9,17,DOTSTO,18,17,DOTSTO,18,11,
		JUMPTO,0,8,CLEARTO,3,8,JUMPTO,24,8,CLEARTO,27,8,
		JUMPTO,0,17,CLEARTO,3,17,JUMPTO,24,17,CLEARTO,27,17,
		JUMPTO,3,17,DOTSTO,6,17,DOTSTO,6,14,DOTSTO,9,14,JUMPTO,3,20,DOTSTO,12,20,DOTSTO,12,17,
		JUMPTO,10,20,DOTSTO,10,23,
		JUMPTO,15,17,DOTSTO,15,20,DOTSTO,24,20,JUMPTO,17,20,DOTSTO,17,23,
		JUMPTO,18,14,DOTSTO,21,14,DOTSTO,21,17,DOTSTO,24,17,
		END
	};

	protected final static byte maze2[] = {
		DEN, 10,12,8,5,
		HUMAN,13,23,
		POWER,1,6,POWER,26,6,POWER,1,28,POWER,26,28,
		JUMPTO,6,4,CLEARTO,6,1,CLEARTO,0,1,JUMPTO,21,4,CLEARTO,21,1,CLEARTO,27,1,
		JUMPTO,3,23,CLEARTO,0,23,JUMPTO,24,23,CLEARTO,27,23,

		JUMPTO,9,4,DOTSTO,9,1,DOTSTO,18,1,DOTSTO,18,4,

		JUMPTO,1,4,DOTSTO,12,4,DOTSTO,12,8,DOTSTO,15,8,DOTSTO,15,4,DOTSTO,26,4,DOTSTO,26,10,
		DOTSTO,21,10,DOTSTO,21,13,DOTSTO,26,13,DOTSTO,26,16,DOTSTO,24,16,
		DOTSTO,24,26,DOTSTO,26,26,
		DOTSTO,26,29,DOTSTO,1,29,DOTSTO,1,26,DOTSTO,3,26,DOTSTO,3,16,DOTSTO,1,16,DOTSTO,1,13,DOTSTO,6,13,DOTSTO,6,10,DOTSTO,1,10,DOTSTO,1,4,

		JUMPTO,4,10,DOTSTO,4,7,DOTSTO,9,7,DOTSTO,9,4,DOTSTO,12,4,DOTSTO,12,8,DOTSTO,15,8,DOTSTO,15,4,DOTSTO,18,4,DOTSTO,18,7,DOTSTO,23,7,DOTSTO,23,10,
		JUMPTO,6,13,DOTSTO,6,15,DOTSTO,9,15,DOTSTO,9,7,JUMPTO,9,11,CLEARTO,18,11,DOTSTO,18,7,DOTSTO,18,15,DOTSTO,21,15,DOTSTO,21,13,
		JUMPTO,6,15,DOTSTO,6,20,DOTSTO,3,20,DOTSTO,11,20,DOTSTO,11,17,DOTSTO,16,17,DOTSTO,16,20,DOTSTO,21,20,DOTSTO,21,15,
		JUMPTO,9,15,DOTSTO,9,17,DOTSTO,18,17,DOTSTO,18,15,
		JUMPTO,6,20,DOTSTO,3,20,DOTSTO,3,23,DOTSTO,6,23,DOTSTO,6,26,DOTSTO,9,26,DOTSTO,9,23,DOTSTO,11,23,DOTSTO,11,20,JUMPTO,16,20,DOTSTO,16,23,DOTSTO,18,23,DOTSTO,18,26,DOTSTO,21,26,DOTSTO,21,23,DOTSTO,24,23,DOTSTO,24,20,DOTSTO,21,20,
		JUMPTO,6,26,DOTSTO,6,29,DOTSTO,12,29,DOTSTO,12,26,DOTSTO,9,26,JUMPTO,21,29,DOTSTO,21,26,DOTSTO,15,26,DOTSTO,15,29,JUMPTO,11,23,CLEARTO,16,23,
		END
	};

	protected final static byte maze3[] = {
		DEN, 10,12,8,5,
		HUMAN,13,23,
		POWER,1,3,POWER,26,3,POWER,1,24,POWER,26,24,
		JUMPTO,0,8,CLEARTO,1,8,JUMPTO,26,8,CLEARTO,27,8,JUMPTO,9,11,CLEARTO,18,11,JUMPTO,12,23,CLEARTO,15,23,

		JUMPTO,4,5,DOTSTO,1,5,DOTSTO,1,1,DOTSTO,9,1,DOTSTO,9,4,JUMPTO,18,4,DOTSTO,18,1,DOTSTO,26,1,DOTSTO,26,5,DOTSTO,23,5,
		JUMPTO,9,11,DOTSTO,4,11,DOTSTO,4,4,DOTSTO,12,4,DOTSTO,12,1,DOTSTO,15,1,DOTSTO,15,4,DOTSTO,23,4,DOTSTO,23,11,DOTSTO,18,11,
		JUMPTO,7,4,DOTSTO,7,8,DOTSTO,20,8,DOTSTO,20,4,JUMPTO,12,4,DOTSTO,12,8,DOTSTO,15,8,DOTSTO,15,4,
		JUMPTO,9,8,DOTSTO,9,17,JUMPTO,18,8,DOTSTO,18,17,
		JUMPTO,6,11,DOTSTO,6,14,DOTSTO,4,14,DOTSTO,4,17,DOTSTO,23,17,DOTSTO,23,14,DOTSTO,21,14,DOTSTO,21,11,
		JUMPTO,4,8,DOTSTO,1,8,DOTSTO,1,14,DOTSTO,4,14,JUMPTO,23,8,DOTSTO,26,8,DOTSTO,26,14,DOTSTO,23,14,
		JUMPTO,1,14,DOTSTO,1,20,DOTSTO,7,20,DOTSTO,7,17,JUMPTO,26,14,DOTSTO,26,20,DOTSTO,20,20,DOTSTO,20,17,
		JUMPTO,4,20,DOTSTO,4,23,DOTSTO,1,23,DOTSTO,1,29,DOTSTO,7,29,DOTSTO,7,23,DOTSTO,12,23,DOTSTO,12,26,DOTSTO,10,26,DOTSTO,10,29,DOTSTO,17,29,DOTSTO,17,26,DOTSTO,15,26,DOTSTO,15,23,DOTSTO,20,23,DOTSTO,20,29,DOTSTO,26,29,DOTSTO,26,23,DOTSTO,23,23,DOTSTO,23,20,
		JUMPTO,1,26,DOTSTO,7,26,DOTSTO,7,17,JUMPTO,12,17,DOTSTO,12,20,DOTSTO,10,20,DOTSTO,10,23,JUMPTO,15,17,DOTSTO,15,20,DOTSTO,17,20,DOTSTO,17,23,JUMPTO,20,20,DOTSTO,20,26,DOTSTO,26,26,

		END
	};

	protected final static byte maze4[] = {
		DEN, 10,12,8,5,
		HUMAN,13,23,
		POWER,1,3,POWER,26,3,POWER,1,27,POWER,26,27,
		JUMPTO,0,14,CLEARTO,3,14,DOTSTO,3,8,DOTSTO,1,8,DOTSTO,1,1,DOTSTO,26,1,DOTSTO,26,8,DOTSTO,21,8,DOTSTO,21,5,DOTSTO,18,5,DOTSTO,18,8,DOTSTO,15,8,DOTSTO,15,11,CLEARTO,12,11,DOTSTO,12,8,DOTSTO,9,8,DOTSTO,9,5,DOTSTO,6,5,DOTSTO,6,8,DOTSTO,3,8,
		JUMPTO,24,8,DOTSTO,24,14,CLEARTO,27,14,JUMPTO,27,17,CLEARTO,24,17,DOTSTO,24,23,DOTSTO,26,23,DOTSTO,26,29,DOTSTO,15,29,DOTSTO,15,26,DOTSTO,12,26,DOTSTO,12,29,DOTSTO,1,29,DOTSTO,1,23,DOTSTO,3,23,DOTSTO,3,17,CLEARTO,0,17,
		JUMPTO,3,11,DOTSTO,6,11,DOTSTO,6,14,DOTSTO,9,14,DOTSTO,9,17,DOTSTO,12,17,DOTSTO,12,20,CLEARTO,12,23,CLEARTO,15,23,CLEARTO,15,20,DOTSTO,15,17,DOTSTO,12,17,
		DOTSTO,18,17,DOTSTO,18,11,DOTSTO,15,11,DOTSTO,15,4,DOTSTO,12,4,DOTSTO,12,11,DOTSTO,9,11,DOTSTO,9,14,
		JUMPTO,4,1,DOTSTO,4,5,DOTSTO,9,5,DOTSTO,9,1,DOTSTO,18,1,DOTSTO,18,5,DOTSTO,23,5,DOTSTO,23,1,
		JUMPTO,24,8,DOTSTO,24,14,DOTSTO,24,11,DOTSTO,21,11,DOTSTO,21,14,DOTSTO,18,14,DOTSTO,21,14,DOTSTO,21,20,DOTSTO,24,20,JUMPTO,21,20,DOTSTO,15,20,DOTSTO,18,20,
		DOTSTO,18,26,DOTSTO,9,26,DOTSTO,9,20,DOTSTO,12,20,DOTSTO,3,20,DOTSTO,6,20,DOTSTO,6,14,JUMPTO,6,20,DOTSTO,3,20,JUMPTO,3,23,DOTSTO,9,23,JUMPTO,6,23,DOTSTO,6,26,DOTSTO,4,26,DOTSTO,4,29,
		JUMPTO,18,23,DOTSTO,24,23,JUMPTO,21,23,DOTSTO,21,26,DOTSTO,23,26,DOTSTO,23,29,

		END
	};

}
