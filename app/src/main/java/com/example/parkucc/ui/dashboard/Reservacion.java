package com.example.parkucc.ui.dashboard;

public class Reservacion {
    private String id;
    private String espacio;
    private String nombreUsuario;
    private String fechaInicio;
    private String fechaFin;

    public Reservacion(String id, String espacio, String nombreUsuario, String fechaInicio, String fechaFin) {
        this.id = id;
        this.espacio = espacio;
        this.nombreUsuario = nombreUsuario;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getId() { return id; }
    public String getEspacio() { return espacio; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
}
