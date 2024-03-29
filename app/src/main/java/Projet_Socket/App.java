/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Projet_Socket;

import Projet_Socket.Client.ClientTcp;
import Projet_Socket.Login.AES_Perso;
import Projet_Socket.Login.Identity.LogUser;
import Projet_Socket.Login.Identity.User;
import Projet_Socket.Server.ServerClientWorker;
import Projet_Socket.Server.ServerTcp;
import Projet_Socket.Server.Services.FileServer;
import Projet_Socket.Utils.Console;
import Projet_Socket.Utils.File.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Application de lancement client/serveur tcp
 */
public final class App {

    public static final int FactoryPort = 5000;
    public static final String RootDirectory = "C:\\temp\\";
    static String _str;

    /**
     * Convertie une chaine de caractères en md5
     * @param input chaine à convertir
     * @return tableau de bytes de la chaine convertie
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getMd5(@NotNull String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        var md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param hash hexadécimal à convertir en chaine de caratère
     * @return chaine de caractère de l'hexadécimal converti
     */
    @NotNull
    private static String toHexString(@NotNull byte[] hash) {
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
     * Lance la saisie des informations de l'utilisateur
     * @param args informations à saisir par l'utilisateur
     * @param command instance d'une console pour l'affichage
     * @return
     * @throws Exception
     */
    @NotNull
    private static ArrayList<String> register(@NotNull ArrayList<String> args, @NotNull Console command) throws Exception {

        var result = new ArrayList<String>();
        System.out.println("------REGISTER-----");
        var logger = new LogUser();
        System.out.println("Nom d'utilisateur : ");
        _str = command.readKey();
        result.add(_str);
        //ON ENCRYPTE LE NOM USER EN SHA256
        var sha256 = getMd5(_str);
        _str = toHexString(sha256);
        args.add(_str);
        //FIN ENCRYPTAGE
        System.out.println("Mot de passe : ");
        _str = command.readKey();
        result.add(_str);
        //ON ENCRYPTE LE MDP EN AES256(mdp, pseudo_en_sha256)
        _str = AES_Perso.encrypt(_str, toHexString(sha256));
        args.add(_str);
        //FIN AES 256
        System.out.println("email : ");
        _str = command.readKey();

        result.add(_str);
        args.add(_str);
        logger.createUser(args);

        return result;
    }


    public static void main(@Nullable String[] args) throws Exception {

        var userMail = "";
        ClientTcp socket_client = null;
        var console = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        var msg = "";

        var userInfo = new ArrayList<String>();


        msg = console.readKey();
        if (msg.equals("client")) {
            //FAIS SI LE LOGIN EST OK
            //ClientTcp socket_client = new ClientTcp(new Socket("127.0.0.1",5000));

            System.out.println("-(1) S'authentifier || S'enregistrer (2)-");
            msg = console.readKey();
            if (msg.equals("1")) {
                //LOGIN
                System.out.println("------LOGIN-----");
                System.out.println("Nom d'utilisateur : ");
                msg = console.readKey();
                userInfo.add(msg);
                System.out.println("Mot de passe : ");
                msg = console.readKey();
                userInfo.add(msg);
            } else if (msg.equals("2")) {
                userInfo = register(userInfo, console);
            }
            try {
                var log = new LogUser();
                //do{
                var hashtable = log.newLogin(userInfo);
                socket_client = hashtable.keys().nextElement();
                userMail = hashtable.get(socket_client);

                //}while(socket_client != null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //FIN LOGIN

            if (socket_client != null) {
                var receiver = new ClientTcp.Thread_Client_Receive(socket_client);

                receiver.start();


                var sender = new ClientTcp.Thread_Client_Send(socket_client, userMail);

                sender.start();
            } else {
                console.writeLine("L'utilisateur n'existe pas!");
            }


        } else if (msg.equals("serveur")) {


            var clientUsername = "";
            var clientMail = "";
            ServerTcp.createServer(new ServerSocket(FactoryPort));
            //Si le msg contient %file% alors on lance un serveur de ficher en plus des autres services
            var fileServerThread = msg.toUpperCase().contains("FILE") ?
                    new FileServer(RootDirectory, console)
                    : null;

            ServerTcp.getGroups();
            var log = "\"Server has started at : [" + ServerTcp._srvSocket.getInetAddress().getHostAddress() + "]::" + FactoryPort + "\"";
            var logger = new Logger();
            logger.writeLog(log, -666, "[INFO]");
            System.out.printf("[INFO] Server has started at : %s port :%d%n", ServerTcp._srvSocket.getInetAddress().getHostAddress(), FactoryPort);
            while (!ServerTcp.getServer().isClosed()) {
                try {
                    while (ServerTcp.getNbSocket() < ServerTcp.getMaxConnection()) {
                        var client = ServerTcp.acceptClient();
                        if (client != null) {
                            try {

                                var hello_request = ServerTcp.readClientStream(client);
                                var text = hello_request.split(",");
                                clientUsername = text[0];
                                clientMail = text[1];
                                var currentUser = new HashMap<Socket, User>();
                                currentUser.put(client, new User(clientUsername));
                                ServerTcp.users.add(currentUser);
                                var userId = ServerTcp.getUserId(clientMail);
                                currentUser.get(client).Id = userId;
                                currentUser.get(client).userMail = clientMail;
                                currentUser.get(client).Groups = ServerTcp.getUserGroups(userId);
                                ServerTcp.groupes.forEach(groupe -> currentUser.get(client).Groups.forEach(usrGroup -> {
                                    if (usrGroup.Id == groupe.Id)
                                        groupe.groupeUsers.add(currentUser);
                                }));
                                log = "{\n\t\t\"user\" : " + currentUser.get(client).Id + ",\n" +
                                        "\t\t\"email\" :\"" + currentUser.get(client).userMail + "\",\n" +
                                        "\t\t\"address\" : \"[" + client.getInetAddress().getHostAddress() + "]" + "\",\n" +
                                        "\t\t\"status\" : \"CONNECTED\"}";
                                logger.writeLog(log, -666, "[INFO]");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            var cliThread = new ServerClientWorker(client);


                            cliThread.start();

                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }
    }
}



