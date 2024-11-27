package com.example.parkucc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignActivity extends AppCompatActivity {

    TextView sign_in_link;
    Button sign_btn;
    EditText username_input, email_input, confirm_password_input, password_input;
    private Toast mToast = null;

    private static final String EMAIL_REGEX = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    // Compile the regex into a Pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign);

        username_input = findViewById(R.id.username_input);
        email_input = findViewById(R.id.email_input);
        confirm_password_input = findViewById(R.id.confirm_password_input);
        password_input = findViewById(R.id.password_input);
        sign_btn = findViewById(R.id.sign_btn_id);
        sign_in_link = findViewById(R.id.sign_in_link);

        sign_in_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SignActivity.this, Login.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        sign_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Disable the button to prevent spamming
                sign_btn.setEnabled(false);

                String username = username_input.getText().toString();
                String email = email_input.getText().toString();
                String password = password_input.getText().toString();
                String confirm = confirm_password_input.getText().toString();

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || confirm.isEmpty()) {

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
                    mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -400); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();

                    // Re-enable the button after showing the toast
                    sign_btn.setEnabled(true);

                } else if (!password.equals(confirm)) {

                    if (mToast != null) mToast.cancel();

                    // Inflate the custom layout
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_custom,
                            (ViewGroup) findViewById(R.id.custom_toast));

                    // Set the message text (optional)
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Las contraseñas no coinciden");

                    // Create the Toast
                    mToast = new Toast(getApplicationContext());
                    mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -400); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();

                    // Re-enable the button after showing the toast
                    sign_btn.setEnabled(true);

                } else if (!isValidEmail(email)) {

                    if (mToast != null) mToast.cancel();

                    // Inflate the custom layout
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_custom,
                            (ViewGroup) findViewById(R.id.custom_toast));

                    // Set the message text (optional)
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Ingrese un correo válido");

                    // Create the Toast
                    mToast = new Toast(getApplicationContext());
                    mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -400); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();

                    // Re-enable the button after showing the toast
                    sign_btn.setEnabled(true);

                } else {

                    OkHttpHelper httpHelper = new OkHttpHelper();

                    // Example: POST request
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("correo", email);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    httpHelper.post("http://157.230.232.203/emailVerificacion", postData, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Handle failure
                            e.printStackTrace();

                            // Re-enable the button after failure
                            runOnUiThread(() -> sign_btn.setEnabled(true));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {

                                int statusCode = response.code();
                                String responseBody = response.body().string();

                                if (statusCode == 200) {

                                    try {
                                        JSONObject jsonResponse = new JSONObject(responseBody);
                                        int status = jsonResponse.getInt("status");

                                        if (status == 200) {

                                            Intent intent = new Intent(SignActivity.this, VerificationCode.class);
                                            intent.putExtra("username", username);
                                            intent.putExtra("password", password);
                                            intent.putExtra("email", email);
                                            startActivity(intent);
                                            finishAffinity();

                                        } else if (status == 409) {
                                            runOnUiThread(() -> showToast("Una cuenta ya existe bajo ese correo"));
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    runOnUiThread(() -> showToast("Email no válido"));
                                }

                                response.close();

                            } else {
                                runOnUiThread(() -> showToast("No hay conexión"));
                            }

                            // Re-enable the button after the HTTP request is complete
                            runOnUiThread(() -> sign_btn.setEnabled(true));
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
                            mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -400); // Center the toast
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

