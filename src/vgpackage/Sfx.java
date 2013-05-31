package vgpackage;

import java.applet.*;
import java.applet.AudioClip;
import java.net.*;
import java.awt.*;
import java.util.*;
import mytools.*;

public class Sfx implements Runnable, VidGameGlobals {

  // Class members:

  public static void open(String[] names) {
    open(names, 2);
  }

  public static void open(String[] names, int filterLevels) {
    active = true;

    Sfx.filterLevels = filterLevels;
    Sfx.names = names;
    Sfx.total = names.length;
    Sfx.currentFilter = 0;

    if (WITH_SFX) {
      // Start loading the sfx in the thread
      list = new Sfx[total];
      for (int i = 0; i < total; i++)
        list[i] = new Sfx();

        // We create a Sfx object, which implements Runnable, to pass
        // to the thread constructor.
      thread = new Thread(new Sfx());
      thread.start();
    }
  }

  public static void close() {
    if (WITH_SFX && active) {
      Thread t = thread;
      thread = null;
      while (t.isAlive())
        ThreadCommand.sleep(50);

      for (int i = 0; i < total; i++) {
        Sfx s = list[i];
        if (s.clip != null)
          s.clip.stop();
        s.playing = false;
      }
    }
    loaded = false;
  }

  public static void play(int i) {
    play(i, 0, 0);
  }

  public static void play(int i, int delay, int minInterval) {
    if (!WITH_SFX || !active) return;

    int currTime = (int) VidGame.getSystemTime();

    if (
        loaded
        && !pausedFlag
        && !quietFlag
        && VidGame.getMode() == VidGame.MODE_PLAYING
        ) {
      Sfx obj = list[i];
      if (obj.clip == null)
        return;

      if (obj.filterValue < currentFilter)
        return;

      int elapsed = currTime - obj.startTime;
      if (elapsed < 0)
        elapsed = 0;

      if (delay > 0) {
        obj.delayTime = delay;
        obj.minInterval = minInterval;
        return;
      }

      if (elapsed < minInterval)
        return;

      if (obj.looping /*i >= loopStart*/) {
        // If not already playing, start it.
        if (!obj.playing) {
          obj.clip.loop();
          obj.startTime = currTime;
          obj.playing = true;
        }
      }
      else {
        obj.clip.play();
        obj.startTime = currTime;
      }
    }
  }

  public static void processQuiet() {
    currentFilter++;
    if (currentFilter == filterLevels)
      currentFilter = 0;

    restartSfx(true);
  }

  public static void setFilter(int eff, int value) {
    if (active)
      list[eff].filterValue = value;
  }

  public static void processPause(boolean pause) {
    if (!loaded)
      return;
    pausedFlag = pause;
    restartSfx(!pause);
  }

  public static void stop(int i) {
    if (loaded && WITH_SFX) {
      Sfx obj = list[i];
      obj.clip.stop();
      obj.playing = false;
      obj.delayTime = 0;
    }
  }

  public static boolean loaded() {
    return !WITH_SFX || !active || loaded;
  }

  public static void update() {
    if (
        loaded
        && !pausedFlag
        && !quietFlag
        && VidGame.getMode() == VidGame.MODE_PLAYING
        && WITH_SFX
        ) {
      for (int i = 0; i < total; i++) {
        Sfx obj = list[i];

        if (obj.delayTime == 0)
          continue;

        obj.delayTime -= Math.min(obj.delayTime, VidGame.CYCLE);
        if (obj.delayTime > 0)
          continue;

        play(i, 0, obj.minInterval);
      }
    }
  }

  private static void restartSfx(boolean playFlag) {
    if (!loaded || !WITH_SFX)
      return;

    for (int i = 0; i < total; i++) {
      Sfx obj = list[i];

      if (!playFlag || obj.filterValue < currentFilter)
        obj.clip.stop();
      else if (obj.looping) {
        // If it is supposed to be playing, restart it
        if (obj.playing)
          obj.clip.loop();
      }
    }
  }

  private static boolean loaded = false;
  private static int sfxTotal;
  private static boolean pausedFlag = false;
  private static String[] names;
  private static int total;
  private static Sfx[] list;
  private static Thread thread;
  private static boolean quietFlag = false;
  private static int currentFilter;
  private static int filterLevels;
  private static boolean active;

  // Instance members:

  private Sfx() {
    filterValue = filterLevels - 2;
  }

  public void run() {
    if (!WITH_SFX || !active) return;
    // Load all sound clips by playing and immediately stopping them.
    for (int i = 0; i < total && (thread != null); i++) {
      Sfx s = list[i];

      // For testing purposes, delay a few seconds between loading sfx:
      if (VidGame.DEBUG && i < 4 && false)
        ThreadCommand.sleep(1200);

      if (names[i].charAt(0) == '*')
        s.looping = true;
      String name = names[i].substring(s.looping ? 1 : 0);

      Applet applet = VidGame.getApplet();
      URL url = applet.getClass().getResource(name + ".au");
      try {
        s.clip = applet.getAudioClip(url);
      }
      catch (Exception e) {
        Debug.showStatus("Exception loading audio clip " + name + ": " +
                         e.toString());
        continue;
      }

      s.clip.play();
      s.clip.stop();
    }
    loaded = true;
  }

  private AudioClip clip;
  private boolean playing = false;
  private int startTime;
  private int filterValue;
  private boolean looping = false;
  private int delayTime;
  private int minInterval;
}