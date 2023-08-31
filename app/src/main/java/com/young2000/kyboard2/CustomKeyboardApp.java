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
    public static final int STAGE_INITIAL = 0;
    public static final int STAGE_CHOSUNG = 1;
    public static final int STAGE_JUNGSUNG = 2;
    public static final int STAGE_NONKNOWN = 3;
    public static final int STAGE_JONGSUNG_OR_NEW = 4;
    boolean delete_chosong;
    int stage;
    int idx1, idx2, idx3;
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
        stage = STAGE_INITIAL;
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
            if (English_Korean==1) {
                stage=STAGE_INITIAL;
            }
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
            stage=STAGE_INITIAL;
        } else {
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == primaryCode) {
                    CharSequence label = key.label;
                    // Non-Shift key pressed
                    Log.i("keystroke", String.valueOf((char) primaryCode));
                    if (English_Korean==0) {
                        inputConnection.commitText(label, 1); // Input the label text
                        // If the normal key is stroked, clean the isShift variable to return to normal.
                    } else {
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
                        String choh = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
                        String jung = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
                        String jong = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

                        int chohIndex = choh.indexOf(label.toString());
                        int jungIndex = jung.indexOf(label.toString());
                        int jongIndex = jong.indexOf(label.toString());

                        Log.i("keystroke", "choh,jung,jong: " + chohIndex + " " + jungIndex + " " + jongIndex );
                        Log.i("keystroke", "idx1,idx2,idx3: " + idx1 + " " + idx2 + " " + idx3 );

                        Log.i("keystroke", "stage :" + stage);

                        if (stage == STAGE_INITIAL) {
                            if (chohIndex != -1) {
                                inputConnection.commitText(label, 1); // Input the label text
                                idx1 = chohIndex;
                                stage = STAGE_CHOSUNG;
                                delete_chosong = true;

                                Log.i("keystroke", "Goes to Chosung with " + choh.charAt(idx1) + " idx: " + idx1);
                            }
                        } else if (stage == STAGE_CHOSUNG) {
                            if (jungIndex != -1) {
                                if (delete_chosong) {
                                    inputConnection.deleteSurroundingText(1, 0);
                                }

                                //inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                                //inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                                idx2 = jungIndex;
                                idx3 = 0;
                                Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + " and Jungsung with " + jung.charAt(idx2) + " idx: " + idx2);
                                int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                                char[] charArray = Character.toChars(unicodeCodePoint);
                                String koreanCharacter = new String(charArray);
                                Log.i("keystroke", "Final: " + koreanCharacter);
                                stage = STAGE_JUNGSUNG;

                                Log.i("keystroke", "Goes to STAGE_JUNGSUNG");
                                inputConnection.commitText(koreanCharacter, 1);
                            }
                        } else if (stage == STAGE_JUNGSUNG) {
                            if (jungIndex != -1) {
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
                                    inputConnection.commitText(koreanCharacter, 1);
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
                                    inputConnection.commitText(koreanCharacter, 1);
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
                                    inputConnection.commitText(koreanCharacter, 1);
                                    Log.i("keystroke", "keep STAGE_JUNGSUNG");
                                }
                                // 아니면 초성없는 중성모드로 이동
                                else {
                                    Log.i("keystroke", "중성시작은 아직 미지원");
                                }
                            } else if (jongIndex != -1) {
                                inputConnection.deleteSurroundingText(1, 0);

                                //inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                                //inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                                idx3 = jongIndex + 1;
                                Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2 + ", Jongsung with " + jong.charAt(idx3 - 1) + " idx: " + idx3);
                                int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                                char[] charArray = Character.toChars(unicodeCodePoint);
                                String koreanCharacter = new String(charArray);
                                Log.i("keystroke", "Final: " + koreanCharacter);
                                inputConnection.commitText(koreanCharacter, 1);
                                stage = STAGE_JONGSUNG_OR_NEW;
                                Log.i("keystroke", "Goes to STAGE_JONGSUNG_OR_NEW");

                            }
                        } else if (stage == STAGE_JONGSUNG_OR_NEW) {
                            // if 중성.
                            if (jungIndex != -1) {
                                // 이전 글자 지우고
                                Log.i("keystroke", "Delete previous and make new");
                                inputConnection.deleteSurroundingText(1, 0);
                                // 종성 제외한 새 글자로 바꾼다음.
                                int temp = idx3;
                                idx3 = 0;
                                Log.i("keystroke", "Chosung with " + choh.charAt(idx1) + " idx: " + idx1 + ", Jungsung with " + jung.charAt(idx2) + " idx: " + idx2);
                                int unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                                char[] charArray = Character.toChars(unicodeCodePoint);
                                String koreanCharacter = new String(charArray);
                                Log.i("keystroke", "Final: " + koreanCharacter);
                                inputConnection.commitText(koreanCharacter, 1);
                                // 새로운 초/중성 구성 글자 출력
                                idx1 = choh.indexOf(jong.charAt(temp - 1));
                                idx2 = jungIndex;
                                idx3 = 0;
                                unicodeCodePoint = idx1 * 21 * 28 + idx2 * 28 + idx3 + 0xAC00;
                                charArray = Character.toChars(unicodeCodePoint);
                                koreanCharacter = new String(charArray);
                                Log.i("keystroke", "Final: " + koreanCharacter);
                                inputConnection.commitText(koreanCharacter, 1);
                                // 중성모드로 이동
                                stage = STAGE_JUNGSUNG;
                                Log.i("keystroke", "Goes to STAGE_JUNGSUNG with idx1,idx2:" + idx1 + ", " + idx2);
                            } else {
                                // else 초성
                                Log.i("keystroke", "Last word has done.");
                                idx1 = chohIndex;
                                // 초성 모드로 넘어가거나
                                inputConnection.commitText(label, 1); // Input the label text
                                stage = STAGE_CHOSUNG;
                                delete_chosong = true;
                                Log.i("keystroke", "Goes to STAGE_CHOSUNG with idx1:" + idx1);

                                // 이중종성처리. 일단 이중종성 없이 초성으로 넘어간다고 치자
                                // ㄱ --> ㄳ
                                // ㄴ --> ㄵ ㄶ
                                // ㄹ --> ㄺ ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ
                                // ㅂ --> ㅄ

                                //0없음, 1‘ㄱ’, 2‘ㄲ’, 3‘ㄳ’, 4‘ㄴ’, 5‘ㄵ’, 6‘ㄶ’, 7‘ㄷ’, 8‘ㄹ’, 9‘ㄺ’, 10‘ㄻ’,
                                //11‘ㄼ’, 12‘ㄽ’, 13‘ㄾ’, 14‘ㄿ’, 15‘ㅀ’, 16‘ㅁ’, 17‘ㅂ’, 18‘ㅄ’,19 ‘ㅅ’, 20‘ㅆ’, 21‘ㅇ’, 22‘ㅈ’, 23‘ㅊ’, 24‘ㅋ’, 25‘ㅌ’, 26‘ㅍ’, 27‘ㅎ’
                                Log.i("keystroke", "이중 종성은 미지원");
                            }
                        }
                    }
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
        } else {
            for (Keyboard.Key key : keys) {
                CharSequence label = key.label;
                int pmCode = key.codes[0];
                // Handle default case here
                if (label != null && pmCode != 10 && pmCode != 32 && pmCode != -1 && pmCode != 67 && pmCode != 10 && pmCode != 1001 && !label.equals(".")) {

                    if (!isShifted) {
                        if (label.toString().equals("ㅃ")) {
                            key.label = "ㅂ";
                        } else if (label.toString().equals("ㅉ")) {
                            key.label = "ㅈ";
                        } else if (label.toString().equals("ㄸ")) {
                            key.label = "ㄷ";
                        } else if (label.toString().equals("ㄲ")) {
                            key.label = "ㄱ";
                        } else if (label.toString().equals("ㅆ")) {
                            key.label = "ㅅ";
                        } else if (label.toString().equals("ㅒ")) {
                            key.label = "ㅐ";
                        } else if (label.toString().equals("ㅖ")) {
                            key.label = "ㅔ";
                        }
                    } else {
                        if (label.toString().equals("ㅂ")) {
                            key.label = "ㅃ";
                        } else if (label.toString().equals("ㅈ")) {
                            key.label = "ㅉ";
                        } else if (label.toString().equals("ㄷ")) {
                            key.label = "ㄸ";
                        } else if (label.toString().equals("ㄱ")) {
                            key.label = "ㄲ";
                        } else if (label.toString().equals("ㅅ")) {
                            key.label = "ㅆ";
                        } else if (label.toString().equals("ㅐ")) {
                            key.label = "ㅒ";
                        } else if (label.toString().equals("ㅔ")) {
                            key.label = "ㅖ";
                        }
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