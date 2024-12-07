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
import com.example.parkucc.databinding.FragmentParkingSectionB7Binding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ParkingSectionB7 extends Fragment {

    private FragmentParkingSectionB7Binding binding;
    private int availableSpaces = 0;
    private Button[] buttons;
    private ImageView[] cars;
    private boolean isGuardRole;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingSectionB7Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        // Obtener el rol del usuario
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", "");
        isGuardRole = "Guardia".equals(userRole);

        // Navegación entre secciones
        ImageView flechaSeccionB7haciaB6 = binding.flechaSeccionB7haciaC2;
        flechaSeccionB7haciaB6.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB7_to_parkingSectionC2)
        );

        ImageView flechaSeccionB7haciaB8 = binding.flechaSeccionB7haciaB8;
        flechaSeccionB7haciaB8.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB7_to_parkingSectionB8)
        );

        // Inicializar botones y carros para los espacios 81 al 90 excepto 82
        buttons = new Button[]{
                binding.buttonCar81, binding.buttonCar83, binding.buttonCar84,
                binding.buttonCar85, binding.buttonCar86, binding.buttonCar87,
                binding.buttonCar88, binding.buttonCar89, binding.buttonCar90
        };

        cars = new ImageView[]{
                binding.car81, binding.car83, binding.car84,
                binding.car85, binding.car86, binding.car87, binding.car88,
                binding.car89, binding.car90
        };

        OkHttpHelper httpHelper = new OkHttpHelper();

        // Obtener datos del servidor y actualizar la interfaz
        fetchParkingData(httpHelper);

        // Listener para los botones (si no es guardia)
        if (!isGuardRole) {
            View.OnClickListener buttonClickListener = v -> {
                for (int i = 0; i < buttons.length; i++) {
                    if (v.getId() == buttons[i].getId()) {
                        if (cars[i].getVisibility() == View.VISIBLE) {
                            Toast.makeText(requireContext(), "Este lugar está ocupado", Toast.LENGTH_SHORT).show();
                        } else {
                            String carInfo = "B" + getCarNumber(i);
                            showPopup(carInfo);
                        }
                        break;
                    }
                }
            };

            for (Button button : buttons) {
                button.setOnClickListener(buttonClickListener);
            }
        }

        // Listener para recargar los datos
        binding.refresh.setOnClickListener(view -> fetchParkingData(httpHelper));

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
                            if (binding != null) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        JSONObject espacioObject = jsonArray.getJSONObject(i);
                                        int idEspacio = espacioObject.getInt("id_espacio");
                                        String disponibilidad = espacioObject.getString("disponibilidad");

                                        int index = getCarIndex(idEspacio);
                                        if (index != -1) {
                                            if ("Disponible".equals(disponibilidad)) {
                                                availableSpaces++;
                                                cars[index].setVisibility(View.INVISIBLE);
                                                if (isGuardRole) {
                                                    buttons[index].setEnabled(false);
                                                    buttons[index].setAlpha(0.0f); // Botón completamente invisible
                                                } else {
                                                    buttons[index].setEnabled(true);
                                                    buttons[index].setAlpha(0.0f); // Botón completamente invisible para otros roles
                                                }
                                            } else if ("Ocupado".equals(disponibilidad)) {
                                                cars[index].setVisibility(View.VISIBLE);
                                                buttons[index].setEnabled(false);
                                                buttons[index].setAlpha(0.0f); // Botón completamente invisible
                                            } else if ("Reservado".equals(disponibilidad)) {
                                                cars[index].setVisibility(View.INVISIBLE);
                                                buttons[index].setEnabled(false);
                                                buttons[index].setAlpha(0.5f); // Botón semitransparente
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                binding.espaciosLibres.setText(availableSpaces + " ESPACIOS LIBRES");
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

    private void showPopup(String carInfo) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        TextView titleText = popupView.findViewById(R.id.nombreCajonPopup);
        Button closeButton = popupView.findViewById(R.id.close_button);
        Button reserveButton = popupView.findViewById(R.id.reserve_button);

        // Extraer el número de carInfo, restarle 20 y actualizar el texto
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
                                int index = getCarIndex(Integer.parseInt(espacio));
                                if (index != -1) {
                                    buttons[index].setAlpha(0.5f);
                                    buttons[index].setEnabled(false);
                                }
                            } else if (status == 409) {
                                Toast.makeText(requireContext(), "Espacio ocupado", Toast.LENGTH_SHORT).show();
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

    private int getCarIndex(int idEspacio) {
        // Excluir espacios fuera del rango o el 82
        if (idEspacio < 81 || idEspacio > 90 || idEspacio == 82) return -1;

        if (idEspacio == 81) return 0;
        else if (idEspacio >= 83 && idEspacio <= 90) return idEspacio - 82;
        else return -1;
    }

    private int getCarNumber(int index) {
        if (index == 0) return 81;
        else if (index >= 1 && index <= 8) return index + 82;
        else return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
