package com.example.parkucc.ui.notifications;

import android.content.Context;
import android.content.Intent;
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

import com.example.parkucc.Login;
import com.example.parkucc.R;
import com.example.parkucc.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView logoutText = binding.logoutText;
        TextView username = binding.username;
        TextView email = binding.email;
        TextView settingsText = binding.settingsText; // Referenciar el texto de configuración
        TextView usersText = root.findViewById(R.id.users_text); // Referencia al texto "Usuarios"
        ImageView usersIcon = root.findViewById(R.id.users_icon); // Referencia al icono "Usuarios"

        // SharedPreferences para cargar datos del usuario
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        username.setText(sharedPreferences.getString("userName", ""));
        email.setText(sharedPreferences.getString("userEmail", ""));

        // Verificar si el usuario tiene rol "Admin"
        String userRole = sharedPreferences.getString("userRole", "");
        if (!"Admin".equals(userRole)) {
            usersText.setVisibility(View.GONE); // Ocultar opción "Usuarios" si no es Admin
            usersIcon.setVisibility(View.GONE); // Ocultar el icono "Usuarios" si no es Admin
        } else {
            usersText.setOnClickListener(v -> {
                // Navegar al nuevo fragmento "UsersFragment"
                Navigation.findNavController(v).navigate(R.id.action_notifications_to_users);
            });
        }

        // Listener para el texto de configuración (settings_text)
        settingsText.setOnClickListener(v -> {
            // Inflar el diseño del popup
            LayoutInflater popupInflater = LayoutInflater.from(requireContext());
            View popupView = popupInflater.inflate(R.layout.popup_layout2, null);

            // Crear el PopupWindow
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            // Mostrar el popup centrado
            popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

            // Obtener referencias a los TextViews dentro del popup
            TextView userNamePopup = popupView.findViewById(R.id.user_name_popup);
            TextView emailPopup = popupView.findViewById(R.id.email_popup);
            TextView rolePopup = popupView.findViewById(R.id.role_popup);

            // Obtener los datos del usuario desde SharedPreferences
            String userName = sharedPreferences.getString("userName", "Usuario Desconocido");
            String userEmail = sharedPreferences.getString("userEmail", "No se encontró correo");
            String userRolePopup = sharedPreferences.getString("userRole", "Sin rol asignado");

            // Asignar los datos a los TextViews
            userNamePopup.setText(userName);
            emailPopup.setText("Correo: " + userEmail);
            rolePopup.setText("Rol: " + userRolePopup);

            // Configurar el botón "Cerrar" para cerrar el popup
            Button closeButton = popupView.findViewById(R.id.close_button_settings);
            closeButton.setOnClickListener(v1 -> popupWindow.dismiss());
        });

        // Listener para cerrar sesión
        logoutText.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // This clears all data in "UserSession".
            editor.apply(); // Apply changes asynchronously.

            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
