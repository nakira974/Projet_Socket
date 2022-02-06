package Projet_Socket.Login.Identity;

import Projet_Socket.Client.ClientTcp;
import Projet_Socket.Login.AES_Perso;

import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class LogUser {

    public static User currentUser;

    public LogUser() {

    }

    private static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        var number = new BigInteger(1, hash);

        // Convert message digest into hex value
        var hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }


    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        var md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public void createUser(ArrayList<String> args) throws ClassNotFoundException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("Requête de création d'utilisateur en cours d'execution...");


            var stmt = conn.createStatement();


            var rs = stmt.executeQuery(
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


    public ClientTcp login(ArrayList<String> args) throws SQLException {
        ClientTcp socket_client = null;
        ResultSet rs = null;
        var pseudo = "";
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
                        socket_client = new ClientTcp(new Socket("127.0.0.1", 5000), pseudo);
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

    public Hashtable<ClientTcp, String> newLogin(ArrayList<String> args) throws Exception {
        var result = new Hashtable<ClientTcp, String>();
        var email = "";
        var sha256 = getSHA(args.get(0));
        var str_sha = toHexString(sha256);
        var str_aes = AES_Perso.encrypt(args.get(1), str_sha);
        ClientTcp socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                var stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "SELECT `password` ,`email` " +
                                "FROM users WHERE pseudo ='" + str_sha + "'");
                if (!rs.wasNull()) {

                    while (rs.next()) {
                        var passwd = rs.getString("password");
                        if (AES_Perso.decrypt(passwd, str_sha).equals(args.get(1))) {
                            //System.out.println(pseudo);
                            socket_client = new ClientTcp(new Socket("127.0.0.1", 5000), args.get(0));
                            stmt.executeQuery("UPDATE users SET isConnected=1 WHERE pseudo='" + str_sha + "'");
                            email = rs.getString("email");
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
        result.put(socket_client, email);
        return result;
    }
}

