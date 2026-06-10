package uy.edu.um.entities;

import java.util.Objects;

public class Usuario {
    private int uid;
    private String alias;
    private TipoUsuario tipo;
    public enum TipoUsuario{
        GENERIC, ADMIN
    }
    public Usuario(int uid, String alias, TipoUsuario tipo){
        this.uid = uid;
        this.alias = alias;
        this.tipo = tipo;

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usuario usuario)) return false;
        return getUid() == usuario.getUid();
    }

    @Override
    public int hashCode() {
        return uid;
    }
}
