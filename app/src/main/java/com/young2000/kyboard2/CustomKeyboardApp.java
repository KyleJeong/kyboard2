package com.young2000.kyboard2;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.List;

public class CustomKeyboardApp extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    KeyboardView keyboardView;
    boolean isShifted;
    @Override
    public View onCreateInputView() {

        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.custom_keyboard_layout, null);
        Keyboard keyboard = new Keyboard(this, R.xml.custom_keyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        isShifted = false;
        return keyboardView;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return; // No input connection, cannot proceed
        }

        Keyboard keyboard = keyboardView.getKeyboard();
        if (keyboard == null) {
            return; // No keyboard defined, cannot proceed
        }

        List<Keyboard.Key> keys = keyboard.getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == primaryCode) {
                CharSequence label = key.label;
                if (label != null) {
                    if (label.equals("Shift")) {
                        // Shift key pressed
                        Log.i("keystroke", "Shift key pressed" + label.toString());
                        isShifted = !isShifted; // Toggle shift state
                    } else if (label.equals(" ")) {
                        // Space key pressed
                        Log.i("keystroke", "Space key pressed");
                        inputConnection.commitText(" ", 1); // Input a space
                    } else if (label.equals("BS")) {
                        // Backspace key pressed
                        Log.i("keystroke", "Backspace key pressed");
                        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                    } else if (label.equals("Enter")) {
                        // Backspace key pressed
                        Log.i("keystroke", "Enter key pressed");
                        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        // If the Enter key is stroked, clean the isShift variable to return to normal.
                        isShifted = false;
                    } else {
                        // Non-Shift key pressed
                        Log.i("keystroke", String.valueOf((char) primaryCode));
                        inputConnection.commitText(label, 1); // Input the label text
                        // If the normal key is stroked, clean the isShift variable to return to normal.
                        isShifted = false;
                    }
                }
                break;
            }
        }

        for (Keyboard.Key key : keys) {
            CharSequence label = key.label;
            if (label != null && !label.equals("Shift") && !label.equals("BS") && !label.equals(" ") && !label.equals("Enter") && !label.equals(".")) {
                if (isShifted && Character.isLowerCase(label.charAt(0))) {
                    label = label.toString().toUpperCase();
                    key.label = label;
                }
                if (!isShifted && Character.isUpperCase(label.charAt(0))) {
                    label = label.toString().toLowerCase();
                    key.label = label;
                }
            }
        }

        keyboardView.invalidateAllKeys(); // Refresh key labels after shift state changes
    }


    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}