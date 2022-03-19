package Projet_Socket.Server;

import Projet_Socket.Login.Identity.Group;
import Projet_Socket.Login.Identity.User;
import Projet_Socket.Utils.File.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe permettant de gérer le serveur TCP
 */
public final class ServerTcp {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Group> groupes = new ArrayList<>();
    public static ArrayList<HashMap<Socket, User>> users = new ArrayList<>();
    public static ServerSocket _srvSocket;
    private static int maxConnection;
    private static int nb_socket;

    /**
     * Lance un serveur tcp
     * @param socket socket serveur
     */
    public static void createServer(@NotNull ServerSocket socket) {
        _srvSocket = socket;
        maxConnection = 20;
        nb_socket = 0;
    }

    @Contract(pure = true)
    public static int getMaxConnection() {
        return maxConnection;
    }

    /**
     * Accepte la connexion d'un client entrant
     * @return socket du client accepté
     * @throws IOException
     */
    public static Socket acceptClient() throws IOException {

        nb_socket++;

        return _srvSocket.accept();


    }

    /**
     * Permet de lancer un script powershell
     * @param scriptPath chemin vers le script
     * @param arguments arguments du script
     */
    public static void runPowershellScript(@NotNull String scriptPath,@NotNull ArrayList<String> arguments) {
        try {
            final String[] script = {scriptPath};
            arguments.forEach(arg -> script[0] += " " + arg);
            var command = "powershell.exe  " + script[0];
            // Executing the command
            var powerShellProcess = Runtime.getRuntime().exec(command);
            // Getting the results
            powerShellProcess.getOutputStream().close();
            var line = "";
            System.out.println("Standard Output:");
            var stdout = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            while ((line = stdout.readLine()) != null) {
                System.out.println(line);
            }
            stdout.close();
            System.out.println("Standard Error:");
            var stderr = new BufferedReader(new InputStreamReader(
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

    }


    /**
     * @return nombre de sockets connectés
     */
    @Contract(pure = true)
    public static int getNbSocket() {
        return nb_socket;
    }

    /**
     * Décremente le nombre de socket sur le serveur
     */
    public static void quit() {
        nb_socket--;
    }


    /**
     * @return instance du socket serveur
     */
    @Contract(pure = true)
    public static ServerSocket getServer() {
        return _srvSocket;
    }

    /**
     * Envoi le contenu du message à une liste de client
     * @param content contenu à envoyer
     * @param clients liste de clients à qui envoyer
     * @throws IOException
     */
    public static void writeSocket(@NotNull String content, @NotNull ArrayList<Socket> clients) throws IOException {

        for (var socket : clients) {

            var out = new PrintWriter(socket.getOutputStream());
            out.println(content);
            out.flush();
        }


    }

    /**
     * Envoi le contenu du message à un seul client
     * @param content contenu à envoyer
     * @param client client à qui envoyer
     * @throws IOException
     */
    public static void writeSocket(@NotNull String content, @NotNull Socket client) throws IOException {


        var out = new PrintWriter(client.getOutputStream());
        out.println(content);
        out.flush();


    }

    /**
     * Renvoie la liste de tous les fichiers pour un groupe
     * @param groupId id du groupe
     * @return liste des fichiers du groupe
     * @throws SQLException
     */
    @NotNull
    public static ArrayList<String> getFilesByGroup(int groupId) throws SQLException {
        var result = new ArrayList<String>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            var logger = new Logger();
            var logMessage = "GET FILES FOR GROUP ID N°" + groupId;
            System.out.println();
            logger.writeLog(logMessage, -666, "[SQL]");
            System.out.println("[SQL] " + logMessage);

            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT rootPath FROM tcpFileSharing WHERE groupId=" + groupId);

            while (rs.next()) {
                var entry = rs.getString("rootPath");
                result.add(entry);
            }
        } catch (Exception e) {
            throw new SQLException();
        }

        return result;
    }

    /**
     * Renvoi un hasmap de tous les groupes avec leur liste de fichiers associés
     * @return mappage groupe/fichiers
     */
    @NotNull
    public static HashMap<Group, ArrayList<String>> getFilesByGroup() {
        var result = new HashMap<Group, ArrayList<String>>();
        groupes.forEach(groupe -> {
            try {
                var groupSpaces = getFilesByGroup(groupe.Id);
                result.put(groupe, groupSpaces);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        });
        result.forEach((__, rootDirectories) -> {

        });
        return result;
    }

    /**
     * Renvoi la liste des fichiers dans un repertoire donné
     * @param directory repertoire à inspecter
     * @return liste des fichiers sur le repertoire avec leur chemin absolu
     */
    @NotNull
    private static ArrayList<String> getFilesByDirectory(@NotNull String directory) {

        var result = new ArrayList<String>();
        //Creating a File object for directory
        var directoryPath = new File("D:\\ExampleDirectory");
        //List of all files and directories
        var messageLog = "\"List of files and directories in :" + directory + "\"";
        var filesList = directoryPath.listFiles();
        System.out.println("[CLOUD] " + messageLog);
        for (var file : filesList != null ? filesList : new File[0]) {
            result.add(file.getAbsolutePath());
            System.out.println("File name: " + file.getName());
            System.out.println("File path: " + file.getAbsolutePath());
            System.out.println("Size :" + file.getTotalSpace());
            System.out.println(" ");
        }
        return result;
    }

    /**
     * Renvoie le contenu texte envoyé par un client
     * @param client client du thread
     * @return contenu envoyé par le client
     * @throws IOException
     */
    @Nullable
    public static String readClientStream(@NotNull Socket client) throws IOException {
        String res = null;

        var result = "";
        try {
            var reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

            var line = reader.readLine();
            if (line.contains("{") && line.contains("}")) {
                var jsonObject = new JSONObject(line);
                var name = (String) jsonObject.get("name");
                var size = (int) jsonObject.get("size");
                var content = jsonObject.getJSONArray("content");
                var data = new byte[content.length()];
                for (var i = 0; i < content.length(); i++) {
                    data[i] = ((Integer) content.get(i)).byteValue();
                }
                var out = new FileOutputStream(name + "_");
                out.write(data);
                out.close();

                result = jsonObject.toString();
            } else {
                res = line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
        if (res == null) {
            res = result;
        }
        return res;
    }

    /**
     * Renvoie la liste des groupes de l'utilisateur
     * @param userId id de l'utilisateur
     * @return liste des groupes de l'utilisateur
     */
    @NotNull
    public static ArrayList<Group> getUserGroups(int userId) {
        var groupId = 0;
        var results = new ArrayList<Group>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            var messageLog = "\"[SQL] "+ "SETTING GROUPS FOR USER ID N° " + userId + "...\"";
            var logger = new Logger();
            logger.writeLog(messageLog, -666,"[SQL]");
            System.out.println(messageLog);


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT groupId FROM group_users WHERE userId='" + userId + "'");

            while (rs.next()) {
                groupId = rs.getInt("groupId");
            }

            rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes WHERE groupe_uuid =" + groupId);
            while (rs.next()) {
                var currentGroup = new Group();
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

    /**
     * Renvoie la liste des groupes stockés en base
     * @return liste des groupes présents en base de données
     */
    @NotNull
    public static ArrayList<Group> getGroups() {
        var results = new ArrayList<Group>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] FETCHING GROUPS FROM DATABASE...");


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes");

            while (rs.next()) {
                var currentGroup = new Group();
                currentGroup.Id = rs.getInt("groupe_uuid");
                currentGroup.administratorId = rs.getInt("administrator");
                currentGroup.name = rs.getString("nom");
                currentGroup.groupeUsers = new ArrayList<>();
                results.add(currentGroup);
            }

            ServerTcp.groupes.addAll(results);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }
        return results;
    }

    /**
     * Renvoie l'id d'un groupe en fonction de son nom
     * @param groupeName nom du groupe
     * @return id du groupe
     */
    public static int getGroupId(@NotNull String groupeName) {
        var groupId = 0;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] GET GROUP ID REQUEST REQUEST for " + groupeName + "...");


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT groupe_uuid FROM groupes WHERE nom='" + groupeName + "'");

            while (rs.next()) {
                groupId = rs.getInt("groupe_uuid");
            }

        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

        return groupId;
    }

    /**
     * Renvoie l'id d'un utilisateur en fonction de son email
     * @param userMail id de l'utilisateur
     * @return id de l'utilisateur
     */
    public static int getUserId(@NotNull String userMail) {
        var userId = 0;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] GET USER ID for " + userMail + "...");


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT user_uuid FROM users WHERE email='" + userMail + "'");

            while (rs.next()) {
                userId = rs.getInt("user_uuid");
            }

            System.out.println("[SQL] USER " + userMail + " is : " + userId);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

        return userId;
    }

    /**
     * Envoi un fichier à un client en écoute
     * @param path chemin du fichier
     * @param client client en écoute
     * @throws IOException
     */
    public void sendFileBroadcast(@NotNull String path, @NotNull Socket client) throws IOException {

        var writer = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);


        var jsonObject = new JSONObject();
        jsonObject.put("test", path);
        writer.write(jsonObject.toString());
        writer.flush();

    }

    /**
     * Envoi un fichier à une liste de clients
     * @param path chemin du fichier
     * @param clients clients en écoutes
     * @throws IOException
     */
    public void sendFileBroadcast(@NotNull String path, @NotNull ArrayList<Socket> clients) throws IOException {

        for (var socket : clients) {
            var writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);


            var jsonObject = new JSONObject();
            jsonObject.put("test", path);
            writer.write(jsonObject.toString());
            writer.flush();
        }

    }

}
