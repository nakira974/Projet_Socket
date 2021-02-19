/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

class Socket_Serveur {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Groupe> groupes = new ArrayList<>();
    public static ArrayList<HashMap<Socket, User>> users = new ArrayList<>();

    private static ServerSocket _srvSocket;
    private static int maxConnection;
    private static int nb_socket;


    public static void createServer(java.net.ServerSocket socket) {
        _srvSocket = socket;
        maxConnection = 20;
        nb_socket = 0;
    }

    public static int getMaxConnection() {
        return maxConnection;
    }

    public static Socket acceptClient() throws IOException {

        nb_socket++;

        return _srvSocket.accept();


    }

    public static int getNbSocket() {
        return nb_socket;
    }

    public static void quit() {
        nb_socket--;
    }


    public static ServerSocket getServer() {
        return _srvSocket;
    }

    public static void ecrireSocket(String texte, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(texte);
            out.flush();
        }


    }

    public static void ecrireSocket(String texte, Socket client) throws IOException {


        PrintWriter out = new PrintWriter(client.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public static String lireSocket(Socket client) throws IOException {


        return new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

    }

}

class ClientServiceThread extends Thread {

    Socket client;

    boolean runState = true;
    boolean ServerOn = true;
    Logger log ;
    ClientServiceThread(Socket s) {

        this.client = s;
        Socket_Serveur.sockets.add(s);
         log = new Logger();


    }

    public void run() {


        System.out.println("Accepted Client Address - " + client.getInetAddress().getHostName());
        System.out.println("Client(s) : " + Socket_Serveur.users.size());
        String clientUsername = null;


        try {

            while (runState) {
                String clientCommand = Socket_Serveur.lireSocket(client);
                if (clientCommand != null) {
                    System.out.println("[BROADCAST] Client Says :" + clientCommand);
                    log.writeLog(client.getInetAddress().getHostName() + "(" + DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now()) + ") : " + clientCommand);
                }

                if (!ServerOn) {
                    System.out.print("Server has already stopped");
                    Socket_Serveur.ecrireSocket("Server has already stopped", client);
                    runState = false;
                }
                assert clientCommand != null;
                if (clientCommand.equalsIgnoreCase("quit")) {
                    runState = false;
                    Socket_Serveur.sockets.remove(client);
                    System.out.print("Stopping client thread for client :`\n ");
                    for (int i = 0; i < Socket_Serveur.users.size(); i++) {
                        //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
                        if (Socket_Serveur.users.get(i).containsKey(client)) {
                            Socket_Serveur.quit();
                            System.out.println("[BROADCAST] Client : " + Socket_Serveur.users.get(i).toString()
                                    + " Disconnected");
                        }
                    }

                    for (int i = 0; i < Socket_Serveur.users.size(); i++) {
                        //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
                        Socket_Serveur.users.get(i).remove(client);
                    }

                    System.out.println("Client(s) : " + Socket_Serveur.users.size());
                } else if (clientCommand.equalsIgnoreCase("/weather")) {
                    Socket_Serveur.users.stream() //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry -> entry.getKey().equals(client))
                                    .forEach(username -> {
                                        try {
                                            Socket_Serveur.ecrireSocket(username.getValue()._username + " : " + username.getValue().getWeather() + "°C", Socket_Serveur.sockets);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }));
                }
                else if (clientCommand.contains("/translate:")) {
                    String[] text = clientCommand.split(":");
                    String msg = text[1];
                    Socket_Serveur.users.stream() //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry -> entry.getKey().equals(client))
                                    .forEach(username -> {
                                        try {
                                            Socket_Serveur.ecrireSocket(username.getValue()._username + " : " + username.getValue().translateMessage(msg), Socket_Serveur.sockets);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }));
                }else if (clientCommand.contains("/@")) {
                    String[] text = clientCommand.split(":");
                    String destination = text[0].replace("/@", "");
                    String msg = text[1];
                    final String[] sender = {null};
                    Socket_Serveur.users //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry1 -> entry1.getKey().equals(client))
                                    .forEach(username -> {
                                        sender[0] = String.valueOf(username.getValue()._username);
                                    }));
                    Socket_Serveur.users //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry -> entry.getValue()._username.equals(destination))
                                    .forEach(username -> {
                                        try {
                                            Socket_Serveur.ecrireSocket(Arrays.toString(sender) + " : " + msg, username.getKey());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }));
                } else if (clientCommand.contains("/G")) {
                    String[] text = clientCommand.split(":");
                    String groupe = text[0].replace("/G", "");
                    String msg = text[1];
                    final String[] sender = {null};

                    for (Groupe current_grp : Socket_Serveur.groupes) {
                        if (current_grp._name.equals(groupe)) {
                            Socket_Serveur.users //stream out of arraylist
                                    .forEach(map -> map.entrySet().stream()
                                            .filter(entry1 -> entry1.getKey().equals(client))
                                            .forEach(username -> {
                                                sender[0] = String.valueOf(username.getValue()._username);
                                            }));
                            current_grp.groupeUsers //stream out of arraylist
                                    .forEach(map -> map.entrySet()
                                            .forEach(username -> {
                                                try {
                                                    Socket_Serveur.ecrireSocket("[" + current_grp._name + "] " + Arrays.toString(sender) + " : " + msg, username.getKey());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }));
                        }
                    }
                } else if (clientCommand.contains("/JG")) {
                    final User[] current = {null};
                    Groupe current_grp = null;
                    String[] text = clientCommand.split(":");
                    String groupe = text[1];
                    for (Groupe curr : Socket_Serveur.groupes) {
                        if (curr._name.equals(groupe)) {
                            current_grp = curr;
                        }
                    }
                    Socket_Serveur.users //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry1 -> entry1.getKey().equals(client))
                                    .forEach(username -> {
                                        current[0] = username.getValue();
                                    }));
                    HashMap<Socket, User> user = new HashMap<Socket, User>();
                    user.put(client, current[0]);
                    Socket_Serveur.groupes.get(Socket_Serveur.groupes.indexOf(current_grp)).groupeUsers.add(user);
                    //System.out.println("Group : " + current_grp._name + " has been created by : " + current[0]._username);
                } else if (clientCommand.contains("/CG")) {
                    final User[] current_usr = {null};
                    Groupe current_grp = null;
                    String[] text = clientCommand.split(":");
                    String groupe = text[1];
                    Socket_Serveur.users //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry1 -> entry1.getKey().equals(client))
                                    .forEach(username -> {
                                        current_usr[0] = username.getValue();
                                    }));
                    current_grp = new Groupe(groupe, current_usr[0], client);
                    Socket_Serveur.groupes.add(current_grp);
                    System.out.println("Group : " + current_grp._name + " has been created by : " + current_usr[0]._username);
                } else if (clientCommand.equalsIgnoreCase("END")) {
                    runState = false;
                    System.out.print("Stopping server...");
                    ServerOn = false;
                    Socket_Serveur.ecrireSocket("END", Socket_Serveur.sockets);
                    Socket_Serveur.sockets.removeAll(Socket_Serveur.sockets);
                    log.closeLog();
                    System.exit(0);
                } else {


                    //LAMBDA POUR AFFICHER LES MESSAGES GENERAUX
                    Socket_Serveur.users.stream() //stream out of arraylist
                            .forEach(map -> map.entrySet().stream()
                                    .filter(entry -> entry.getKey().equals(client))
                                    .forEach(username -> {
                                        try {
                                            Socket_Serveur.ecrireSocket(username.getValue()._username + " : " + clientCommand, Socket_Serveur.sockets);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }));
                    //FIN LAMBDA


                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
    private String _username;

    public SocketPerso(java.net.Socket socket) {


        this.socket = socket;


    }

    public SocketPerso(java.net.Socket socket, String p_userName) {


        this._username = p_userName;
        this.socket = socket;


    }

    public String getUserName() {

        return this._username;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void ecrireSocket(String texte) throws IOException {


        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public void envoyerPseudo(String pseudo) throws IOException {


        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(pseudo);
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

            try {
                do {
                    String val = client.lireSocket();
                    if (val.contains("END")) {
                        System.exit(0);
                    } else {
                        commandes.ecrireEcran(val);
                    }
                } while (client.getSocket().isConnected());

            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }

    }

    static class Thread_Client_Send extends Thread {
        //String destination;
        SocketPerso socket;
        IOCommandes commandes;
        String msg;
        String username;

        public Thread_Client_Send(SocketPerso s) throws IOException {
            socket = s;
            commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {
            try {
                socket.envoyerPseudo(socket._username);
                commandes.ecrireEcran("Connexion au serveur: " + socket._username);
                //destination = commandes.lireEcran();

                do {
                    msg = commandes.lireEcran();
                    if (!msg.equals("quit")) {
                        //socket.ecrireSocket("{dest:[" + destination + "], msg:[" + msg + "]}");
                        socket.ecrireSocket(msg);
                    }


                } while (!msg.equals("quit"));

                socket.ecrireSocket(msg);
                System.exit(0);

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }
    }


}





