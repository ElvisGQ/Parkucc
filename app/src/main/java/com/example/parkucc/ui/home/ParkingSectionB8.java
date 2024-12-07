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
import com.example.parkucc.databinding.FragmentParkingSectionB8Binding;

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

public class ParkingSectionB8 extends Fragment {

    private FragmentParkingSectionB8Binding binding;
    private int availableSpaces = 0;
    private Button[] buttons;
    private ImageView[] cars;
    private boolean isGuardRole;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingSectionB8Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        // Obtener el rol del usuario
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", "");
        isGuardRole = "Guardia".equals(userRole);

        // Navegación entre secciones
        ImageView flechaSeccionB8haciaB7 = binding.flechaSeccionB8haciaB7;
        flechaSeccionB8haciaB7.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB8_to_parkingSectionB7)
        );
        ImageView flechaSeccionB8haciaC1 = binding.flechaSeccionB8haciaC1;
        flechaSeccionB8haciaC1.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionB8_to_parkingSectionC1)
        );

        // Inicializar botones y carros para los espacios 91 al 100
        buttons = new Button[]{
                binding.buttonCar91, binding.buttonCar92, binding.buttonCar93,
                binding.buttonCar94, binding.buttonCar95, binding.buttonCar96,
                binding.buttonCar97, binding.buttonCar98, binding.buttonCar99,
                binding.buttonCar100
        };

        cars = new ImageView[]{
                binding.car91, binding.car92, binding.car93,
                binding.car94, binding.car95, binding.car96,
                binding.car97, binding.car98, binding.car99,
                binding.car100
        };

        OkHttpHelper httpHelper = new OkHttpHelper();

        // Obtener datos del servidor y actualizar UI
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
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject espacioObject = jsonArray.getJSONObject(i);
                                    int idEspacio = espacioObject.getInt("id_espacio");
                                    String disponibilidad = espacioObject.getString("disponibilidad");

                                    if (idEspacio >= 91 && idEspacio <= 100) {
                                        int index = idEspacio - 91;
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
                                int index = Integer.parseInt(espacio) - 91;
                                buttons[index].setAlpha(0.5f);
                                buttons[index].setEnabled(false);
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
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error del servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getCurrentDateTimePlus30Minutes() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30); // Agregar 30 minutos
        return sdf.format(calendar.getTime());
    }

    private int getCarNumber(int index) {
        return index + 91;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
