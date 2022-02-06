package Projet_Socket.Server;

import Projet_Socket.Client.ClientTcp;
import Projet_Socket.Login.Identity.Group;
import Projet_Socket.Login.Identity.User;
import Projet_Socket.Utils.File.Logger;
import Projet_Socket.Utils.InternalCommandsEnum;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ServerClientWorker extends Thread {

    final private Socket client;
    final private Logger log;
    private boolean runState = true;
    private boolean ServerOn = true;

    public ServerClientWorker(Socket s) {

        this.client = s;
        ServerTcp.sockets.add(s);
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


        ServerTcp.users //stream out of arraylist
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
        ServerTcp.users.forEach(socketUserHashMap -> {
            if (socketUserHashMap.containsKey(client))
                userId[0] = socketUserHashMap.get(client).Id;
        });
        if (clientCommand != null) {
            System.out.println("\"[BROADCAST] { Client : " + userId[0] + " } Says :" + clientCommand+"\"");
            log.writeLog("\""+clientCommand+"\"", userId[0], "BROADCAST");
        }
    }

    private void sendBroadcast(String clientCommand) {

        ServerTcp.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                if (clientCommand.contains("{") && clientCommand.contains("}")) {
                                    ServerTcp.writeSocket(clientCommand, ServerTcp.sockets);
                                } else {
                                    ServerTcp.writeSocket(username.getValue()._username + " : " + clientCommand, ServerTcp.sockets);
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
        ServerTcp.writeSocket("END", ServerTcp.sockets);
        ServerTcp.sockets.removeAll(ServerTcp.sockets);
        log.closeLog();
        System.exit(0);
    }


    private void createGroup(String clientCommand) {
        final User[] current_usr = {null};
        Group current_grp = null;
        String[] text = clientCommand.split(":");
        String groupe = text[0];


        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> {
                            current_usr[0] = username.getValue();
                        }));
        current_grp = new Group(groupe, current_usr[0], client);
        ServerTcp.groupes.add(current_grp);
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
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(dest -> {
                            sender[0] = String.valueOf(dest.getValue()._username);
                        }));
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getValue()._username.equals(destination))
                        .forEach(src -> {
                            try {
                                ServerTcp.writeSocket(Arrays.toString(sender) + " : " + msg, src.getKey());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void joinGroup(String clientCommand) {
        final User[] current = {null};
        Group current_grp = null;
        String[] text = clientCommand.split(":");
        String groupe = text[1];
        for (Group curr : ServerTcp.groupes) {
            if (curr.name.equals(groupe)) {
                current_grp = curr;
            }
        }
        ServerTcp.users //stream out of arraylist
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
        ClientTcp socket_client = null;
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

        for (Group current_grp : ServerTcp.groupes) {
            if (current_grp.name.equals(groupe)) {
                ServerTcp.users //stream out of arraylist
                        .forEach(map -> map.entrySet().stream()
                                .filter(entry1 -> entry1.getKey().equals(client))
                                .forEach(username -> {
                                    sender[0] = String.valueOf(username.getValue()._username);
                                }));
                current_grp.groupeUsers //stream out of arraylist
                        .forEach(map -> map.entrySet()
                                .forEach(username -> {
                                    try {
                                        ServerTcp.writeSocket("[" + current_grp.name + "] " + Arrays.toString(sender) + " : " + msg, username.getKey());
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

        for (Group current_grp : ServerTcp.groupes) {
            if (current_grp.name.equals(groupe)) {
                ServerTcp.users //stream out of arraylist
                        .forEach(map -> map.entrySet().stream()
                                .filter(entry1 -> entry1.getKey().equals(client))
                                .forEach(username -> {
                                    sender[0] = String.valueOf(username.getValue()._username);
                                }));
                current_grp.groupeUsers //stream out of arraylist
                        .forEach(map -> map.entrySet()
                                .forEach(username -> {
                                    try {
                                        ServerTcp.writeSocket("[" + current_grp.name + "] " + Arrays.toString(sender) + " : \n" + msg, username.getKey());
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
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> {
                            sender[0] = String.valueOf(username.getValue()._username);
                        }));
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getValue()._username.equals(destination))
                        .forEach(username -> {
                            try {
                                ServerTcp.writeSocket(Arrays.toString(sender) + " : " + msg, username.getKey());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void getTranslate(String clientCommand) {
        String[] text = clientCommand.split(":");
        String msg = text[1];
        ServerTcp.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                ServerTcp.writeSocket(username.getValue()._username + " : " + username.getValue().translateMessage(msg), ServerTcp.sockets);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void sendFile(String clientCommand) {

    }

    private void getWeather(String clientCommand) {
        ServerTcp.users.stream() //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(client))
                        .forEach(username -> {
                            try {
                                ServerTcp.writeSocket(username.getValue()._username + " : " + username.getValue().getWeather() + "°C", ServerTcp.sockets);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }

    private void clientExit(String clientCommand) throws NoSuchAlgorithmException {
        runState = false;
        ServerTcp.sockets.remove(client);
        System.out.print("Stopping client thread for client :`\n ");
        for (int i = 0; i < ServerTcp.users.size(); i++) {
            //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
            if (ServerTcp.users.get(i).containsKey(client)) {
                ServerTcp.quit();
                System.out.println("[CLIENT EXIT] Client : " + ServerTcp.users.get(i).toString()
                        + " Disconnected");
            }
        }

        //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
        for (int i = 0; i < ServerTcp.users.size(); i++) {
            if (ServerTcp.users.get(i).containsKey(client)) {
                setUserDown();
                try {
                    ServerTcp.users.remove(i);
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
        }


        System.out.println("Client(s) : " + ServerTcp.users.size());
    }

    private void serverStop() throws IOException {
        System.out.print("Server has already stopped");
        ServerTcp.writeSocket("Server has already stopped", client);
        runState = false;
    }

    private ArrayList<String> checkGroupFiles(String path){
        ArrayList<String> result = new ArrayList<>();
        //Creating a File object for directory
        File directoryPath = new File(path);
        FileFilter textFilefilter = new FileFilter(){
            public boolean accept(File file) {
                boolean isFile = file.isFile();
                if (isFile) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        //List of all the text files
        File filesList[] = directoryPath.listFiles(textFilefilter);
        System.out.println("List of the text files in the specified directory:");
        for(File file : filesList) {
            result.add(file.getAbsolutePath());
            System.out.println("File name: "+file.getName());
            System.out.println("File path: "+file.getAbsolutePath());
            System.out.println("Size :"+file.getTotalSpace());
            System.out.println(" ");
        }
        return result;
    }

    public void fileSynchronisationRequest(String clientCommand){
        try{

            final String[] sender = {null};
            ServerTcp.users //stream out of arraylist
                    .forEach(map -> map.entrySet().stream()
                            .filter(entry1 -> entry1.getKey().equals(client))
                            .forEach(username -> {
                                sender[0] = String.valueOf(username.getValue()._username);
                            }));

            String userName = sender[0];
            HashMap<String, ArrayList<String>> filesByGroup = new HashMap<>();
            ArrayList<Group> groups = ServerTcp.getGroups();

            var content = new JSONObject();
            var date = log.getDateNow();
            content.put("date", "\""+date+"\"");
            content.put("server_files", filesByGroup);
            content.put("user","\""+ userName+"\"");
            content.put("groups", groups);
            content.put("response status", 200);
            var json = content.toString();

            ServerTcp.writeSocket(json, client);

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
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
        ClientTcp socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        Statement stmt = null;
        try {
            groupId = ServerTcp.getGroupId(groupe);
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "INSERT INTO tcpFileSharing(groupId, rootPath) VALUES (" + groupId + ",'" + path + " ')");
                var message = "\"Group N°" + groupId + " created new Cloud Service at : " + path + "\"";
                log.writeLog(message, -666, "[CLOUD]");
                sendGroup("/G"+groupe+":\n"+"[CLOUD] "+message);
                createRootDirectory(path);
            } catch (Exception e) {
                if (rs == null) {
                    System.err.println("Erreur de création d'un groupe de partage ! ");
                    System.err.println("Erreur :\n" + (stmt != null ? stmt.getWarnings().getSQLState() : null));
                    var message = "\"Group N°" + groupId + " cannot be created at : " + path + "\"";
                    log.writeLog(message, -666, "[CLOUD]");
                    sendGroup("/G"+groupe+":\n"+"[CLOUD] "+message);
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
            var scriptPath = currentDirectoryPath + "\\create_root_directory.ps1";
            var arguments = new ArrayList<String>();
            arguments.add(path);
            ServerTcp.runPowershellScript(scriptPath,arguments);
            //String command = "powershell.exe  your command";
            //Getting the version

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        log.writeLog("\"Root directory : " + path + " created\"", -666, "[CLOUD]");
        System.out.println("[CLOUD] Root directory :\"" + path + "\" created");

    }


    public void run() {
        InternalCommandsEnum clientRequest = InternalCommandsEnum.Lazy;
        var message = "\"[NEW THREAD] Accepted Client Address - " + client.getInetAddress().getHostName()+"\"";
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");
        message = "\"[LIST UPDATE] Client(s) : " + ServerTcp.users.size()+"\"";
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");
        String clientUsername = null;

        try {

            while (runState) {

                String clientCommand = client.isConnected() ? ServerTcp.readClientStream(client) : null;
                if (clientCommand != null) {
                    printBroadcast(clientCommand);
                }
                if (!ServerOn) {
                    serverStop();
                }

                if (clientCommand == null) continue;

                //region Internal Commands
                if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.Quit.Label)) clientExit(clientCommand);

                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.EndProcess.Label)) endProcess(log);
                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.Lazy.Label)) endProcess(log);
                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.WeatherInfo.Label)) getWeather(clientCommand);

                else if (clientCommand.contains(InternalCommandsEnum.Translate.Label)) getTranslate(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.PrivateMessage.Label)) sendPrivate(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.SendFile.Label)) sendFile(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.GroupMessage.Label)) sendGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.JoinGroupRequest.Label)) joinGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.GroupCreationRequest.Label)) createGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.CreateSharingSpace.Label)) createCloudSubscription(clientCommand);
                else if(clientCommand.contains(InternalCommandsEnum.FileSynchronisation.Label)) fileSynchronisationRequest(clientCommand);
                else sendBroadcast(clientCommand);
                //endregion Internal Commands
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

            ServerTcp.users.forEach(user -> {
                if (user.containsKey(client)) {
                    clientId[0] = user.get(client).Id;
                    currentUser[0] = user;
                }
            });
            ServerTcp.groupes.forEach(groupe -> {
                if (groupe.groupeUsers.contains((currentUser[0])))
                    groupe.groupeUsers.remove(currentUser[0]);
            });

            ServerTcp.users.remove(currentUser[0]);
            message = "\"Thread Client N°" + clientId[0] + " finished, now disconnected\"";
            System.out.println(message);
            log.writeLog(message, -666, "[INFO]");
        }
    }


}
