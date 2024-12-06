package com.example.parkucc.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.databinding.FragmentDashboardBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.parkucc.databinding.FragmentDashboardBinding; // Update with your actual package name
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ReservacionAdapter adapter;
    private List<Reservacion> reservacionList = new ArrayList<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        fetchReservaciones();

        observeErrors();

        return root;
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservacionAdapter(reservacionList);
        binding.recyclerView.setAdapter(adapter);
    }

    private void fetchReservaciones() {
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
                        // Since the response is a JSONArray, parse it as such
                        JSONArray reservacionesArray = new JSONArray(responseBody);

                        List<Reservacion> listaReservaciones = new ArrayList<>();
                        for (int i = 0; i < reservacionesArray.length(); i++) {
                            JSONObject reservacion = reservacionesArray.getJSONObject(i);
                            String id = reservacion.getString("id");
                            String espacio = reservacion.getString("espacio");
                            String nombreUsuario = reservacion.getString("nombre_usuario");
                            String fechaInicio = reservacion.getString("fecha");
                            String fechaFin = reservacion.getString("fecha_fin");
                            listaReservaciones.add(new Reservacion(id, espacio, nombreUsuario, fechaInicio, fechaFin));
                        }

                        requireActivity().runOnUiThread(() -> {
                            reservacionList.clear();
                            reservacionList.addAll(listaReservaciones);
                            adapter.notifyDataSetChanged();
                        });
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

    private void observeErrors() {
        errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message, e.g., a Toast
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
