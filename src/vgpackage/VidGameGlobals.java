package vgpackage;
public interface VidGameGlobals {
   /** Determines if this class is compiled with extra debugging
    *  features.  These features include the ability of single
    *  stepping through the logic.  Step mode is activated by typing
    *  a period.  The gpame freezes on the current frame.  Typing
    *  another period will advance by a single frame.  To resume
    *  normal play, type a comma.  Another debug feature is instant
    *  level advance, which is invoked by typing an L.
    */
	public static final boolean DEBUG = false;

        public static final boolean WITH_SFX = true || !DEBUG;

   /** The frame rate of the video game.  This is the number
    *  of frames per second that the logic will run at.  The program
    *  will attempt to draw the frames as quickly as the logic
    *  produces them, but on slower machines some frames will be
    *  dropped.
    */
	public static final int FPS = 32;

   // !!!!!! Move FRACBITS, FPS, ONE to MyMath.

   /** The number of fractional bits in the world space. */
	public static final int FRACBITS = 11;

   /** One pixel, as expressed in world space coordinates. */
	public static final int ONE = (1 << FRACBITS);

   /** One 256th of a pixel, as expressed in world space coordinates. */
	public static final int DASH = (ONE / 256);
   /** One 256th of a pixel, as expressed in world space coordinates. */
	public static final float FDASH = 1 / 256.0f;

   /** The duration of one cycle of the game animation, in milliseconds. */
	public static final int CYCLE = (1000 / FPS);

   /** (1/16) pixels per second, expressed in world space coordinates,
    *  and adjusted for the frame rate.
    */
	public static final int TICK = ((ONE * 16) / (256 * FPS));
   /** (1/16) pixels per second, expressed in world space coordinates,
    *  and adjusted for the frame rate.
    */
	public static final float FTICK = (1 / (16f * FPS));

   /** Game mode for pregame.  This is when the game is in 'attract'
    *  or self-running 'demo' mode.  The user will be prompted to
    *  start a game (i.e. "press space bar to start").
    */
	public static final int MODE_PREGAME = 0;
   /** Game mode for playing a game.  The user is currently playing
    *  the game.
    */
	public static final int MODE_PLAYING = 1;
   /**
    * Game mode for a game just having finished.  This is where a
    * "game over" message would be displayed.  After this mode,
    * the game will return to MODE_PREGAME.
    */
	public static final int MODE_GAMEOVER = 2;

    public static final int LANG_ENGLISH = 0;
    public static final int LANG_FRENCH = 1;
    public static final int LANG_TOTAL = 2;

	public static final int TRIGBITS = FRACBITS;

	public static final short
    //END = 0,
    MOVETO = 1
   ,LINETO = 2
   ,COLOR = 3
   ,CENTER = 4
   ,COLPT = 5
   ,POLYPT = 6
   ;
	public static final int MAX = (256 << TRIGBITS);
	public static final double RADTODEG = ((MAX/2) / Math.PI);
	public static final int ADASH = (1 << TRIGBITS) / VidGame.FPS;
}
