package obligatorio2026.src.main.resources;

public class Usuario {
    private int uid;
    private String alias;
    private TipoUsuario tipo;
    public enum TipoUsuario{
        GENERIC, ADMIN
    }
}
