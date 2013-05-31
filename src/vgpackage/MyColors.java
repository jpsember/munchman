package vgpackage;

import java.awt.*;
import mytools.*;

public class MyColors {

   public static final int DEFAULT_TOTAL = 1;

	// Colors:
	public static int maxColors() {
          return maxColors;
	}
        private static int maxColors;
	public static final int COLOR_LEVELS = 32;
	private static Color[] colors;

	public static void set(Graphics g, int hue, int level) {
		if (level < 0)
			level = 0;
		else if (level >= COLOR_LEVELS)
			level = COLOR_LEVELS-1;
		g.setColor(colors[hue * COLOR_LEVELS + level]);
	}

	public static void init(int n) {
		maxColors = n;

		colors = new Color[maxColors * COLOR_LEVELS];

		// Prepare some default colors.

		add(0, 255,255,255);

	}

	public static void add(int index, int r, int g, int b) {
		for (int i=0; i<COLOR_LEVELS; i++) {
			int scale = (i << 8) / (COLOR_LEVELS/2);
			int rs = (r * scale) >> 8;
			int gs = (g * scale) >> 8;
			int bs = (b * scale) >> 8;
			if (rs > 255) rs = 255;
			if (gs > 255) gs = 255;
			if (bs > 255) bs = 255;

			colors[index * COLOR_LEVELS + i] = new Color(rs,gs,bs);
		}
	}

}