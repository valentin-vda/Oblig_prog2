package uy.edu.um.doors;


import uy.edu.um.entities.Eventos;
import uy.edu.um.entities.Proceso;
import uy.edu.um.entities.Usuario;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.MyStackImpl;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProcessManagerImpl implements ProcessManager{

    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA
    private MyHashImpl<Integer, Usuario> usuarios = new MyHashImpl<>();
    private MyQueueImpl<Proceso> procesosNuevos = new MyQueueImpl();
    private MyHeapImpl<Proceso> procesosProsesando= new MyHeapImpl<>();
    private Proceso running = null;
    private MyStackImpl<Proceso> procesosFinalizados = new MyStackImpl<>();

    //Hay que preguntar por casos particulares sizes 0 y demas
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
                Usuario u = new Usuario(uid, alias, tipo);
                usuarios.put(uid, u);
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
                Usuario u = usuarios.get(uid);
                procesosNuevos.enqueue(new Proceso(pid, u, nombre, listaEventos));
            }
        }
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @Override
    public void prepareProcesses() throws EmptyQueueException {
        if (!procesosNuevos.isEmpty()) {
            for (int i = 0; i < procesosNuevos.size(); i++) {
                int cantCPU = 0;
                int cantRAM = 0;
                int cantDISK = 0;
                if (procesosNuevos.isEmpty()) throw new EmptyQueueException();
                Proceso p = procesosNuevos.dequeue();
                MyLinkedListImpl<Eventos> e = p.getEventos();
                for (int j=0; j < e.size(); j++){
                    Eventos even = e.get(j);
                    switch (even.getTipoEvento()){
                        case Eventos.TipoEvento.CPU:{cantCPU+=1; break;}
                        case Eventos.TipoEvento.RAM:{cantRAM+=1; break;}
                        case Eventos.TipoEvento.DISC:{cantDISK+=1;break;}

                    }
                }
                int w = 1;
                if (p.getPropietario().getTipo() == Usuario.TipoUsuario.ADMIN) {w = 32;}
                else {w = 16;}

                //Hay que ver si es int o otro tipo de dato
                int prio = ((8*cantCPU + 2*cantRAM + 2*cantDISK)/e.size()) + w*e.size();
                p.setPrioridad(prio);
                procesosProsesando.insert(p);
            }
        }else {System.out.println("NO HAY PROCESOS PARA PREPARAR");}
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
