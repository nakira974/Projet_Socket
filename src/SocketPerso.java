import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class Socket_Serveur {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static HashMap<User, Socket> users= new HashMap<User, Socket>();

    private final ServerSocket _srvSocket;
    private int maxConnection;

    public Socket_Serveur(java.net.ServerSocket socket) {

        this._srvSocket = socket;

    }

    public Socket acceptClient() throws IOException {

        return _srvSocket.accept();


    }

    public ServerSocket getServer() {
        return this._srvSocket;
    }

    public void ecrireSocket(String texte, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(texte);
            out.flush();
        }


    }

    public String lireSocket(Socket client) throws IOException {

        return new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

    }
}

class ClientServiceThread extends Thread {
    public static Socket client;
    boolean runState = true;
    boolean ServerOn= true;

    ClientServiceThread(Socket s) {

        client = s;
        Socket_Serveur.sockets.add(s);

    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        System.out.println(
                "Accepted Client Address - " + client.getInetAddress().getHostName());
        try {
            in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(
                    new OutputStreamWriter(client.getOutputStream()));

            while(runState) {
                String clientCommand = in.readLine();
                if(clientCommand!=null) {
                    System.out.println("Client Says :" + clientCommand);
                    Logger.writeLog(client.getInetAddress().getHostName() + "(" + DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now()) + ") : " + clientCommand);
                }

                if(!ServerOn) {
                    System.out.print("Server has already stopped");
                    out.println("Server has already stopped");
                    out.flush();
                    runState = false;
                }
                assert clientCommand != null;
                if(clientCommand.equalsIgnoreCase("quit")) {
                    runState = false;
                    Socket_Serveur.sockets.remove(client);
                    System.out.print("Stopping client thread for client : ");
                } else if(clientCommand.equalsIgnoreCase("END")) {
                    runState = false;
                    System.out.print("Stopping server...");
                    ServerOn = false;
                    for (Socket cl : Socket_Serveur.sockets){
                        PrintWriter output = new PrintWriter(cl.getOutputStream());
                        output.println("END");
                        output.flush();
                    }
                    Socket_Serveur.sockets.removeAll(Socket_Serveur.sockets);
                    Logger.closeLog();
                    System.exit(0);
                }
                else {
                    out.println("Server Says : " + clientCommand);
                    out.flush();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert in != null;
                in.close();
                assert out != null;
                out.close();
                client.close();
                System.out.println("...Stopped");
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}


public class SocketPerso {

    private final Socket socket;

    public SocketPerso(java.net.Socket socket){


        this.socket = socket;


    }

    public Socket getSocket(){
        return this.socket;
    }

    public void ecrireSocket(String texte) throws IOException {


        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    static class Thread_Client_Receive extends Thread {
        SocketPerso client;
        IOCommandes commandes;

        public Thread_Client_Receive(SocketPerso client) throws IOException {
            this.client = client;
            commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {

            try{
                do{
                    String val = client.lireSocket();
                    if(val.contains("END")) {
                        System.exit(0);
                    }else{
                        commandes.ecrireEcran(val);
                    }
                }while(client.getSocket().isConnected());

            }catch(Exception ex){
                ex.printStackTrace();
            }


        }

    }

    static class Thread_Client_Send extends Thread {
        String destination;
        SocketPerso socket;
        IOCommandes commandes;
        String msg;

        public Thread_Client_Send(SocketPerso s) throws IOException {
            socket = s;
            commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {
            try{
            /*System.out.println("Saisir un destinataire : ");
                    msg = commandes.lireEcran();
                    destination = msg;
                    socket.ecrireSocket(msg);*/


                do {
                    msg = commandes.lireEcran();
                    if (!msg.equals("quit")) {
                        socket.ecrireSocket(msg);
                    }
                } while (!msg.equals("quit"));

                socket.ecrireSocket(msg);
                System.exit(0);

            }catch(IOException ex){
                ex.printStackTrace();
            }


        }
    }


}