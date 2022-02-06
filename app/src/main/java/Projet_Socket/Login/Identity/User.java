package Projet_Socket.Login.Identity;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.util.ArrayList;


/**
 * Utilisateur des services du serveur tcp
 */
public final class User {

    public int Id;
    public String userMail;
    public String _username;
    public LocalTime _lastConnection;
    public ArrayList<Group> Groups;

    User() {
    }

    /**
     * Créer un utilisateur
     * @param username nom de l'utilisateur
     */
    public User(String username) {
        _username = username;
        _lastConnection = LocalTime.now();
        Groups = new ArrayList<>();
    }

    /**
     * Lance une requête HTTP GET vers google translate api sur rapidapi
     * @param message message à traduire en anglais
     * @return message traduit en anglais
     */
    public String translateMessage(String message) {
        try {
            assert false;
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://google-translate1.p.rapidapi.com/language/translate/v2"))
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("accept-encoding", "application/gzip")
                    .header("x-rapidapi-key", "f3c529b0c4msh16d0759eef9d379p14c09ejsnbdf2ec0a9b7e")
                    .header("x-rapidapi-host", "google-translate1.p.rapidapi.com")
                    .method("POST", HttpRequest.BodyPublishers.ofString("q=" + message + "&source=fr&target=en"))
                    .build();
            var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

            var obj = JSONValue.parse(response.body());
            var jsonObject = (JSONObject) obj;

            var jsonMain = (JSONObject) jsonObject.get("data");

            var jsonTrans = (JSONArray) jsonMain.get("translations");

            var translate = (JSONObject) jsonTrans.get(0);

            return (String) translate.get("translatedText");
            //System.out.println(response.body());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Impossible de traduire!";
    }

    /**
     * Renvoie la température à Amiens
     * @return température à Amiens
     */
    public double getWeather() {
        try {
            assert false;
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://community-open-weather-map.p.rapidapi.com/weather?q=Amiens%20%2Cfr&lat=0&lon=0&id=2172797&lang=fr&units=metric"))
                    .header("x-rapidapi-key", "8bcb441bf5mshb79ef4191cd9db1p150c6ejsn08a43923b961")
                    .header("x-rapidapi-host", "community-open-weather-map.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            var obj = JSONValue.parse(response.body());
            var jsonObject = (JSONObject) obj;

            var jsonMain = (JSONObject) jsonObject.get("main");

            return (double) jsonMain.get("temp");
            //System.out.println(response.body());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

}