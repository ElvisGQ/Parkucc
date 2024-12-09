package com.example.parkucc.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FragmentUsers extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> usersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersAdapter = new UsersAdapter(usersList);
        recyclerView.setAdapter(usersAdapter);

        // Configurar el botón de regreso
        View backArrow = view.findViewById(R.id.flechaDeregreso);
        backArrow.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_users_to_notifications));

        // Cargar usuarios
        fetchUsers();

        return view;
    }

    private void fetchUsers() {
        OkHttpHelper httpHelper = new OkHttpHelper();

        httpHelper.get("http://157.230.232.203/usuarios", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Error al conectar con la API", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al conectarse al servidor", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", "Datos recibidos: " + responseBody);

                    try {
                        JSONArray usuariosArray = new JSONArray(responseBody);
                        List<User> fetchedUsers = new ArrayList<>();

                        for (int i = 0; i < usuariosArray.length(); i++) {
                            JSONObject usuario = usuariosArray.getJSONObject(i);

                            // Leer los datos desde el JSON
                            String idUsuario = usuario.optString("id_usuario", "N/A");
                            String idRol = usuario.optString("id_rol", "N/A");
                            String nombre = usuario.optString("nombre", "N/A");
                            String correo = usuario.optString("correo", "N/A");

                            // Crear un objeto User y agregarlo a la lista
                            fetchedUsers.add(new User(idUsuario, idRol, nombre, correo));
                        }

                        // Actualizar el RecyclerView en el hilo principal
                        requireActivity().runOnUiThread(() -> {
                            usersList.clear();
                            usersList.addAll(fetchedUsers);
                            usersAdapter.notifyDataSetChanged();
                        });

                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error al procesar el JSON", e);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    response.close();
                    Log.e("API_ERROR", "Error en la respuesta del servidor: " + response.code());
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
