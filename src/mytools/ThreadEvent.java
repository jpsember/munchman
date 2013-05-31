package mytools;

/**
 * Title:        ThreadEvent
 * Description:  Class for posting events from other threads for the
 *               Event Dispatch thread to process.  Each event consists
 *               of an integer id, and an optional Object.  The class
 *               includes an event queue, which supports posting and
 *               processing events to and from a first in / first out
 *               queue.  This class is thread safe.
 * @author Jeff Sember
 * @version 1.0
 */

public class ThreadEvent {

   /**
    * Gets the id of the event
    * @return the id of the event
    */
   public int getId() {
      return iId;
   }

   /**
    * Get the integer argument associated with the event.
    * Assuming the event was posted using an integer argument, the
    * Object is converted to an Integer, and its value is returned.
    * @return the int that the event was posted with
    * @see #postEvent(int,int)
    */
   public int getArg() {
      return getArg(obj);
   }

   /**
    * Returns the int associated with an Object.  Assumes that the
    * Object is of type Integer.
    */
   public static int getArg(Object obj) {
      return ((Integer)obj).intValue();
   }

   /**
    * Gets the object associated with the event
    * @return the object, or null if there is none for this event
    */
   public Object getObj() {
      return obj;
   }

   private ThreadEvent() {}

   /**
    * Constructs a ThreadEvent
    * @param id an integer to identify the type of event
    * @param obj an optional Object to include with this event
    */
   private ThreadEvent(int id, Object obj) {
      iId = id;
      this.obj = obj;
   }

   /**
    * Determines if the event queue is empty
    * @return true if it is empty
    */
   public static synchronized boolean isEmpty() {
      return (iQueueUsed == 0);
   }

   /**
    * Returns a string describing the ThreadEvent
    */
   public String toString() {
      return Integer.toString(getId()) + ","
         + (getObj() == null ? "(no object)" : getObj());
   }

   /**
    * Get the next ThreadEvent from the queue
    * @return a ThreadEvent
    */
   public static synchronized ThreadEvent getEvent() {
      if (iQueueUsed == 0)
         return null;
      ThreadEvent te = teQueue[iQueueTail];

      iQueueTail++;
      if (iQueueTail == iQueueSize)
         iQueueTail = 0;
      iQueueUsed--;
      return te;
   }

   /**
    * Post an event into the queue, with integer argument that is
    * converted to an Integer and stored as the event's Object
    * @param id an integer to identify the type of event
    * @param arg an integer value to include with the event,
    *    as an Integer
    */
   public static synchronized void postEvent(int id, int arg) {
      postEvent(id, new Integer(arg));
   }

   /**
    * Post an event into the queue
    * @param id the id of the event
    */
   public static synchronized void postEvent(int id) {
      postEvent(id, null);
   }

   /**
    * Post an event into the queue
    * @param id the id of the event
    * @param obj an optional object associated with the event
    */
   public static synchronized void postEvent(int id, Object obj) {

    // Will the queue hold more events?
      if (iQueueUsed == iQueueSize)
         resizeQueue(iQueueSize * 2 + 5);

      teQueue[iQueueHead++] = new ThreadEvent(id, obj);
      if (iQueueHead == iQueueSize)
         iQueueHead = 0;
      iQueueUsed++;
   }

   private static void resizeQueue(int iNewSize) {
      ThreadEvent[] teArrayNew = new ThreadEvent[iNewSize];
      int j = iQueueTail;
      for (int i = 0; i < iQueueUsed; i++) {
         teArrayNew[i] = teQueue[j];
         j++;
         if (j == iQueueSize)
           j = 0;
      }

      teQueue = teArrayNew;
      iQueueSize = iNewSize;

      iQueueTail = 0;
      iQueueHead = iQueueUsed;
      if (iQueueHead == iQueueSize)
         iQueueHead = 0;
  }

  private static int iQueueSize = 0;
  private static int iQueueUsed = 0;
  private static int iQueueHead = 0;
  private static int iQueueTail = 0;
  private static ThreadEvent[] teQueue = null;

  private int iId;            // id for event
  private Object obj;         // object for event
}