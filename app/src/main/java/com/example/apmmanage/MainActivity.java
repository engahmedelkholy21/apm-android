package com.example.apmmanage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    Connection con;
    String Connection_result = "";
    private static final int ALL_PERMISSIONS_REQUEST = 200;
    private final String[] requiredPermissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
    };

    // For Remember Me and Server Info
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "apm_login_pref";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_SERVER_INFO = "server_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAppPermissions();

        MaterialButton btn_Get = findViewById(R.id.idBtnLogin);
        TextInputEditText pw_txt = findViewById(R.id.pw_txt);
        TextInputEditText user_name_txt = findViewById(R.id.un_txt);
        TextInputEditText server_info = findViewById(R.id.server_info);
        SwitchMaterial switchRememberMe = findViewById(R.id.switchRememberMe);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Auto-fill saved data
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, null);
        String savedServerInfo = sharedPreferences.getString(KEY_SERVER_INFO, null);

        if (savedServerInfo != null) {
            server_info.setText(savedServerInfo);
        }

        if (savedUsername != null && savedPassword != null) {
            user_name_txt.setText(savedUsername);
            pw_txt.setText(savedPassword);
            switchRememberMe.setChecked(true);
        }

        btn_Get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalData.server="";
                String serverInfoText = server_info.getText().toString().trim();
                if (serverInfoText.isEmpty()) {
                    server_info.setError("يرجى إدخال عنوان الخادم");
                    return;
                }

                GlobalData.server=serverInfoText;

                try {
                    // Save server info regardless of Remember Me
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_SERVER_INFO, serverInfoText);
                    editor.apply();

                    ConnectionHelper co = new ConnectionHelper();
                    // You might want to modify your ConnectionHelper to use the server info
                    con = co.conclass();

                    if (con != null) {
                        String query = "SELECT * FROM user_previliges WHERE user_name = '" + user_name_txt.getText().toString() + "'";
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(query);

                        String retrievedPassword = null;
                        String userFar3 = null;

                        while (rs.next()) {
                            retrievedPassword = rs.getString("password");
                            userFar3 = rs.getString("user_far3");
                        }

                        if (retrievedPassword != null && pw_txt.getText().toString().equals(retrievedPassword)) {
                            // Save login info if Remember Me is checked
                            if (switchRememberMe.isChecked()) {
                                editor = sharedPreferences.edit();
                                editor.putString(KEY_USERNAME, user_name_txt.getText().toString());
                                editor.putString(KEY_PASSWORD, pw_txt.getText().toString());
                                editor.apply();
                            } else {
                                // If not checked, clear saved login data but keep server info
                                editor = sharedPreferences.edit();
                                editor.remove(KEY_USERNAME);
                                editor.remove(KEY_PASSWORD);
                                editor.apply();
                            }

                            pw_txt.setText("");

                            GlobalData.userFar3 = userFar3;
                            GlobalData.user_name = user_name_txt.getText().toString();

                            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this, "تأكد من البيانات", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "فشل الاتصال بالخادم", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean arePermissionsGranted() {
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, ALL_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_REQUEST) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(this, "يجب منح الصلاحيات المطلوبة!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestAppPermissions() {
        if (!arePermissionsGranted()) {
            requestPermissions();
        }
    }
}