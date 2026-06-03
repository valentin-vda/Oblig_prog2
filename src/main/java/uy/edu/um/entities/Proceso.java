package uy.edu.um.entities;


import uy.edu.um.tad.list.MyLinkedListImpl;

public class Proceso implements Comparable<Proceso> {
    private int pid;
    private String nombre;
    private Usuario propietario;
    private int prioridad;
    private TipoEstado tipoEstado;
    private TipoFinalizacion tipoFinalizacion;
    private Usuario finalizadoPor;
    private MyLinkedListImpl<Eventos> eventos;

    public Proceso(int pid, Usuario u, String nombre, MyLinkedListImpl<Eventos> eventos) {
        this.pid = pid;
        this.propietario = u;
        this.nombre = nombre;
        this.eventos = eventos;

    }

    @Override
    public int compareTo(Proceso o) {
        if (this.pid > o.pid) {
            return 1;
        }
        if (this.pid < o.pid) {
            return -1;
        }
        return 0;

    }

    public enum TipoEstado {
        NEW, PENDING, RUNNING, FINISHED
    }

    public enum TipoFinalizacion {
        OK, ERROR, TERMINATED
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public void setPropietario(Usuario propietario) {
        this.propietario = propietario;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public TipoEstado getTipoEstado() {
        return tipoEstado;
    }

    public void setTipoEstado(TipoEstado tipoEstado) {
        this.tipoEstado = tipoEstado;
    }

    public TipoFinalizacion getTipoFinalizacion() {
        return tipoFinalizacion;
    }

    public void setTipoFinalizacion(TipoFinalizacion tipoFinalizacion) {
        this.tipoFinalizacion = tipoFinalizacion;
    }

    public Usuario getFinalizadoPor() {
        return finalizadoPor;
    }

    public void setFinalizadoPor(Usuario finalizadoPor) {
        this.finalizadoPor = finalizadoPor;
    }

    public MyLinkedListImpl<Eventos> getEventos() {
        return eventos;
    }

    public void setEventos(MyLinkedListImpl<Eventos> eventos) {
        this.eventos = eventos;
    }
}