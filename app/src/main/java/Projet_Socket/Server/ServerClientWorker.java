package Projet_Socket.Server;

import Projet_Socket.Login.Identity.Group;
import Projet_Socket.Login.Identity.User;
import Projet_Socket.Shared.WorkerService;
import Projet_Socket.Utils.File.Logger;
import Projet_Socket.Utils.InternalCommandsEnum;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.Closeable;
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
import java.util.Objects;

/**
 * Thread d'écoute d'un client tcp
 */
public final class ServerClientWorker extends WorkerService<ServerClientWorker> {

    final private Socket client;
    final private Logger log;
    private boolean runState = true;
    private boolean ServerOn = true;

    /**
     * Lance un thread serveur de réception pour un client
     * @param s socket client
     */
    public ServerClientWorker(@NotNull Socket s) {
        super(s, "[SERVER]");

        this.client = s;
        ServerTcp.sockets.add(s);
        log = new Logger();


    }

    /**
     * Affichage console sur le serveur des messages broadcast
     * @param clientCommand commande du client
     */
    private void printBroadcast(@NotNull String clientCommand) {
        final int[] userId = {0};
        ServerTcp.users.forEach(socketUserHashMap -> {
            if (socketUserHashMap.containsKey(client))
                userId[0] = socketUserHashMap.get(client).Id;
        });
        System.out.println("\"[BROADCAST] { Client : " + userId[0] + " } Says :" + clientCommand + "\"");
        log.writeLog("\"" + clientCommand + "\"", userId[0], "BROADCAST");
    }

    /**
     * Transmet le message d'un client à tout le monde
     * @param clientCommand message du client
     */
    private void sendBroadcast(@NotNull String clientCommand) {

        ServerTcp.users //stream out of arraylist
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

    /**
     * Stop le thread du client
     * @param log context logger
     * @throws IOException
     */
    private void endProcess(@NotNull Logger log) throws IOException {
        runState = false;
        System.out.print("Stopping server...");
        setUsersDown();
        ServerOn = false;
        ServerTcp.writeSocket("END", ServerTcp.sockets);
        ServerTcp.sockets = null;
        log.closeLog();
        System.exit(0);
    }


    /**
     * Créer un groupe sur le serveur et en base de données
     * @param clientCommand commande du client
     */
    private void createGroup(@NotNull String clientCommand) {
        final User[] current_usr = {null};
        var text = clientCommand.split(":");
        var groupe = text[0];


        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> current_usr[0] = username.getValue()));

        var current_grp = new Group(groupe, current_usr[0], client);
        var rootPath = "C:/temp/"+groupe;
        ServerTcp.groupes.add(current_grp);
        try {

            var administrator = current_usr[0].Id;
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                var stmt = conn.createStatement();

                var rs = stmt.executeQuery(
                        "INSERT INTO groupes(nom, administrator, rootPath) VALUES ('" + groupe + "','" + administrator + "', '"+groupe+"');");
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

    /**
     * Permet de rejoindre un groupe en base et sur le serveur et recevoir les messages de ce dernier
     * @param clientCommand commande du client
     */
    private void joinGroup(@NotNull String clientCommand) {
        final User[] current = {null};
        var current_grp = new Group();
        var text = clientCommand.split(":");
        var groupe = text[1];
        for (var curr : ServerTcp.groupes) {
            if (curr.name.equals(groupe)) {
                current_grp = curr;
            }
        }
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> current[0] = username.getValue()));

        var userId = current[0].Id;
        var groupId = current_grp.Id;

        var arguments = new ArrayList<Integer>();
        arguments.add(userId);
        arguments.add(groupId);
        try {
            joinGroup(arguments);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Permet de rejoindre un groupe en base de données
     * @param args id de l'utilisateur et du groupe
     * @throws ClassNotFoundException
     */
    public void joinGroup(@NotNull ArrayList<Integer> args) throws ClassNotFoundException {
        boolean finished = false;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");


            var stmt = conn.createStatement();


            stmt.executeQuery(
                    "INSERT INTO group_users (`userId`, `groupId`) VALUES('" + args.get(0) + "','" + args.get(1) + "')");


        } catch (SQLException ex1) {
            var code = ex1.getErrorCode();
            if (code != 1062) {
                ex1.printStackTrace();
                finished = true;
            }
        }
        if (!finished) {
            System.out.println("USER ID N°" + args.get(0) + " HAS JOINED GROUP N°" + args.get(1) + "...");
        }
    }


    /**
     * Renvoi la traduction en anglais de la saisie depuis yandex traduction
     * @param clientCommand
     */
    private void getTranslate(@NotNull String clientCommand) {
        var text = clientCommand.split(":");
        var msg = text[1];
        ServerTcp.users //stream out of arraylist
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

    private void sendFile() {

    }

    /**
     * Renvoi les informations météo depuis openweathermap
     */
    private void getWeather() {
        ServerTcp.users //stream out of arraylist
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



    /**
     * Compare la liste des fichiers sur le serveur avec celle des clients
     */
    public void fileSynchronisationRequest() {
        try {

            final String[] sender = {null};
            ServerTcp.users //stream out of arraylist
                    .forEach(map -> map.entrySet().stream()
                            .filter(entry1 -> entry1.getKey().equals(client))
                            .forEach(username -> sender[0] = String.valueOf(username.getValue()._username)));

            var userName = sender[0];
            var filesByGroup = new HashMap<String, ArrayList<String>>();
            var groups = ServerTcp.getGroups();

            var content = new JSONObject();
            var date = log.getDateNow();
            content.put("date", "\"" + date + "\"");
            content.put("server_files", filesByGroup);
            content.put("user", "\"" + userName + "\"");
            content.put("groups", groups);
            content.put("response status", 200);
            var json = content.toString();

            ServerTcp.writeSocket(json, client);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Créer une souscription à un espace cloud en base et créer le dossier correspondant
     * @param clientCommand contenu de la commande client
     * @throws Exception
     */
    public void createCloudSubscription(@NotNull String clientCommand) throws Exception {
        var text = clientCommand.split(":");
        var args = text[1].split(",");
        var groupe = args[0];
        var spaceName = args[1];

        try {
            var groupId = ServerTcp.getGroupId(groupe);
            Class.forName("org.mariadb.jdbc.Driver");
            var path = "C:/temp/" + groupe + "/" + spaceName + "/";
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                var stmt = conn.createStatement();

                stmt.executeQuery(
                        "INSERT INTO tcpFileSharing(groupId, rootPath) VALUES (" + groupId + ",'" + path + " ')");
                var message = "\"Group N°" + groupId + " created new Cloud Service at : " + path + "\"";
                log.writeLog(message, -666, "[CLOUD]");
                sendGroup("/G" + groupe + ":\n" + "[CLOUD] " + message);
                createRootDirectory(path);
            } catch (Exception e) {
                e.printStackTrace();
                throw new SQLException();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Créer un nouveau repertoire pour un groupe
     * @param path chemin du repertoire à créer
     */
    private void createRootDirectory(@NotNull String path) {
        try {
            var currentDirectoryPath = FileSystems.getDefault().
                    getPath("").
                    toAbsolutePath().
                    toString();
            var scriptPath = currentDirectoryPath + "\\create_root_directory.ps1";
            var arguments = new ArrayList<String>();
            arguments.add(path);
            ServerTcp.runPowershellScript(scriptPath, arguments);
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
        var message = "\"[NEW THREAD] Accepted Client Address - " + client.getInetAddress().getHostName() + "\"";
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");
        message = "\"[LIST UPDATE] Client(s) : " + ServerTcp.users.size() + "\"";
        System.out.println(message);
        log.writeLog(message, -666, "[INFO]");

        try {

            while (runState) {

                var clientCommand = client.isConnected() ? ServerTcp.readClientStream(client) : null;
                if (clientCommand != null) {
                    printBroadcast(clientCommand);
                }
                if (!ServerOn) {
                    serverStop();
                }

                if (clientCommand == null) continue;

                //region Internal Commands
                if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.Quit.Label)) clientExit();

                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.EndProcess.Label)) endProcess(log);
                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.Lazy.Label)) endProcess(log);
                else if (clientCommand.equalsIgnoreCase(InternalCommandsEnum.WeatherInfo.Label))
                    getWeather();

                else if (clientCommand.contains(InternalCommandsEnum.Translate.Label)) getTranslate(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.PrivateMessage.Label)) sendPrivate(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.SendFile.Label)) sendFile();
                else if (clientCommand.contains(InternalCommandsEnum.GroupMessage.Label)) sendGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.JoinGroupRequest.Label)) joinGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.GroupCreationRequest.Label)) createGroup(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.CreateSharingSpace.Label)) createCloudSubscription(clientCommand);
                else if (clientCommand.contains(InternalCommandsEnum.FileSynchronisation.Label)) fileSynchronisationRequest();
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
            var currentUser = new HashMap[]{null};

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
