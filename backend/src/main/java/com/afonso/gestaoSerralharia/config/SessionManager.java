package com.afonso.gestaoSerralharia.config;

import com.afonso.gestaoSerralharia.models.Dono;

public class SessionManager {

    private static SessionManager instance;
    private Dono dono;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void loginDono(Dono d) { this.dono = d; }
    public void logout()          { this.dono = null; }

    public boolean isDono()        { return dono != null; }
    public boolean isAutenticado() { return dono != null; }

    public Dono getDono() { return dono; }

    public String getNome() {
        if (dono != null) return dono.getNome();
        return "";
    }

    public String getRole() {
        if (dono != null) return "Administrador";
        return "";
    }

    public String getImagemPerfil() {
        if (dono != null) return dono.getImagemPerfil();
        return null;
    }

    public String getPainelInicial() {
        if (dono != null && dono.getPainelInicial() != null && !dono.getPainelInicial().isBlank())
            return dono.getPainelInicial();
        return "dashboard";
    }
}
