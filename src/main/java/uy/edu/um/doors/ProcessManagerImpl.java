package uy.edu.um.doors;


import uy.edu.um.entities.Eventos;
import uy.edu.um.entities.Proceso;
import uy.edu.um.entities.Usuario;
import uy.edu.um.exceptions.NingunProcesoEnEjecucion;
import uy.edu.um.exceptions.ProcesoSinEventos;
import uy.edu.um.exceptions.ProcessAlreadyRunningException;
import uy.edu.um.exceptions.UsuarioNoEncontrado;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.queue.EmptyQueueException;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.stack.EmptyStackException;
import uy.edu.um.tad.stack.MyStackImpl;


import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ProcessManagerImpl implements ProcessManager{

    //EL DISEÑO DE LA ESTRUCTURA DE ALMACENAMIENTO DEBE IMPLEMENTARSE EN ESTA CLASE EN RELACIÓN CON LAS ENTIDADES QUE DEFINA
    private MyHashImpl<Integer, Usuario> usuarios = new MyHashImpl<>();
    private MyQueueImpl<Proceso> procesosNuevos = new MyQueueImpl();
    private MyHeapImpl<Proceso> procesosProcesando= new MyHeapImpl<>(false);
    private Proceso running = null;
    private MyStackImpl<Proceso> procesosFinalizados = new MyStackImpl<>();


    private void escribirLog(String cont){
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String ruta = System.getProperty("user.dir") + File.separator + "log_"+fecha+".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta, true))){
            pw.println(cont);
        }
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
                    Eventos.TipoEvento tipo = Eventos.TipoEvento.valueOf(datos[0].trim());
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

                if (e.isEmpty()) throw new ProcesoSinEventos();

                for (int j=0; j < e.size(); j++){
                    Eventos even = e.get(j);
                    switch (even.getTipoEvento()){
                        case Eventos.TipoEvento.CPU:{cantCPU+=1; break;}
                        case Eventos.TipoEvento.RAM:{cantRAM+=1; break;}
                        case Eventos.TipoEvento.DISK:{cantDISK+=1;break;}

                    }
                }
                int w = 1;
                if (p.getPropietario().getTipo() == Usuario.TipoUsuario.ADMIN) {w = 32;}
                else {w = 16;}


                int prio = ((8*cantCPU + 2*cantRAM + 2*cantDISK)/e.size()) + w*e.size();
                p.setPrioridad(prio);
                procesosProcesando.insert(p);
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
        if (procesosProcesando.isEmpty()){
            throw new EmptyQueueException();
        }
        running = procesosProcesando.remove();
    }

    @Override
    public void finishProcessOk() throws EmptyStackException {
        if (running == null) {
            throw new NingunProcesoEnEjecucion("No hay ningun proceso en ejecución");
        }
        Proceso terminadoOk = running;
        terminadoOk.setTipoFinalizacion(Proceso.TipoFinalizacion.OK);
        running = null;
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //Hay que imprimir todos los procesos finalizados cuando se llena el stack
        if (procesosFinalizados.size() == MAX_FINISHED_PROCESS_ON_RAM) {
           printProcesosFinalizados();
        }
        //Pushea el nuevo proceso terminado
        procesosFinalizados.push(terminadoOk);
        String log = "["+fechaHora+"]: ENDING PROCESS: PID="+terminadoOk.getPid()+" |STATE: OK";
        escribirLog(log);
        System.out.println("Proceso Terminado OK");
    }
    //MUY SIMILAR AL ANTERIOR
    @Override
    public void finishProcessError() throws EmptyStackException {
        if (running == null) {
            throw new NingunProcesoEnEjecucion("");
        }
        Proceso terminadoE = running;
        terminadoE.setTipoFinalizacion(Proceso.TipoFinalizacion.ERROR);
        running = null;
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //Hay que imprimir todos los procesos finalizados cuando se llena el stack
        if (procesosFinalizados.size() == MAX_FINISHED_PROCESS_ON_RAM) {
            printProcesosFinalizados();
        }
        //Pushea el nuevo proceso terminado
        procesosFinalizados.push(terminadoE);
        String log = "["+fechaHora+"]: ENDING PROCESS: PID="+terminadoE.getPid()+" |STATE: ERROR";
        escribirLog(log);
        System.out.println("Proceso Terminado ERROR");
    }

    @Override
    public void terminateProcess(int uid) throws EmptyStackException {
        if (running == null) {
            throw new NingunProcesoEnEjecucion("");
        }
        Proceso terminated = running;
        terminated.setTipoFinalizacion(Proceso.TipoFinalizacion.TERMINATED);
        //Chequea que el usuario terminador exista antes de buscarlo en el hash
        if (!usuarios.contains(uid)){
            throw new UsuarioNoEncontrado("El usuario no está registrado");
        }
        Usuario terminador = usuarios.get(uid);
        terminated.setFinalizadoPor(terminador);
        running = null;
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //Hay que imprimir todos los procesos finalizados cuando se llena el stack
        if (procesosFinalizados.size() == MAX_FINISHED_PROCESS_ON_RAM) {
            printProcesosFinalizados();
        }
        //Pushea el nuevo proceso terminado
        procesosFinalizados.push(terminated);
        String log = "["+fechaHora+"]: ENDING PROCESS: PID="+terminated.getPid()+" |STATE: TERMINATED by USER:"+terminador.getAlias()+" UID:"+terminador.getUid();
        escribirLog(log);
        System.out.println("Proceso Terminado");
    }

    public void printProcesosFinalizados() throws EmptyStackException {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        escribirLog(fechaHora+": Finished process stack overflow");
        int total = procesosFinalizados.size();
        //Va imprimiendo en el log los procesos
        for (int i=0; i < total ; i++) {
            Proceso aux = procesosFinalizados.pop();
            String logaux = "PID="+aux.getPid()+" | "+aux.getNombre()+ " | " + "STATE:"+aux.getTipoFinalizacion()+
                    "USER:"+aux.getPropietario().getTipo()+" UID:"+aux.getPropietario().getUid()+"";
            escribirLog(logaux);

        }
    }

    @Override
    public void printStatus() {
        System.out.println("PROCESS STATUS");
        System.out.println("EXECUTING:");
        if (running != null) {
            System.out.println("\tPID=" + running.getPid() + " | " + running.getNombre() + " | USER:" + running.getPropietario().getAlias() + " UID:" + running.getPropietario().getUid() + " | P=" + running.getPrioridad());
        }
        else{
            System.out.println("\tNo hay procesos en ejecucion");
        }
        System.out.println("PENDING:");
        MyStackImpl<Proceso> aux = new MyStackImpl<>();
        while (!procesosProcesando.isEmpty()) {
            Proceso p = procesosProcesando.remove();
            System.out.println("\tPID=" + p.getPid() + " | " + p.getNombre() + " | USER:" + p.getPropietario().getAlias() + " UID:" + p.getPropietario().getUid()  + " | P=" + p.getPrioridad());
            aux.push(p);
        }
        while (!aux.isEmpty()) {
            try{
                procesosProcesando.insert(aux.pop());
            } catch (EmptyStackException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("FINISHED:");
        if(procesosFinalizados.isEmpty()){
            System.out.println("\tNo hay procesos finalizados");
        }
        else {
            for (int i = 0; i < procesosFinalizados.size(); i++) {
                Proceso pf = procesosFinalizados.get(i);
                System.out.println("\tPID=" + pf.getPid() + " " + pf.getNombre() + " | STATE:" + pf.getTipoFinalizacion() + " | USER:" + pf.getPropietario().getAlias() + " UID:" + pf.getPropietario().getUid());
            }
        }

    }
    //METODO AUXILIAR
    public void imprimirEventos(Proceso p){
        MyLinkedListImpl<Eventos> eventos = p.getEventos();
        for (int i = 0; i < eventos.size(); i++) {
            Eventos e = eventos.get(i);
            System.out.println("\t\tEVENT: " + e.getTipoEvento() + " | Instruccions " + e.getInstrucciones());
        }
    }
    @Override
    public void printStatusVerbose() {
        System.out.println("PROCESS STATUS");
        System.out.println("EXECUTING:");
        if (running != null) {
            System.out.println("\tPID=" + running.getPid() + " | " + running.getNombre() + " | USER:" + running.getPropietario().getAlias() + " UID:" + running.getPropietario().getUid() + " | P=" + running.getPrioridad());
            imprimirEventos(running);
        }
        else{
            System.out.println("\tNo hay procesos en ejecucion");
        }
        System.out.println("PENDING:");
        MyStackImpl<Proceso> aux = new MyStackImpl<>();
        while (!procesosProcesando.isEmpty()) {
            Proceso p = procesosProcesando.remove();
            System.out.println("\tPID=" + p.getPid() + " | " + p.getNombre() + " | USER:" + p.getPropietario().getAlias() + " UID:" + p.getPropietario().getUid()  + " | P=" + p.getPrioridad());
            imprimirEventos(p);
            aux.push(p);}
        while (!aux.isEmpty()) {
            try{
                procesosProcesando.insert(aux.pop());
            } catch (EmptyStackException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("FINISHED:");
        if(procesosFinalizados.isEmpty()){
            System.out.println("\tNo hay procesos finalizados");
        }
        else {
            for (int i = 0; i < procesosFinalizados.size(); i++) {
                Proceso pf = procesosFinalizados.get(i);
                System.out.println("\tPID=" + pf.getPid() + " " + pf.getNombre() + " | STATE:" + pf.getTipoFinalizacion() + " | USER:" + pf.getPropietario().getAlias() + " UID:" + pf.getPropietario().getUid());
                imprimirEventos(pf);
            }
        }
    }

    @Override
    public void printStatusByUser(int uid) {
        if (!usuarios.contains(uid)){
            System.out.println("ERROR: Usuario no encontrado");
            return;
        }
        System.out.println("PROCESS STATUS");
        System.out.println("EXECUTING:");
        if (running != null) {
            if (running.getPropietario().getUid() == uid) {
                System.out.println("\tPID=" + running.getPid() + " | " + running.getNombre() + " | USER:" + running.getPropietario().getAlias() + " UID:" + running.getPropietario().getUid() + " | P=" + running.getPrioridad());
            }
            else{
                System.out.println("\tNo hay procesos en ejecucion del usuario indicado");
            }
        }
        else{
            System.out.println("\tNo hay procesos en ejecucion");
        }
        System.out.println("PENDING:");
        MyStackImpl<Proceso> aux = new MyStackImpl<>();
        while (!procesosProcesando.isEmpty()) {
            Proceso p = procesosProcesando.remove();
            if (p.getPropietario().getUid() == uid) {
                System.out.println("\tPID=" + p.getPid() + " | " + p.getNombre() + " | USER:" + p.getPropietario().getAlias() + " UID:" + p.getPropietario().getUid() + " | P=" + p.getPrioridad());
            }
            aux.push(p);
        }
        while (!aux.isEmpty()) {
            try{
                procesosProcesando.insert(aux.pop());
            } catch (EmptyStackException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("FINISHED:");
        if(procesosFinalizados.isEmpty()){
            System.out.println("\tNo hay procesos finalizados");
        }
        else {
            for (int i = 0; i < procesosFinalizados.size(); i++) {
                Proceso pf = procesosFinalizados.get(i);
                if(pf.getPropietario().getUid() == uid) {
                    System.out.println("\tPID=" + pf.getPid() + " " + pf.getNombre() + " | STATE:" + pf.getTipoFinalizacion() + " | USER:" + pf.getPropietario().getAlias() + " UID:" + pf.getPropietario().getUid());
                }
            }
        }
    }

    @Override
    public void printStatusByProcess(int pid) {
        boolean encontrado = false;
        System.out.println("PROCESS STATUS");
        System.out.println("EXECUTING:");
        if (running != null) {
            if (running.getPid() == pid) {
                encontrado = true;
                System.out.println("\tPID=" + running.getPid() + " | " + running.getNombre() + " | USER:" + running.getPropietario().getAlias() + " UID:" + running.getPropietario().getUid() + " | P=" + running.getPrioridad());
            imprimirEventos(running);
            }
            else{
                System.out.println("\tNo hay procesos en ejecucion del proceso indicado");
            }
        }
        else{
            System.out.println("\tNo hay procesos en ejecucion");
        }
        System.out.println("PENDING:");
        MyStackImpl<Proceso> aux = new MyStackImpl<>();
        while (!procesosProcesando.isEmpty()) {
            Proceso p = procesosProcesando.remove();
            if (p.getPid() == pid) {
                encontrado = true;
                System.out.println("\tPID=" + p.getPid() + " | " + p.getNombre() + " | USER:" + p.getPropietario().getAlias() + " UID:" + p.getPropietario().getUid() + " | P=" + p.getPrioridad());
            imprimirEventos(p);
            }
            aux.push(p);
        }
        while (!aux.isEmpty()) {
            try{
                procesosProcesando.insert(aux.pop());
            } catch (EmptyStackException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("FINISHED:");
        if(procesosFinalizados.isEmpty()){
            System.out.println("\tNo hay procesos finalizados");
        }
        else {
            for (int i = 0; i < procesosFinalizados.size(); i++) {
                Proceso pf = procesosFinalizados.get(i);
                if(pf.getPid() == pid) {
                    encontrado = true;
                    System.out.println("\tPID=" + pf.getPid() + " " + pf.getNombre() + " | STATE:" + pf.getTipoFinalizacion() + " | USER:" + pf.getPropietario().getAlias() + " UID:" + pf.getPropietario().getUid());
                    imprimirEventos(pf);
                }
            }
        }
        if (!encontrado) {
            System.out.println("ERROR: Proceso no encontrado" );
        }
    }
}
