package MunchMan;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Fruit extends Obj {

	private final static int MAX_FRUIT = 1;
	private final static int MAX_TYPES = 16;

	private final static int THIEF = 64;

	private static Board board;
//	private static BEngine ge;
	private static MunchMan parent;
	private static Player player;
	private static int delayNextFruit;		// delay before bringing out fruit
	private static int nextFruitType;
	private static int activeFruitTotal;
	private static int fruitCount;

	private int status;			// S_x
	private int maxSpeed;
	private int type;
	private int aliveTime;
	private boolean inTunnelFlag;
	private Pt exitLoc;
	private int value;

	private final static int S_ACTIVE = 1;
	private final static int S_CAUGHT = 2;

	private static Fruit list[];

	private static Sprite ims[];
	private final static int SPR_TOTAL = MAX_TYPES;

	public Fruit() {
		super(board, parent);
		wrapInTunnels = false;
		exitLoc = new Pt();
	}

	public static void init(Board board, MunchMan parent, Player player) {
		Fruit.board = board;
//		Fruit.ge = ge;
		Fruit.parent = parent;
		Fruit.player = player;

		list = new Fruit[MAX_FRUIT];
		for (int i = 0; i < MAX_FRUIT; i++)
			list[i] = new Fruit();

//		Sprite base = new Sprite("fruit",0,0);
		ims = new Sprite[SPR_TOTAL];
		for (int i = 0; i < SPR_TOTAL; i++) {
			int x = (i & 7) * 28;
			int y = (i >> 3) * 28 + 240;
			ims[i] = new Sprite(parent.sprite, x, y, 28,28, 14, 14);
			if (i == 0) {
				setCollision(ims[i]);
//				ims[i].addColRect(5,10,27-5,27-10);
//				ims[i].addColRect(10,5,27-10,27-5);
			}
		}
	}

	public static void prepareNewLevel() {
		fruitCount = 0;
	}

	public static void start() {
		removeAll();
	}

	public static void move() {
		// If player starting to move, select
		if (VidGame.getStage() == MunchMan.GS_STARTING &&
         VidGame.getStageTime() == 0 ) {
			removeAll();
			setDelay();
		}

		if (
			activeFruitTotal == 0
		 && player.dotsEaten() >= 20
		) {
			if (delayNextFruit != 0) {
				delayNextFruit -= 1024 / VidGame.FPS;
				if (delayNextFruit <= 0) {
					// Find a free fruit.
					Fruit f = findFree();

					// Until thief is implemented, don't add it.
					if (nextFruitType != THIEF)
						f.setOnBoard();
					setDelay();
				}
			}
		}

		for (int i = 0; i < MAX_FRUIT; i++)
			list[i].moveOne();
	}

	public static void plot() {
		switch (VidGame.getStage()) {
		case MunchMan.GS_COMPLETED2:
		case MunchMan.GS_DYING2:
			return;
		}

		for (int i = 0; i<MAX_FRUIT; i++)
			list[i].plotOne();

	}

	// private members:

	private void setOnBoard() {
		type = nextFruitType;

		int side = MyMath.rnd(2);

		dir = (side == 0) ? Board.RIGHT : Board.LEFT;
		setStatus(S_ACTIVE);

		maxSpeed = BEngine.TICK * 800;
		speed = maxSpeed;

		// Place fruit inside a tunnel.

		board.chooseRandomTunnel(side, position);
		inTunnelFlag = true;

		// Determine exit tunnel location.
		exitLoc.x = board.WORLD_CELL_SIZE * board.width() - position.x;
		exitLoc.y = position.y;

		aliveTime = 0;
		onBoard = true;
		activeFruitTotal++;
		fruitCount++;
	}

	private static void removeAll() {
		for (int i = 0; i < MAX_FRUIT; i++) {
			list[i].onBoard = false;
		}
		activeFruitTotal = 0;
	}

	private final static int MAX_FRUIT_LEVEL = 3;


	private static void setDelay() {
		int level = VidGame.getLevel();
		final int THIEF_LEVEL = 2;

		// Set up delay for thief initially.

		int dIndex = fruitCount;
		if (dIndex > 2)
			dIndex = 2;

		final int del[] = {15000,10000,6000};
		delayNextFruit = MyMath.rnd(5000) + del[dIndex];
		nextFruitType = THIEF;

		if (fruitCount < MAX_FRUIT_LEVEL) {
			int thiefNumber = 2;
			if (level >= THIEF_LEVEL && ((level & 1) == 1))
				thiefNumber = 1;

			nextFruitType = typeForLevel(level);
//			if (level >= MAX_TYPES)
//				nextFruitType = MyMath.rnd(MAX_TYPES);

			if (thiefNumber == fruitCount)
				nextFruitType = THIEF;
		}

		if (nextFruitType == THIEF && level < THIEF_LEVEL)
			delayNextFruit = 0;

	}

	public static void drawMisc(int type, int x, int y) {
		BEngine.drawSprite(ims[type], x, y);
	}

	public static int typeForLevel(int level) {
		int type = level;
		if (level >= MAX_TYPES) {
			final int rnd_types[] = {
				0,14,2,12,4,15,3,13,5,10,6,9,8,11,1,7
			};
			type = rnd_types[(level - MAX_TYPES) % MAX_TYPES];
		}
		return type;
	}

	private static void setDelay0() {

		final byte info[] = {
		    0,10,0,10,0,0,
		    1,10,1,10,0,0,
		    2,10,2,10,(byte)THIEF,15,
		    3,10,3,10,(byte)THIEF,5,

		    4,10,(byte)THIEF,5,4,10,
		    5,10,5,10,(byte)THIEF,5,
		    6,10,(byte)THIEF,10,6,10,
		    7,10,7,10,(byte)THIEF,10,

		    8,10,8,10,(byte)THIEF,5,
		    9,5,(byte)THIEF,5,9,5,
		    10,12,10,12,(byte)THIEF,5,
		    11,10,(byte)THIEF,7,11,13,
		    12,12,12,12,(byte)THIEF,5,
		    13,10,(byte)THIEF,7,13,13,
		    14,10,14,13,(byte)THIEF,7,
		    15,12,15,10,(byte)THIEF,8,
		};


		delayNextFruit = 0;
		if (fruitCount < MAX_FRUIT_LEVEL) {
			int level = VidGame.getLevel();
			if (level > MAX_TYPES) {
				level = (level % (MAX_TYPES/2)) + MAX_TYPES/2;
			}

			delayNextFruit = info[fruitCount * 2 + 1] * 1024;
			nextFruitType = info[fruitCount * 2 + 0];
        } else {
            /*  If there were other thieves, add another.   */
			if (VidGame.getLevel() >= 2) {
	            delayNextFruit = MyMath.rnd(5000) + 1024 * 8;
				nextFruitType = THIEF;
			}
        }

	}

	private static Fruit findFree() {
		for (int i = 0; i < MAX_FRUIT; i++)
			if (!list[i].onBoard)
				return list[i];
		return null;
	}

	private void moveOne() {

		if (!onBoard) return;


		switch (VidGame.getStage()) {
		case MunchMan.GS_DYING:
		case MunchMan.GS_RUNNING:
		case MunchMan.GS_CAUGHTONE:
			switch (status) {
			 case S_ACTIVE:
			    if (VidGame.getStage() == parent.GS_RUNNING) {
		            moveActive();
			        testCatch();
			    }
			    break;
			 case S_CAUGHT:
				aliveTime += 1024/VidGame.FPS;
				if (aliveTime >= 2200) {
					onBoard = false;
					activeFruitTotal--;
				}
			    break;
			}
			break;
		case MunchMan.GS_DYING2:
			onBoard = false;
			break;
        }
	}


	private void testCatch() {
        if (VidGame.getStage() != parent.GS_RUNNING) return;
		if (VidGame.getMode() != VidGame.MODE_PLAYING) return;

//		if (testCatch(player))
		if (ims[0].collided(position.x,position.y,player.position.x,player.position.y,player.icon() ))
		{
			setStatus(S_CAUGHT);

			aliveTime = 0;

			Sfx.play(parent.E_FRUIT);

			// Determine point value for this fruit.

			final int values[] = {
				200,500,750,1000,
				1200,1500,2500,5000,
				3000,6000,7500,4500,
				6500,8500,9000,10000
			};
			if (type == THIEF)
				value = 1000;
			else
				value = values[type];
			VidGame.adjScore(value);
		}
	}

	private void setStatus(int status) {
		this.status = status;
	}

	final static int ARC_FRAMES = 16;
	private void moveActive() {

		aliveTime += 1024/VidGame.FPS;

        int dist = speed;
		while (dist > 0) {
			int moves = board.detMoves(position);
			moves = filterReverse(moves, dir);

			// Some of the time, seek the exit location.  Do this all the time
			// if we have been on the screen a long time.

			if (MyMath.rnd(2) == 0 || aliveTime > 1024*8)
                moves = seek(exitLoc, moves, 0);

			// Move this function into Obj.
			desiredDir = chooseRandomDir(moves);
			dist = super.move(dist);
        }
		animate(ARC_FRAMES);
		if (frame == 0)
			Sfx.play(parent.E_FMOVE);

		// Are we deep in a tunnel?  If so, and we weren't last time, we have left the
		// maze.

		boolean tFlag = board.inTunnel(position, board.WORLD_CELL_SIZE * 2);
		if (!tFlag)
			inTunnelFlag = false;
		else {
			if (!inTunnelFlag) {
				onBoard = false;
				activeFruitTotal--;
			}
		}
	}

	private void plotOne() {
		if (!onBoard) return;
//		db.a(board.defined(),"Player.plot board not defined");
		Pt sLoc = new Pt();
		board.worldToView(position.x,position.y,sLoc);

		if (status == S_ACTIVE) {
			final int arcY[] = {
		        0,2,4,5,6,7,8,9,
		        9,8,7,6,5,4,2,0
		    };

			int s = type;
			if (type == THIEF)
				s = 0;	// Add thief type later.

			BEngine.drawSprite(ims[s], sLoc.x, sLoc.y - arcY[frame]);
		} else {
			parent.charSet0.centerString(Integer.toString(value), sLoc.x, sLoc.y);
		}
	}

}