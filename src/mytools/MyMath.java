package mytools;
import java.util.*;

/**
 * Title:        MyMath
 * Description:  This class encompasses various mathematical
 *               functions that I have found useful.
 * @author Jeff Sember
 * @version 1.0
 */

public final class MyMath {

   private MyMath() {}

   /**
    * Forces an integer into range of a lower and upper bounds.
    * @param min the lower bounds
    * @param max the upper bounds
    * @return an integer, n, that satisfies (min <= n <= max)
    */
	public static int clamp(int val, int min, int max) {
		if (val < min) return min;
		if (val > max) return max;
		return val;
	}
   /**
    * Forces an integer into range of a lower and upper bounds.
    * @param min the lower bounds
    * @param max the upper bounds
    * @return an integer, n, that satisfies (min <= n <= max)
    */
	public static float clamp(float val, float min, float max) {
		if (val < min) return min;
		if (val > max) return max;
		return val;
	}


	/**
    *  A better mod function.  This behaves as expected for negative
    *  numbers.  It has the following effect:  if the input value is
    *  negative, then enough multiples of the divisor are added to it
    *  to make it positive, then the standard mod function (%) is
    *  applied.
    *  @param value the number on the top of the fraction
    *  @param divisor the number on the bottom of the fraction
    *  @return the remainder of value / divisor.
    */
	public static int mod(int value, int divisor) {
		value = value % divisor;
		if (value < 0)
			value += divisor;
		return value;
	}
	/**
    *  A better mod function.  This behaves as expected for negative
    *  numbers.  It has the following effect:  if the input value is
    *  negative, then enough multiples of the divisor are added to it
    *  to make it positive, then the standard mod function (%) is
    *  applied.
    *  @param value the number on the top of the fraction
    *  @param divisor the number on the bottom of the fraction
    *  @return the remainder of value / divisor.
    */
	public static float mod(float value, float divisor) {
		value = (int)value % (int)divisor;
		if (value < 0)
			value += divisor;
		return value;
	}



   /**
    * Chooses a non-negative random number
    * @param range 2..n for a random value between 0..range-1, or 0
    * for any int >= 0
    * @return an int n >= 0, and if range was > 0, n < range
    */
	public static int rnd(/*Random r,*/ int range) {
		int val = random.nextInt() & (int)~0x80000000;
		if (range > 0)
			val = val % range;
		return val;
	}
   /**
    * Chooses a non-negative random number
    * @param range 2..n for a random value between 0..range-1, or 0
    * for any int >= 0
    * @return an int n >= 0, and if range was > 0, n < range
    */
	public static float rnd(float range) {
          int irange = (int)(range * 256);
          int val = random.nextInt() & (int)~0x80000000;
	  if (irange > 0)
            val = val % (int)irange;
	  return val / 256f;
	}


   /**
    * Choose a random number that is centered around zero
    * @param r the Random object
    * @param range a number from 2...n.  If range is even, it will
    * return a value that satisfies (-(range / 2) <= n < (range / 2)).
    * If range is odd, it will satisfy (-(range / 2) <= n <= (range / 2).
    * @return a random number from -range/2 ... range/2
    */
	public static int rndCtr(/*Random r,*/ int range) {
		return rnd(range) - (range >> 1);
	}

   private static Random random = new Random();
}