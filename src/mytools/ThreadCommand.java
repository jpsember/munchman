package mytools;

/**
 * Title:        ThreadCommand class
 * Description:  Facilitates communication between threads.  The
 *               class supports thread-safe access to an array of
 *               integer arguments.
 * Copyright:    Copyright (c) 2001
 * Company:      Sember Enterprises
 * @author Jeff Sember
 * @version 1.0
 */

public class ThreadCommand {

   /**
    * Constructs an object with an array that
    * has a single int argument.
    */
   public ThreadCommand() {
      this(1);
   }

   /**
    * Constructs an object with an array that has
    * the specified number of int arguments.
    * @param iMaxArgs the number of int arguments to allocate
    */
   public ThreadCommand(int iMaxArgs) {
      aiArgs = new int[iMaxArgs];
   }

   /**
    * Constructs an object with the same number of
    * arguments as another object.  The arguments are NOT
    * copied from the source object.
    * @param t Source ThreadCommmand
    */
   public ThreadCommand(ThreadCommand t) {
      this(t.aiArgs.length);
   }

   /**
    * Returns a string description of the object.
    */
   public String toString() {
      String s = "ThreadCommand Signal " +
         (readSignal() ? 'T' : 'F')
         + " Args [";
      int l = aiArgs.length;
      for (int i = 0; i < l; i++) {
         s += aiArgs[i];
         if (i+1 < l)
            s += ",";
      }
      s += "]";
      return s;
   }

/*
  public void setCMD(int iCmd) {
    setArg(CMD,iCmd);
  }

  public int getCMD() {
    return getArg(CMD);
  }

  public void setOPER(int iOper) {
    setArg(OPER,iOper);
  }

  public int getOPER() {
    return getArg(OPER);
  }
*/

   /**
    * Attempt to attain a 'lock' on the object.  The first int
    * argument represents the state of the lock: 0 if unlocked, 1
    * if locked.  This method and releaseLock() are useful to
    * control access to the object between multiple threads.
    * @return true if a lock was attained
    * @see #releaseLock()
    */
    /*
   public synchronized boolean attemptLock() {
      boolean fLockAttained = false;
      if (getArg(LOCK) == 0) {
         fLockAttained = true;
         setArg(LOCK, 1);
      }
      return fLockAttained;
   }
*/
   /**
    * Release a lock on the object previously attained by
    * attemptLock().  Clears the first integer argument to 0.
    * @see #attemptLock()
    */
    /*
   public synchronized void releaseLock() {
      setArg(LOCK,0);
   }
   */
   /**
    * Set the signal flag.
    * The signal methods are useful for threads to signal such things as
    * an operation being completed, or a message needing processing.
    * Another use is to limit access of the object to a single thread.
    * This method returns a flag that indicates whether the flag was
    * already set, in which case the caller can decide not to access
    * the object.
    * This is not a synchronized method; use setSignalLock() as an
    * alternative.
    * @return true if signal was not already set
    * @see #setSignalLock()
    * @see #testSignal()
    * @see #testSignalLock()
    * @see #readSignal()
    * @see #readSignalLock()
    * @see #clearSignal()
    * @see #clearSignalLock()
    */
   public boolean setSignal() {
      boolean f = !fSignal;
      if (f)
         fSignal = true;
      return f;
   }

   /**
    * Synchronized version of setSignal().
    * @return true if signal was not already set
    * @see #setSignal()
    */
   public synchronized boolean setSignalLock() {
      return setSignal();
   }

   /**
    * Test if the signal argument as been set, and clear it if so.
    * See setSignal() for a description of the signal flag.
    * This is not a synchronized method; use testSignalLock()
    * as an alternative.
    *
    * @return true if signal argument was set
    * @see #setSignal()
    */
   public boolean testSignal() {
      boolean f = readSignal();
      clearSignal();
      return f;
   }

   /**
    * Synchronized version of testSignal().
    * @return true if signal argument was set
    * @see #testSignal()
    */
   public synchronized boolean testSignalLock() {
      return testSignal();
   }

   /**
    * Test if the signal flag has been set, but don't clear it.
    * See setSignal() for a description of the signal flag.
    * This is not a synchronized method; use readSignalLock()
    * as an alternative.
    *
    * @return true if signal flag is set
    * @see #setSignal()
    */
   public boolean readSignal() {
      return fSignal;
//      return (getArg(SIGNAL) != 0);
   }

   /**
    * Synchronized version of readSignal().
    *
    * @return true if signal flag is set
    * @see #readSignal()
    */
   public synchronized boolean readSignalLock() {
      return readSignal();
   }

   /**
    * Clears the signal flag.
    *
    * This is not a synchronized method; use clearSignalLock()
    * as an alternative.
    *
    * @see #setSignal()
    */
   public void clearSignal() {
      fSignal = false;
   }

   /**
    * Synchronized version of clearSignal()
    * @see #clearSignal()
    */
   public synchronized void clearSignalLock() {
      clearSignal();
   }

/* Are these necessary?
   public synchronized int getAndClearArgLock(int iIndex) {
      return getAndClearArg(iIndex);
   }

   public synchronized int getAndClearArg(int iIndex) {
      int i = getArg(iIndex);
      setArg(iIndex,0);
      return i;
   }
*/
   /*
   public synchronized int getAndClearArgLock(int iIndex) {
      int i = getArg(iIndex);
      setArg(0,0);
      return i;
   }
*/
   /**
    * Get an integer argument.
    * This is not a synchronized method; use getArgLock(int) as an
    * alternative.
    * @param iIndex the index of the argument to read (0...n-1)
    * @return the argument
    * @see #getArgLock(int)
    * @see #setArg(int, int)
    * @see #setArgLock(int, int)
    */
   public int getArg(int iIndex) {
      return aiArgs[iIndex];
   }

   /**
    * Set an integer argument.
    * This is not a synchronized method; use setArgLock(int, int) as an
    * alternative.
    * @param iIndex the index of the argument to set (0...n-1)
    * @param iValue the value of the argument
    * @see #getArg(int)
    * @see #getArgLock(int)
    * @see #setArgLock(int, int)
    */
   public void setArg(int iIndex, int iValue) {
      aiArgs[iIndex] = iValue;
   }


   /**
    * Synchronized version of getArg(int).
    * @param iIndex the index of the argument to read (0...n-1)
    * @return the argument
    * @see #getArg(int)
    */
   public synchronized int getArgLock(int iIndex) {
      return getArg(iIndex);
   }

   /**
    * Synchronized version of setArg(int, int).
    * @param iIndex the index of the argument to set (0...n-1)
    * @param iValue the value of the argument
    * @see #setArg(int, int)
    */
   public synchronized void setArgLock(int iIndex, int iVal) {
      setArg(iIndex, iVal);
   }

   /**
    * Determine if all the arguments match those of another object.
    * The other object should have the same number of arguments as
    * this one.
    * This is not a synchronized method; use matchesLock(ThreadCommand)
    * as an alternative.
    * @param dest the other ThreadCommand object
    * @return true if all the arguments match
    * @see #matchesLock(ThreadCommand)
    */
   public boolean matches(ThreadCommand dest) {
  		for (int i = 0; i<aiArgs.length; i++)
   			if (getArg(i) != dest.getArg(i)) return false;
 		return true;
   }

   /**
    * Synchronized version of matches(ThreadCommand)
    * @param dest the other ThreadCommand object
    * @return true if all the arguments match
    * @see #matches(ThreadCommand)
    */
	public synchronized boolean matchesLock(ThreadCommand dest) {
      boolean f = true;
      synchronized(dest) {
         f = matches(dest);
      }
		return f;
	}

   /**
    * Copy all the arguments from this object to another.
    * Both this object and the destination should have the same
    * number of arguments.
    * This is not a synchronized method; use copyToLock(ThreadCommand)
    * as an alternative.
    * @param dest the ThreadCommand object to receive the arguments
    * @see #copyToLock(ThreadCommand dest)
    */
   public void copyTo(ThreadCommand dest) {
   	for (int i = aiArgs.length - 1; i >= 0; i--)
         dest.aiArgs[i] = aiArgs[i];
   }

   /**
    * Synchronized version of copyTo(ThreadCommand)
    * @param dest the ThreadCommand object to receive the arguments
    * @see #copyTo(ThreadCommand dest)
    */
  	public synchronized void copyToLock(ThreadCommand dest) {
      synchronized(dest) {
         copyTo(dest);
     }
	}

   /**
    * Sleep for a number of milliseconds.  Calls Thread.sleep(n).
    * Ignores any InterruptedExceptions.
    * @param millis the number of milliseconds to sleep for
    */
   public static void sleep(long millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException e) {}
   }

//   private final static int LOCK = 0;
//   private final static int SIGNAL = 0;
//   public final static int CMD = 0;
//   public final static int OPER = 1;

   private int[] aiArgs;
   private boolean fSignal = false;
}