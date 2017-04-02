package evolv.io.peripherals;

import java.util.HashMap;
import java.util.Map;

import processing.event.MouseEvent;

public enum MouseAction {
   PRESS(MouseEvent.PRESS),
   RELEASE(MouseEvent.RELEASE),
   CLICK(MouseEvent.CLICK),
   WHEEL(MouseEvent.WHEEL),
   MOVE(MouseEvent.MOVE),
   DRAG(MouseEvent.DRAG);
   private static final Map<Integer, MouseAction> CONSTANT_TO_KEY_ACTIONS = new HashMap<>();
   private final int                              constant;

   static {
      for (MouseAction keyAction : MouseAction.values()) {
         CONSTANT_TO_KEY_ACTIONS.put(keyAction.constant, keyAction);
      }
   }

   private MouseAction(int constant) {
      this.constant = constant;
   }

   public static MouseAction getFromConstant(int constant) {
      return CONSTANT_TO_KEY_ACTIONS.get(constant);
   }
}
