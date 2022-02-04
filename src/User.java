import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.math.BigInteger;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

class LogUser {

    public static User currentUser;

    LogUser() {

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

    public void createUser(ArrayList<String> args) throws ClassNotFoundException {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("Requête de création d'utilisateur en cours d'execution...");


            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO users (`pseudo`, `password`, `email`,`isConnected`) VALUES('" + args.get(0) + "','" + args.get(1) + "' , '" + args.get(2) + "',1)");


            System.out.println("You've been registered on : " + conn);
        } catch (SQLException ex1) {
            int code = ex1.getErrorCode();
            if (code != 1062) {
                ex1.printStackTrace();
                return;
            }
            System.out.println("Nom d'utilisateur déjà pris.");
        }

    }

    //TO DO
    /*
    INSERT INTO GroupesMembres(groupe, membre) VALUES (
                        (SELECT groupe_uuid FROM groupes WHERE nom = 1804),
                        (SELECT user_uuid FROM users WHERE pseudo = 'nakiradu77'));

     */
    public void joinGroup(ArrayList<String> args) throws ClassNotFoundException {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("Requête de création d'utilisateur en cours d'execution...");


            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO users (`pseudo`, `password`, `email`) VALUES('" + args.get(0) + "','" + args.get(1) + "' , '" + args.get(2) + "')");


            System.out.println("You've been registered on : " + conn);
        } catch (SQLException ex1) {
            int code = ex1.getErrorCode();
            if (code != 1062) {
                ex1.printStackTrace();
                return;
            }
            System.out.println("Nom d'utilisateur déjà pris.");
        }

    }

    public SocketPerso login(ArrayList<String> args) throws SQLException {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                Statement stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "SELECT `pseudo` " +
                                "FROM users WHERE password ='" + args.get(1) + "' AND pseudo='" + args.get(0) + "'");

                if (!rs.wasNull()) {
                    while (rs.next()) {
                        pseudo = rs.getString("pseudo");
                        //System.out.println(pseudo);
                        socket_client = new SocketPerso(new Socket("127.0.0.1", 5000), pseudo);
                        stmt.executeUpdate("UPDATE users SET isConnected=1 WHERE pseudo='" + args.get(0) + "'");
                    }

                    conn.close();
                }

            } catch (Exception e) {
                if (rs == null) {
                    System.err.println("Erreur d'authenfication ! ");
                    System.err.println("Nom d'utilisateur ou mot de passe incorrect(s) ! ");
                    return null;
                }
                if (socket_client == null) {
                    System.err.println("Erreur de connexion au serveur de chat...");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return socket_client;
    }

    public SocketPerso newLogin(ArrayList<String> args) throws Exception {
        byte[] sha256;
        String str_sha;
        String str_aes;
        sha256 = getSHA(args.get(0));
        str_sha = toHexString(sha256);
        str_aes = AES_Perso.encrypt(args.get(1), str_sha);
        SocketPerso socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                Statement stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "SELECT `password` " +
                                "FROM users WHERE pseudo ='" + str_sha + "'");
                if (!rs.wasNull()) {

                    while (rs.next()) {
                        String passwd = rs.getString("password");
                        if (AES_Perso.decrypt(passwd, str_sha).equals(args.get(1))) {
                            //System.out.println(pseudo);
                            socket_client = new SocketPerso(new Socket("127.0.0.1", 5000), args.get(0));
                            stmt.executeQuery("UPDATE users SET isConnected=1 WHERE pseudo='" + str_sha + "'");
                        }

                    }

                    conn.close();
                }

            } catch (Exception e) {
                if (rs == null) {
                    System.err.println("Erreur d'authenfication ! ");
                    System.err.println("Nom d'utilisateur ou mot de passe incorrect(s) ! ");
                    return null;
                }
                if (socket_client == null) {
                    System.err.println("Erreur de connexion au serveur de chat...");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return socket_client;
    }
}


public class User {


    public String _username;
    public LocalTime _lastConnection;

    User() {
    }
    User(String username) {
        _username = username;
        LocalTime time = LocalTime.now();
        _lastConnection = time;
    }

    public String translateMessage(String message) {
        try {
            assert false;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://google-translate1.p.rapidapi.com/language/translate/v2"))
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("accept-encoding", "application/gzip")
                    .header("x-rapidapi-key", "f3c529b0c4msh16d0759eef9d379p14c09ejsnbdf2ec0a9b7e")
                    .header("x-rapidapi-host", "google-translate1.p.rapidapi.com")
                    .method("POST", HttpRequest.BodyPublishers.ofString("q=" + message + "&source=fr&target=en"))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

            Object obj = JSONValue.parse(response.body());
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject jsonMain = (JSONObject) jsonObject.get("data");

            JSONArray jsonTrans = (JSONArray) jsonMain.get("translations");

            JSONObject translate = (JSONObject) jsonTrans.get(0);

            return (String) translate.get("translatedText");
            //System.out.println(response.body());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Impossible de traduire!";
    }

    public double getWeather() {
        try {
            assert false;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://community-open-weather-map.p.rapidapi.com/weather?q=Amiens%20%2Cfr&lat=0&lon=0&id=2172797&lang=fr&units=metric"))
                    .header("x-rapidapi-key", "8bcb441bf5mshb79ef4191cd9db1p150c6ejsn08a43923b961")
                    .header("x-rapidapi-host", "community-open-weather-map.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            Object obj = JSONValue.parse(response.body());
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject jsonMain = (JSONObject) jsonObject.get("main");

            return (double) jsonMain.get("temp");
            //System.out.println(response.body());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

}


class Groupe {
    public static User _administrator;
    public String _name;
    public ArrayList<HashMap<Socket, User>> groupeUsers;

    public Groupe() {
        _name = "";
        _administrator = new User();
    }

    public Groupe(String name, User administrator, Socket admin_sock) {
        groupeUsers = new ArrayList<>(10);
        HashMap<Socket, User> currentHash = new HashMap<>();
        currentHash.put(admin_sock, administrator);
        _name = name;
        _administrator = administrator;
        groupeUsers.add(currentHash);
    }

    /*
    INSERT INTO groupes  (nom, administrator)  VALUES (1804,5);
     */
    public void createGroup() {

    }
}
