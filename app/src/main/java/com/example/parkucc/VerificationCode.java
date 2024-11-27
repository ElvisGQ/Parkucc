package com.example.parkucc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VerificationCode extends AppCompatActivity {

    Button verify_btn;
    private Toast mToast = null;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification_code);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        verify_btn = findViewById(R.id.verify_btn);
        EditText digit1 = findViewById(R.id.digit1);
        EditText digit2 = findViewById(R.id.digit2);
        EditText digit3 = findViewById(R.id.digit3);
        EditText digit4 = findViewById(R.id.digit4);

        ImageView imageView8 = findViewById(R.id.imageView8);
        ImageView imageView7 = findViewById(R.id.imageView7);
        ImageView popupIcon = findViewById(R.id.pop_id); // For popup icon
        Button continuarBtn = findViewById(R.id.continuar_btn);
        TextView popupMessage = findViewById(R.id.text_gone); // Adjust `id` to match your TextView id
        TextView cancelar = findViewById(R.id.cancel_link);

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(VerificationCode.this, Login.class);
                startActivity(intent);
                finishAffinity();

            }
        });

        continuarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(VerificationCode.this, Login.class);
                startActivity(intent);
                finishAffinity();

            }
        });

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(digit1.getText().toString().isEmpty() || digit2.getText().toString().isEmpty() || digit3.getText().toString().isEmpty() || digit4.getText().toString().isEmpty()){

                    if (mToast != null) mToast.cancel();

                    // Inflate the custom layout
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_custom,
                            (ViewGroup) findViewById(R.id.custom_toast));

                    // Set the message text (optional)
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Ingrese todos los dígitos");

                    // Create the Toast
                    mToast = new Toast(getApplicationContext());
                    mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -150); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();
                }else{

                    String code = digit1.getText().toString() + digit2.getText().toString() + digit3.getText().toString() + digit4.getText().toString();

                    OkHttpHelper httpHelper = new OkHttpHelper();

                    // Example: POST request
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("correo", email);
                        postData.put("token", code);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    httpHelper.post("http://157.230.232.203/validarCodigo", postData, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Handle failure
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()) {

                                int statusCode = response.code();
                                String responseBody = response.body().string();

                                if (statusCode == 200) {

                                    // Create the second request here
                                    JSONObject secondPostData = new JSONObject();
                                    try {
                                        secondPostData.put("id_rol", 1);
                                        secondPostData.put("nombre", username);
                                        secondPostData.put("correo", email);
                                        secondPostData.put("contrasena", password);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // Make the second request
                                    httpHelper.post("http://157.230.232.203/usuarios", secondPostData, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            // Handle failure of the second request
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {

                                        }
                                    });

                                    verify_btn.setEnabled(false);
                                    cancelar.setEnabled(false);

                                    runOnUiThread(() -> {
                                        imageView8.setVisibility(View.VISIBLE);
                                        imageView7.setVisibility(View.VISIBLE);
                                        popupIcon.setVisibility(View.VISIBLE);
                                        continuarBtn.setVisibility(View.VISIBLE);
                                        popupMessage.setVisibility(View.VISIBLE);
                                    });

                                }
                                else {
                                    runOnUiThread(() -> showToast("Token no válido"));
                                }

                                response.close();

                            }else{
                                runOnUiThread(() -> showToast("No hay conexión"));
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

        digit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 1) {
                    digit2.requestFocus();  // Move to next EditText
                } else if (editable.length() == 0) {
                    // If text is deleted, move focus back to digit1 (keep the cursor visible)
                    if (!digit1.hasFocus()) {
                        digit1.requestFocus();
                    }
                }
            }
        });

        digit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 1) {
                    digit3.requestFocus();  // Move to next EditText
                } else if (editable.length() == 0) {
                    digit1.requestFocus();  // Move focus back to previous EditText
                }
            }
        });

        digit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 1) {
                    digit4.requestFocus();  // Move to next EditText
                } else if (editable.length() == 0) {
                    digit2.requestFocus();  // Move focus back to previous EditText
                }
            }
        });

        digit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    digit3.requestFocus();  // Move focus back to previous EditText
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