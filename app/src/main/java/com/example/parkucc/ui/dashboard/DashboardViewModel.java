package com.example.parkucc.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkucc.OkHttpHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Reservacion>> reservaciones = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel() {
        cargarReservaciones();
    }

    public LiveData<List<Reservacion>> getReservaciones() {
        return reservaciones;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private void cargarReservaciones() {
        OkHttpHelper httpHelper = new OkHttpHelper();
        httpHelper.get("http://157.230.232.203/getReservaciones", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                errorMessage.postValue("Error al obtener reservaciones");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray reservacionesArray = jsonResponse.getJSONArray("reservaciones");

                        List<Reservacion> listaReservaciones = new ArrayList<>();
                        for (int i = 0; i < reservacionesArray.length(); i++) {
                            JSONObject reservacion = reservacionesArray.getJSONObject(i);
                            String id = reservacion.getString("id");
                            String espacio = reservacion.getString("espacio");
                            String nombreUsuario = reservacion.getString("nombreUsuario");
                            String fechaInicio = reservacion.getString("fechaInicio");
                            String fechaFin = reservacion.getString("fechaFin");
                            listaReservaciones.add(new Reservacion(id, espacio, nombreUsuario, fechaInicio, fechaFin));
                        }

                        reservaciones.postValue(listaReservaciones);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMessage.postValue("Error al procesar los datos");
                    }
                } else {
                    response.close();
                    errorMessage.postValue("Error en la respuesta del servidor");
                }
            }
        });
    }
}
