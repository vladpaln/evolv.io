package evolv.io.peripherals;

import java.util.HashMap;
import java.util.Map;

import processing.core.PConstants;

public enum KeyCode {
   UP(PConstants.UP),
   DOWN(PConstants.DOWN),
   LEFT(PConstants.LEFT),
   RIGHT(PConstants.RIGHT),
   BACKSPACE(PConstants.BACKSPACE),
   DELETE(PConstants.DELETE),
   ENTER(PConstants.ENTER),
   ESC(PConstants.ESC),
   TAB(PConstants.TAB),
   ALT(PConstants.ALT),
   CONTROL(PConstants.CONTROL),
   SHIFT(PConstants.SHIFT);
   private static final Map<Integer, KeyCode> CONSTANT_TO_KEY_CODES = new HashMap<>();
   private final int                          constant;

   static {
      for (KeyCode keyCode : KeyCode.values()) {
         CONSTANT_TO_KEY_CODES.put(keyCode.constant, keyCode);
      }
   }

   private KeyCode(int constant) {
      this.constant = constant;
   }

   public static KeyCode getFromConstant(int constant) {
      return CONSTANT_TO_KEY_CODES.get(constant);
   }
}
