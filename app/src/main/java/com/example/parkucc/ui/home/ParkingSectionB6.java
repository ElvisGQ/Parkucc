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
import androidx.navigation.Navigation;

import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.R;
import com.example.parkucc.databinding.FragmentParkingSectionB6Binding;

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

public class ParkingSectionB6 extends Fragment {

    private FragmentParkingSectionB6Binding binding;
    private int availableSpaces = 0;
    private Button[] buttons;
    private ImageView[] cars;
    private boolean isGuardRole;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingSectionB6Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        // Obtener el rol del usuario
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", "");
        isGuardRole = "Guardia".equals(userRole);

        // Configurar navegación entre secciones
        ImageView flechaSeccionB6haciaB5 = binding.flechaSeccionB6haciaB5;
        flechaSeccionB6haciaB5.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB6_to_parkingSectionB5)
        );

        ImageView flechaSeccionB6haciaB7 = binding.flechaSeccionB6haciaB7;
        flechaSeccionB6haciaB7.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB6_to_parkingSectionB7)
        );

        // Inicializar botones y carros
        buttons = new Button[]{
                binding.buttonCar71, binding.buttonCar72, binding.buttonCar73,
                binding.buttonCar74, binding.buttonCar75, binding.buttonCar76,
                binding.buttonCar77, binding.buttonCar78, binding.buttonCar79, binding.buttonCar80
        };

        cars = new ImageView[]{
                binding.car71, binding.car72, binding.car73,
                binding.car74, binding.car75, binding.car76,
                binding.car77, binding.car78, binding.car79, binding.car80
        };

        OkHttpHelper httpHelper = new OkHttpHelper();

        // Obtener datos del servidor y actualizar la UI
        fetchParkingData(httpHelper);

        // Configurar listener para actualizar datos
        binding.refresh.setOnClickListener(view -> fetchParkingData(httpHelper));

        // Asignar listeners a los botones si el usuario no es guardia
        if (!isGuardRole) {
            View.OnClickListener buttonClickListener = v -> {
                for (int i = 0; i < buttons.length; i++) {
                    if (v.getId() == buttons[i].getId()) {
                        if (cars[i].getVisibility() == View.VISIBLE) {
                            Toast.makeText(requireContext(), "Este lugar está ocupado", Toast.LENGTH_SHORT).show();
                        } else {
                            int finalI = i;
                            checkActiveReservation(() -> {
                                String carInfo = "B" + (finalI + 71);
                                showPopup(carInfo);
                            });
                        }
                        break;
                    }
                }
            };

            for (Button button : buttons) {
                button.setOnClickListener(buttonClickListener);
            }
        }

        return root;
    }

    private void fetchParkingData(OkHttpHelper httpHelper) {
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
                            if (!isAdded() || binding == null) return; // Validación para evitar crashes

                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject espacioObject = jsonArray.getJSONObject(i);
                                    int idEspacio = espacioObject.getInt("id_espacio");
                                    String disponibilidad = espacioObject.getString("disponibilidad");

                                    if (idEspacio >= 71 && idEspacio <= 80) {
                                        int index = idEspacio - 71;
                                        if ("Disponible".equals(disponibilidad)) {
                                            availableSpaces++;
                                            cars[index].setVisibility(View.INVISIBLE);
                                            buttons[index].setEnabled(true);
                                            buttons[index].setAlpha(0.0f);
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

                            binding.espaciosLibres.setText(availableSpaces + " ESPACIOS LIBRES");
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
                requireActivity().runOnUiThread(onNoActiveReservation::run);
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

        int carNumber = Integer.parseInt(carInfo.replaceAll("[^\\d]", "")) - 20;
        titleText.setText("Lugar: B" + carNumber);

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
            String nombre = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getString("userName", "UsuarioDemo");
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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
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
                                int index = Integer.parseInt(espacio) - 71;
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
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error del servidor", Toast.LENGTH_SHORT).show());
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
