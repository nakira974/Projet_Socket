package Projet_Socket.Client;
/*
 --- creators : nakira974 && Weefle  ----
 */

import Projet_Socket.Utils.Console;
import org.jetbrains.annotations.Contract;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;


/**
 * Client des services du serveur tcp
 */
public final class ClientTcp {

    private final Socket socket;
    private final String _username;

    /**
     * Créer un socket client tcp
     * @param socket socket du client
     * @param p_userName nom de l'utilisateur
     */
    public ClientTcp(Socket socket, String p_userName) {


        this._username = p_userName;
        this.socket = socket;


    }


    /**
     * @return instance du client
     */
    @Contract(pure = true)
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * Envoie un message sur le serveur
     * @param texte message à envoyer sur le serveur
     * @throws IOException
     */
    public void writeSocket(String texte) throws IOException {

        var out = new PrintWriter(Objects.requireNonNull(this.socket).getOutputStream());
        out.println(texte);
        out.flush();


    }

    /**
     * Envoie un fichier sur le serveur
     * @param filename nom du fichier à envoyer sur le serveur
     * @throws IOException
     */
    public void writeFileSocket(String filename) throws IOException {

        var myFile = new File(filename);
        var bFile = new byte[(int) myFile.length()];
        var fileInputStream = new FileInputStream(myFile);
        fileInputStream.read(bFile);
        fileInputStream.close();
        //var writer = new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8);
        var writer = new PrintWriter(Objects.requireNonNull(this.socket).getOutputStream());

        var jsonObject = new JSONObject();
        jsonObject.put("name", myFile.getName());
        jsonObject.put("size", Files.size(myFile.toPath()) / 1024);
        jsonObject.put("content", bFile);
        //writer.write(jsonObject.toString());
        writer.println(jsonObject);
        writer.flush();

    }

    /**
     * Envoi le pseudo de l'utilisateur au serveur
     * @param pseudo nom de l'utilisateur
     * @throws IOException
     */
    public void sendUserName(String pseudo) throws IOException {


        var out = new PrintWriter(this.socket.getOutputStream());
        out.println(pseudo);
        out.flush();


    }

    /**
     * Renvoie les messages en provenance du serveur
     * @return message provenant du serveur
     * @throws IOException
     */
    public String readSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    public String readSocketFileStream() throws IOException {


        var reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));

        var line = reader.readLine();
        if (line.contains("{") && line.contains("}")) {
            var jsonObject = new JSONObject(line);
            return jsonObject.toString();
        } else {
            return line;
        }

    }

    /**
     * Thread de réception du flux en provenance du serveur
     */
    public static final class Thread_Client_Receive extends Thread {
        private final ClientTcp client;
        public Console commandes;
        private ISocketListener iSocketListener;

        public Thread_Client_Receive(ClientTcp client, ISocketListener iSocketListener) throws IOException {
            this.iSocketListener = iSocketListener;
            this.client = client;
            commandes = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {

            try {
                do {
                    //String val = client.readSocketFileStream();
                    var val = client.readSocket();
                    if (val.contains("END")) {

                        System.exit(0);
                    } else {
                        commandes.writeLine(val);
                    }
                } while (client.getSocket().isConnected());

            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }

    }

    /**
     * Thread d'envoi à destination du serveur
     */
    public static final class Thread_Client_Send extends Thread {
        //String destination;
        private final ClientTcp socket;
        public Console console;
        private ISocketListener iSocketListener;

        public Thread_Client_Send(ClientTcp s, ISocketListener iSocketListener) throws IOException {
            this.iSocketListener = iSocketListener;
            socket = s;
            console = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {
            String msg;
            try {
                socket.sendUserName(socket._username);
                console.writeLine("Connexion au serveur: " + socket._username);
                //destination = console.readKey();

                do {
                    msg = console.readKey();
                    if (!msg.equals("quit")) {
                        //socket.writeSocket("{dest:[" + destination + "], msg:[" + msg + "]}");
                        if (msg.contains("/file:")) {
                            String[] test = msg.split(":");
                            //TODO terminer le check des fichiers
                            //checkFilesFromServer();
                            socket.writeFileSocket(test[1]);
                        }
                        socket.writeSocket(msg);
                    }


                } while (!msg.equals("quit"));

                socket.writeSocket(msg);
                System.exit(0);

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }
    }


}





