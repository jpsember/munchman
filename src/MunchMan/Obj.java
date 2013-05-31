package MunchMan;
import vgpackage.*;
import mytools.*;

public abstract class Obj {
	protected Board board;
	protected Pt position;
	protected int dir;
	protected int speed;
	protected int desiredDir;
//	protected BEngine ge;
	protected MunchMan parent;
	protected boolean onBoard;
	protected int frame;			// animation frame; incremented by animate()
	protected boolean movedFlag;	// true if moved since last call to animate()
	protected boolean wrapInTunnels;

	public Obj(Board board, MunchMan parent) {
		this.parent = parent;
		this.board = board;
//		this.ge = ge;
		position = new Pt();
		onBoard = false;
		wrapInTunnels = true;
	}

	// Move object.
	// Precondition:
	//	distance = distance to move
	//	desiredDir = direction of preferred movement
	// Postcondition:
	//	returns 0 if done moving, or distance still left to move, if
	//	object has reached an intersection.  This gives the calling function
	//	an opportunity to change directions.
	public int move(int distance) {

//		db.pr("move distance="+distance+" speed="+speed);
		if (distance > speed)
			distance = speed;

		int possibleDir = board.detMoves(position);
//		db.pr(" position="+position.x+","+position.y);
//		db.pr(" possibleDirs = "+possibleDir);

		if (board.movePossible(desiredDir, possibleDir))
			dir = desiredDir;

		// Calculate the distance to the next intersection in the current direction

		int distToIntersection = 0;
		int cellSize = board.WORLD_CELL_SIZE;
		switch (dir) {
		case Board.UP:
			distToIntersection = MyMath.mod(position.y, cellSize);
			break;
		case Board.DOWN:
			distToIntersection = MyMath.mod(-position.y, cellSize);
			break;
		case Board.RIGHT:
			cellSize >>= 1;
			distToIntersection = MyMath.mod(-position.x, cellSize);
			break;
		case Board.LEFT:
			cellSize >>= 1;
			distToIntersection = MyMath.mod(position.x, cellSize);
			break;
		}

		if (distToIntersection == 0) {
			distToIntersection = cellSize;
		}

		int stepDist = Math.min(distToIntersection, distance);

//		db.pr(" distToIntersection="+distToIntersection+", stepdist = "+stepDist);

		if (board.movePossible(dir, possibleDir)) {
			movedFlag = true;
			moveInDir(stepDist);

			if (wrapInTunnels)
				board.tunnelWrap(position);

		}
		distance -= stepDist;
		return distance;
	}

	// Adjust the animation frame if the object has moved since the last call
	// Precondition:
	//	length = length of animated sequence (frame will wrap to zero at this value)
	// Postcondition:
	//	frame has been adjusted.  If object has stopped moving, frame will wrap to 0
	//	 then stop.
	public void animate(int length) {
		if (movedFlag || frame != 0) {
			frame++;
			if (frame >= length)
				frame = 0;
		}
		movedFlag = false;
	}

	public void resetAnim() {
		frame = 0;
		movedFlag = false;
	}

	public void moveInDir(int distance) {
		position.x += board.xMoves[dir] * distance;
		position.y += board.yMoves[dir] * distance;
	}

	public void moveInDir() {
		moveInDir(speed);
	}

	protected int filterReverse(int moves, int dir) {
		int fMoves = (moves & ~(1 << (dir ^ 2)));
		if (fMoves != 0)
			return fMoves;
		return moves;
	}

	// Test if an object has reached or passed a particular position.
	// If so, places at that position and returns true.
	public boolean reachedPosition(Pt prevLoc, Pt dest) {

		boolean result = false;

		Pt start = new Pt();
		Pt end = new Pt();

		boolean vertFlag;

		if (Math.abs(position.x - prevLoc.x) > Math.abs(position.y - prevLoc.y)) {
			// Mainly moving horizontally.
			start.set(prevLoc.x, (prevLoc.y + position.y) >> 1);
			end.set(position.x, start.y);
			vertFlag = false;
		} else {
			start.set((prevLoc.x + position.x) >> 1, prevLoc.y);
			end.set(start.x, position.y);
			vertFlag = true;
		}

		// Flip coordinates so we are dealing with x.

        if (vertFlag) {
			start.swap();
			end.swap();
        }

		// Exchange so start is always less.

		if (start.x > end.x) {
			start.swapWith(end);
        }

        if (
            start.x <= dest.x
         && end.x >= dest.x
         && Math.abs(start.y - dest.y) < Board.WORLD_CELL_SIZE / 4
        ) {
            result = true;
			dest.copyTo(position);
        }
        return result;
	}

	// Determine which moves will seek a particular position
	// Precondition:
	//	type =  0:normal
	//			1:a more rigorous method, produces more accurate but less varied results
	public int seek(Pt desired, int moves, int type) {
        int code = 0;   /*  Two dimensional */

        if (type != 0) {
            int xD = Math.abs(position.x - desired.x);
            int yD = Math.abs(position.y - desired.y);
			if (xD > yD*4
			 && xD > Board.WORLD_CELL_SIZE * 3
			)
				code = 1;
			else if (yD > xD * 4
		 	 && yD > Board.WORLD_CELL_SIZE * 3
			)
				code = 2;
        }

		int newMoves = 0;
        if (code != 2) {
			if (position.x + Board.WORLD_CELL_SIZE < desired.x)
                newMoves |= 1 << Board.RIGHT;
            else if (position.x - Board.WORLD_CELL_SIZE > desired.x)
				newMoves |= 1 << Board.LEFT;
        }
        if (code != 1) {
			if (position.y + Board.WORLD_CELL_SIZE < desired.y)
                newMoves |= 1 << Board.DOWN;
            else if (position.y - Board.WORLD_CELL_SIZE > desired.y)
				newMoves |= 1 << Board.UP;
        }
		newMoves &= moves;

        if (newMoves == 0) {
			newMoves = moves;
            /*  Try again, but eliminate moves that are clearly wrong.  */
            if (code == 1) {
                if (position.x < desired.x)
					newMoves &= ~(1 << Board.LEFT);
				else if (position.x > desired.x)
					newMoves &= ~(1 << Board.RIGHT);
            } else if (code == 2) {
                if (position.y < desired.y)
					newMoves &= ~(1 << Board.UP);
				else if (position.y > desired.y)
					newMoves &= ~(1 << Board.DOWN);
            }
        }

        if (newMoves == 0)
			newMoves = moves;
		return newMoves;
	}

	private final static int COLA = 5;
	private final static int COLB = 12;
	private final static int colData[] = {
		COLA,COLB,28-1-COLA,28-1-COLB,
		COLB,COLA,28-1-COLB,28-1-COLA,
	};

	public static void setCollision(Sprite s) {
		s.addColRect(colData);
	}

/*
	public boolean testCatch(Obj target) {

		int dx = Math.abs(target.position.x - position.x);
		int dy = Math.abs(target.position.y - position.y);

		final int CATCH_SEP = (Board.WORLD_CELL_SIZE * 13)/16;
		final int CATCH_SEP2 = Board.WORLD_CELL_SIZE / 4;

        return (
            dx < CATCH_SEP
         && dy < CATCH_SEP
         && (dx < CATCH_SEP2 || dy < CATCH_SEP2)
        );
	}
*/
	// Choose a random direction
	public static int chooseRandomDir(int moves) {
		final short orders[] = {
			(0<<6)|(3<<4)|(1<<2)|2,
			(1<<6)|(3<<4)|(0<<2)|2,
			(0<<6)|(3<<4)|(2<<2)|1,
			(2<<6)|(3<<4)|(0<<2)|1,
			(2<<6)|(3<<4)|(1<<2)|0,
			(1<<6)|(3<<4)|(2<<2)|0,
			(3<<6)|(0<<4)|(1<<2)|2,
			(3<<6)|(1<<4)|(0<<2)|2,
			(3<<6)|(0<<4)|(2<<2)|1,
			(3<<6)|(2<<4)|(0<<2)|1,
			(3<<6)|(2<<4)|(1<<2)|0,
			(3<<6)|(1<<4)|(2<<2)|0,
			(0<<6)|(1<<4)|(2<<2)|3,
			(1<<6)|(0<<4)|(2<<2)|3,
			(0<<6)|(2<<4)|(1<<2)|3,
			(2<<6)|(0<<4)|(1<<2)|3,
			(2<<6)|(1<<4)|(0<<2)|3,
			(1<<6)|(2<<4)|(0<<2)|3,
			(0<<6)|(1<<4)|(3<<2)|2,
			(1<<6)|(0<<4)|(3<<2)|2,
			(0<<6)|(2<<4)|(3<<2)|1,
			(2<<6)|(0<<4)|(3<<2)|1,
			(2<<6)|(1<<4)|(3<<2)|0,
			(1<<6)|(2<<4)|(3<<2)|0
		};
//		db.a(moves != 0, "Obj.chooseRandomMove, moves = 0");
		int order = orders[MyMath.rnd(24)];

		while (true) {
			int dir = order & 3;
			if ((moves & (1 << dir)) != 0)
				return dir;
			order >>= 2;
		}
	}

}
