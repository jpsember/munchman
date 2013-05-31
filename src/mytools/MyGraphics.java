package mytools;
import java.awt.*;

public class MyGraphics {

   /**
    * Fill a rectangle with the current color
    * @param g Graphics object
    * @param r Rectangle to fill
    */
   public static void fillRect(Graphics g, Rectangle r) {
      g.fillRect(r.x,r.y,r.width,r.height);
   }

}