package uy.edu.um.exceptions;

public class ProcesoSinEventos extends Exception {
    public ProcesoSinEventos() {
        super("El Proceso no tiene Eventos");
    }
}
