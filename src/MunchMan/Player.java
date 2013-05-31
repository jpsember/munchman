package MunchMan;
import java.awt.*;
import vgpackage.*;

public class Player extends Obj {

	private Sprite ims[];
	private int dotsEaten;
	private int dotEatSustain;

	private int S_PACU = 0;
	private int S_PACR = S_PACU+3;
	private int S_PACD = S_PACR+3;
	private int S_PACL = S_PACD+3;
	private int S_DIE = S_PACL+3;
	private int S_TOTAL = S_DIE+6;

	public int dotsEaten() {
		return dotsEaten;
	}

	public Sprite icon() {
		return ims[S_PACR];
	}

	public Player(Board board, MunchMan parent) {
		super(board, parent);

		final String paths[] = {
			"pacu0","pacu1","pacu2",
			"pacr0","pacr1","pacr2",
			"pacd0","pacd1","pacd2",
			"pacl0","pacl1","pacl2",
//			"pacc0","pacc1","pacc2","pacc3","pacc4","pacc5",
		};

		ims = new Sprite[S_TOTAL];

//		// Load the sprite that contains the embedded pacdeath sprites.
//		Sprite pacdeath = new Sprite("pacdeath",0,0);

		for (int i = 0; i < S_TOTAL; i++) {
			int x = (7 + (i % 3)) * 30;
			int y = (i / 3) * 30;

			ims[i] = new Sprite(parent.sprite, x, y, 28, 28, 14, 14);
			if (i == S_PACR)	// Only add collision test to this sprite.
			{
//				ims[i].addColRect(5,5,28-1-5,28-1-5);
				setCollision(ims[i]);
			}

/*
			if (i < S_DIE)
				ims[i] = new Sprite(paths[i], 14,14);
			else {
				int f = i - S_DIE;
				ims[i] = new Sprite(pacdeath, f*28, 0, 28, 28, 14, 14);
			}
*/
		}

	}

	public void start() {
		onBoard = false;
	}

	private void placeOnBoard() {
		board.getHumanStartLoc(position);
		dir = board.RIGHT;
		desiredDir = dir;

		int level = Math.min(VidGame.getLevel(), 8-1);
		final int speedScales[] = {

			126,134,144,146,
			148,150,152,154
//			105,120,130,140,
//			142,144,146,148
		};
		speed = ((VidGame.TICK * 2000) * speedScales[level]) / 128;

		resetAnim();

		if (!onBoard) {
			VidGame.adjustLives(-1);
			onBoard = true;
		}
	}

	public void move() {

		dotEatSustain = Math.max(0, dotEatSustain - VidGame.CYCLE);

		do {
			if (VidGame.getMode() != VidGame.MODE_PLAYING) break;

			/*if (parent.stageStart(parent.GS_DYING2))
				Sfx.play(parent.E_DEAD); */

			if (VidGame.stageStart(MunchMan.GS_STARTING)) {
				dotsEaten = 0;
				placeOnBoard();
			}

			if (VidGame.getStage() != parent.GS_RUNNING) break;

	//		db.a(onBoard,"Player.move, not on board!");
	//		db.a(board.defined(), "Player.move, board not defined");

			final int posToDir[] = {-1,Board.UP,-1,Board.RIGHT,-1,Board.DOWN,-1,Board.LEFT,-1};
			int newDir = posToDir[VidGame.getJoystick().pos()];
			if (newDir != -1)
				desiredDir = newDir;

			int dist = speed;

			do {
				dist = super.move(dist);

				if (!board.atIntersection(position, false))
					continue;

				int dotEaten = board.testEatDot(position);
				if (dotEaten != 0) {
					dotsEaten++;
					dotEatSustain = 150;
//					Sfx.play(parent.E_DOT,0,200);
					VidGame.adjScore(dotEaten == 2 ? 50 : 10);
					if (dotEaten == 2) {
						Ghost.powerUp();
					}
				}

			} while (dist > 0);
			animate(8);
		} while (false);
		if (dotEatSustain > 0)
			Sfx.play(parent.E_DOT);
		else
			Sfx.stop(parent.E_DOT);
	}

	public void plot() {
		if (!onBoard) return;

//		db.a(board.defined(),"Player.plot board not defined");
		final Pt sLoc = new Pt();
		board.worldToView(position.x,position.y,sLoc);

		if (VidGame.getStage() == parent.GS_DYING2) {
			int frame = VidGame.getStageTime() >> (11-4);
			if (frame < 6) {
				BEngine.drawSprite(ims[S_DIE + frame], sLoc);//.x, sLoc.y);
			} else
				onBoard = false;
		} else {
			final int seq[] = {0,1,2,1};

			BEngine.drawSprite(
				ims[(S_PACU + 3*dir) + seq[frame / 2]], sLoc);
		}
	}
}