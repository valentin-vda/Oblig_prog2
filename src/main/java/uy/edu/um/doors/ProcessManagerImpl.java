package uy.edu.um.doors;


import uy.edu.um.entities.Eventos;
import uy.edu.um.entities.Proceso;
import uy.edu.um.entities.Usuario;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.MyStackImpl;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProcessManagerImpl implements ProcessManager{

    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA
    // private MyHashImpl<ACA VA ALGO> usuarios = new MyHashImpl<>();
    private MyQueueImpl<Proceso> procesosNuevos = new MyQueueImpl();
    private MyHeapImpl<Proceso> porcesosProsesando= new MyHeapImpl<>();
    private Proceso running = null;
    private MyStackImpl<Proceso> procesosFinalizados = new MyStackImpl<>();

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(usersCsvPath))){
            String linea;
            boolean primera = true;
            while ((linea = br.readLine())!= null){
                if (primera == true) {primera = false; continue;}
                String[] separacion = linea.split(";");

                int uid = Integer.parseInt(separacion[0]);
                String alias = separacion[1];
                Usuario.TipoUsuario tipo = Usuario.TipoUsuario.valueOf(separacion[2]);
                new Usuario(uid, alias, tipo);
                // hay que añadir el usuario al hash
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader br2 = new BufferedReader(new FileReader(processCsvPath))){
            String linea;
            boolean primera = true;
            while ((linea = br2.readLine())!= null){
                if (primera == true) {primera = false; continue; }
                String[] separacion = linea.split(";");

                int pid = Integer.parseInt(separacion[0]);
                int uid = Integer.parseInt(separacion[1]);
                String nombre = separacion[2];

                String sinllave = separacion[4].replace("{","").replace("}","");
                String[] eventos = sinllave.split("#");
                MyLinkedListImpl<Eventos> listaEventos = new MyLinkedListImpl<>();

                for (int i=0; i < eventos.length; i++){
                    String[] datos = eventos[i].split(":");
                    Eventos.TipoEvento tipo = Eventos.TipoEvento.valueOf(datos[0]);
                    String desc = datos[1];
                    listaEventos.add(new Eventos(tipo, desc));
                }
                procesosNuevos.enqueue(new Proceso(pid, uid, nombre, listaEventos));
                //Pasar el uid a Usuario
            }
        }
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @Override
    public void prepareProcesses() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void executeNextProcess() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessOk() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void finishProcessError() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void terminateProcess(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatus() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByUser(int uid) {
        System.out.println("IMPLEMENTAR");
    }

    @Override
    public void printStatusByProcess(int pid) {
        System.out.println("IMPLEMENTAR");
    }
}
