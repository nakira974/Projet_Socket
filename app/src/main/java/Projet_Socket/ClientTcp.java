package Projet_Socket;
/*
 --- creators : nakira974 && Weefle  ----
 */

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class ClientTcp {

    private final Socket socket;
    private String _username;

    public ClientTcp(java.net.Socket socket) {


        this.socket = socket;


    }

    public ClientTcp(java.net.Socket socket, String p_userName) {


        this._username = p_userName;
        this.socket = socket;


    }


    public String getUserName() {

        return this._username;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void writeSocket(String texte) throws IOException {

        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public void writeFileSocket(String filename) throws IOException {

        File myFile = new File(filename);
        byte[] bFile = new byte[(int) myFile.length()];
        FileInputStream fileInputStream = new FileInputStream(myFile);
        fileInputStream.read(bFile);
        fileInputStream.close();
        OutputStreamWriter writer = new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", filename);
        jsonObject.put("size", Files.size(myFile.toPath()) / 1024);
        jsonObject.put("content", bFile);
        writer.write(jsonObject.toString());
        writer.flush();

    }

    public void sendUserName(String pseudo) throws IOException {


        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(pseudo);
        out.flush();


    }

    public String readSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    public String readSocketFileStream() throws IOException {


        BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));

        String line = reader.readLine();
        if (line.contains("{") && line.contains("}")) {
            JSONObject jsonObject = new JSONObject(line);
            return jsonObject.toString();
        } else {
            return line;
        }

    }

    static class Thread_Client_Receive extends Thread {
        private final ClientTcp client;
        private final Console commandes;

        public Thread_Client_Receive(ClientTcp client) throws IOException {
            this.client = client;
            commandes = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {

            try {
                do {
                    //String val = client.readSocketFileStream();
                    String val = client.readSocket();
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

    static class Thread_Client_Send extends Thread {
        //String destination;
        private final ClientTcp socket;
        private final Console console;
        private String email;
        private String username;

        public Thread_Client_Send(ClientTcp s, String p_email) throws IOException {
            socket = s;
            email = p_email;
            console = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        }

        public void run() {
            String msg;
            try {
                socket.sendUserName(socket._username + "," + email);
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




