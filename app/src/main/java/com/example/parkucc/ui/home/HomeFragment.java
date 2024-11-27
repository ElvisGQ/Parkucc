package com.example.parkucc.ui.home;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.R;
import com.example.parkucc.SignActivity;
import com.example.parkucc.VerificationCode;
import com.example.parkucc.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private Toast mToast = null;
    private FragmentHomeBinding binding;
    private int availableSpaces = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        // -------------------------------------------------------------------------------------
        Button refresh = binding.refresh;
        TextView espacios_libres = binding.espaciosLibres;

        // Define the car ImageViews
        ImageView[] cars = {
                binding.car1, binding.car2, binding.car3, binding.car4,
                binding.car5, binding.car6, binding.car7, binding.car8,
                binding.car9, binding.car10
        };

        OkHttpHelper httpHelper = new OkHttpHelper();

        // Make the HTTP request to get the parking space data
        httpHelper.get("http://157.230.232.203/espacios", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    response.close();

                    availableSpaces = 0;

                    try {
                        // Parse the JSON response
                        JSONArray jsonArray = new JSONArray(responseBody);

                        // Iterate through the spaces and update car visibility
                        requireActivity().runOnUiThread(() -> {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject espacioObject = jsonArray.getJSONObject(i);
                                    int idEspacio = espacioObject.getInt("id_espacio");
                                    String disponibilidad = espacioObject.getString("disponibilidad");

                                    if ("Disponible".equals(disponibilidad)) {
                                        availableSpaces++; // Increment counter for available spaces
                                        if (idEspacio >= 1 && idEspacio <= 10) {
                                            cars[idEspacio - 1].setVisibility(View.INVISIBLE); // Hide the car icon
                                        }
                                    } else if ("Ocupado".equals(disponibilidad) && idEspacio >= 1 && idEspacio <= 10) {
                                        cars[idEspacio - 1].setVisibility(View.VISIBLE); // Show the car icon
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Update the TextView with the number of available spaces
                            espacios_libres.setText(availableSpaces + " ESPACIOS LIBRES");
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else {
                    response.close();
                }
            }

        });
        // -------------------------------------------------------------------------------------

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Define the car ImageViews
                ImageView[] cars = {
                        binding.car1, binding.car2, binding.car3, binding.car4,
                        binding.car5, binding.car6, binding.car7, binding.car8,
                        binding.car9, binding.car10
                };

                OkHttpHelper httpHelper = new OkHttpHelper();

                // Make the HTTP request to get the parking space data
                httpHelper.get("http://157.230.232.203/espacios", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Handle failure
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            response.close();

                             availableSpaces = 0;
                            try {
                                // Parse the JSON response
                                JSONArray jsonArray = new JSONArray(responseBody);

                                // Iterate through the spaces and update car visibility
                                requireActivity().runOnUiThread(() -> {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            JSONObject espacioObject = jsonArray.getJSONObject(i);
                                            int idEspacio = espacioObject.getInt("id_espacio");
                                            String disponibilidad = espacioObject.getString("disponibilidad");

                                            if ("Disponible".equals(disponibilidad)) {
                                                availableSpaces++; // Increment counter for available spaces
                                                if (idEspacio >= 1 && idEspacio <= 10) {
                                                    cars[idEspacio - 1].setVisibility(View.INVISIBLE); // Hide the car icon
                                                }
                                            } else if ("Ocupado".equals(disponibilidad) && idEspacio >= 1 && idEspacio <= 10) {
                                                cars[idEspacio - 1].setVisibility(View.VISIBLE); // Show the car icon
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // Update the TextView with the number of available spaces
                                    espacios_libres.setText(availableSpaces + " ESPACIOS LIBRES");
                                });
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        } else {
                            response.close();
                        }
                    }
                });

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


