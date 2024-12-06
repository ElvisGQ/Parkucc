package com.example.parkucc.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkucc.R;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerReservaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ReservacionAdapter adapter = new ReservacionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        dashboardViewModel.getReservaciones().observe(getViewLifecycleOwner(), reservaciones -> {
            adapter.updateReservaciones(reservaciones);
        });

        dashboardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        });

        return root;
    }
}
