import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Socket_Serveur {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Groupe> groupes = new ArrayList<>();
    //public ArrayList<HashMap<User, Socket>> users = new ArrayList<>();
    public static HashMap<String, Socket> users= new HashMap<String, Socket>();

    private ServerSocket _srvSocket;
    private int maxConnection;

    public Socket_Serveur(java.net.ServerSocket socket) {

        this._srvSocket = socket;

    }


    public static SocketPerso createUser(ArrayList<String> args){
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3307/" +
                    "serveur_db?user=ServerMaster&password=Master2004$");

            System.out.println("Refreshing client_list.");

            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO users (`pseudo`, `password`) VALUES('"+ args.get(0)+"','"+args.get(1)+"');");

            if(!rs.wasNull()){


            }
            else{
                return null;}

        } catch (SQLException ex1){
            ex1.printStackTrace();
        }
        catch(Exception ex2){
            ex2.printStackTrace();
        }
        return socket_client;
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

    public void ecrireSocket(String texte, Socket client) throws IOException {


            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(texte);
            out.flush();


    }

    public String lireSocket(Socket client) throws IOException {


        return new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

    }

    public static void ajouter_groupe(Socket client) throws IOException {

        String res = null ;
        ecrireSocket("Saisir le nom du Groupe : ", client);
        res= lireSocket(client);
        Set set = users.entrySet();

        // Get an iterator
        Iterator iterator = set.iterator();

        // Display elements
        while(iterator.hasNext()) {
            if (users.get(iterator) == client) {
                Map.Entry me = (Map.Entry) iterator.next();

                Groupe currentGroup = new Groupe(res, (String) me.getKey(), (Socket) me.getValue());
                groupes.add(currentGroup);
                System.out.println("Groupe @"+currentGroup._name+" a été créé");
            }
        }
    }
}

class ClientServiceThread extends Thread {

    Socket client;
    Socket_Serveur server;

    boolean runState = true;
    boolean ServerOn= true;

    ClientServiceThread(Socket s, Socket_Serveur server) {

        this.client = s;
        this.server = server;
        this.server.sockets.add(s);

    }

    public void run() {
        System.out.println("Accepted Client Address - " + client.getInetAddress().getHostName());
        try {

            while(runState) {
                String clientCommand = this.server.lireSocket(client);
                if(clientCommand!=null) {
                    System.out.println("Client Says :" + clientCommand);
                    Logger.writeLog(client.getInetAddress().getHostName() + "(" + DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now()) + ") : " + clientCommand);
                }

                if(!ServerOn) {
                    System.out.print("Server has already stopped");
                    this.server.ecrireSocket("Server has already stopped", client);
                    runState = false;
                }
                assert clientCommand != null;
                if(clientCommand.equalsIgnoreCase("quit")) {
                    runState = false;
                    this.server.sockets.remove(client);
                    System.out.print("Stopping client thread for client : ");
                } else if (clientCommand.equalsIgnoreCase("/create_group")) {
                    Socket_Serveur.ajouter_groupe(client);
                }
                else if(clientCommand.equalsIgnoreCase("END")) {
                    runState = false;
                    System.out.print("Stopping server...");
                    ServerOn = false;
                    this.server.ecrireSocket("END", this.server.sockets);
                    this.server.sockets.removeAll(this.server.sockets);
                    Logger.closeLog();
                    System.exit(0);
                }
                else {
                    this.server.ecrireSocket("Server Says : " + clientCommand, client);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("...Stopped");
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
                commandes.ecrireEcran("Saisir un destinataire : ");
                    destination = commandes.lireEcran();

                do {
                    msg = commandes.lireEcran();
                    if (!msg.equals("quit")) {
                        //socket.ecrireSocket("{dest:[" + destination + "], msg:[" + msg + "]}");
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





