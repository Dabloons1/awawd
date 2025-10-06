package com.myfloatingmodule;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    
    private SharedPreferences preferences;
    private EditText targetPackageEdit;
    private CheckBox autoStartCheck;
    private EditText windowXEdit;
    private EditText windowYEdit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferences = getSharedPreferences("floating_module_prefs", MODE_PRIVATE);
        setContentView(createLayout());
        loadSettings();
    }
    
    private LinearLayout createLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        TextView title = new TextView(this);
        title.setText("Module Settings");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);
        
        // Target package setting
        TextView targetLabel = new TextView(this);
        targetLabel.setText("Target Package (leave empty for all):");
        targetLabel.setPadding(0, 0, 0, 10);
        layout.addView(targetLabel);
        
        targetPackageEdit = new EditText(this);
        targetPackageEdit.setHint("com.example.app");
        layout.addView(targetPackageEdit);
        
        // Auto start setting
        autoStartCheck = new CheckBox(this);
        autoStartCheck.setText("Auto start floating window");
        autoStartCheck.setPadding(0, 20, 0, 20);
        layout.addView(autoStartCheck);
        
        // Window position settings
        TextView positionLabel = new TextView(this);
        positionLabel.setText("Window Position:");
        positionLabel.setPadding(0, 20, 0, 10);
        layout.addView(positionLabel);
        
        LinearLayout positionLayout = new LinearLayout(this);
        positionLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        windowXEdit = new EditText(this);
        windowXEdit.setHint("X");
        windowXEdit.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        positionLayout.addView(windowXEdit);
        
        windowYEdit = new EditText(this);
        windowYEdit.setHint("Y");
        windowYEdit.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        positionLayout.addView(windowYEdit);
        
        layout.addView(positionLayout);
        
        // Save button
        Button saveButton = new Button(this);
        saveButton.setText("Save Settings");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        saveButton.setPadding(0, 30, 0, 0);
        layout.addView(saveButton);
        
        return layout;
    }
    
    private void loadSettings() {
        targetPackageEdit.setText(preferences.getString("target_package", ""));
        autoStartCheck.setChecked(preferences.getBoolean("auto_start", false));
        windowXEdit.setText(String.valueOf(preferences.getInt("window_x", 100)));
        windowYEdit.setText(String.valueOf(preferences.getInt("window_y", 100)));
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("target_package", targetPackageEdit.getText().toString());
        editor.putBoolean("auto_start", autoStartCheck.isChecked());
        
        try {
            int x = Integer.parseInt(windowXEdit.getText().toString());
            int y = Integer.parseInt(windowYEdit.getText().toString());
            editor.putInt("window_x", x);
            editor.putInt("window_y", y);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid position values", Toast.LENGTH_SHORT).show();
            return;
        }
        
        editor.apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }
}
