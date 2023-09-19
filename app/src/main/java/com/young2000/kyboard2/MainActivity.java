package com.young2000.kyboard2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputMethodManager imeManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledMethods = imeManager.getEnabledInputMethodList();
        Log.i("activity", "MainActivity is executed");
        boolean checkPass = false;

        TextView tvRegStatus = findViewById(R.id.regStatus);
        for (InputMethodInfo method : enabledMethods) {
            if (method.getPackageName().equals("com.young2000.kyboard2")) {
                // Your keyboard is enabled
                tvRegStatus.setText("Kyboard is registered already");
                checkPass = true;
                break;
            }
        }
        if (!checkPass) {
            tvRegStatus.setText("Kyboard is not registered!");
        }

        String selectedKeyboardId = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);


        TextView tvSelectStatus = findViewById(R.id.selectStatus);
        if (selectedKeyboardId != null && selectedKeyboardId.contains("com.young2000.kyboard2")) {
            // Your keyboard is currently selected
            tvSelectStatus.setText("Kyboard is selected already");
        } else {
            // Your keyboard is not currently selected
            tvSelectStatus.setText("Kyboard is not selected!");
            //PackageManager ims = getPackageManager();
            //Log.i("checkSS", String.valueOf(ims.hasSystemFeature("FEATURE_INPUT_METHODS")));


        }
    }

    public void KyboarRegistration (View view) {
        //Toast.makeText(this, "Kyboard is not registered", Toast.LENGTH_LONG).show();
        Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(enableIntent);
    }

    public void KyboardSelection(View view) {
        InputMethodManager imeManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        final Runnable r = new Runnable() {
            public void run() {
                imeManager.showInputMethodPicker();
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(r, 1000);
    }
}

