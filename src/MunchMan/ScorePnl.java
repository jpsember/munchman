package MunchMan;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class ScorePnl extends Scoreboard implements IAnimScript {

	private MunchMan parent;
	private Player player;

    private static final int[] scoreLocs = {
        28,390,
        28,410,
    };

	public ScorePnl(MunchMan parent, Player player) {
        super(scoreLocs, parent.charSet1);
		this.parent = parent;
		this.player = player;
	}

	public void plotChanges() {
        super.plotChanges();
		if (!BEngine.layerValid()) {
			Sprite title = new Sprite(parent.sprite,304,0,236,180);
			BEngine.drawSprite(title, 0, 0);
		}

		plotMen();
		plotFruit();
		plotMsg();
	}

	private final static int MEN_X = 18;
	private final static int MEN_Y = 330;
	private final static int MEN_SPACING = 29;
	private final static int MEN_TOTAL = 6;
	private final static int MEN_WIDTH = 28;
	private int menPlotFlags;

	private void plotMen() {
		int newPlotFlags = 0;
        boolean valid = BEngine.layerValid();
		for (int i = 0; i < MEN_TOTAL; i++) {
			int flag = 1 << i;
			int plotFlag = 0;

			if (VidGame.getMode() == VidGame.MODE_PLAYING)
				plotFlag = (VidGame.getLives() > i) ? flag : 0;

			if (!valid || ((menPlotFlags & flag) != plotFlag)) {

				int x = MEN_X + MEN_SPACING * i;

				BEngine.setColor(Color.black);
				BEngine.fillRect(x-MEN_WIDTH/2, MEN_Y - MEN_WIDTH/2, MEN_WIDTH, MEN_WIDTH);
				if (plotFlag != 0)
					BEngine.drawSprite(player.icon(), x, MEN_Y);
			}
			newPlotFlags |= plotFlag;
		}
		menPlotFlags = newPlotFlags;
	}

	private final static int FRUIT_X = 18;
	private final static int FRUIT_Y = 362;
	private final static int FRUIT_SPACING = 29;
	private final static int FRUIT_MAX_X = 7;

	private int oldLevel;
	private void plotFruit() {
        boolean valid = BEngine.layerValid();
		int newLevel = VidGame.getLevel() + 1;

		if (VidGame.getMode() <= VidGame.MODE_PREGAME)
			newLevel = 0;

		if (newLevel != oldLevel || !valid) {
			oldLevel = newLevel;
			int base = newLevel - FRUIT_MAX_X;
			if (base < 0) base = 0;
			for (int i = 0; i < FRUIT_MAX_X; i++) {
				int x = i * FRUIT_SPACING + FRUIT_X;
				int y = FRUIT_Y;
				int j = i + base;
				BEngine.setColor(Color.black);
				BEngine.fillRect(x - FRUIT_SPACING/2, y - FRUIT_SPACING/2, FRUIT_SPACING, FRUIT_SPACING);
				if (j < newLevel)
					Fruit.drawMisc(Fruit.typeForLevel(j), x, y);
			}
		}
	}

    private int prevMsg;
    private AnimScript script;

    private static final short[][] scripts = {
        {CSTRING,0, Y,235, END},

        {CSTRING,1, Y,235,
         CSTRING,2, Y,255,
         CSTRING,3, Y,275, END},

        {CSTRING,4, Y,210,
         CSTRING,5, Y,250, START, 800,
         CSTRING,6, Y,265, START, 800,
         CSTRING,7, Y,280, START, 800, END},

        {CSTRING,8, Y,255, END},

        {CSTRING,9, Y,255, END}
    };

    public Object getObject(AnimScript script, int id) {
        final String strings[] = {
            "LOADING...",
            "PRESS",
            "SPACE BAR",
            "TO START",
            "CONTROLS",
            "  ^        ",
            "<   >  MOVE",
            "  ~        ",
            "GAME OVER",
            "GAME PAUSED",
        };
        return strings[id];
    }

    private void plotMsg() {
        boolean valid = BEngine.layerValid();
        if (!valid) {
            prevMsg = 0;
            script = null;
        }

        int msg = 0;

		if (VidGame.getMode() == VidGame.MODE_GAMEOVER)
            msg = 4;

		if (VidGame.paused())
            msg = 5;

        if (VidGame.loading())
            msg = 1;

		if (msg == 0 && VidGame.getMode() <= VidGame.MODE_PREGAME) {
			int time = (VidGame.getTime() * (1024 / VidGame.FPS)) % 14000;
			if (time < 6000)
                msg = 2;
			else
                msg = 3;
		}
//        db.pr("VidGame.loading = "+VidGame.loading()+" msg = "+msg);

        if (msg != prevMsg) {
            if (prevMsg != 0) {
                script.stop();
                prevMsg = 0;
                script = null;
            }
            if (msg != 0) {
                script = new AnimScript(scripts[msg-1], parent.charSet1, this);
            }
            prevMsg = msg;
        }
        if (script != null)
            script.update();
    }

}