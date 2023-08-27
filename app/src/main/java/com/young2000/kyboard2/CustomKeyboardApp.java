package com.young2000.kyboard2;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.List;

public class CustomKeyboardApp extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    KeyboardView keyboardView;
    boolean isShifted;
    int English_Korean;
    List<KeyMapping> mappingList;
    @Override
    public View onCreateInputView() {

        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.custom_keyboard_layout, null);
        Keyboard keyboard = new Keyboard(this, R.xml.custom_keyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        mappingList = new ArrayList<>();
        mappingList.add(new KeyMapping('a', 'ㅁ'));
        mappingList.add(new KeyMapping('b', 'ㅠ'));
        mappingList.add(new KeyMapping('c', 'ㅊ'));
        mappingList.add(new KeyMapping('d', 'ㅇ'));
        mappingList.add(new KeyMapping('e', 'ㄷ'));
        mappingList.add(new KeyMapping('f', 'ㄹ'));
        mappingList.add(new KeyMapping('g', 'ㅎ'));
        mappingList.add(new KeyMapping('h', 'ㅗ'));
        mappingList.add(new KeyMapping('i', 'ㅑ'));
        mappingList.add(new KeyMapping('j', 'ㅓ'));
        mappingList.add(new KeyMapping('k', 'ㅏ'));
        mappingList.add(new KeyMapping('l', 'ㅣ'));
        mappingList.add(new KeyMapping('m', 'ㅡ'));
        mappingList.add(new KeyMapping('n', 'ㅜ'));
        mappingList.add(new KeyMapping('o', 'ㅐ'));
        mappingList.add(new KeyMapping('p', 'ㅔ'));
        mappingList.add(new KeyMapping('q', 'ㅂ'));
        mappingList.add(new KeyMapping('r', 'ㄱ'));
        mappingList.add(new KeyMapping('s', 'ㄴ'));
        mappingList.add(new KeyMapping('t', 'ㅅ'));
        mappingList.add(new KeyMapping('u', 'ㅕ'));
        mappingList.add(new KeyMapping('v', 'ㅍ'));
        mappingList.add(new KeyMapping('w', 'ㅈ'));
        mappingList.add(new KeyMapping('x', 'ㅌ'));
        mappingList.add(new KeyMapping('y', 'ㅛ'));
        mappingList.add(new KeyMapping('z', 'ㅋ'));

        isShifted = false;
        English_Korean = 0;
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

        Log.i("keystroke", "code:" + primaryCode);
        Boolean languageUpdate = false;
        List<Keyboard.Key> keys = keyboard.getKeys();

        if (primaryCode == 10) {
            // Backspace key pressed
            Log.i("keystroke", "Enter key pressed");
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            // If the Enter key is stroked, clean the isShift variable to return to normal.
            isShifted = false;
        } else if (primaryCode == 32) {
            // Space key pressed
            Log.i("keystroke", "Space key pressed");
            inputConnection.commitText(" ", 1); // Input a space
        } else if (primaryCode == -1) {
            // Shift key pressed
            Log.i("keystroke", "Shift key pressed");
            isShifted = !isShifted; // Toggle shift state
        } else if (primaryCode == 67) {
            // Backspace key pressed
            Log.i("keystroke", "Backspace key pressed");
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        } else if (primaryCode == 10) {
            // Backspace key pressed
            Log.i("keystroke", "Enter key pressed");
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            // If the Enter key is stroked, clean the isShift variable to return to normal.
            isShifted = false;
        } else if (primaryCode == -1001) {
            Log.i("keystroke", "Language key pressed");
            English_Korean = (English_Korean+1)%2;
            languageUpdate = true;
            isShifted = false;  // reset Shift when the language is updated
        } else {
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == primaryCode) {
                    CharSequence label = key.label;
                    // Non-Shift key pressed
                    Log.i("keystroke", String.valueOf((char) primaryCode));
                    inputConnection.commitText(label, 1); // Input the label text
                    // If the normal key is stroked, clean the isShift variable to return to normal.
                    isShifted = false;
                    break;
                }
            }
        }

        // Korean/English first
        if (languageUpdate) {
            for (Keyboard.Key key : keys) {
                CharSequence label = key.label;
                int pmCode = key.codes[0];
                if (label != null && pmCode != 10 && pmCode != 32 && pmCode != -1 && pmCode != 67 && pmCode != 10 && pmCode != 1001 && !label.equals(".")) {
                    for (KeyMapping mapping : mappingList) {
                        if (English_Korean == 0) {
                            if (mapping.koreanCharacter == label.toString().toLowerCase().charAt(0)) {
                                key.label = String.valueOf(mapping.englishKey);
                                break; // Stop the loop once you find the mapping
                            }
                        } else {
                            if (mapping.englishKey == label.toString().toLowerCase().charAt(0)) {
                                key.label = String.valueOf(mapping.koreanCharacter);
                                break; // Stop the loop once you find the mapping
                            }
                        }
                    }
                }
            }
            languageUpdate = false;
        }

        if (English_Korean == 0) {
            for (Keyboard.Key key : keys) {
                CharSequence label = key.label;
                int pmCode = key.codes[0];
                if (label != null && pmCode != 10 && pmCode != 32 && pmCode != -1 && pmCode != 67 && pmCode != 10 && pmCode != 1001 && !label.equals(".")) {
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


    class KeyMapping {
        public char englishKey;
        public char koreanCharacter;

        public KeyMapping(char englishKey, char koreanCharacter) {
            this.englishKey = englishKey;
            this.koreanCharacter = koreanCharacter;
        }
    }

}