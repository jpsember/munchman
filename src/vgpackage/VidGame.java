package vgpackage;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.applet.*;
import mytools.*;

/**
 * This class provides the basic structure for a video game.
 */
public final class VidGame
    implements KeyListener, MouseMotionListener, MouseListener,
    VidGameGlobals {

  private static int debugSignal;
  public static boolean debugSignal(int index) {
    if (DEBUG) {
      boolean flag = (debugSignal & (1 << index)) != 0;
      debugSignal &= ~(1 << index);
      return flag;
    } else return false;
  }

  // Make the constructor private so nobody tries to
  // instantiate this static class.
  private VidGame() {
  }

  /**
   * Set the bonus score, for a one-time bonus life.
   * @param b A new life will be awarded when the score reaches
   * this value.
   */
  public static void setBonusScore(int b) {
    bonusScores = new int[2];
    bonusScores[0] = b;
    bonusScores[1] = 0;
  }

  /**
   * Set the 'bonus life awarded' sound effect.
   * @param sfx The index of the sound effect to play
   */
  public static void setBonusSfx(int sfx) {
    bonusLifeSfx = sfx;
  }

  /**
   * Define the scores to award bonus lives at.
   * @param bonus an array of int[].  This array can be of any
   * length, and may start with a series of positive integers of
   * increasing value.  The array also may end with a single
   * negative integer.  The positive values indicate the thresholds
   * to award bonus lives at, and the negative value will indicate
   * the multiple beyond the last of the bonus life thresholds at
   * which to award continuing bonus lives. For example, to award
   * bonus lives at 2000, 5000, 12000, and then every 10000 points
   * after that (22000, 32000, etc), the array would be:
   * {2000,5000,12000,-10000}.
   */
  public static void setBonusScores(int bonus[]) {
    bonusScores = bonus;
  }

  /**
   * Returns true if it's the first frame of MODE_PLAYING.
   *
   * @see VidGame#MODE_PLAYING
   */
  public static boolean initFlag() {
    return (mode == MODE_PLAYING && time == 0);
  }

  /**
   * Returns true if the game is currently paused by the user.
   */
  public static boolean paused() {
    return pauseStatus;
  }

  /**
   * Set the game mode.  If set to MODE_PLAYING, the game is started
   * by resetting the score and level to zero, and setting the number
   * of lives remaining to 3.
   *
   * @param s one of MODE_PREGAME, MODE_PLAYING, MODE_GAMEOVER
   *
   * @see VidGame#MODE_PREGAME
   * @see VidGame#MODE_PLAYING
   * @see VidGame#MODE_GAMEOVER
   */
  public static void setMode(int s) {
    mode = s;
    time = 0;
    switch (s) {
      case MODE_PLAYING:
        startNewGame();
        break;
    }
  }

  /**
   * Returns the current game mode.
   *
   * @see VidGame#MODE_PREGAME
   * @see VidGame#MODE_PLAYING
   * @see VidGame#MODE_GAMEOVER
   */
  public static int getMode() {
    return mode;
  }

  /**
   * Returns the number of cycles at the current game mode.
   * Returns a value from 0...n
   *
   * @see VidGame#MODE_PREGAME
   * @see VidGame#MODE_PLAYING
   * @see VidGame#MODE_GAMEOVER
   */
  public static int getTime() {
    return time;
  }

  /**
   * Return the game level.
   */
  public static int getLevel() {
    return level;
  }

  /**
   * Adjust the level.
   *
   * @param n amount to increase the level by
   */
  public static void adjLevel(int n) {
    level += n;
  }

  /**
   * Set the level.
   *
   * @param n new level
   */
  public static void setLevel(int n) {
    level = n;
  }

  /**
   * Return the number of lives remaining.
   */
  public static int getLives() {
    return livesLeft;
  }

  /**
   * Set the number of lives.  This method can be called to change the
   * number of lives the player starts with.  It should be called
   * immediately after a call to setMode(MODE_PLAYING).
   *
   * @param n number of lives
   *
   * @see VidGame#setMode(int)
   */
  public static void setLives(int n) {
    livesLeft = n;
  }

  public static void adjustLives(int adj) {
    livesLeft += adj;
    if (adj > 0 && bonusLifeSfx != 0)
      Sfx.play(bonusLifeSfx, 200, 0);
  }

  public static void setScore(int n) {
    score = n;
    if (score > highScore)
      highScore = score;
  }

  // Add to the player's score.
  // Precondition:
  //	n >= 0
  public static void adjScore(int n) {
    if (DEBUG)
      Debug.ASSERT (n >= 0, "VidGame.adjScore called with " + n);

      if (mode != MODE_PLAYING)
        return;

      while (n > 0) {
        // Determine the next score to award a bonus life at.
        int nextBonus = calcNextBonusScore(score);

        // Calculate the step amount, which is the adjust amount or the
        // distance to the next bonus, whichever is less.
        // This makes sure we don't miss a bonus point interval.

        int step = n;
        if (nextBonus != 0)
          step = Math.min(nextBonus - score, n);

          // Add the step amount
        setScore(score + step);

        // Subtract from the adjust amount
        n -= step;

        // If we are at the bonus point, award an extra life.
        if (score == nextBonus)
          adjustLives(1);
      }

  }

  public static int getScore() {
    return score;
  }

  public static int getScore(int n) {
    return (n == 0) ? score : highScore;
  }

  public static void setHighScore(int n) {
    highScore = n;
  }

  public static int getHighScore() {
    return highScore;
  }

  // Determine if applet is supposed to draw a new frame.
  // If false, don't perform the update/paint; we may be in the
  // middle of the logic task, or shutting down the applet.
  // If beginPaint() returns true, then a balancing call to endPaint()
  // must be made by the caller.
  public static boolean beginPaint() {
    boolean fDraw = tcDrawSignal.readSignalLock();
    if (fDraw) {
      if (!tcDrawing.setSignalLock())
        fDraw = false;
    }
    return fDraw;
  }

  public static void endPaint() {
    tcDrawing.clearSignalLock();
    // signal that we are done with plotting.
    tcDrawSignal.clearSignalLock();
  }

  /**
   * Initialize the VidGame class.  Should be called by the applet's
   * init() method.
   * @param parent The video game applet.
   */
  public static void doInit(VidGameInt vg) {

    vidGameInt = vg;
    applet = (Applet) vg;
    component = (Component) vg;

    readLanguage();

    currentCursor = -1;
    desiredCursor = Cursor.WAIT_CURSOR;
    updateCursor();

    VidGame vgame = new VidGame();
    component.addKeyListener(vgame);
    component.addMouseMotionListener(vgame);
    component.addMouseListener(vgame);

    Debug.init(applet);
    startTime = System.currentTimeMillis();

//		seedRandom(1999);
    joystick = new Joystick();
    setMode(MODE_PREGAME);
    time = -1; // So it is 0 first time through loop

    tcDrawSignal = new ThreadCommand(0);
    tcDrawing = new ThreadCommand(0);
    fLoading = true;

    initStage();
  }

  /**
   * Start the video game.
   * Should be called by the applet's start() method.
   */
  public static void doStart() {
    tcDrawSignal.clearSignalLock();
    tcDrawing.clearSignalLock();

    logicThread = new Thread( (Runnable) component);
    logicThread.start();
  }

  /**
   * Process the game logic thread.  Should be called by the
   * applet's run() method.
   */
  public static void doRun() {
    long prevTime = 0;
    boolean prevTimeDefined = false;
    int framesDropped = 0;

    while (logicThread != null) {

      // Is the frame available for manipulation?
      // If it's not available, the update/paint cycle is currently executing.

      boolean available = !tcDrawSignal.readSignalLock();

      // Increment the dropped frames counter, so we process
      // at least one more frame.
      framesDropped++;

      logic: {
        if (!available)
          break logic;

        // Update the pause / single step status

        if (stepMode) {
          if (!stepFlag) {
            available = false;
            break logic;
          }
          stepFlag = false;
          // Never skip draw frames in single step mode.
          framesDropped = 1;
        }

        // Never skip more than n frames.
        framesDropped = Math.min(framesDropped, 3);

        // Process logic once for each dropped frame.

        while (framesDropped > 0) {
          framesDropped--;

          if (pauseStatus)
            continue;
          if (pauseDelay > 0)
            pauseDelay--;

          updateMode();
          // Call game-specific logic...
          vidGameInt.processLogic();
        }
      }

      if (available) {
        tcDrawSignal.setSignalLock();
        component.repaint();
      }

      // Sleep a period of time so that we execute the
      // logic frames CYCLE cycles apart.

      long sleepTime = 0;
      long currentTime = getSystemTime();
      if (prevTimeDefined)
        sleepTime = CYCLE - ( (currentTime - prevTime));
      prevTime = currentTime;
      prevTimeDefined = true;
      if (sleepTime > 0) {
        ThreadCommand.sleep(sleepTime);
        prevTime += sleepTime;
      }
    }
  }

  /**
   * Stop the video game.  Should be called by the applet's
   * stop() method.
   */
  public static void doStop() {
    // Halt main thread
    logicThread = null;
    // Wait for any update() operation to be complete, and
    // don't allow any more.
    while (!tcDrawing.setSignalLock())
      ThreadCommand.sleep(20);
  }

  /**
   * Destroy the video game.  Should be called by the applet's
   * destroy() method.
   */
  public static void doDestroy() {
    tcDrawSignal = null;
    tcDrawing = null;
    vidGameInt = null;
    applet = null;
    component = null;
  }

  /**
   * Returns the current game stage.  The game stage determines what
   * part of the game logic is running (intro, alive, dying, etc.).
   * These values are defined by the calling game.  These 'stage'
   * methods are not to be confused with the 'mode', which is a
   * separate value determining whether the game is in a demo mode
   * or loading state.
   */
  public static int getStage() {
    return stage;
  }

  /**
   * Returns true if it is the first cycle of specified stage
   */
  public static boolean stageStart(int s) {
    return (stage == s && stageTime == 0);
  }

  /**
   * Returns the number of milliseconds the current stage has been
   * active.  Guaranteed to be zero the first time
   */
  public static int getStageTime() {
    return stageTime;
  }

  private static int stage; //, prevStage;
  private static int stageTime;
  private static int pendingStage;

  /**
   * Requests that a new stage be set at the next call to updateStage().
   * If there is already a stage requested, it will
   * only be replaced by this new one if the new one has a higher
   * index (i.e. higher priority).
   */
  public static void setStage(int newStage) {
    int newPending = newStage + 1;
    if (pendingStage < newPending)
      pendingStage = newPending;
  }

  private static void initStage() {
    stage = 0;
    stageTime = 0;
    initStage(0);
  }

  /**
   * Update the current stage.  Replaces current stage with pending one,
   * if one exists.
   */
  public static void updateStage() {
    stageTime += 1024 / VidGame.FPS;
    if (pendingStage > 0) {
      stage = pendingStage - 1;
      stageTime = 0;
      pendingStage = 0;
    }
  }

  /**
   * Requests that new stage be set at next call to updateStage(),
   * replacing any pending stage, even one of a higher priority
   */
  public static void initStage(int s) {
    pendingStage = s + 1;
  }

  /**
   * Process a keyPressed(), keyReleased() event for the applet.
   * The video game applet should implement the KeyListener interface,
   * and its keyPressed() and keyReleased() methods should pass the
   * events on to this method.
   * @param e The KeyEvent received by the applet's keyPressed() or
   * keyReleased() method
   * @param pressedFlag true if calling from keyPressed(), false if
   * from keyReleased().
   */
  private static void processKeyEvent(KeyEvent e, boolean pressedFlag) {
    if (pressedFlag) {
      int code = e.getKeyCode();

      if (code == KeyEvent.VK_P) {
        if (
            (mode == MODE_PLAYING || DEBUG)
            && stepMode == false
            ) {
          if (!pauseStatus) {
            if (pauseDelay == 0) {
              pauseStatus = true;
              Sfx.processPause(true);
            }
          }
          else {
            pauseDelay = PAUSE_FRAMES;
            pauseStatus = false;
            Sfx.processPause(false);
          }
        }
      }

      if (code == KeyEvent.VK_S) {
        Sfx.processQuiet();
      }

      if (DEBUG) {
        if (code == KeyEvent.VK_PERIOD) {
          if (!stepMode) {
            stepMode = true;
            Sfx.processPause(true);
          }
          stepFlag = true;
        }
        if (code == KeyEvent.VK_COMMA) {
          if (stepMode) {
            stepMode = false;
            Sfx.processPause(false);
          }
        }
        if (code == KeyEvent.VK_L) {
          debAdvLevel = true;
        }
        if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
          debugSignal |=  1 << (code - KeyEvent.VK_0);
        }
      }
    }
    joystick.processKeyEvent(e, pressedFlag);

  }

  /** Determines if the applet is still loading sound effects.  To
   *  decrease the perceived load time, it may be desirable to leave
   *  some of the larger sound effects outside of the JAR files, and
   *  load them after the JAR files are loaded and the game is running.
   *  At least then the game can be displaying pre-game animation with
   *  a load message.  If the sound effects are stored in the JAR files,
   *  then they will all be loaded before this method is called.
   *  @return true if loading is still occurring
   */
  public static boolean loading() {
    return fLoading;
  }

  /**
   * Returns the Joystick object
   * @return the Joystick object
   */
  public static Joystick getJoystick() {
    return joystick;
  }

  /*   public static Random getRandom() {
        return random;
     }
   */
  public static Applet getApplet() { // Used to be getParent
    return applet;
  }

  /**
   * Returns the language code
   * @return language code:  LANG_xxx
   */
  public static int getLanguage() {
    return language;
  }

  public static int getLanguage(int maxSupported) {
    if (language < maxSupported)
      return language;
    Debug.ASSERT (language < maxSupported,
                  "Language " + language + " not supported");
    return LANG_ENGLISH;

    /**
     * Read the desired language value from the HTML tags.
     * Should be called by the applet's init() method.
     */
  }

  private static void readLanguage() {
    String langStr = applet.getParameter("LANGUAGE");
    if (langStr != null) {
      int langVal = Integer.parseInt(langStr);
      if (langVal >= 0 && langVal < LANG_TOTAL) {
        language = langVal;
      }
    }
  }

  private static void startNewGame() {
    livesLeft = 3;
    level = 0;
    score = 0;
  }

  private static void updateMode() {
    time++;
//		Debug.print("updateMode, time="+time+", mode="+mode);

    switch (mode) {
      case MODE_PREGAME:
        if (fLoading) {
          if (Sfx.loaded()) {
            fLoading = false;
            desiredCursor = cursorType;
          }
        }
        break;
      case MODE_GAMEOVER:
        if (time >= FPS * 5) {
          setMode(MODE_PREGAME);
        }
        break;
    }

    if (mode == MODE_PREGAME || mode == MODE_GAMEOVER) {
      boolean clicked = joystick.fireButtonClicked(1);
      if (clicked && ! (mode == MODE_GAMEOVER && time < FPS * 2)) {
        joystick.clear();

        if (!loading())
          setMode(MODE_PLAYING);
      }
    }
    Sfx.update();
    updateCursor();
  }

  private static void updateCursor() {
    if (desiredCursor != currentCursor) {
      currentCursor = desiredCursor;
      applet.setCursor(Cursor.getPredefinedCursor(currentCursor));
    }
  }

  // Determine the next score to award a bonus life.
  // Precondition:
  //	currScore = current score
  //	bonusScores = int[], intervals for bonus scores, or null if no bonus
  //		lives to be awarded
  // Postcondition:
  //	next bonus score returned, or 0 if no more bonus lives available
  private static int calcNextBonusScore(int currScore) {
    if (bonusScores == null)
      return 0;

    int nextBonus = 0;
    int threshold = 0;

    int k = bonusScores.length; // IE bug
    for (int i = 0; i < k; i++) {
      int n = bonusScores[i];
      if (n < 0) {
        int multiple = -n;

        // Bonus awarded at threshold + multiple * k.
        // Calculate lowest bonus score > current score.

        nextBonus = ( ( ( (currScore - threshold) / multiple) + 1) * multiple) +
            threshold;
        break;
      }
      threshold = n;
      if (threshold > currScore) {
        nextBonus = threshold;
        break;
      }
    }
    return nextBonus;
  }

  /**
   * Determine the amount of time, in milliseconds, since the
   * doInit() method was called.
   */
  public static long getSystemTime() {
    return System.currentTimeMillis() - startTime;
  }

  /**
   * Determines if the debug 'advance level' flag was set.
   */
  public static boolean getAdvanceLevelFlag() {
    return debAdvLevel;
  }

  /**
   * Clear the debug 'advance level' flag
   */
  public static void clearAdvanceLevelFlag() {
    debAdvLevel = false;
  }

  private static boolean stepFlag;
  private static boolean stepMode;

  private static boolean pauseStatus;
  private static int pauseDelay;
  private static final int PAUSE_FRAMES = VidGame.FPS / 4;
  private static Joystick joystick;
  private static long startTime;
  private static Thread logicThread; // the main thread
  private static VidGameInt vidGameInt;
  private static Applet applet;
  private static int bonusLifeSfx;
  private static ThreadCommand tcDrawing;

  private static int livesLeft;
  private static int level;
  private static int score;
  private static int highScore;
  private static int[] bonusScores;
  private static int time;
  private static int mode;

  private static boolean debAdvLevel;
  private static Component component;
  private static ThreadCommand tcDrawSignal;
  private static boolean fLoading;

  private static int language = LANG_ENGLISH;

  // ===================================
  // KeyListener interface
  // ===================================
  public void keyTyped(KeyEvent e) {}

  public void keyPressed(KeyEvent e) {
    processKeyEvent(e, true);
  }

  public void keyReleased(KeyEvent e) {
    processKeyEvent(e, false);
  }

  // ===================================
  // MouseMotionListener interface
  // ===================================
  private static Point mousePoint = new Point();
  private static int cursorType = Cursor.DEFAULT_CURSOR;
  private static int currentCursor = -1;
  private static int desiredCursor;

  public void mouseDragged(MouseEvent e) {
//        mouseInactiveCycles = 0;
    mousePoint.setLocation(e.getPoint());
  }

  public void mouseMoved(MouseEvent e) {
    mousePoint.setLocation(e.getPoint());
//        mouseInactiveCycles = 0;
  }

  public static Point getMousePoint() {
    return new Point(mousePoint);
  }

  public static void setCursorType(int type) {
    cursorType = type;
  }

  /*
       // Modify the mouse icon appearance.
       // Make it invisible if it hasn't moved for a while.
       private static void updateMouseDisplay() {
      boolean showStatus = (++mouseInactiveCycles < FPS * 1);
      if (showStatus != prevStatus) {
          prevStatus = showStatus;
          component.setCursor(showStatus ? null : cursor);
      }
       }
       private static int mouseInactiveCycles;
       private static Cursor cursor;
       private static boolean prevStatus;
       private static void prepareMouse() {
      cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
      mouseInactiveCycles = 0;
//        Image img = component.createImage(32,32);
//        cursor = Toolkit.getDefaultToolkit().createCustomCursor(
//            img, new Point(0,0), "Blank cursor");
       }
   */
  // ===================================
  // MouseListener interface
  // ===================================
  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    joystick.processMouseButtonEvent(true);
  }

  public void mouseReleased(MouseEvent e) {
    joystick.processMouseButtonEvent(false);
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}