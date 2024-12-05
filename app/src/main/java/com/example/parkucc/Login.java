package com.example.parkucc;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    Button login_btn;
    EditText email_input, password_input;
    private Toast mToast = null;
    TextView sign_up_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(Login.this, NavBarActivity.class);
            startActivity(intent);
            finish();
        }

        login_btn = findViewById(R.id.login_btn_id);
        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        sign_up_link = findViewById(R.id.sign_up_link);

        sign_up_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignActivity.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = email_input.getText().toString();
                String password = password_input.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    if (mToast != null) mToast.cancel();

                    // Inflate the custom layout
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_custom,
                            (ViewGroup) findViewById(R.id.custom_toast));

                    // Set the message text (optional)
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Favor de llenar todos los campos");

                    // Create the Toast
                    mToast = new Toast(getApplicationContext());
                    mToast.setGravity(Gravity.CENTER, 0, 0); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();

                } else {

                    OkHttpHelper httpHelper = new OkHttpHelper();

                    // Example: POST request
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("correo", email);
                        postData.put("contrasena", password);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    httpHelper.post("http://157.230.232.203/validarUsuario", postData, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Handle failure
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseBody);
                                    int status = jsonResponse.getInt("status");

                                    if (status == 200) {
                                        // Extract the user object
                                        JSONObject user = jsonResponse.getJSONObject("user");

                                        String userRole = "";

                                        switch (user.getInt("id_rol")){

                                            case 1: userRole = "Estudiante"; break;
                                            case 2: userRole = "Guardia"; break;
                                            case 3: userRole = "Visitante"; break;
                                            case 4: userRole = "Admin"; break;

                                        }

                                        // Save user data to SharedPreferences
                                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("isLoggedIn", true);
                                        editor.putString("userEmail", user.getString("correo"));
                                        editor.putString("userName", user.getString("nombre"));
                                        editor.putString("userRole", userRole);
                                        editor.apply(); // Save changes


                                        // Navigate to another activity
                                        Intent intent = new Intent(Login.this, LoadingLogin.class);
                                        startActivity(intent);
                                        finish();

                                    } else if (status == 401) {
                                        runOnUiThread(() -> showToast("Credenciales incorrectas"));
                                    } else if (status == 404) {
                                        runOnUiThread(() -> showToast("Cuenta no extistente"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    response.close();
                                }
                            } else {
                                runOnUiThread(() -> showToast("No hay conexión."));
                            }
                        }

                        private void showToast(String message) {
                            if (mToast != null) {
                                mToast.cancel();
                            }

                            // Inflate the custom layout for Toast
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast_custom,
                                    (ViewGroup) findViewById(R.id.custom_toast));

                            // Set the message text (optional)
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText(message);

                            // Create the Toast
                            mToast = new Toast(getApplicationContext());
                            mToast.setGravity(Gravity.CENTER, 0, 0); // Center the toast
                            mToast.setDuration(Toast.LENGTH_SHORT);
                            mToast.setView(layout); // Set the custom layout as the Toast view
                            mToast.show();
                        }

                    });

                }

            }
        });

        View rootView = findViewById(R.id.main);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Hide the keyboard and remove focus
                    View focusedView = getCurrentFocus();
                    if (focusedView != null && focusedView instanceof EditText) {
                        focusedView.clearFocus();  // Remove focus
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);  // Hide the keyboard
                    }
                }
                return false;  // Let the event propagate further
            }
        });
    }
}
