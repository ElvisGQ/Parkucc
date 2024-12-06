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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ForgotPassword extends AppCompatActivity {

    private Toast mToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        TextView cancel = findViewById(R.id.cancel_link);
        Button enviar = findViewById(R.id.enviar_btn);
        EditText email_input = findViewById(R.id.email_input);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enviar.setEnabled(false);

                String email = email_input.getText().toString();

                if(email.isEmpty()){

                    // Inflate the custom layout
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_custom,
                            (ViewGroup) findViewById(R.id.custom_toast));

                    // Set the message text (optional)
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Favor de llenar el campo");

                    // Create the Toast
                    mToast = new Toast(getApplicationContext());
                    mToast.setGravity(Gravity.CENTER, 0, 0); // Center the toast
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout); // Set the custom layout as the Toast view
                    mToast.show();

                    enviar.setEnabled(true);
                    return;
                }

                OkHttpHelper httpHelper = new OkHttpHelper();

                // Example: POST request
                JSONObject postData = new JSONObject();
                try {
                    postData.put("correo", email);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                httpHelper.post("http://157.230.232.203/emailContrasena", postData, new Callback() {
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
                                    runOnUiThread(() -> {
                                        enviar.setEnabled(true);

                                        // Navigate to another activity
                                        Intent intent = new Intent(ForgotPassword.this, Login.class);
                                        startActivity(intent);
                                        finish();
                                    });

                                } else {
                                    runOnUiThread(() -> showToast("No existe el correo"));
                                }

                                enviar.setEnabled(true);

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
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ForgotPassword.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishAffinity();

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