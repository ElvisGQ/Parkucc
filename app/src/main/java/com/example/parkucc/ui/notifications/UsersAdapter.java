package com.example.parkucc.ui.notifications;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkucc.OkHttpHelper;
import com.example.parkucc.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<User> userList;

    public UsersAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvNombre.setText("Nombre: " + user.getNombre());
        holder.tvCorreo.setText("Correo: " + user.getCorreo());
        holder.tvRol.setText("Rol: " + getRoleName(user.getIdRol()));
        holder.tvIdUsuario.setText("ID Usuario: " + user.getIdUsuario());

        // Verificar si el rol es Admin y deshabilitar botones si es el caso
        if (user.getIdRol().equals("4")) { // Admin
            holder.btnChangeToGuard.setEnabled(false);
            holder.btnChangeToStudent.setEnabled(false);
            holder.btnChangeToGuard.setText("No disponible");
            holder.btnChangeToStudent.setText("No disponible");
        } else {
            // Configurar botones
            holder.btnChangeToGuard.setOnClickListener(v ->
                    changeUserRole(user.getIdUsuario(), user.getIdRol(), "2", v)); // Cambiar a Guardia
            holder.btnChangeToStudent.setOnClickListener(v ->
                    changeUserRole(user.getIdUsuario(), user.getIdRol(), "1", v)); // Cambiar a Estudiante
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private String getRoleName(String idRol) {
        switch (idRol) {
            case "1":
                return "Estudiante";
            case "2":
                return "Guardia";
            case "4":
                return "Admin";
            default:
                return "Desconocido";
        }
    }

    private void changeUserRole(String idUsuario, String currentRole, String newRole, View view) {
        // Verificar si el usuario tiene rol de Admin
        if (currentRole.equals("Admin")) {
            Toast.makeText(view.getContext(), "No se puede cambiar el rol de un Admin.", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpHelper httpHelper = new OkHttpHelper();
        JSONObject postData = new JSONObject();

        try {
            postData.put("id_usuario", Integer.parseInt(idUsuario));
            postData.put("rol", Integer.parseInt(newRole));
        } catch (Exception e) {
            e.printStackTrace();
        }

        httpHelper.post("http://157.230.232.203/setRole", postData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                ((Activity) view.getContext()).runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Error al conectarse al servidor", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) view.getContext()).runOnUiThread(() -> {
                        Toast.makeText(view.getContext(), "Rol actualizado correctamente", Toast.LENGTH_SHORT).show();

                        // Actualizar el rol en la lista de usuarios local
                        for (User user : userList) {
                            if (user.getIdUsuario().equals(idUsuario)) {
                                user.setIdRol(newRole); // Actualizar el rol del usuario en la lista
                                break;
                            }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        notifyDataSetChanged();
                    });
                } else {
                    ((Activity) view.getContext()).runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Error al actualizar el rol", Toast.LENGTH_SHORT).show()
                    );
                }
                response.close();
            }
        });
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo, tvRol, tvIdUsuario;
        Button btnChangeToGuard, btnChangeToStudent;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvRol = itemView.findViewById(R.id.tvRol);
            tvIdUsuario = itemView.findViewById(R.id.tvIdUsuario);
            btnChangeToGuard = itemView.findViewById(R.id.btnAsignarGuardia);
            btnChangeToStudent = itemView.findViewById(R.id.btnAsignarEstudiante);
        }
    }
}
