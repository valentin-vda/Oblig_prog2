package uy.edu.um.exceptions;

public class NingunProcesoEnEjecucion extends RuntimeException {
    public NingunProcesoEnEjecucion(String message) {
        super(message);
    }
}
