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
            String command = "powershell.exe  " + script[0];
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

    public static ArrayList<String> getFilesByGroup(int groupId) throws SQLException {
        ArrayList<String> result = new ArrayList<>();
        try{
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            var logger = new Logger();
            var logMessage = "GET FILES FOR GROUP ID N°"+groupId;
            System.out.println();
            logger.writeLog(logMessage, -666, "[SQL]" );
            System.out.println("[SQL] "+logMessage);

            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT rootPath FROM tcpFileSharing WHERE groupId="+groupId);

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
        });
        return result;
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

    public static ArrayList<Group> getUserGroups(int userId) {
        int groupId = 0;
        ArrayList<Group> results = new ArrayList<>();
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
        ArrayList<Group> results = new ArrayList<>();
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] FETCHING GROUPS FROM DATABASE...");


            Statement stmt = conn.createStatement();


            ResultSet rs = stmt.executeQuery("SELECT groupe_uuid, administrator,nom FROM groupes");

            while (rs.next()) {
                Group currentGroup = new Group();
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
