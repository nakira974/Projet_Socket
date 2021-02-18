import jdk.internal.access.JavaIOFileDescriptorAccess;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

class LogUser {

    public static User currentUser;

    LogUser() {

    }

    public SocketPerso createUser(ArrayList<String> args) {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-serveur.alwaysdata.net/" +
                    "serveur_db?user=serveur&password=Master2004$");

            System.out.println("Requête de création d'utilisateur en cours d'execution...");


            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO users (`pseudo`, `password`) VALUES('" + args.get(0) + "','" + args.get(1) + "');");

            if (!rs.wasNull()) {

                socket_client = login(args);
            } else {
                return null;
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
        return socket_client;
    }

    public SocketPerso login(ArrayList<String> args) throws SQLException {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-serveur.alwaysdata.net/" +
                    "serveur_db?user=serveur&password=Master2004$")) {
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


    User(){}
    User(String username){
        _username = username;
        LocalTime time = LocalTime.now();
        _lastConnection = time;
    }

    public String _username;
    public LocalTime _lastConnection;


    public double getWeather(){
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

            double temp = (double) jsonMain.get("temp");

            return temp;
            //System.out.println(response.body());

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 0.0;
    }

}


/*class Groupe {
    public String _name ;
    private String _administrator;
    public HashMap<String, Socket> groupeUsers;
    public Groupe() {
        _name="test";
        _administrator= "root";
    }

    public Groupe(String name, String administrator, Socket admin_sock) {
        groupeUsers=new HashMap<String,Socket>();
        _name=name;
        _administrator=administrator;
        groupeUsers.put(administrator, admin_sock);
    }
}*/
