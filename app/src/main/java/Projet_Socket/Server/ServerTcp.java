package Projet_Socket.Server;

import Projet_Socket.Login.Identity.Group;
import Projet_Socket.Login.Identity.User;
import Projet_Socket.Utils.File.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerTcp {

    public static ArrayList<Socket> sockets = new ArrayList<>();
    public static ArrayList<Group> groupes = new ArrayList<>();
    public static ArrayList<HashMap<Socket, User>> users = new ArrayList<>();
    public static ServerSocket _srvSocket;
    private static int maxConnection;
    private static int nb_socket;

    public static void createServer(ServerSocket socket) {
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

    public static void runPowershellScript(String scriptPath, ArrayList<String> arguments){
        try{
            final String[] script = {scriptPath};
            arguments.forEach(arg ->{
                script[0] +=" "+arg;
            });
            var command = "powershell.exe  " + script[0];
            // Executing the command
            var powerShellProcess = Runtime.getRuntime().exec(command);
            // Getting the results
            powerShellProcess.getOutputStream().close();
            var line ="";
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
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }

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

        for (var socket : clients) {

            var out = new PrintWriter(socket.getOutputStream());
            out.println(content);
            out.flush();
        }


    }

    public static void writeSocket(String content, Socket client) throws IOException {


        var out = new PrintWriter(client.getOutputStream());
        out.println(content);
        out.flush();


    }

    public static ArrayList<String> getFilesByGroup(int groupId) throws SQLException {
        var result = new  ArrayList<String>();
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            var logger = new Logger();
            var logMessage = "GET FILES FOR GROUP ID N°"+groupId;
            System.out.println();
            logger.writeLog(logMessage, -666, "[SQL]" );
            System.out.println("[SQL] "+logMessage);

            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT rootPath FROM tcpFileSharing WHERE groupId="+groupId);

            while (rs.next()) {
                var entry = rs.getString("rootPath");
                result.add(entry);
            }
        }catch (Exception e){
            throw new SQLException();
        }

        return result;
    }

    public static HashMap<Group, ArrayList<String>> getFilesByGroup(){
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

    private static ArrayList<String> getFilesByDirectory(String directory){

        var result = new ArrayList<String>();
        //Creating a File object for directory
        var directoryPath = new File("D:\\ExampleDirectory");
        //List of all files and directories
        var messageLog = "\"List of files and directories in :"+directory+"\"";
        var filesList = directoryPath.listFiles();
        System.out.println("[CLOUD] "+messageLog);
        for(var file : filesList != null ? filesList : new File[0]) {
            result.add(file.getAbsolutePath());
            System.out.println("File name: "+file.getName());
            System.out.println("File path: "+file.getAbsolutePath());
            System.out.println("Size :"+file.getTotalSpace());
            System.out.println(" ");
        }
        return result;
    }
    public static String readClientStream(Socket client) throws IOException {

        String result = "";
        try {
            var reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

            String line = client.isConnected() ? reader.readLine() : "";
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
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
        return result;
    }

    public static ArrayList<Group> getUserGroups(int userId) {
        int groupId = 0;
        var results = new ArrayList<Group>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] SETTING GROUPS FOR USER ID N° " + userId + "...");


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery("SELECT groupId FROM group_users WHERE userId='" + userId + "'");

            while (rs.next()) {
                groupId = rs.getInt("groupId");
            }

            rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes WHERE groupe_uuid =" + groupId);
            while (rs.next()) {
                Group currentGroup = new Group();
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

    public static ArrayList<Group> getGroups() {
        int groupId = 0;
        var results = new  ArrayList<Group>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
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

    public static int getGroupId(String groupeName) {
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

    public static int getUserId(String userMail) {
        int userId = 0;
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

    public void sendFileBroadcast(String path, Socket client) throws IOException {

        var writer = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8);


        var jsonObject = new JSONObject();
        jsonObject.put("test", path);
        writer.write(jsonObject.toString());
        writer.flush();

    }

    public void sendFileBroadcast(String path, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {
            var writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);


            var jsonObject = new JSONObject();
            jsonObject.put("test", path);
            writer.write(jsonObject.toString());
            writer.flush();
        }

    }

}
