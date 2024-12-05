package com.example.parkucc.ui.home;

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
import com.example.parkucc.databinding.FragmentParkingSectionC1Binding;

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

public class ParkingSectionC1 extends Fragment {

    private FragmentParkingSectionC1Binding binding;
    private int availableSpaces = 0;
    private Button[] buttons;
    private ImageView[] cars;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingSectionC1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        availableSpaces = 0;

        // Navegación entre secciones
        ImageView flechaSeccionC1haciaB8 = binding.flechaSeccionC1haciaB8;
        flechaSeccionC1haciaB8.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionC1_to_parkingSectionB8)
        );

        ImageView flechaSeccionC1haciaC2 = binding.flechaSeccionC1haciaC2;
        flechaSeccionC1haciaC2.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_parkingSectionC1_to_parkingSectionC2)
        );

        // Inicializar botones y carros para los espacios 101 al 110
        buttons = new Button[]{
                binding.buttonCar101, binding.buttonCar102, binding.buttonCar103,
                binding.buttonCar104, binding.buttonCar105, binding.buttonCar106,
                binding.buttonCar107, binding.buttonCar108, binding.buttonCar109,
                binding.buttonCar110
        };

        cars = new ImageView[]{
                binding.car101, binding.car102, binding.car103, binding.car104,
                binding.car105, binding.car106, binding.car107, binding.car108,
                binding.car109, binding.car110
        };

        OkHttpHelper httpHelper = new OkHttpHelper();

        // Obtener datos del servidor y actualizar UI
        fetchParkingData(httpHelper);

        // Listener para recargar los datos
        binding.refresh.setOnClickListener(view -> fetchParkingData(httpHelper));

        // Listener para los botones
        View.OnClickListener buttonClickListener = v -> {
            for (int i = 0; i < buttons.length; i++) {
                if (v.getId() == buttons[i].getId()) {
                    if (cars[i].getVisibility() == View.VISIBLE) {
                        Toast.makeText(requireContext(), "Este lugar está ocupado", Toast.LENGTH_SHORT).show();
                    } else {
                        String carInfo = "C" + (i + 101);
                        showPopup(carInfo);
                    }
                    break;
                }
            }
        };

        for (Button button : buttons) {
            button.setOnClickListener(buttonClickListener);
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
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    JSONObject espacioObject = jsonArray.getJSONObject(i);
                                    int idEspacio = espacioObject.getInt("id_espacio");
                                    String disponibilidad = espacioObject.getString("disponibilidad");

                                    if (idEspacio >= 101 && idEspacio <= 110) {
                                        int index = idEspacio - 101;
                                        if ("Disponible".equals(disponibilidad)) {
                                            availableSpaces++;
                                            cars[index].setVisibility(View.INVISIBLE);
                                            buttons[index].setEnabled(true);
                                            buttons[index].setAlpha(0.0f); // Botón invisible
                                        } else if ("Ocupado".equals(disponibilidad)) {
                                            cars[index].setVisibility(View.VISIBLE);
                                            buttons[index].setEnabled(false);
                                            buttons[index].setAlpha(0.0f); // Botón invisible
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
            String espacio = carInfo.replaceAll("[^\\d]", ""); // Quitar caracteres no numéricos
            String nombre = "UsuarioDemo"; // Aquí se usa el nombre del usuario
            String fechaFin = getCurrentDateTimePlus30Minutes(); // Fecha +30 minutos

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
                                int index = Integer.parseInt(espacio) - 101;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
