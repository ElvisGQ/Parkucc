package com.example.parkucc.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.R;
import com.example.parkucc.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private int availableSpaces = 0;
    private Button[] buttons;
    private ImageView[] cars;
    private boolean isGuardiaRole = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", "");
        isGuardiaRole = userRole.equals("Guardia");

        Button refresh = binding.refresh;
        TextView espacios_libres = binding.espaciosLibres;

        ImageView flechaSeccionA1haciaA2 = binding.flechaSeccionA1haciaA2;
        flechaSeccionA1haciaA2.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_parkingSectionA2)
        );
        ImageView flechaSeccionA1haciaB1 = binding.flechaSeccionA1haciaB1;
        flechaSeccionA1haciaB1.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_parkingSectionB1)
        );

        buttons = new Button[]{
                binding.buttonCar1, binding.buttonCar2, binding.buttonCar3,
                binding.buttonCar4, binding.buttonCar5, binding.buttonCar6,
                binding.buttonCar7, binding.buttonCar8, binding.buttonCar9,
                binding.buttonCar10
        };

        cars = new ImageView[]{
                binding.car1, binding.car2, binding.car3, binding.car4,
                binding.car5, binding.car6, binding.car7, binding.car8,
                binding.car9, binding.car10
        };

        OkHttpHelper httpHelper = new OkHttpHelper();
        fetchParkingData(httpHelper, espacios_libres);

        refresh.setOnClickListener(view -> fetchParkingData(httpHelper, espacios_libres));

        View.OnClickListener buttonClickListener = v -> {
            if (isGuardiaRole) {
                Toast.makeText(requireContext(), "Los guardias no pueden realizar reservaciones.", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < buttons.length; i++) {
                if (v.getId() == buttons[i].getId()) {
                    if (cars[i].getVisibility() == View.VISIBLE) {
                        Toast.makeText(requireContext(), "Este lugar está ocupado", Toast.LENGTH_SHORT).show();
                    } else {
                        int finalI = i;
                        checkActiveReservation(() -> {
                            String carInfo = "A" + (finalI + 1);
                            showPopup(carInfo);
                        });
                    }
                    break;
                }
            }
        };

        for (Button button : buttons) {
            button.setOnClickListener(buttonClickListener);
            if (isGuardiaRole) {
                button.setEnabled(false);
                button.setAlpha(0.5f);
            }
        }

        return root;
    }

    private void fetchParkingData(OkHttpHelper httpHelper, TextView espacios_libres) {
        httpHelper.get("http://157.230.232.203/espacios", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    response.close();

                    availableSpaces = 0;

                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        requireActivity().runOnUiThread(() -> {
                            if (binding != null && isAdded()) { // Validación de binding y estado del fragmento
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        JSONObject espacioObject = jsonArray.getJSONObject(i);
                                        int idEspacio = espacioObject.getInt("id_espacio");
                                        String disponibilidad = espacioObject.getString("disponibilidad");

                                        if (idEspacio >= 1 && idEspacio <= 10) {
                                            int index = idEspacio - 1;

                                            if ("Disponible".equals(disponibilidad)) {
                                                availableSpaces++;
                                                cars[index].setVisibility(View.INVISIBLE);
                                                if (isGuardiaRole) {
                                                    buttons[index].setEnabled(false);
                                                    buttons[index].setAlpha(0.0f);
                                                } else {
                                                    buttons[index].setEnabled(true);
                                                    buttons[index].setAlpha(0.0f);
                                                }
                                            } else if ("Ocupado".equals(disponibilidad)) {
                                                cars[index].setVisibility(View.VISIBLE);
                                                buttons[index].setEnabled(false);
                                                buttons[index].setAlpha(0.0f);
                                            } else if ("Reservado".equals(disponibilidad)) {
                                                cars[index].setVisibility(View.INVISIBLE);
                                                buttons[index].setEnabled(false);
                                                buttons[index].setAlpha(0.5f);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                espacios_libres.setText(availableSpaces + " ESPACIOS LIBRES");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.close();
                }
            }
        });
    }

    private void checkActiveReservation(Runnable onNoActiveReservation) {
        OkHttpHelper httpHelper = new OkHttpHelper();
        httpHelper.get("http://157.230.232.203/getReservaciones", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "No se pudo verificar reservaciones. Continuando...", Toast.LENGTH_SHORT).show();
                    onNoActiveReservation.run();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    response.close();

                    try {
                        JSONArray reservaciones = new JSONArray(responseBody);

                        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                        String currentUser = sharedPreferences.getString("userName", "");

                        boolean hasActiveReservation = false;

                        for (int i = 0; i < reservaciones.length(); i++) {
                            JSONObject reservacion = reservaciones.getJSONObject(i);

                            String userName = reservacion.getString("nombre_usuario");
                            String fechaFin = reservacion.getString("fecha_fin");

                            if (userName.equals(currentUser)) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date fechaFinDate = sdf.parse(fechaFin);
                                Date currentDate = new Date();

                                if (fechaFinDate != null && fechaFinDate.after(currentDate)) {
                                    hasActiveReservation = true;
                                    break;
                                }
                            }
                        }

                        boolean finalHasActiveReservation = hasActiveReservation;
                        requireActivity().runOnUiThread(() -> {
                            if (finalHasActiveReservation) {
                                Toast.makeText(requireContext(), "Ya tienes una reservación activa.", Toast.LENGTH_SHORT).show();
                            } else {
                                onNoActiveReservation.run();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(onNoActiveReservation::run);
                    }
                } else {
                    response.close();
                    requireActivity().runOnUiThread(onNoActiveReservation::run);
                }
            }
        });
    }

    private void showPopup(String carInfo) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        TextView titleText = popupView.findViewById(R.id.nombreCajonPopup);
        Button closeButton = popupView.findViewById(R.id.close_button);
        Button reserveButton = popupView.findViewById(R.id.reserve_button);

        titleText.setText("Lugar: " + carInfo);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);

        closeButton.setOnClickListener(v -> popupWindow.dismiss());

        reserveButton.setOnClickListener(v -> {
            String espacio = carInfo.replaceAll("[^\\d]", "");

            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            String nombre = sharedPreferences.getString("userName", "UsuarioDemo");

            String fechaFin = getCurrentDateTimePlus30Minutes();

            makeReservation(espacio, nombre, fechaFin, popupWindow);
        });
    }

    private void makeReservation(String espacio, String nombre, String fechaFin, PopupWindow popupWindow) {
        OkHttpHelper httpHelper = new OkHttpHelper();
        JSONObject postData = new JSONObject();

        try {
            postData.put("espacio", espacio);
            postData.put("nombre_usuario", nombre);
            postData.put("fecha_fin", fechaFin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        httpHelper.post("http://157.230.232.203/setReservacion", postData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    response.close();

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        int status = jsonResponse.getInt("status");

                        requireActivity().runOnUiThread(() -> {
                            if (status == 200) {
                                Toast.makeText(requireContext(), "Reservación exitosa", Toast.LENGTH_SHORT).show();
                                popupWindow.dismiss();
                                int index = Integer.parseInt(espacio) - 1;
                                buttons[index].setAlpha(0.5f);
                                buttons[index].setEnabled(false);
                            } else if (status == 409) {
                                Toast.makeText(requireContext(), "Espacio ocupado en el tiempo especificado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Error al reservar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.close();
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error del servidor", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private String getCurrentDateTimePlus30Minutes() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        return sdf.format(calendar.getTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
