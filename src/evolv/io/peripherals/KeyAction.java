package evolv.io.peripherals;

import java.util.HashMap;
import java.util.Map;

import processing.event.KeyEvent;

public enum KeyAction {
   PRESS(KeyEvent.PRESS),
   RELEASE(KeyEvent.RELEASE),
   TYPE(KeyEvent.TYPE);
   private static final Map<Integer, KeyAction> CONSTANT_TO_KEY_ACTIONS = new HashMap<>();
   private final int                            constant;

   static {
      for (KeyAction keyAction : KeyAction.values()) {
         CONSTANT_TO_KEY_ACTIONS.put(keyAction.constant, keyAction);
      }
   }

   private KeyAction(int constant) {
      this.constant = constant;
   }

   public static KeyAction getFromConstant(int constant) {
      return CONSTANT_TO_KEY_ACTIONS.get(constant);
   }
}
