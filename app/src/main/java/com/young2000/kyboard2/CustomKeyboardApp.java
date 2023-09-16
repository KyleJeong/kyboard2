package com.young2000.kyboard2;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class CustomKeyboardApp extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    public static final int STAGE_INITIAL = 0;
    public static final int STAGE_CHOSUNG = 1;
    public static final int STAGE_JUNGSUNG = 2;
    public static final int STAGE_JONGSUNG_OR_NEW = 4;
    public static final int STAGE_INITIAL_OR_CHOSUNG = 6;
    public static final int STAGE_JUNGSUNG_ONLY = 5;
    int stage;
    int idx1, idx2, idx3;
    int last_jaumIndex;
    int last_moumIndex;
    boolean last_was_jaum;
    int prev_cho_jung_DanjaumJongsung;
    KeyboardView keyboardView;
    boolean isShifted;
    boolean markEnabled;
    int English_Korean;
    CharSequence lastText;
    List<KeyMapping> mappingList;
    List<KeyMapping> markMappingList;

    @Override
    public View onCreateInputView() {

        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.custom_keyboard_layout, null);
        Keyboard keyboard = new Keyboard(this, R.xml.custom_keyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        mappingList = new ArrayList<>();
        markMappingList = new ArrayList<>();

        char[] keys = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // 10
                'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', // 10
                'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', // 9
                'z', 'x', 'c', 'v', 'b', 'n', 'm', '?', // 8
                '.', ','}; // 2
        char[] values = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // 10
                'ㅂ', 'ㅈ', 'ㄷ', 'ㄱ', 'ㅅ', 'ㅛ', 'ㅕ', 'ㅑ', 'ㅐ', 'ㅔ', // 10
                'ㅁ', 'ㄴ', 'ㅇ', 'ㄹ', 'ㅎ', 'ㅗ', 'ㅓ', 'ㅏ', 'ㅣ', // 9
                'ㅋ', 'ㅌ', 'ㅊ', 'ㅍ', 'ㅠ', 'ㅜ', 'ㅡ', '?', // 8
                '.', ','}; // 2

        char[] markValues = {
                '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', // 10
                '~', '|', '\\', '{', '}', '[', ']', 'º', '\'', '\"', // 10
                '`', '+', '-', '=', '_', '±', '₩', ':', ';', // 9
                '<', '>', '→', '←', '↑', '↓', '/', '?',  // 8
                '.', ','}; // 2

        for (int i = 0; i < keys.length; i++) {
            mappingList.add(new KeyMapping(keys[i], values[i]));
            markMappingList.add(new KeyMapping(keys[i], markValues[i]));
        }

        isShifted = false;
        English_Korean = 0;
        stage = STAGE_INITIAL;
        markEnabled = false;
        lastText = null;
        return keyboardView;
    }


    @Override
    public void onBindInput() {
        super.onBindInput();
        // 새로운 입력창으로 바뀔때, 한글의 이전 상태를 지우고 새롭게 시작하도록 함.
        stage = STAGE_INITIAL;
        Log.i("keystroke", "new bind");
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

        Log.i("keystroke", "code:" + primaryCode + " and character is " + (char) primaryCode + " and codes are " + keyCodes);
        boolean languageUpdate = false;
        boolean primaryFound = false;
        List<Keyboard.Key> keys = keyboard.getKeys();

        CharSequence newText = inputConnection.getTextBeforeCursor(1000, 0);
        Log.i("keystroke", "Previous string before cursor aprior new input" + newText);
        if (lastText != null && !lastText.equals(newText)) {
            Log.i("keystroke", "Cursor is updated anyhow.. by user or UI");
            stage = STAGE_INITIAL;
        }

        if (primaryCode == 10) {
            // Backspace key pressed
            Log.i("keystroke", "Enter key pressed");
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            // If the Enter key is stroked, clean the isShift variable to return to normal.
            isShifted = false;
            stage = STAGE_INITIAL;
        } else if (primaryCode == 32) {
            // Space key pressed
            Log.i("keystroke", "Space key pressed");
            inputConnection.commitText(" ", 1); // Input a space
            stage = STAGE_INITIAL;
        } else if (primaryCode == -1) {
            // Shift key pressed
            Log.i("keystroke", "Shift key pressed");
            isShifted = !isShifted; // Toggle shift state
            languageUpdate = true;
        } else if (primaryCode == 67) {
            // Backspace key pressed
            Log.i("keystroke", "Backspace key pressed");
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            stage = STAGE_INITIAL;
        } else if (primaryCode == -1001) {
            Log.i("keystroke", "Language key pressed");
            English_Korean = (English_Korean + 1) % 2;
            languageUpdate = true;
            markEnabled = false;
            isShifted = false;  // reset Shift when the language is updated
            stage = STAGE_INITIAL;
        } else if (primaryCode == -1010) {
            Log.i("keystroke", "mark key pressed");
            languageUpdate = true;
            markEnabled = !markEnabled;
            isShifted = false;  // reset Shift when the language is updated
            stage = STAGE_INITIAL;
        } else {
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == primaryCode) {
                    primaryFound = true;
                    CharSequence label = key.label;
                    // Non-Shift key pressed
                    Log.i("keystroke", String.valueOf((char) primaryCode));
                    if (English_Korean == 0) {
                        inputConnection.commitText(label, 1); // Input the label text
                        // If the normal key is stroked, clean the isShift variable to return to normal.
                    } else HangulProcess(inputConnection, label);
                    isShifted = false;
                    languageUpdate = true;
                    break;
                }
            }
            // If we havenn't found Primary
            if (!primaryFound) {
                //
                //Font.canDisplay(int)
                Log.i("keystroke", "handle popupKeys" + (char) primaryCode);
                inputConnection.commitText(Character.toString((char) primaryCode), 1);

            }
        }

        // Korean/English first
        if (languageUpdate) {
            Log.i("keystroke", "language Update");
            for (Keyboard.Key key : keys) {
                CharSequence label = key.label;
                int pmCode = key.codes[0];
                if (label != null && pmCode != 10 && pmCode != 32 && pmCode != -1 && pmCode != 67 && pmCode != 1001 && pmCode != 1010) {
                    // key의 pmCode        로 MappingList의 위치를 찾아서
                    // Log.i("keystroke", "key is : " + (char)pmCode + " code: " + pmCode);
                    if (markEnabled) {
                        for (KeyMapping mapping : markMappingList) {
                            if (mapping.englishKey == (char) pmCode) {
                                key.label = String.valueOf(mapping.anotherCharacter);
                                Log.i("keystroke", "found : " + key.label);
                                break; // Stop the loop once you find the mapping
                            }
                        }
                    } else {
                        for (KeyMapping mapping : mappingList) {
                            if (mapping.englishKey == (char) pmCode) {
                                if (English_Korean == 0) {
                                    if (!isShifted) {
                                        key.label = String.valueOf(mapping.englishKey);
                                    } else {
                                        key.label = String.valueOf(mapping.englishKey).toUpperCase();
                                    }
                                } else {
                                    key.label = String.valueOf(mapping.anotherCharacter);
                                    if (isShifted) {
                                        if (key.label.toString().equals("ㅂ")) {
                                            key.label = "ㅃ";
                                        } else if (key.label.toString().equals("ㅈ")) {
                                            key.label = "ㅉ";
                                        } else if (key.label.toString().equals("ㄷ")) {
                                            key.label = "ㄸ";
                                        } else if (key.label.toString().equals("ㄱ")) {
                                            key.label = "ㄲ";
                                        } else if (key.label.toString().equals("ㅅ")) {
                                            key.label = "ㅆ";
                                        } else if (key.label.toString().equals("ㅐ")) {
                                            key.label = "ㅒ";
                                        } else if (key.label.toString().equals("ㅔ")) {
                                            key.label = "ㅖ";
                                        }
                                    }
                                }
                                break; // Stop the loop once you find the mapping
                            }
                        }
                    }
                }
            }

            keyboardView.invalidateAllKeys(); // Refresh key labels after shift state changes
            languageUpdate = false;
        }

        lastText = inputConnection.getTextBeforeCursor(1000, 0);
        Log.i("keystroke", "Final string before cursor:" + lastText);
    }

    private void HangulCharacterUpdate(InputConnection inputConnection, char charAt) {
        HangulCharacterUpdate(inputConnection, Character.toString(charAt));
    }

    private void HangulCharacterUpdate(InputConnection inputConnection, CharSequence text) {
        inputConnection.deleteSurroundingText(1, 0);
        inputConnection.commitText(text, 1);
    }

    private void HangulProcess(InputConnection inputConnection, CharSequence label) {
        // state
        /*  * 0:""
         * 1: 모음 입력 상태
         * 2: 모음 + 자음 입력상태
         * 3: 모음 + 자음 + 모음입력상태(초 중 종성)
         */
        // https://ehpub.co.kr/%EB%8B%A4-%ED%95%9C%EA%B8%80-%EC%98%A4%ED%86%A0%EB%A7%88%ED%83%80-%EB%A7%8C%EB%93%A4%EA%B8%B0/
                        /*
                        한글 코드 값 = 초성*중성개수*종성 개수+중성 *종성개수+종성+BASE 코드(0xAC00)
                        중성개수 = always 21, 종성갯수 = alwways 27 이므로
                        수식을 다시 쓰면 초성 idx * 21 * 28 + 중성 idx * 28 + 종성 idx + 0xAC00;
                        초성은 모두 19자로 0부터 18의 값을 0ㄱ,1ㄲ,2ㄴ,3ㄷ,4ㄸ,5ㄹ,6ㅁ,7ㅂ,8ㅃ,9ㅅ,10ㅆ,11ㅇ,12ㅈ,13ㅉ,14ㅊ,15ㅋ,16ㅌ,17ㅍ,18ㅎ 순으로 결정하였습니다.
                        중성은 모두 21자로 0부터 20까지 값을 부여하였습니다. 중성은 ㅏ,ㅐ,ㅑ,ㅒ,ㅓ,ㅔ,ㅕ,ㅖ,ㅗ,ㅘ,ㅙ,ㅚ,ㅛ,ㅜ,ㅝ,ㅞ,ㅟ,ㅠ,ㅡ,ㅢ,ㅣ순입니다.
                        종성은 27자인데 받침이 없는 것까지 코드를 부여하고 있어서 0~27까지 값을 부여하였습니다.
                        종성은 받침 없음,0ㄱ,1ㄲ,2ㄳ,3ㄴ,4ㄵ,5ㄶ,6ㄷ,7ㄹ,8ㄺ,9ㄻ,10ㄼ,11ㄽ,12ㄾ,13ㄿ,14ㅀ,15ㅁ,16ㅂ,17ㅄ,18ㅅ,
                        19ㅆ,20ㅇ,21ㅈ,22ㅊ,23ㅋ,24ㅌ,25ㅍ,26ㅎ 순입니다.
                        중성(28)
                        0없음, 1‘ㄱ’, 2‘ㄲ’, 3‘ㄳ’, 4‘ㄴ’, 5‘ㄵ’, 6‘ㄶ’, 7‘ㄷ’, 8‘ㄹ’, 9‘ㄺ’, 10‘ㄻ’,
                        11‘ㄼ’, 12‘ㄽ’, 13‘ㄾ’, 14‘ㄿ’, 15‘ㅀ’, 16‘ㅁ’, 17‘ㅂ’, 18‘ㅄ’,19 ‘ㅅ’, 20‘ㅆ’, 21‘ㅇ’, 22‘ㅈ’, 23‘ㅊ’, 24‘ㅋ’, 25‘ㅌ’, 26‘ㅍ’, 27‘ㅎ’
                        예를 들어 한글의 첫 글자인 ‘가’ 는 0*21*28+0*28+0+0xAC00 이므로 BASE 코드 값이 됩니다. 그리고 초성의 시작점인 ‘ㄱ’은 0x1100이며 중성의 시작점인 ‘ㅏ’는 0x1161입니다.
                         */
        // 자음으로 초/종성을 다룰때 주의할 점:
        // 자음중 초성은 될 수 있지만, 종성이 되지 못하는 것이 있으니 "4ㄸ8ㅃ13ㅉ";

        String jaum = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
        // String jaum = 0ㄱ,1ㄲ,2ㄴ,3ㄷ,4ㄸ,5ㄹ,6ㅁ,7ㅂ,8ㅃ,9ㅅ,10ㅆ,11ㅇ,12ㅈ,13ㅉ,14ㅊ,15ㅋ,16ㅌ,17ㅍ,18ㅎ";
        String moum = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅛㅜㅠㅡㅣ";
        // String moum = 0ㅏ,1ㅐ,2ㅑ,3ㅒ,4ㅓ,5ㅔ,6ㅕ,7ㅖ,8ㅗ,9ㅛ,10ㅜ,11ㅠ,12ㅡ,13ㅣ";
        int jaumIndex = jaum.indexOf(label.toString());
        int moumIndex = moum.indexOf(label.toString());
        if ((jaumIndex == -1) && (moumIndex == -1)) {
            inputConnection.commitText(label, 1); // Input the label text
            Log.i("keystroke", "non Korea, so GOTO Initial");
            stage = STAGE_INITIAL;
        } else {

            boolean liul_comb = (jaumIndex != -1) && (jaumIndex == 0 || jaumIndex == 6 || jaumIndex == 7 || jaumIndex == 9 || jaumIndex == 16 || jaumIndex == 17 || jaumIndex == 18);
            String choh = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
            String jung = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
            String jong = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

            int jungIndex;// = jung.indexOf(label.toString());

            Log.i("keystroke", "input: " + ((jaumIndex != -1) ? jaum.charAt(jaumIndex) : moum.charAt(moumIndex)));
            Log.i("keystroke", "idx1,idx2,idx3: " + idx1 + " " + idx2 + " " + idx3);
            Log.i("keystroke", "stage :" + stage);

            if (stage == STAGE_INITIAL) {
                if (jaumIndex != -1) {
                    inputConnection.commitText(label, 1); // Input the label text
                    idx1 = choh.indexOf(label.toString());
                    stage = STAGE_CHOSUNG;

                    Log.i("keystroke", "Goes to STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                } else { // JUNGSUNG
                    inputConnection.commitText(label, 1); // Input the label text
                    idx2 = jung.indexOf(label.toString());
                    stage = STAGE_JUNGSUNG_ONLY;
                    Log.i("keystroke", "Goes to STAGE_JUNGSUNG_ONLY with " + jung.charAt(idx2) + " idx: " + idx2);
                }
            } else if (stage == STAGE_JUNGSUNG_ONLY) {
                // 만일 초성이 들어오면 STAGE_CHOSUNG으로 이동
                if (jaumIndex != -1) {
                    inputConnection.commitText(label, 1); // Input the label text
                    idx1 = choh.indexOf(label.toString());
                    stage = STAGE_CHOSUNG;
                    Log.i("keystroke", "Goes to STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                } else { // JUNGSUNG
                    // 중성이 들어오면. 이중중성 체크하여
                    // 이중중성 처리하고 goto Initial
                    jungIndex = jung.indexOf(label.toString());
                    // 이중중성 허용되는 값인지 확인
                    // 중성은 0ㅏ,1ㅐ,2ㅑ,3ㅒ,4ㅓ,5ㅔ,6ㅕ,7ㅖ,8ㅗ,9ㅘ,10ㅙ,11ㅚ,12ㅛ,13ㅜ,14ㅝ,15ㅞ,16ㅟ,17ㅠ,18ㅡ,19ㅢ,20ㅣ순입니다.
                    // ㅗ 인데,, ㅏ ㅐ ㅣ 가 오면 ok
                    if ((idx2 == 8) && ((jungIndex == 0) || (jungIndex == 1) || (jungIndex == 20))) {
                        //9, 10, 11 만들기
                        idx2 = (9 + jungIndex);
                        if (idx2 > 20) idx2 = 11;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Final: " + jung.charAt(idx2));
                        HangulCharacterUpdate(inputConnection, jung.charAt(idx2));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    }
                    // ㅜ 인데, ㅓ ㅔ ㅣ 가 오면 ok
                    else if ((idx2 == 13) && ((jungIndex == 4) || (jungIndex == 5) || (jungIndex == 20))) {
                        //14,15,16 만들기
                        idx2 = (10 + jungIndex);
                        if (idx2 > 20) idx2 = 16;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Final: " + jung.charAt(idx2));
                        HangulCharacterUpdate(inputConnection, jung.charAt(idx2));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    }
                    // - 인데,ㅣ 가 오면 ok
                    else if (idx2 == 18 && jungIndex == 20) {
                        idx2 = 19;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Final: " + jung.charAt(idx2));
                        HangulCharacterUpdate(inputConnection, jung.charAt(idx2));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    }
                    // 비허용되면 새로 입력된 중성을 추가 출력하고, 중성 상태유지
                    // 이중중성은 아직 미지원
                    else {
                        inputConnection.commitText(label, 1); // Input the label text
                        idx2 = jungIndex;
                        stage = STAGE_JUNGSUNG_ONLY;
                        Log.i("keystroke", "keep STAGE_JUNGSUNG_ONLY with " + jung.charAt(idx2) + " idx: " + idx2);
                    }
                }
            } else if (stage == STAGE_CHOSUNG) {
                if (jaumIndex != -1) {
                    // 이중자음이면 이전값 지우고 새로운 값으로 update 출력하고, goto Initial

//                                    초성은 모두 19자로 0부터 18의 값을 0ㄱ,1ㄲ,2ㄴ,3ㄷ,4ㄸ,5ㄹ,6ㅁ,7ㅂ,8ㅃ,9ㅅ,10ㅆ,11ㅇ,12ㅈ,13ㅉ,14ㅊ,15ㅋ,16ㅌ,17ㅍ,18ㅎ 순으로 결정하였습니다.
//                                    종성은 27자인데 받침이 없는 것까지 코드를 부여하고 있어서 0~27까지 값을 부여하였습니다.
//                                            종성은 받침 없음,0ㄱ,1ㄲ,2ㄳ,3ㄴ,4ㄵ,5ㄶ,6ㄷ,7ㄹ,8ㄺ,9ㄻ,10ㄼ,11ㄽ,12ㄾ,13ㄿ,14ㅀ,15ㅁ,16ㅂ,17ㅄ,18ㅅ,
//                                            19ㅆ,20ㅇ,21ㅈ,22ㅊ,23ㅋ,24ㅌ,25ㅍ,26ㅎ 순입니다.
//                                    중성(28)
//                                    0없음, 1‘ㄱ’, 2‘ㄲ’, 3‘ㄳ’, 4‘ㄴ’, 5‘ㄵ’, 6‘ㄶ’, 7‘ㄷ’, 8‘ㄹ’, 9‘ㄺ’, 10‘ㄻ’,
//                                    11‘ㄼ’, 12‘ㄽ’, 13‘ㄾ’, 14‘ㄿ’, 15‘ㅀ’, 16‘ㅁ’, 17‘ㅂ’, 18‘ㅄ’,19 ‘ㅅ’, 20‘ㅆ’, 21‘ㅇ’, 22‘ㅈ’, 23‘ㅊ’, 24‘ㅋ’, 25‘ㅌ’, 26‘ㅍ’, 27‘ㅎ’
//                                    예를 들어 한글의 첫 글자인 ‘가’ 는 0*21*28+0*28+0+0xAC00 이므로 BASE 코드 값이 됩니다. 그리고 초성의 시작점인 ‘ㄱ’은 0x1100이며 중성의 시작점인 ‘ㅏ’는 0x1161입니다.
//                                            */
//                                    String jaum = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
                    // String jaum = 0ㄱ,1ㄲ,2ㄴ,3ㄷ,4ㄸ,5ㄹ,6ㅁ,7ㅂ,8ㅃ,9ㅅ,10ㅆ,11ㅇ,12ㅈ,13ㅉ,14ㅊ,15ㅋ,16ㅌ,17ㅍ,18ㅎ";

                    if ((idx1 == 0) && (jaumIndex == 9)) { // ㄱ --> ㄳ
                        idx3 = 2;
                        Log.i("keystroke", "Label matches with jong update at index: " + idx3);
                        Log.i("keystroke", "Final: " + jong.charAt(idx3));
                        HangulCharacterUpdate(inputConnection, jong.charAt(idx3));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    } else if ((idx1 == 2) && (jaumIndex == 12 || jaumIndex == 18)) { // ㄴ --> ㄵ ㄶ
                        idx3 = (jaumIndex == 12) ? 4 : 5;
                        Log.i("keystroke", "Label matches with jong update at index: " + idx3);
                        Log.i("keystroke", "Final: " + jong.charAt(idx3));
                        HangulCharacterUpdate(inputConnection, jong.charAt(idx3));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    } else if ((idx1 == 5) && liul_comb) { // ㄹ --> ㄺ ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ
                        if (jaumIndex == 0) { // 0 --> 8
                            idx3 = 8;
                        } else if (jaumIndex == 6 || jaumIndex == 7) { // 6~7 --> 9~10
                            idx3 = jaumIndex + 3;
                        } else if (jaumIndex == 9) { // 9 --> 11
                            idx3 = 11;
                        } else { // 16~18 --> 12~14
                            idx3 = jaumIndex - 4;
                        }
                        Log.i("keystroke", "Label matches with jong update at index: " + idx3);
                        Log.i("keystroke", "Final: " + jong.charAt(idx3));
                        HangulCharacterUpdate(inputConnection, jong.charAt(idx3));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    } else if ((idx1 == 7) && (jaumIndex == 9)) { // ㅂ --> ㅄ
                        idx3 = 17;
                        Log.i("keystroke", "Label matches with jong update at index: " + idx3);
                        Log.i("keystroke", "Final: " + jong.charAt(idx3));
                        HangulCharacterUpdate(inputConnection, jong.charAt(idx3));
                        stage = STAGE_INITIAL;
                        Log.i("keystroke", "Goto STAGE_INITIAL");
                    }
                    // 아니라면(즉, 이중자음이 아니라면 입력된 초성을 추가 출력하고 stage_chosung 유지
                    else {
                        inputConnection.commitText(label, 1); // Input the label text
                        idx1 = jaumIndex;
                        stage = STAGE_CHOSUNG;
                        Log.i("keystroke", "keep STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                    }
                } else {
                    idx2 = jung.indexOf(label.toString());
                    idx3 = 0;
                    Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + " and Jungsung with " + jung.charAt(idx2) + " idx: " + idx2);
                    int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                    char[] charArray = Character.toChars(unicodeCodePoint);
                    String koreanCharacter = new String(charArray);
                    Log.i("keystroke", "Final: " + koreanCharacter);
                    HangulCharacterUpdate(inputConnection, koreanCharacter);
                    stage = STAGE_JUNGSUNG;
                    Log.i("keystroke", "Goes to STAGE_JUNGSUNG");
                }
            } else if (stage == STAGE_JUNGSUNG) {
                // 중성까지 왔는데 자음이면, 이전글자를 지우고 초/중/종 완벽한 글자를 만든다음. STAGE_JONGSUNG_OR_NEW로 이동
                if (jaumIndex != -1) {
                    idx3 = jong.indexOf(label.toString());
                    // 특수한 경우가 있는데. ㄸㅃㅉ 입력은 종성으로 허용되지 않으므로 새로운 초성이 입력된 것으로 간주해야 함
                    if (idx3 == -1) { // Same as
                        inputConnection.commitText(label, 1); // Input the label text
                        idx1 = choh.indexOf(label.toString());
                        stage = STAGE_CHOSUNG;
                        Log.i("keystroke", "New Jaum is not allowed for Jongsung, so it is considered as a new Chosung");
                        Log.i("keystroke", "Goes to STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                    } else {
                        idx3 += 1;
                        Log.i("keystroke", label + " " + idx3);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;

                        prev_cho_jung_DanjaumJongsung = unicodeCodePoint;

                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        stage = STAGE_JONGSUNG_OR_NEW;
                        Log.i("keystroke", "Goes to STAGE_JONGSUNG_OR_NEW");
                    }
                }
                // 초/중성 상태인데,다시 모음이면, 이중모음이거나, 새로운 중성 only 상태여야 한다.
                // 이중모음이면 이전글자를 지우고.. 새롭게 쓰고 STAGE_JUNGSUNG 유지
                // 그게 아니면 STAGE_JUNGSUNG_ONLY 상태로 바뀐다.
                else {
                    jungIndex = jung.indexOf(label.toString());
                    // 중성은 0ㅏ,1ㅐ,2ㅑ,3ㅒ,4ㅓ,5ㅔ,6ㅕ,7ㅖ,8ㅗ,9ㅘ,10ㅙ,11ㅚ,12ㅛ,13ㅜ,14ㅝ,15ㅞ,16ㅟ,17ㅠ,18ㅡ,19ㅢ,20ㅣ순입니다.
                    // ㅗ 인데,, ㅏ ㅐ ㅣ 가 오면 ok
                    if ((idx2 == 8) && ((jungIndex == 0) || (jungIndex == 1) || (jungIndex == 20))) {
                        //9, 10, 11 만들기
                        idx2 = (9 + jungIndex);
                        if (idx2 > 20) idx2 = 11;
                        idx3 = 0;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with ");
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        Log.i("keystroke", "keep STAGE_JUNGSUNG");
                    }
                    // ㅜ 인데, ㅓ ㅔ ㅣ 가 오면 ok
                    else if ((idx2 == 13) && ((jungIndex == 4) || (jungIndex == 5) || (jungIndex == 20))) {
                        //14,15,16 만들기
                        idx2 = (10 + jungIndex);
                        if (idx2 > 20) idx2 = 16;
                        idx3 = 0;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with ");
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        Log.i("keystroke", "keep STAGE_JUNGSUNG");
                    }
                    // - 인데,ㅣ 가 오면 ok
                    else if (idx2 == 18 && jungIndex == 20) {
                        idx2 = 19;
                        idx3 = 0;
                        Log.i("keystroke", "Label matches with jung update at index: " + idx2);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with ");
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        Log.i("keystroke", "keep STAGE_JUNGSUNG");
                    }
                    // 아니면 초성없는 중성모드로 이동
                    else {
                        inputConnection.commitText(label, 1); // Input the label text
                        idx2 = jungIndex;
                        stage = STAGE_JUNGSUNG_ONLY;
                        Log.i("keystroke", "Goes to STAGE_JUNGSUNG_ONLY with " + jung.charAt(idx2) + " idx: " + idx2);
                    }
                }
            } else if (stage == STAGE_JONGSUNG_OR_NEW) {
                // 초중종으로 글자를 끝냈는데,다시 자음이 들어오면 이중종성(STAGE_??)이거나 새로운 초성(GOTO_CHOSONG)일 것이고
                // STAGE_?? 은 STAGE_INITIAL_OR_CHOSUNG 인데.. 그 이유는 정말 이중종성이이서 initial 과 같은 경우가 있을 수 있고..
                // 사실은 이중종성이 아니고 새로운 글자의 초성일 수도 있기 때문이다.

                if (jaumIndex != -1) {
//                                    0없음, 1‘ㄱ’, 2‘ㄲ’, 3‘ㄳ’, 4‘ㄴ’, 5‘ㄵ’, 6‘ㄶ’, 7‘ㄷ’, 8‘ㄹ’, 9‘ㄺ’, 10‘ㄻ’,
//                                    11‘ㄼ’, 12‘ㄽ’, 13‘ㄾ’, 14‘ㄿ’, 15‘ㅀ’, 16‘ㅁ’, 17‘ㅂ’, 18‘ㅄ’,19 ‘ㅅ’, 20‘ㅆ’, 21‘ㅇ’, 22‘ㅈ’, 23‘ㅊ’, 24‘ㅋ’, 25‘ㅌ’, 26‘ㅍ’, 27‘ㅎ’
                    // String jaum = 0ㄱ,1ㄲ,2ㄴ,3ㄷ,4ㄸ,5ㄹ,6ㅁ,7ㅂ,8ㅃ,9ㅅ,10ㅆ,11ㅇ,12ㅈ,13ㅉ,14ㅊ,15ㅋ,16ㅌ,17ㅍ,18ㅎ";
                    if ((idx3 == 1) && (jaumIndex == 9)) { // ㄱ --> ㄳ
                        idx3 = 3;
                        Log.i("keystroke", "Label matches with chong update at index: " + idx3);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        stage = STAGE_INITIAL_OR_CHOSUNG;
                        Log.i("keystroke", "Goes to STAGE_INITIAL_OR_CHOSUNG");
                    } else if ((idx3 == 4) && (jaumIndex == 12 || jaumIndex == 18)) { // ㄴ --> ㄵ ㄶ
                        idx3 = (jaumIndex == 12) ? 5 : 6;
                        Log.i("keystroke", "Label matches with chong update at index: " + idx3);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        stage = STAGE_INITIAL_OR_CHOSUNG;
                        Log.i("keystroke", "Goes to STAGE_INITIAL_OR_CHOSUNG");
                    } else if ((idx3 == 8) && liul_comb) { // ㄹ --> ㄺ ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ
                        if (jaumIndex == 0) { // 0 --> 9
                            idx3 = 9;
                        } else if (jaumIndex == 6 || jaumIndex == 7) { // 6~7 --> 10~11
                            idx3 = jaumIndex + 4;
                        } else if (jaumIndex == 9) { // 9 --> 12
                            idx3 = 12;
                        } else { // 16~18 --> 13~15
                            idx3 = jaumIndex - 3;
                        }
                        Log.i("keystroke", "Label matches with chong update at index: " + idx3);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        stage = STAGE_INITIAL_OR_CHOSUNG;
                        Log.i("keystroke", "Goes to STAGE_INITIAL_OR_CHOSUNG");
                    } else if ((idx3 == 17) && (jaumIndex == 9)) { // ㅂ --> ㅄ
                        idx3 = 18;
                        Log.i("keystroke", "Label matches with chong update at index: " + idx3);
                        Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                        int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                        char[] charArray = Character.toChars(unicodeCodePoint);
                        String koreanCharacter = new String(charArray);
                        Log.i("keystroke", "Final: " + koreanCharacter);
                        HangulCharacterUpdate(inputConnection, koreanCharacter);
                        stage = STAGE_INITIAL_OR_CHOSUNG;
                        Log.i("keystroke", "Goes to STAGE_INITIAL_OR_CHOSUNG");
                    }
                    // 아니라면(즉, 이중자음이 아니라면 입력된 초성을 추가 출력하고 stage_chosung 유지
                    else {
                        Log.i("keystroke", "Last word has done and new Chosung has come.");
                        inputConnection.commitText(label, 1); // Input the label text
                        idx1 = jaumIndex;
                        stage = STAGE_CHOSUNG;
                        Log.i("keystroke", "Goes to STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                    }

                }
                // 초중성 조합을 끝냈는데 모음이 들어왔다면 이전글자를 초/중성으로 변환하고, 이전종성을 초성으로 바꾼다음. 이번 중성과 합쳐서 초/중 조합을 만들고 STAGE_JUNGSUNG 으로 이동
                else {
                    // 이전 글자 지우고
                    Log.i("keystroke", "Delete previous and make new");
                    // 종성 제외한 새 글자로 바꾼다음.
                    int temp = idx3;
                    idx3 = 0;
                    Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2);
                    int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                    char[] charArray = Character.toChars(unicodeCodePoint);
                    String koreanCharacter = new String(charArray);
                    Log.i("keystroke", "Final: " + koreanCharacter);
                    HangulCharacterUpdate(inputConnection, koreanCharacter);
                    // 새로운 초/중성 구성 글자 출력
                    idx1 = choh.indexOf(jong.charAt(temp - 1));
                    idx2 = jung.indexOf(label.toString());
                    idx3 = 0;
                    unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                    charArray = Character.toChars(unicodeCodePoint);
                    koreanCharacter = new String(charArray);
                    Log.i("keystroke", "Final: " + koreanCharacter);
                    inputConnection.commitText(koreanCharacter, 1);
                    // 중성모드로 이동
                    stage = STAGE_JUNGSUNG;
                    Log.i("keystroke", "Goes to STAGE_JUNGSUNG with idx1,idx2:" + idx1 + ", " + idx2);
                }
            } else if (stage == STAGE_INITIAL_OR_CHOSUNG) {
                // 이전글자는 입력 이중종성이었다.
                // 자음이 입력되면 해당 음을 출력하고 STAGE_CHOSUNG 으로 이동하면 된다. STAGE_INITIAL 에서의 자음입력시 동작과 동일하다.
                if (jaumIndex != -1) {
                    inputConnection.commitText(label, 1); // Input the label text
                    idx1 = choh.indexOf(label.toString());
                    stage = STAGE_CHOSUNG;
                    Log.i("keystroke", "Goes to STAGE_CHOSUNG with " + choh.charAt(idx1) + " idx: " + idx1);
                }
                // 모음이 입력되면 이중종성의 첫번째자음는 이전글자를 갱신하는데 사용하고, 이중종성의 두번째 자음은 새롭게 입력된 모음과 합쳐 새로운 글자를 만든다.
                // 그래서 이동할 stage는 STAGE_JUNGSUNG 이다.
                else {
                    // 이중종성에서 글자를 뜯어내는건 할수는 있지만 코딩이 지저분하다. 그러니 이 stage 로 넘어오기전에 이미 필요한 값을 미리 확보하는게 더 편리하다.
                    // 그냥 functional coding 관점에서는 지저분한 코딩이긴 한데. 그게 쉬우니까 그렇게 하기로 한다. (사실 이러면 나중에 백space 동작으로 자모 한글자씩 지울때
                    // 분명 문제가 생긴다는 게 보인다. 그렇지만... 지금은 그 단계가 아니니까...)
                    // JONGSUNG_OR_NEW 로 들어와서 새로운 이중종성으로 바꾸기 전의 글자를 기억해 둘 필요가 있다.
                    // 그런데 그 글자는 STAGE_JUNGSUNG 에서 자음을 받아 종성으로 만들던 시점의 글자코드이다. 그걸 기억하자. variable name: prev_cho_jung_DanjaumJongsung;
                    // 그리고 이중종성의 두번째 자음은 JONGSUNG_OR_NEW 에서 입력받은 자임이다. 그걸 기억하자. variable name: last_jaumIndex;
                    // 다만 last_jaumIndex 은 그냥 구현의 편의상.. 또는 나중을 위해, 항상 기억해 두기로 하자. 당장 필요는 없지만, last_moumIndex 도 만들어두자.
                    // 만드는 김에 last_was_jaum 이라는 boolean 도 만들어 두자.

                    // 이전 글자 지우고
                    Log.i("keystroke", "Delete previous char and show ex of previous.");
                    // 새로운 단자음 종성으로 구성된 글자를 출력하고
                    char[] charArray = Character.toChars(prev_cho_jung_DanjaumJongsung);
                    String koreanCharacter = new String(charArray);
                    Log.i("keystroke", "Final: " + koreanCharacter);
                    HangulCharacterUpdate(inputConnection, koreanCharacter);

                    // 새로운 자음/모음 조합을 출력한다.
                    idx1 = last_jaumIndex;
                    idx2 = jung.indexOf(label.toString());
                    idx3 = 0;
                    Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + " and Jungsung with " + jung.charAt(idx2) + " idx: " + idx2);
                    int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                    charArray = Character.toChars(unicodeCodePoint);
                    koreanCharacter = new String(charArray);
                    Log.i("keystroke", "Final: " + koreanCharacter);
                    inputConnection.commitText(koreanCharacter, 1);

                    stage = STAGE_JUNGSUNG;
                    Log.i("keystroke", "Goes to STAGE_JUNGSUNG");
                }
            } else {
                Toast.makeText(this, "Unsupported Stage. Goto Initial", Toast.LENGTH_LONG).show();
                stage = STAGE_INITIAL;
                Log.i("keystroke", "Goes to STAGE_INITIAL because of unexpected automata state");
            }
        }
        if (jaumIndex != -1) {
            last_jaumIndex = jaumIndex;
            last_was_jaum = true;
        }

        if (moumIndex != -1) {
            last_moumIndex = moumIndex;
            last_was_jaum = false;
        }
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


    static class KeyMapping {
        public final char englishKey;
        public final char anotherCharacter;

        public KeyMapping(char englishKey, char anotherCharacter) {
            this.englishKey = englishKey;
            this.anotherCharacter = anotherCharacter;
        }
    }

}