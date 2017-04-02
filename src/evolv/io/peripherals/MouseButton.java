package evolv.io.peripherals;

import java.util.HashMap;
import java.util.Map;

import processing.core.PConstants;

public enum MouseButton {
   NONE(0 /* no PConstants value for no mouse button */),
   LEFT(PConstants.LEFT),
   CENTER(PConstants.CENTER),
   RIGHT(PConstants.RIGHT);
   private static final Map<Integer, MouseButton> CONSTANT_TO_MOUSE_BUTTONS = new HashMap<>();
   private final int                              constant;

   static {
      for (MouseButton mouseButton : MouseButton.values()) {
         CONSTANT_TO_MOUSE_BUTTONS.put(mouseButton.constant, mouseButton);
      }
   }

   private MouseButton(int constant) {
      this.constant = constant;
   }

   public static MouseButton getFromConstant(int constant) {
      return CONSTANT_TO_MOUSE_BUTTONS.get(constant);
   }
}
