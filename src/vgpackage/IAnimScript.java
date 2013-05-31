package vgpackage;

public interface IAnimScript extends AnimScriptConst {
    /**
     * Returns the requested object for an AnimScript.
     * @param script the AnimScript requesting the object
     * @param id the id of the object
     * @return an Object which is a String or Sprite, corresponding
     * to the short[] array that created the AnimScript
     */
    Object getObject(AnimScript script, int id);
}