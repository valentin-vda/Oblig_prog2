package uy.edu.um.doors;


import uy.edu.um.entities.Eventos;
import uy.edu.um.entities.Proceso;
import uy.edu.um.entities.Usuario;
import uy.edu.um.exceptions.ProcesoSinEventos;
import uy.edu.um.exceptions.ProcessAlreadyRunningException;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.MyStackImpl;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ProcessManagerImpl implements ProcessManager{

    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA
    private MyHashImpl<Integer, Usuario> usuarios = new MyHashImpl<>();
    private MyQueueImpl<Proceso> procesosNuevos = new MyQueueImpl();
    private MyHeapImpl<Proceso> procesosProsesando= new MyHeapImpl<>();
    private Proceso running = null;
    private MyStackImpl<Proceso> procesosFinalizados = new MyStackImpl<>();


    private void escribirLog(String cont){
        String ruta = "uy/edu/um/log.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta, true))){pw.println(cont);}
        catch (IOException e) { throw new RuntimeException(e);}
    }


    //Hay que preguntar por casos particulares sizes 0 y demas
    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(usersCsvPath))){
            String linea;
            boolean primera = true;
            while ((linea = br.readLine())!= null){
                if (primera) {primera = false; continue;}
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
                if (primera) {primera = false; continue; }
                String[] separacion = linea.split(";");

                int pid = Integer.parseInt(separacion[0]);
                int uid = Integer.parseInt(separacion[1]);
                String nombre = separacion[2];

                String sinllave = separacion[3].replace("{","").replace("}","");
                String[] eventos = sinllave.split("#");
                MyLinkedListImpl<Eventos> listaEventos = new MyLinkedListImpl<>();

                for (int i=0; i < eventos.length; i++){
                    String[] datos = eventos[i].split(":");
                    Eventos.TipoEvento tipo = Eventos.TipoEvento.valueOf(datos[0]);
                    String desc = datos[1];
                    listaEventos.add(new Eventos(tipo, desc));
                }
                Usuario u = usuarios.get(uid);
                if (u == null) {
                    System.out.println("Advertencia: no existe el usuario " + uid + " para el proceso " + pid);
                    continue;
                }
                procesosNuevos.enqueue(new Proceso(pid, u, nombre, listaEventos));
            }
        }
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @Override
    public void prepareProcesses() throws EmptyQueueException, ProcesoSinEventos{
        if (!procesosNuevos.isEmpty()) {
            for (int i = 0; i < procesosNuevos.size(); i++) {
                int cantCPU = 0;
                int cantRAM = 0;
                int cantDISK = 0;
                if (procesosNuevos.isEmpty()) throw new EmptyQueueException();
                Proceso p = procesosNuevos.dequeue();
                MyLinkedListImpl<Eventos> e = p.getEventos();

                if (e.size() == 0) throw new ProcesoSinEventos();

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


                int prio = ((8*cantCPU + 2*cantRAM + 2*cantDISK)/e.size()) + w*e.size();
                p.setPrioridad(prio);
                procesosProsesando.insert(p);
                p.setTipoEstado(Proceso.TipoEstado.PENDING);

                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                String log = "["+fechaHora+"]: NEW PENDING PROCESS: PID="+p.getPid()+" | "+p.getNombre()+" | USER:"+p.getPropietario().getTipo()+" UID:"+p.getPropietario().getUid()+" | P="+prio;
                escribirLog(log);
            }
        }else {System.out.println("NO HAY PROCESOS PARA PREPARAR");}
    }

    @Override
    public void executeNextProcess() throws ProcessAlreadyRunningException, EmptyQueueException{
        if (running != null) {
            throw new ProcessAlreadyRunningException(running.getNombre());
        }
        if (procesosProsesando.isEmpty()){
            throw new EmptyQueueException();
        }
        running = procesosProsesando.remove();
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
