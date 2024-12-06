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
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        username.setText(sharedPreferences.getString("userName", ""));
        email.setText(sharedPreferences.getString("userEmail", ""));

        // Listener para el texto de configuración (settings_text)
        settingsText.setOnClickListener(v -> {
            // Inflar el diseño del nuevo popup_layout2
            LayoutInflater popupInflater = LayoutInflater.from(requireContext());
            View popupView = popupInflater.inflate(R.layout.popup_layout2, null);

            // Crear el PopupWindow
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            // Mostrar el popup centrado en la pantalla
            popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

            // Configurar el botón Cerrar en el popup
            Button closeButton = popupView.findViewById(R.id.close_button_settings);
            closeButton.setOnClickListener(v1 -> popupWindow.dismiss());
        });

        // Listener para cerrar sesión
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // This clears all data in "UserSession".
                editor.apply(); // Apply changes asynchronously.

                Intent intent = new Intent(requireContext(), Login.class);
                startActivity(intent);
                requireActivity().finish();

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
