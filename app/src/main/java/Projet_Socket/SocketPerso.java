package Projet_Socket;
/*
 --- creators : nakira974 && Weefle  ----
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Socket_Serveur {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Groupe> groupes = new ArrayList<>();
    public static ArrayList<HashMap<Socket, User>> users = new ArrayList<>();
    public static ServerSocket _srvSocket;
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

    public static void writeSocket(String content, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(content);
            out.flush();
        }


    }

    public static void writeSocket(String content, Socket client) throws IOException {


        PrintWriter out = new PrintWriter(client.getOutputStream());
        out.println(content);
        out.flush();


    }

    public static String readClientStream(Socket client) throws IOException {

        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

            String line = client.isConnected() ? reader.readLine() : "";
            if (line.contains("{") && line.contains("}")) {
                JSONObject jsonObject = new JSONObject(line);
                String name = (String) jsonObject.get("name");
                int size = (int) jsonObject.get("size");
                JSONArray content = jsonObject.getJSONArray("content");
                byte[] data = new byte[content.length()];
                for (int i = 0; i < content.length(); i++) {
                    data[i] = ((Integer) content.get(i)).byteValue();
                }
                FileOutputStream out = new FileOutputStream(name + "_");
                out.write(data);
                out.close();

                result = jsonObject.toString();
            } else {
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
        return result;
    }

    public static ArrayList<Groupe> getUserGroups(int userId) {
        int groupId = 0;
        ArrayList<Groupe> results = new ArrayList<>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] SETTING GROUPS FOR USER ID N° " + userId + "...");


            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT groupId FROM group_users WHERE userId='" + userId + "'");

            while (rs.next()) {
                groupId = rs.getInt("groupId");
            }

            rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes WHERE groupe_uuid =" + groupId);
            while (rs.next()) {
                Groupe currentGroup = new Groupe();
                currentGroup.Id = rs.getInt("groupe_uuid");
                currentGroup.administratorId = rs.getInt("administrator");
                currentGroup.name = rs.getString("nom");
                results.add(currentGroup);
            }
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }
        return results;
    }

    public static ArrayList<Groupe> getGroups() {
        int groupId = 0;
        ArrayList<Groupe> results = new ArrayList<>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] FETCHING GROUPS FROM DATABASE...");


            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes");

            while (rs.next()) {
                Groupe currentGroup = new Groupe();
                currentGroup.Id = rs.getInt("groupe_uuid");
                currentGroup.administratorId = rs.getInt("administrator");
                currentGroup.name = rs.getString("nom");
                currentGroup.groupeUsers = new ArrayList<>();
                results.add(currentGroup);
            }

            Socket_Serveur.groupes.addAll(results);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }
        return results;
    }

    public static int getGroupId(String groupeName) {
        int groupId = 0;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] GET GROUP ID REQUEST REQUEST for " + groupeName + "...");


            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT groupe_uuid FROM groupes WHERE nom='" + groupeName + "'");

            while (rs.next()) {
                groupId = rs.getInt("groupe_uuid");
            }

        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

        return groupId;
    }

    public static int getUserId(String userMail) {
        int userId = 0;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] GET USER ID for " + userMail + "...");


            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT user_uuid FROM users WHERE email='" + userMail + "'");

            while (rs.next()) {
                userId = rs.getInt("user_uuid");
            }

            System.out.println("[SQL] USER " + userMail + " is : " + userId);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

        return userId;
    }

    public void sendFileBroadcast(String path, Socket client) throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", path);
        writer.write(jsonObject.toString());
        writer.flush();

    }

    public void sendFileBroadcast(String path, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("test", path);
            writer.write(jsonObject.toString());
            writer.flush();
        }

    }

}


class ClientServiceThread extends Thread {

    final private Socket client;
    final private Logger log;
    private boolean runState = true;
    private boolean ServerOn = true;

    ClientServiceThread(Socket s) {

        this.client = s;
        Socket_Serveur.sockets.add(s);
        log = new Logger();


    }

    private static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private void setUsersDown() {
        int rs;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] Exiting users...");


            Statement stmt = conn.createStatement();


            stmt.executeUpdate("UPDATE users SET isConnected= 0 where isConnected =1");

            System.out.println("[SQL] User has been disconnected from : " + conn);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

    }

    private void setUserDown() throws NoSuchAlgorithmException {

        byte[] sha256;
        final String[] currentUser = {null};
        String result = "";


        Socket_Serveur.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> {
                            currentUser[0] = username.getValue()._username;

                        }));
        sha256 = getSHA(currentUser[0]);
        currentUser[0] = toHexString(sha256);
        result = currentUser[0];
        ResultSet rs;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] Exiting users...");


            Statement stmt = conn.createStatement();


            stmt.executeUpdate("UPDATE users SET isConnected=0 WHERE pseudo='" + result + "'");

            System.out.println("[SQL] User has been disconnected on : " + conn);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

    }

    private void printBroadcast(String clientCommand) {
        final int[] userId = {0};
        Socket_Serveur.users.forEach(socketUserHashMap -> {
            if (socketUserHashMap.containsKey(client))
                userId[0] = socketUserHashMap.get(client).Id;
        });
        if (clientCommand != null) {
            System.out.println("[BROADCAST] { Client : " + userId[0] + " } Says :" + clientCommand);
            log.writeLog(clientCommand, userId[0], "BROADCAST");
        }
    }

    private void sendBroadcast(String clientCommand) {

        Socket_Serveur.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                if (clientCommand.contains("{") && clientCommand.contains("}")) {
                                    Socket_Serveur.writeSocket(clientCommand, Socket_Serveur.sockets);
                                } else {
                                    Socket_Serveur.writeSocket(username.getValue()._username + " : " + clientCommand, Socket_Serveur.sockets);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void endProcess(Logger log) throws IOException {
        runState = false;
        System.out.print("Stopping server...");
        setUsersDown();
        ServerOn = false;
        Socket_Serveur.writeSocket("END", Socket_Serveur.sockets);
        Socket_Serveur.sockets.removeAll(Socket_Serveur.sockets);
        log.closeLog();
        System.exit(0);
    }


    private void createGroup(String clientCommand) {
        final User[] current_usr = {null};
        Groupe current_grp = null;
        String[] text = clientCommand.split(":");
        String groupe = text[0];


        Socket_Serveur.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> {
                            current_usr[0] = username.getValue();
                        }));
        current_grp = new Groupe(groupe, current_usr[0], client);
        Socket_Serveur.groupes.add(current_grp);
        try {

            int administrator = current_usr[0].Id;
            Statement stmt = null;
            ResultSet rs = null;
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "INSERT INTO groupes(nom, administrator) VALUES ('" + groupe + "','" + administrator + "');");
                rs = stmt.executeQuery("SELECT groupe_uuid FROM groupes WHERE nom=" + groupe);
                while (rs.next()) {
                    current_grp.Id = rs.getInt("groupe_uuid");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[GROUP] : " + current_grp.name + " has been created by : " + current_usr[0]._username);

    }

    private void writePrivate(String[] sender, String destination, String msg) throws IOException {
        Socket_Serveur.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(dest -> {
                            sender[0] = String.valueOf(dest.getValue()._username);
                        }));
        Socket_Serveur.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getValue()._username.equals(destination))
                        .forEach(src -> {
                            try {
                                Socket_Serveur.writeSocket(Arrays.toString(sender) + " : " + msg, src.getKey());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void joinGroup(String clientCommand) {
        final User[] current = {null};
        Groupe current_grp = null;
        String[] text = clientCommand.split(":");
        String groupe = text[1];
        for (Groupe curr : Socket_Serveur.groupes) {
            if (curr.name.equals(groupe)) {
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
        int userId = current[0].Id;
        int groupId = current_grp != null ? current_grp.Id : 0;

        ArrayList<Integer> arguments = new ArrayList<>();
        arguments.add(userId);
        arguments.add(groupId);
        try {
            joinGroup(arguments);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void joinGroup(ArrayList<Integer> args) throws ClassNotFoundException {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");


            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO group_users (`userId`, `groupId`) VALUES('" + args.get(0) + "','" + args.get(1) + "')");


        } catch (SQLException ex1) {
            int code = ex1.getErrorCode();
            if (code != 1062) {
                ex1.printStackTrace();
                return;
            }
        }
        System.out.println("USER ID N°" + args.get(0) + " HAS JOINED GROUP N°" + args.get(1) + "...");
    }

    private void sendGroup(String clientCommand) {
        String[] text = clientCommand.split(":");
        String groupe = text[0].replace("/G", "");
        String msg = text[1];
        final String[] sender = {null};

        for (Groupe current_grp : Socket_Serveur.groupes) {
            if (current_grp.name.equals(groupe)) {
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
                                        Socket_Serveur.writeSocket("[" + current_grp.name + "] " + Arrays.toString(sender) + " : " + msg, username.getKey());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }));
            }
        }
    }

    //TODO Chercher le repertoire du groupe, le fichier .json et envoyer au clients
    // Ils reçoivent ce qu'il y'a sur le serveur et demande/supprime/ des fichiers
    private void groupFileUpload(String clientCommand) {
        String[] text = clientCommand.split(":");
        String groupe = text[0].replace("/createSharingSpace", "");
        String msg = text[1];

        final String[] sender = {null};

        for (Groupe current_grp : Socket_Serveur.groupes) {
            if (current_grp.name.equals(groupe)) {
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
                                        Socket_Serveur.writeSocket("[" + current_grp.name + "] " + Arrays.toString(sender) + " : \n" + msg, username.getKey());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }));
            }
        }
    }

    private void sendPrivate(String clientCommand) {
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
                                Socket_Serveur.writeSocket(Arrays.toString(sender) + " : " + msg, username.getKey());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void getTranslate(String clientCommand) {
        String[] text = clientCommand.split(":");
        String msg = text[1];
        Socket_Serveur.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                Socket_Serveur.writeSocket(username.getValue()._username + " : " + username.getValue().translateMessage(msg), Socket_Serveur.sockets);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void sendFile(String clientCommand) {

    }

    private void getWeather(String clientCommand) {
        Socket_Serveur.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                Socket_Serveur.writeSocket(username.getValue()._username + " : " + username.getValue().getWeather() + "°C", Socket_Serveur.sockets);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void clientExit(String clientCommand) throws NoSuchAlgorithmException {
        runState = false;
        Socket_Serveur.sockets.remove(client);
        System.out.print("Stopping client thread for client :`\n ");
        for (int i = 0; i < Socket_Serveur.users.size(); i++) {
            //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
            if (Socket_Serveur.users.get(i).containsKey(client)) {
                Socket_Serveur.quit();
                System.out.println("[CLIENT EXIT] Client : " + Socket_Serveur.users.get(i).toString()
                        + " Disconnected");
            }
        }

        //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
        for (int i = 0; i < Socket_Serveur.users.size(); i++) {
            if (Socket_Serveur.users.get(i).containsKey(client)) {
                setUserDown();
                try {
                    Socket_Serveur.users.remove(i);
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
        }


        System.out.println("Client(s) : " + Socket_Serveur.users.size());
    }

    private void serverStop() throws IOException {
        System.out.print("Server has already stopped");
        Socket_Serveur.writeSocket("Server has already stopped", client);
        runState = false;
    }

    public void createCloudSubscription(String clientCommand) throws Exception {
        String[] text = clientCommand.split(":");
        String[] args = text[1].split(",");
        String groupe = args[0];
        String spaceName = args[1];

        String path = "C:/temp/" + groupe + "/" + spaceName + "/";
        //Creating a File object
        File file = new File(path);
        //Creating the directory
        boolean bool = file.mkdir();
        int groupId = 0;
        SocketPerso socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        Statement stmt = null;
        try {
            groupId = Socket_Serveur.getGroupId(groupe);
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "INSERT INTO tcpFileSharing(groupId, rootPath) VALUES (" + groupId + ",'" + path + " ')");
                log.writeLog("\"Group N°" + groupId + " created new Cloud Service at : " + path + "\"", -666, "[SQL]");
                createRootDirectory(path);
            } catch (Exception e) {
                if (rs == null) {
                    System.err.println("Erreur de création d'un groupe de partage ! ");
                    System.err.println("Erreur :\n" + (stmt != null ? stmt.getWarnings().getSQLState() : null));
                }
                System.err.println("Erreur de connexion au serveur de fichier...");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createRootDirectory(String path) {
        try {
            String currentDirectoryPath = FileSystems.getDefault().
                    getPath("").
                    toAbsolutePath().
                    toString();
            var script = currentDirectoryPath + "\\create_root_directory.ps1 " + path;
            //String command = "powershell.exe  your command";
            //Getting the version
            String command = "powershell.exe  " + script;

            // Executing the command
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            // Getting the results
            powerShellProcess.getOutputStream().close();
            String line;
            System.out.println("Standard Output:");
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            while ((line = stdout.readLine()) != null) {
                System.out.println(line);
            }
            stdout.close();
            System.out.println("Standard Error:");
            BufferedReader stderr = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getErrorStream()));
            while ((line = stderr.readLine()) != null) {
                System.out.println(line);
            }
            stderr.close();
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        log.writeLog("Root directory : " + path + "\" created", -666, "[CLOUD]");
        System.out.println("[CLOUD] Root directory :\"" + path + "\" created");
    }

    public void run() {
        ClientCommandEnum clientRequest = ClientCommandEnum.Lazy;
        var message = "[NEW THREAD] Accepted Client Address - " + client.getInetAddress().getHostName();
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");
        message = "[LIST UPDATE] Client(s) : " + Socket_Serveur.users.size();
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");
        String clientUsername = null;

        try {

            while (runState) {

                String clientCommand = client.isConnected() ? Socket_Serveur.readClientStream(client) : null;
                if (clientCommand != null) {
                    printBroadcast(clientCommand);
                }
                if (!ServerOn) {
                    serverStop();
                }

                if (clientCommand == null) continue;
                if (clientCommand.equalsIgnoreCase(ClientCommandEnum.Quit.Label))
                    clientExit(clientCommand);
                else if (clientCommand.equalsIgnoreCase(ClientCommandEnum.WeatherInfo.Label)) getWeather(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.Translate.Label)) getTranslate(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.PrivateMessage.Label)) sendPrivate(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.SendFile.Label)) sendFile(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.GroupMessage.Label)) sendGroup(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.JoinGroupRequest.Label)) joinGroup(clientCommand);
                else if (clientCommand.contains(ClientCommandEnum.GroupCreationRequest.Label))
                    createGroup(clientCommand);
                else if (clientCommand.equalsIgnoreCase(ClientCommandEnum.EndProcess.Label)) endProcess(log);
                else if (clientCommand.equalsIgnoreCase(ClientCommandEnum.Lazy.Label)) endProcess(log);
                else if (clientCommand.contains(ClientCommandEnum.CreateSharingSpace.Label))
                    createCloudSubscription(clientCommand);
                else sendBroadcast(clientCommand);

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final int[] clientId = {0};
            final HashMap<Socket, User>[] currentUser = new HashMap[]{null};

            Socket_Serveur.users.forEach(user -> {
                if (user.containsKey(client)) {
                    clientId[0] = user.get(client).Id;
                    currentUser[0] = user;
                }
            });
            Socket_Serveur.groupes.forEach(groupe -> {
                if (groupe.groupeUsers.contains((currentUser[0])))
                    groupe.groupeUsers.remove(currentUser[0]);
            });

            Socket_Serveur.users.remove(currentUser[0]);
            message = "Thread Client N°" + clientId[0] + " finished, now disconnected";
            System.out.println(message);
            log.writeLog(message, -666, "[INFO]");
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

    public void ecrireFichierSocket(String filename) throws IOException {

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
        private final SocketPerso client;
        private final Console commandes;

        public Thread_Client_Receive(SocketPerso client) throws IOException {
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
        private final SocketPerso socket;
        private final Console console;
        private String email;
        private String username;

        public Thread_Client_Send(SocketPerso s, String p_email) throws IOException {
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
                            socket.ecrireFichierSocket(test[1]);
                        }
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





