package MunchMan;

import vgpackage.*;
import mytools.*;
import java.awt.*;

public class MunchMan
    extends java.applet.Applet
    implements Runnable, VidGameInt {

  public static final int VIEW_SCORE = 1;
  public static final int VIEW_MAZE = 2;
  public static final int VIEW_STATUS = 3;

  public static final int GS_STARTING = 0; // Playing opening song
  public static final int GS_RUNNING = 1; // Running around
  public static final int GS_COMPLETED = 2; // Completed maze
  public static final int GS_COMPLETED2 = 3; //  second part of compl.
  public static final int GS_DYING = 4; // Dying (stage 1 of 2)
  public static final int GS_DYING2 = 5; // Dying
  public static final int GS_CAUGHTONE = 6; // Just caught ghost

  public static final int E_DEAD = 0;
  public static final int E_EAT = 1;
  public static final int E_FRUIT = 2;
  public static final int E_DOT = 3;
  public static final int E_POWER = 4;
  public static final int E_SIREN = 5;
  public static final int E_EYES = 6;
  public static final int E_LIFE = 7;
  public static final int E_FMOVE = 8;

  // ===================================
  // Applet interface
  // ===================================
  public void init() {
    VidGame.doInit(this);
    VidGame.setHighScore(2000);
    VidGame.setBonusScore(12000);

    BEngine.open();

    sprite = new Sprite("combined");

    Board.init(this);

    board = new Board();
    player = new Player(board, this);

    Ghost.init(board, this, player);
    Fruit.init(board, this, player);

    charSet0 = new CharSet(new Sprite(sprite, 120, 210, 90, 13),
                           8, 12, 1, 1, "0123456789");
    charSet1 = new CharSet(new Sprite(sprite, 0, 300, 192, 72)
                           , 15, 17, 1, 1,
                           "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!<>^~:%()?");
    charSet1.setSpacingX( -4);

    scorePnl = new ScorePnl(this, player);

  }

  public void start() {
    Sfx.open(sfxNames, 3);
    Sfx.setFilter(E_SIREN, 0);
    VidGame.setBonusSfx(E_LIFE);

    VidGame.doStart();
    mazeDefined = false;
    VidGame.initStage(GS_STARTING);
    player.start();
    Ghost.start();
    Fruit.start();
    clearBoardFlag = true;
  }

  public void run() {
    VidGame.doRun();
  }

  public void stop() {
    VidGame.doStop();
    Sfx.close();
  }

  public void destroy() {
    BEngine.close();
    VidGame.doDestroy();
  }

  // ===================================
  // VidGameInt interface
  // ===================================
  public void processLogic() {
    updateStage();
    if (VidGame.getMode() == VidGame.MODE_GAMEOVER)
      return;

    if (
        VidGame.getMode() == VidGame.MODE_PLAYING
// ???		 && prevStage != GS_CAUGHTONE
        ) {
      Fruit.move();
    }
    player.move();
    Ghost.move();
  }

  // ===================================

  private void updateStage() {
    int stageTime = VidGame.getStageTime();
    int stage = VidGame.getStage();

    switch (stage) {
      case GS_STARTING:
        if (stageTime == 0) {
          animTimer = 0;
        }

        if (stageTime > 770 ||
            (stageTime > 100 && VidGame.getMode() == VidGame.MODE_PREGAME))
          VidGame.setStage(GS_RUNNING);
        break;

      case GS_RUNNING:
        if (!board.dotsRemaining()
            || VidGame.getAdvanceLevelFlag()
            ) {
          VidGame.clearAdvanceLevelFlag();
          VidGame.setStage(GS_COMPLETED);
        }

        if (
            VidGame.getMode() == VidGame.MODE_PREGAME
            && stageTime > 20000
            ) {
          mazeDefined = false;
          VidGame.setStage(GS_STARTING);
        }
        break;

      case GS_COMPLETED:
        if (stageTime > 1500)
          VidGame.setStage(GS_COMPLETED2);
        break;

      case GS_COMPLETED2:
        if (stageTime > 1500) {
          VidGame.adjLevel(1);
          mazeDefined = false;
          VidGame.setStage(GS_STARTING);
          Fruit.prepareNewLevel();
        }
        break;

      case GS_DYING:
        if (stageTime > 1500) {
          VidGame.setStage(GS_DYING2);
          Sfx.play(MunchMan.E_DEAD);
        }
        break;

      case GS_DYING2:
        if (VidGame.getMode() == VidGame.MODE_GAMEOVER)
          break;
        if (stageTime > 2000) {
          if (VidGame.getLives() == 0) {
            VidGame.setMode(VidGame.MODE_GAMEOVER);
            clearBoardFlag = true;
          }
          VidGame.setStage(GS_STARTING);
        }
        break;

      case GS_CAUGHTONE:
        if (stageTime > 800)
          VidGame.setStage(GS_RUNNING);
        break;
    }

    animTimer += 1024 / VidGame.FPS;

    if (
        clearBoardFlag
        && VidGame.getMode() != VidGame.MODE_GAMEOVER
        ) {
      clearBoardFlag = false;
      mazeDefined = false;
      VidGame.setStage(GS_STARTING);
    }

    if (VidGame.initFlag()) {
      VidGame.setStage(GS_STARTING);
      Fruit.prepareNewLevel();
      mazeDefined = false;
    }

//		prevStage = stage;
    VidGame.updateStage();

    // Stage may have changed, so reload stage & time:

    stage = VidGame.getStage();
    stageTime = VidGame.getStageTime();

    if (stage == GS_STARTING && !mazeDefined) {
      board.setMaze();
      mazeDefined = true;
    }

    {
      int flags = 0;
      if (stage == GS_RUNNING) {
        flags |= Ghost.sfxFlags();
        if (flags == 0)
          flags |= 4;
      }
      if (stage == GS_CAUGHTONE
          && stageTime > 300
          )
        flags = 2;

      final int sfx[] = {
          E_POWER,
          E_EYES,
          E_SIREN,
      };

      if (stage == GS_STARTING)
        flags = 0;

      for (int i = 0; i < 3; i++) {
        if ( (flags & (1 << i)) == 0)
          Sfx.stop(sfx[i]);
        else {
          Sfx.play(sfx[i]);
        }
      }
    }

  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {
    if (!VidGame.beginPaint())
      return;

    // Prepare for update.  Constructs offscreen buffers if required.
    BEngine.prepareUpdate();

    // Process bg layer
    BEngine.openLayer(BEngine.L_BGND);
    plotBgnd();
    BEngine.closeLayer();

    // Process sprite layer
    BEngine.openLayer(BEngine.L_SPRITE);
    BEngine.erase();
    plotSprites();
    BEngine.closeLayer();

    BEngine.updateScreen(g);

    VidGame.endPaint();
  }

  private void plotBgnd() {
    boolean valid = BEngine.layerValid();

    if (!valid) {
      Pt p = Board.screenSize();
      BEngine.defineView(VIEW_MAZE, 0, 0, p.x, p.y);
      BEngine.defineView(VIEW_SCORE, p.x, 0, 236, p.y);
    }

    BEngine.selectView(VIEW_SCORE);
    scorePnl.plotChanges();

    BEngine.selectView(VIEW_MAZE);

    // Compare board cells with the last drawn ones.  If
    // they differ, plot the differences.

    if (boardDrawn == null) {
      valid = false;
      boardDrawn = new Board();
    }
    if (board.defined())
      board.plotChanges(boardDrawn, valid);
    else
      BEngine.clearView();
  }

  private void plotSprites() {
    BEngine.selectView(VIEW_MAZE);

    if (VidGame.getMode() == VidGame.MODE_GAMEOVER)
      return;

    board.plot(animTimer);
    Fruit.plot();
    player.plot();
    Ghost.plot();
  }

  private Board board;
  private Board boardDrawn;
  private Player player;

  private static final int SCORE_HEIGHT = 24;
  private static final int STATUS_HEIGHT = 16;

  private static final String sfxNames[] = {
      "dead", "eat", "fruit", "*dot", "*power",
      "*siren", "*eyes", "life", "fmove"
  };

  private boolean mazeDefined;
  private int animTimer;
  private ScorePnl scorePnl;
  private boolean clearBoardFlag; // true if we should reset the game for PREGAME
  public Sprite sprite;
  public CharSet charSet0, charSet1;
}