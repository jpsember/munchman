package mytools;

import java.io.*;
import java.awt.*;
import java.applet.*;
import vgpackage.*;

/**
 * Title:        db
 * Description:  Class for miscellaneous debugging features.
 * @author Jeff Sember
 * @version 1.0
 */
public class Debug {

  private Debug() {}

  /**
   * Initializes the debug class for an applet.  This establishes
   * the browser context.
   * @param a the applet
   */
  public static void init(Applet a) {
    applet = a;
  }

  public static String ptStringScaled(Pt p) {
    return "[" + (p.x >> VidGame.FRACBITS) + "," + (p.y >> VidGame.FRACBITS) +
        "]";
  }

  /**
   * Displays an error string to System.out, and to the
   * status line of the browser.
   * Only the first error is reported to the status line of
   * the browser.
   * @param str the string to display
   */
  public static void dispError(String str) {
    print(str);
    if (!errorReported) {
      errorReported = true;
      showStatus(str);
    }
  }

  /**
   * Displays a string in the status line of the browser.  If
   * init(applet) has not been called, nothing is displayed.
   * @param s the string to display
   * @see #init(Applet)
   */
  public static void showStatus(String s) {
    if (applet != null)
      applet.showStatus(s);
  }

  /**
   * Makes the current thread sleep for a specified time.  Ignores
   * any InterruptedExceptions that occur.
   * @param time time, in milliseconds, to sleep() for
   */
  public static void delay(int time) {
    try {
      Thread.sleep(time);
    }
    catch (InterruptedException e) {}
  }

  /**
   * Prints a string to System.out.  Calls System.out.println(str)
   * @param str the string to print
   */
  public static void print(String str) {
    System.out.println(str);
  }
  /**
     Prints a string to System.out.  Calls System.out.println(str)
     @param str the string to print
   */
  public static void pr(String str) {
    System.out.println(str);
  }

  /**
   * Tests an assertion.  If the assertion is false, prints
   * an error message to System.out, and if init(Applet) was
   * called, to the browser status line.  Also prints a stack
   * trace to System.out, and finally calls System.exit(1).
   * @see #init(Applet)
   */
  public static void ASSERT(boolean flag, String message) {
    if (!flag) {
      dispError("ASSERTION FAILED: " + message);
      System.out.println(getStackTrace()); // print stack
      System.exit(1);
    }
  }

  /**
   * Returns a string representing x,y coordinates
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  public static String p2String(int x, int y) {
    String s;
    s = "(" + x + "," + y + ") ";
    return s;
  }

  /**
   * Returns a string representing a rectangle
   * @param x the x-coordinate of the top-left corner
   * @param y the y-coordinate of the top-left corner
   * @param w the width of the rectangle
   * @param h the height of the rectangle
   */
  public static String rString(int x, int y, int w, int h) {
    String s;
    s = "(Loc=" + x + "," + y + ", Size=" + w + "," + h + ") ";
    return s;
  }

  /**
   * Returns a string representing a rectangle, scaled by VidGame.FRACBITS
   * @param x the x-coordinate of the top-left corner
   * @param y the y-coordinate of the top-left corner
   * @param w the width of the rectangle
   * @param h the height of the rectangle
   */
  public static String rStringScaled(Rectangle r) {
    String s;
    s = "(Loc=" + (r.x >> VidGame.FRACBITS) + "," + (r.y >> VidGame.FRACBITS) +
        ", Size=" + (r.width >> VidGame.FRACBITS) + "," +
        (r.height >> VidGame.FRACBITS) + ") ";
    return s;
  }

  /**
   * Returns a string representing a rectangle
   * @param r the Rectangle
   */
  public static String rString(Rectangle r) {
    return r.toString();
  }

  // Displays a stack trace to System.out
  private static void dispStackTrace() {
    Throwable t = new Throwable();
    t.printStackTrace();
  }

  // gets a stack trace into a string
  private static String getStackTrace() {
    Throwable t = new Throwable(); // for getting stack trace
    ByteArrayOutputStream os = new ByteArrayOutputStream(); // for storing stack trace
    PrintStream ps = new PrintStream(os); // printing destination
    t.printStackTrace(ps);
    return os.toString();
  }

  private static Applet applet;
  private static boolean errorReported;
}