import org.json.simple.JSONArray;
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

    public void createUser(ArrayList<String> args) {
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-serveur.alwaysdata.net/" +
                    "serveur_db?user=serveur&password=Master2004$");

            System.out.println("Requête de création d'utilisateur en cours d'execution...");


            Statement stmt = conn.createStatement();


            rs = stmt.executeQuery(
                    "INSERT INTO users (`pseudo`, `password`, `email`) VALUES('" + args.get(0) + "','" + args.get(1) + "' , '"+ args.get(2) +"')");


            System.out.println("You've been registered on : "+ conn);
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

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

    public String translateMessage(String message){
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

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "Impossible de traduire!";
    }

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

            return (double) jsonMain.get("temp");
            //System.out.println(response.body());

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 0.0;
    }

}


class Groupe {
    public String _name ;
    public static User _administrator;
    public  ArrayList<HashMap<Socket, User> >groupeUsers;
    public Groupe() {
        _name="";
        _administrator= new User();
    }

    public Groupe(String name, User administrator, Socket admin_sock) {
        groupeUsers=new ArrayList<>(10);
        HashMap<Socket, User> currentHash = new HashMap<>();
        currentHash.put(admin_sock, administrator);
        _name=name;
        _administrator=administrator;
        groupeUsers.add(currentHash);
    }
}
