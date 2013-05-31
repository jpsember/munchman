package vgpackage;
import java.awt.*;
import mytools.*;

public class Scoreboard {

    private Scoreboard() {
    }

    private static final int LABELS_TOTAL = 2;

    public Scoreboard(int[] sl, CharSet cs) {
        scoreLocations = sl;
        charSet = cs;

        langOffset = LABELS_TOTAL * VidGame.getLanguage(labels.length / LABELS_TOTAL);

        for (int i = 0; i < SCORES_TOTAL; i++)
            labelWidths[i] = charSet.stringBounds(labels[langOffset + 0]).x;
    }

	public void plotChanges() {//Graphics g, boolean valid) {
		if (!BEngine.layerValid())
			BEngine.clearView();//g);
		plotScores();//g, valid);
	}

    private static final String[] labels = {
        // LANG_ENGLISH
        "SCORE:",
        "HIGH:",

        // LANG_FRENCH
        "SCORE:",
        "HIGH:",

    };

    private int[] labelWidths = new int[SCORES_TOTAL];

	private void plotScores() {//Graphics g, boolean valid) {

		for (int i = 0; i < SCORES_TOTAL; i++) {
            int j = i << 1;
            int sx = scoreLocations[j+0];
            int sy = scoreLocations[j+1] + charSet.getSizeY();

            boolean valid = BEngine.layerValid();
			if (!valid || drawnScores[i] != VidGame.getScore(i)) {

//                Graphics g = BEngine.getGraphics();
        		if (!valid) {
                    String s = labels[langOffset + i];
                    if (VidGame.DEBUG) {
                        if (i == 1)
                            s = "HIGH:";
                    }

        			charSet.plotString(s,sx,sy);
                }
  				drawnScores[i] = VidGame.getScore(i);

                sx += labelWidths[i];

                BEngine.setColor(Color.black);
//				g.setColor(Color.black);
				charSet.clearBounds(/*g,*/sx,sy,SCORE_DIGITS_TOTAL);
				charSet.plotString(//g,
                    cvtScoreToString(drawnScores[i]),sx,sy);
			}
		}
	}

	private static String cvtScoreToString(int n) {
        String s = Integer.toString(n);
        if (s.length() > SCORE_DIGITS_TOTAL)
            s = "";

        StringBuffer sb = new StringBuffer(SCORE_DIGITS_TOTAL);
        int padCount = SCORE_DIGITS_TOTAL - s.length();
        while (padCount-- > 0)
            sb.append(' ');
        sb.append(s);
        return sb.toString();
	}

	private static final int SCORES_TOTAL = 2;
	private static final int SCORE_DIGITS_TOTAL = 7;

    private int[] scoreLocations;
    private CharSet charSet;
    private int labelWidth;
    private int langOffset;

	private int[] drawnScores = new int[SCORES_TOTAL];
	private String[] strs = new String[SCORES_TOTAL];
}