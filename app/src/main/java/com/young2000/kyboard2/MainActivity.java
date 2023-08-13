package com.young2000.kyboard2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputMethodManager imeManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledMethods = imeManager.getEnabledInputMethodList();

        boolean checkPass = false;
        for (InputMethodInfo method : enabledMethods) {
            if (method.getPackageName().equals("com.young2000.kyboard2")) {
                // Your keyboard is enabled
                Toast.makeText(this, "Kyboard is registered", Toast.LENGTH_LONG).show();;
                checkPass = true;
            }
        }
        if (!checkPass) {
            Toast.makeText(this, "Kyboard is not registered", Toast.LENGTH_LONG).show();;
            Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(enableIntent);
        }

        String selectedKeyboardId = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

        if (selectedKeyboardId != null && selectedKeyboardId.contains("com.young2000.kyboard2")) {
            // Your keyboard is currently selected
            Toast.makeText(this, "Kyboard is selected currently", Toast.LENGTH_LONG).show();;
        } else {
            // Your keyboard is not currently selected
            Toast.makeText(this, "Kyboard is not selected currently", Toast.LENGTH_LONG).show();;
        }
    }
}