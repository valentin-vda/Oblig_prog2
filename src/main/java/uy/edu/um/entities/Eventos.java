package uy.edu.um.entities;

public class Eventos {
    private TipoEvento tipoEvento;
    private String instrucciones;
    public enum TipoEvento{
        CPU, RAM, DISK
    }
    public Eventos(TipoEvento tipo, String instrucciones){
        this.tipoEvento = tipo;
        this.instrucciones = instrucciones;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }
}

