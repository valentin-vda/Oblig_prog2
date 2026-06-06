package uy.edu.um.exceptions;
public class ProcessAlreadyRunningException extends Exception {
    public ProcessAlreadyRunningException(String processName) {
        super("Proceso ya en ejecución: " + processName);
    }
}