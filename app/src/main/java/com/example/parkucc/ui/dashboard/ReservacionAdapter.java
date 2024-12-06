package com.example.parkucc.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkucc.R;

import java.util.ArrayList;
import java.util.List;

public class ReservacionAdapter extends RecyclerView.Adapter<ReservacionAdapter.ReservacionViewHolder> {

    private List<Reservacion> reservaciones;

    public ReservacionAdapter(List<Reservacion> reservaciones) {
        this.reservaciones = reservaciones;
    }

    public void updateReservaciones(List<Reservacion> newReservaciones) {
        this.reservaciones = newReservaciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservacion, parent, false);
        return new ReservacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservacionViewHolder holder, int position) {
        Reservacion reservacion = reservaciones.get(position);
        holder.tvEspacio.setText("Espacio: " + reservacion.getEspacio());
        holder.tvNombreUsuario.setText("Usuario: " + reservacion.getNombreUsuario());
        holder.tvFechaInicio.setText("Inicio: " + reservacion.getFechaInicio());
        holder.tvFechaFin.setText("Fin: " + reservacion.getFechaFin());
    }

    @Override
    public int getItemCount() {
        return reservaciones.size();
    }

    static class ReservacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvEspacio, tvNombreUsuario, tvFechaInicio, tvFechaFin;

        ReservacionViewHolder(View itemView) {
            super(itemView);
            tvEspacio = itemView.findViewById(R.id.tvEspacio);
            tvNombreUsuario = itemView.findViewById(R.id.tvNombreUsuario);
            tvFechaInicio = itemView.findViewById(R.id.tvFechaInicio);
            tvFechaFin = itemView.findViewById(R.id.tvFechaFin);
        }
    }
}
