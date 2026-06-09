package uy.edu.um.exceptions;

public class UsuarioNoEncontrado extends RuntimeException {
    public UsuarioNoEncontrado(String message) {
        super(message);
    }
}
