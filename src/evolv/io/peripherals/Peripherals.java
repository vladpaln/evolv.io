package evolv.io.peripherals;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class Peripherals {
   private final Map<KeyAction, Map<Character, Consumer<KeyEvent>>>       keyActions     = new HashMap<>();
   private final Map<KeyAction, Map<KeyCode, Consumer<KeyEvent>>>         keyCodeActions = new HashMap<>();
   private final Map<MouseAction, Map<MouseButton, Consumer<MouseEvent>>> mouseActions   = new HashMap<>();

   public void onKey(char key, KeyAction keyAction, Consumer<KeyEvent> action) {
      getValidActionMap(keyAction, keyActions).put(key, action);
   }

   public void onKeyRemove(char key, KeyAction keyAction) {
      Map<Character, Consumer<KeyEvent>> keyActionMap = keyActions.get(keyAction);
      if (keyActionMap != null) {
         keyActionMap.remove(key);
      }
   }

   public void onKeyCode(KeyCode keyCode, KeyAction keyAction, Consumer<KeyEvent> action) {
      getValidActionMap(keyAction, keyCodeActions).put(keyCode, action);
   }

   public void onKeyCodeRemove(KeyCode keyCode, KeyAction keyAction) {
      Map<KeyCode, Consumer<KeyEvent>> keyActionMap = keyCodeActions.get(keyAction);
      if (keyActionMap != null) {
         keyActionMap.remove(keyCode);
      }
   }

   public void onMouse(MouseButton mouseButton, MouseAction mouseAction, Consumer<MouseEvent> action) {
      getValidActionMap(mouseAction, mouseActions).put(mouseButton, action);
   }

   public void onMouseRemove(MouseButton mouseButton, MouseAction mouseAction) {
      Map<MouseButton, Consumer<MouseEvent>> keyActionMap = mouseActions.get(mouseAction);
      if (keyActionMap != null) {
         keyActionMap.remove(mouseButton);
      }
   }

   private <A, P, E> Map<P, Consumer<E>> getValidActionMap(A action, Map<A, Map<P, Consumer<E>>> map) {
      Map<P, Consumer<E>> actionMap = map.get(action);
      if (actionMap == null) {
         actionMap = new HashMap<>();
         map.put(action, actionMap);
      }
      return actionMap;
   }

   public void handleKeyEvent(KeyEvent keyEvent) {
      int keyActionConstant = keyEvent.getAction();
      KeyAction keyAction = KeyAction.getFromConstant(keyActionConstant);
      char key = keyEvent.getKey();
      if (key == PConstants.CODED) {
         int keyCodeConstant = keyEvent.getKeyCode();
         KeyCode keyCode = KeyCode.getFromConstant(keyCodeConstant);
         performAction(keyAction, keyCode, keyCodeActions, keyEvent);
      } else {
         performAction(keyAction, key, keyActions, keyEvent);
      }
   }

   public void handleMouseEvent(MouseEvent mouseEvent) {
      int mouseActionConstant = mouseEvent.getAction();
      MouseAction mouseAction = MouseAction.getFromConstant(mouseActionConstant);
      int button = mouseEvent.getButton();
      MouseButton mouseButton = MouseButton.getFromConstant(button);
      performAction(mouseAction, mouseButton, mouseActions, mouseEvent);
   }

   private <A, P, E> void performAction(A action, P peripheral, Map<A, Map<P, Consumer<E>>> map, E event) {
      Map<P, Consumer<E>> actionMap = map.get(action);
      if (actionMap != null) {
         Consumer<E> actionToPerform = actionMap.get(peripheral);
         if (actionToPerform != null) {
            actionToPerform.accept(event);
         }
      }
   }
}
