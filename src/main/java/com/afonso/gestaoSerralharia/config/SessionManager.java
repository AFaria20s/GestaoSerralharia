package com.afonso.gestaoSerralharia.config;

import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.models.Funcionario;

public class SessionManager {

    private static SessionManager instance;

    private Dono dono;
    private Funcionario funcionario;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void loginDono(Dono d)             { this.dono = d; this.funcionario = null; }
    public void loginFuncionario(Funcionario f) { this.funcionario = f; this.dono = null; }
    public void logout()                      { this.dono = null; this.funcionario = null; }

    public boolean isDono()        { return dono != null; }
    public boolean isFuncionario() { return funcionario != null; }
    public boolean isAutenticado() { return dono != null || funcionario != null; }

    public Dono getDono()                { return dono; }
    public Funcionario getFuncionario()  { return funcionario; }

    public String getNome() {
        if (dono != null)        return dono.getNome();
        if (funcionario != null) return funcionario.getNome();
        return "";
    }

    public String getRole() {
        if (dono != null)        return "Administrador";
        if (funcionario != null) return "Funcionário";
        return "";
    }
}
