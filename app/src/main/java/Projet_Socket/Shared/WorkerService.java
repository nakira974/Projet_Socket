package Projet_Socket.Shared;

import Projet_Socket.Server.ServerTcp;
import Projet_Socket.Utils.File.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Worker service est un service qui tourne et effectue une tâche de fond avec un client
 */
public abstract class WorkerService<T> extends Thread implements IWorkerService<T> {
    protected Logger logger;
    protected Socket client;
    protected boolean runState;
    protected String workerType;


    protected WorkerService(Socket p_client, String p_workerType){
        workerType = p_workerType;
        client = p_client;
        logger = new Logger();
    }

    /**
     * Extinction du serveur
     *
     * @throws IOException
     */
    @Override
    public void serverStop() throws IOException {
        System.out.print("Server has already stopped");
        ServerTcp.writeSocket("Server has already stopped", client);
        runState = false;
    }

    /**
     * Fermeture de la connexion avec le client
     *
     * @throws NoSuchAlgorithmException
     */
    @Override
    public void clientExit() throws NoSuchAlgorithmException {
        runState = false;
        ServerTcp.sockets.remove(client);
        System.out.print("Stopping client thread for client :`\n ");
        for (var i = 0; i < ServerTcp.users.size(); i++) {
            //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
            if (ServerTcp.users.get(i).containsKey(client)) {
                ServerTcp.quit();
                System.out.println("[CLIENT EXIT] Client : " + ServerTcp.users.get(i).toString()
                        + " Disconnected");
            }
        }

        //SI LE SOCKET EST TROUVE DANS LES KEYS DES HASMAP DU ARRAYLIST
        for (var i = 0; i < ServerTcp.users.size(); i++) {
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

    /**
     * Distribue un message dans un groupe donné
     *
     * @param clientCommand commande du client
     */
    @Override
    public void sendGroup(@NotNull String clientCommand) {
        var text = clientCommand.split(":");
        var groupe = text[0].replace("/G", "");
        var msg = text[1];
        final String[] sender = {null};

        for (var current_grp : ServerTcp.groupes) {
            if (current_grp.name.equals(groupe)) {
                ServerTcp.users //stream out of arraylist
                        .forEach(map -> map.entrySet().stream()
                                .filter(entry1 -> entry1.getKey().equals(client))
                                .forEach(username -> sender[0] = String.valueOf(username.getValue()._username)));
                current_grp.groupeUsers //stream out of arraylist
                        .forEach(map -> map.forEach((key, value) -> {
                            try {
                                ServerTcp.writeSocket("[" + current_grp.name + "] " + Arrays.toString(sender) + " : " + msg, key);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
            }
        }
    }

    /**
     * Transforme une chaine de caractères en md5
     *
     * @param input contenu à transformer
     * @return md5 de la chaine passée en paramètres
     * @throws NoSuchAlgorithmException
     */
    @Override
    public byte[] getMd5(@NotNull String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        var md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converti un byte array en string
     *
     * @param hash tableau de byte à convertir en string
     * @return
     */
    @NotNull
    @Override
    public String toHexString(@NotNull byte[] hash) {
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

    /**
     * Déconnecte l'utilisateur en base de données
     *
     * @throws NoSuchAlgorithmException
     */
    @Override
    public void setUserDown() throws NoSuchAlgorithmException {
        final String[] currentUser = {null};

        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(username -> currentUser[0] = username.getValue()._username));
        var sha256 = getMd5(currentUser[0]);
        currentUser[0] = toHexString(sha256);
        var result = currentUser[0];

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] Exiting users...");


            var stmt = conn.createStatement();


            stmt.executeUpdate("UPDATE users SET isConnected=0 WHERE pseudo='" + result + "'");

            System.out.println("[SQL] User has been disconnected on : " + conn);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

    }

    /**
     * Distribue le message privé à un utilisateur donné
     *
     * @param clientCommand contenu la commande client
     */
    @Override
    public void sendPrivate(@NotNull String clientCommand) {
        var text = clientCommand.split(":");
        var destination = text[0].replace("/@", "");
        var msg = text[1];
        final String[] sender = {null};
        privateSend(sender, destination, msg);
    }

    /**
     * Distribue le message privé d'un utilisateur à un autre
     * @param sender envoyeur
     * @param destination destinataire
     * @param msg contenu du message
     */
    @Override
    public void privateSend(@NotNull String[] sender, @NotNull String destination, @NotNull String msg) {
        ServerTcp.users //stream out of arraylist
                .forEach(map -> map.entrySet().stream()
                        .filter(entry1 -> entry1.getKey().equals(client))
                        .forEach(dest -> sender[0] = String.valueOf(dest.getValue()._username)));
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

    /**
     * Déconnecte tous les utilisateurs en base de données
     */
    @Override
    public void setUsersDown() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            var conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123");

            System.out.println("[SQL] Exiting users...");


            var stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE users SET isConnected= 0 where isConnected =1");

            System.out.println("[SQL] User has been disconnected from : " + conn);
        } catch (SQLException | ClassNotFoundException ex1) {
            ex1.printStackTrace();
        }

    }

    @Override
    public synchronized void start() {
        var message = workerType+ " Worker Service Start ... ";
        System.out.println(message);
        logger.writeLog("\""+message+"\"", -666, workerType);
        super.start();
    }

}
