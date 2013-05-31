package vgpackage;

import java.awt.*;
import mytools.*;

public final class ShimmerLine {

  private static final int masks[] = {
      0x6ed7b5da, 0x7fffffff - 0x44891222, 0x6ed7b5da >> 1,
      0x7fffffff - (0x44891222 >> 1),
      0x5ab5a656, 0x7fffffff - 0x4a52a524, 0x5ab5a656 >> 1,
      0x7fffffff - (0x4a52a524 >> 1),
      0x4a52a524, 0x7fffffff - 0x5ab5a656, 0x4a52a524 >> 1,
      0x7fffffff - (0x5ab5a656 >> 1),
      0x44891222, 0x7fffffff - 0x6ed7b5da, 0x44891222 >> 1,
      0x7fffffff - (0x6ed7b5da >> 1),
  };

  private static int noiseIndex = 0;
  /**
     Plot a 'shimmering' line, one that has gaps in it

     @param weight : weight of line (0: few gaps; 3: mostly gaps)
     @param g : Graphics object
     @param x0 : start x coordinate
     @param y0 : start y coordinate
     @param x1 : end x coordinate (inclusive)
     @param y1 : end y coordinate
   */
  public static void plot(int weight, Graphics g, int x0, int y0, int x1,
                          int y1) {
    noiseIndex++;
    int mask = masks[weight * 4 + (noiseIndex & 3)];

    // We need to implement a Bresenham line function.

    int xd = Math.abs(x1 - x0);
    int yd = Math.abs(y1 - y0);
    boolean xFlag = (xd >= yd);

    int pixelTotal;
    int mainInc, auxInc;
    int mainAdd, auxAdd;

    if (xFlag) {
      pixelTotal = xd;
      mainAdd = xd;
      auxAdd = yd;
      mainInc = (x1 >= x0 ? 1 : -1);
      auxInc = (y1 >= y0 ? 1 : -1);
    }
    else {
      pixelTotal = yd;
      mainAdd = yd;
      auxAdd = xd;
      mainInc = (y1 >= y0 ? 1 : -1);
      auxInc = (x1 >= x0 ? 1 : -1);
    }

    int accum = mainAdd >> 1;
    int bitFlag = (1 << 30);

    do {
      if ( (bitFlag & mask) != 0)
        g.drawLine(x0, y0, x0, y0);
      if (xFlag) {
        x0 += mainInc;
        accum -= auxAdd;
        if (accum < 0) {
          accum += mainAdd;
          y0 += auxInc;
        }
      }
      else {
        y0 += mainInc;
        accum -= auxAdd;
        if (accum < 0) {
          accum += mainAdd;
          x0 += auxInc;
        }
      }
      bitFlag >>= 1;
      if (bitFlag == 0)
        bitFlag = (1 << 30);
    }
    while (pixelTotal-- != 0);
  }
}