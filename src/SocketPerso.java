import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

class Socket_Serveur {

    public static ArrayList<Socket> sockets = new ArrayList<>();

    private ServerSocket _srvSocket;
    private int maxConnection;

    public Socket_Serveur(java.net.ServerSocket socket) {

        this._srvSocket=socket;

    }

    public Socket acceptClient() throws IOException {

        return _srvSocket.accept();



    }

    public ServerSocket getServer(){
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

class ClientRecieveThread extends Thread {
    Socket client;
    boolean runState = true;
    boolean ServerOn= true;
    public ClientRecieveThread(Socket s){
        client =s;
    }

    public void run(){

    }

}

class ClientServiceThread extends Thread {
    Socket client;
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
                    Logger.writeLog(client.getInetAddress().getHostName() + "(" + new Date() + ") : " + clientCommand);
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

    private Socket socket;
    public boolean _state;

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

    public void chercherDestinataire(String pseudo) throws IOException {
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println("/SEARCH ");
        out.flush();
        out.println(pseudo);
        out.flush();
    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    class Thread_ClientReceive extends Thread {
        SocketPerso client;
        public Thread_ClientReceive(SocketPerso s){
            client = s;
        }
        public void run() {

            try{
                do{
                    client.lireSocket();
                }while(_state);

            }catch(Exception ex){
                ex.printStackTrace();
            }


        }
    }

    class ThreadServiceCLient extends Thread {
        String destination;
        SocketPerso socket;
        IOCommandes commandes;
        String msg;

        public ThreadServiceCLient(SocketPerso s){
            socket =s;
        }

        public void run() {
            try{
            System.out.println("Saisir un destinataire : ");
                    msg = commandes.lireEcran();
                    destination = msg;
                    socket.ecrireSocket(msg);
                        commandes.ecrireEcran(socket.lireSocket());

                do {
                    msg = commandes.lireEcran();
                    if (!msg.equals("quit")) {
                        socket.ecrireSocket(msg);
                        commandes.ecrireEcran(socket.lireSocket());
                    }
                } while (!msg.equals("quit"));

            }catch(Exception ex){
                ex.printStackTrace();
            }


        }
    }


}