package com.example.parkucc.ui.notifications;

public class User {
    private String idUsuario;
    private String idRol;
    private String nombre;
    private String correo;

    public User(String idUsuario, String idRol, String nombre, String correo) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.nombre = nombre;
        this.correo = correo;
    }

    // Getters
    public String getIdUsuario() {
        return idUsuario;
    }

    public String getIdRol() {
        return idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    // Setter para el rol
    public void setIdRol(String idRol) {
        this.idRol = idRol;
    }
}
