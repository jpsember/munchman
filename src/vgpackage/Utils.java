package vgpackage;
import java.io.*;
/**

   Utils.java : miscellaneous useful functions

*/
public class Utils implements VidGameGlobals {
  /** Performs an assertion, a test for an illegal program condition.
      Can be used to verify preconditions for public methods, for instance.

      @param flag : if false, assertion will fail, the program will
          be halted, and a stack trace will be displayed
   */
  public static void ASSERT(boolean flag) {
    if (DEBUG)  {
      if (!flag)
	ASSERT(flag, "");
    }
  }

  /** Performs an assertion, a test for an illegal program condition.
      Can be used to verify preconditions for public methods, for instance.

      @param flag : if false, assertion will fail, the program will
          be halted, a message and stack trace will be displayed
      @param message : message to display if assertion fails
   */
  public static void ASSERT(boolean flag, String message) {
    if (DEBUG) {
      if (!flag) {
	System.out.println("Assertion failure: " + message);
	System.out.println(getStackTrace(3));
        System.exit(-1);
      }
    }
  }

  /** Constructs a string containing a stack trace.
      A debugging aid, used by the assert() method.

      @return A string describing the calls on the stack.
   */
  public static String getStackTrace() {
    return getStackTrace(1);
  }

  /** Constructs a string containing a stack trace.
      A debugging aid, used by the assert() method.

      @param trimAmount : number of calls to remove from
        front of string before returning

      @return A string describing the calls on the stack.
   */
  public static String getStackTrace(int trimAmount) {
    if (DEBUG) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      Throwable t = new Throwable();
      t.printStackTrace(new PrintStream(os));
      String s = os.toString();
      int start = 0;
      while (trimAmount-- > 0) {
        while (true) {
          if (start == s.length()) break;
          start++;
          if (s.charAt(start-1) == 0x0a) break;
        }
      }
      return s.substring(start);
    } else return null;
  }

  /** Print a string; DEBUG only.  Prints a string followed by a newline.

   */
  public static void pr(String s) {
    if (DEBUG)
      System.out.println(s);
  }

  /**
     Given a string, trim off a CR and anything following it

     @param s : string to process
     @return the possibly truncated string
   */
  public static String trimCR(String s) {
      int cr = s.indexOf('\n');
      if (cr < 0)
        return s;
      return s.substring(0,cr);
  }

  /**
     Add spaces to a StringBuffer until its length is at
     some value.  Sort of a 'tab' feature, useful for
     aligning output.

     @param sb : StringBuffer to pad out
     @param len : desired length of StringBuffer; if
        it is already past this point, nothing is added to it
   */
  public static void tab(StringBuffer sb, int len) {
    while (sb.length() < len)
      sb.append(' ');
  }

  /**
     DEBUG only:
     Convert an array of ints as a string.

   */
  public static String arrayToString(int[] a) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(' ');
      sb.append(a[i]);
    }
    sb.append(']');
    return sb.toString();
  }

  /**
     Convert a double to a right-justified string of fixed
     length (spaces added to left side).

     @param val : double to display in string
     @param len : desired length of string (if integer
        representation exceeds this amount, string is not
	truncated)
     @param dec : number of decimal places
   */
  public static String formatDbl(double val, int len, int dec) {
    final StringBuffer sb = new StringBuffer(30);

    sb.setLength(0);
    final double[] powers = {
      1,10,100,1000,10000,100000,10000000
    };
    String s = Integer.toString((int)(val * powers[dec]));
    tab(sb, len - 1 - s.length());
    sb.append(s);

    int k = sb.length() - dec;
    sb.insert(k,'.');
    while (true) {
      k++;
      if (sb.charAt(k) != ' ') break;
      sb.setCharAt(k,'0');
    }
    return sb.toString();
  }

  /**
     Convert an integer to a right-justified string of fixed
     length (spaces added to left side).

     @param val : integer value to display in string
     @param len : desired length of string (if integer
        representation exceeds this amount, string is not
	truncated)
   */
  public static String formatInt(int val, int len) {
    final StringBuffer sb = new StringBuffer(30);

    sb.setLength(0);
    String s = Integer.toString(val);
    tab(sb, len - s.length());
    sb.append(Integer.toString(val));
    return sb.toString();
  }

  /**
     Determine the decimal value of a character.

     @return 0..9 if it's '0'..'9', otherwise -1
   */
  public static int decValue(char c) {
    int val = c - '0';
    if (val < 0 || val > 9) val = -1;
    return val;
  }

  /**
     Determine the octal value of a character.

     @return 0..7 if it's '0'..'7', otherwise -1
   */
  public static int octValue(char c) {
    int val = c - '0';
    if (val < 0 || val >= 8) val = -1;
    return val;
  }

  /**
     Determine the hex value of a character.

     @return 0..15 if it's '0'..'9','A'..'F','a'..'f';
        otherwise return -1
   */
  public static int hexValue(char c) {
    c = Character.toUpperCase(c);
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    return -1;
  }

  /**
     Parse a string as a value.

     @param s : String containing value; expressed as
         base 10 number (dddd..), where d is a digit '0'..'9';
	 hex value ($hhh..), where h is a digit or 'A'..'F','a'..'f'
	 binary value (bddd..), where d is '0'..'1'

     @return value parsed
     @exception  NumberFormatException if the string doesn't
         conform to one of the expected formats, or an overflow
	 occurs
  */
  public static int parseValue(String s)
      throws NumberFormatException {

    if (s.length() == 0)
      s = "?";  // Cause an exception below.
    int value = 0;

    String s2 = s.substring(1);

    switch (Character.toUpperCase(s.charAt(0))) {
      case '$':
	value = parseHexValue(s2);
	break;
      case 'B':
	value = parseBinaryValue(s2);
	break;
      default:
	value = Integer.parseInt(s);
	break;
    }
    return value;
  }

  /**
     Parse a string as a hex value.

     @param s : string containing hex digits '0'..'9',
         'A'..'F', 'a'..'f'

     @return integer value

     @exception  NumberFormatException if the string contains
         illegal characters or has length zero
  */
  public static int parseHexValue(String s)
      throws NumberFormatException {

    if (s.length() == 0)
      s="?"; // cause exception below

    long value = 0;
    for (int i = 0; i < s.length(); i++) {
      int digit = hexValue(s.charAt(i));
      value = (value << 4) | digit;
      if (digit < 0 || value > Integer.MAX_VALUE)
	throw new NumberFormatException();
    }
    return (int)value;
  }

  /**
     Parse a string as a binary value.

     @param s : string containing binary digits '0' or '1'

     @return integer value

     @exception  NumberFormatException if the string contains
         illegal characters or has length zero
  */
  public static int parseBinaryValue(String s)
      throws NumberFormatException {

    if (s.length() == 0)
      s="?"; // cause exception below

    long value = 0;
    for (int i = 0; i < s.length(); i++) {
      int digit = s.charAt(i) - '0';
      value = (value << 1) | digit;
      if (digit < 0 || digit >= 2 || value > Integer.MAX_VALUE)
	throw new NumberFormatException();
    }
    return (int)value;
  }

  /**
     Determine if a value occurs in an array.

     @param val : value to search for
     @param array : array to search in

     @return true if val found in array
   */
  public static boolean inArray(int val, int[] array) {
    int i = array.length;
    while (i-- > 0)
      if (val == array[i])
        return true;
    return false;
  }

  /**
     Constructor.  Private, since no instances of this
     class can be instantiated.
   */
  private Utils() {}

  /**
     DEBUG only:
     Print a hex dump of an array of bytes.

     @param a : array to print
   */
  public static void hexDump(byte[] a) {
 {
      int rowSize = 16;
      int[] vals = new int[rowSize];

      long len = a.length;
      int i = 0;
      while (i < len) {
        int rSize = rowSize;
        if (rSize + i > len)
          rSize = (int) (len - i);

        writeHex(i, 4);
        System.out.print(": ");
        for (int j = 0; j < rowSize; j++) {
          if (j < rSize) {
            int val = a[i + j];
            writeHex(val, 2);
            vals[j] = (byte) val;
          }
          else
            System.out.print("  ");

          System.out.print(' ');
          if ( (j & 3) == 3)
            System.out.print(' ');
        }
        System.out.print(' ');
        for (int j = 0; j < rSize; j++) {
          int v = vals[j] & 0x7f;
          if (v < 0x20)
            v = '.';
          System.out.print( (char) v);
        }

        System.out.print('\n');
        i += rSize;
      }
    }
  }

  /**
     Return a string describing a value as a 4-digit hex value.
   */
  public static String toHex(int val) {
    return toHex(val, 4);
  }

  /**
     Return a string that breaks a value down into binary flags,
     with '.' for zeros, and characters from a string for ones.

     @param val : value to convert
     @param flags : string containing chars to print for each bit
       position, with high bit first
   */
  public static String toBinary(int val, String flags) {

    StringBuffer sb = new StringBuffer(flags);
    int bit = 1;
    for (int j = flags.length() - 1; j >= 0; j--, bit <<= 1) {
      if ((val & bit) == 0)
        sb.setCharAt(j,'.');
    }
    return sb.toString();
  }

  /**
     Convert a number to a hex string.

     @param val : number to convert
     @param digits : number of hex digits
     @return string containing hex digits
   */
  public static String toHex(int val, int digits) {
      StringBuffer sb = new StringBuffer(digits);
      final char[] chars = {
          '0', '1', '2', '3', '4', '5', '6', '7',
          '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
      };
      for (int j = digits - 1; j >= 0; j--) {
        int d = (val >> (j << 2)) & 0xf;
        sb.append(chars[d]);
      }
      return sb.toString();
  }

  /**
     DEBUG only:
     Write a value as a series of hex digits.

     @param val : (unsigned) integer value
     @param digits : number of hex digits to extract from val
   */
  private static void writeHex(int val, int digits) {
    {
      System.out.print(toHex(val,digits));
    }
  }

  public static String dashedLine =
  "-----------------------------------------------------------------------";
}