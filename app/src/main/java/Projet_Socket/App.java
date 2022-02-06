/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Projet_Socket;
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
import java.util.Arrays;
import java.util.HashMap;

public class App {

    public static final int FactoryPort = 5000;
    public static final String RootDirectory = "C:\\temp\\";
    static String _str;

    private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
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

    private static ArrayList<String> register(ArrayList<String> args, Console command) throws Exception {
        byte[] sha256;

        ArrayList<String> result = new ArrayList<>();
        System.out.println("------REGISTER-----");
        LogUser logger = new LogUser();
        System.out.println("Nom d'utilisateur : ");
        _str = command.readKey();
        result.add(_str);
        //ON ENCRYPTE LE NOM USER EN SHA256
        sha256 = getSHA(_str);
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


    public static void main(String[] args) throws Exception {

        String userMail = "";
        SocketPerso socket_client = null;
        Console console = new Console(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

        ArrayList<String> userInfo = new ArrayList<>();


        msg = console.readKey();
        if (msg.equals("client")) {
            //FAIS SI LE LOGIN EST OK
            //SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));

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
                LogUser log = new LogUser();
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
                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();


                SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client, userMail);

                sender.start();
            } else {
                console.writeLine("L'utilisateur n'existe pas!");
            }


        } else if (msg.equals("serveur")) {


            String clientUsername = null;
            String clientMail = null;
            Socket_Serveur.createServer(new ServerSocket(FactoryPort));
            //Si le msg contient %file% alors on lance un serveur de ficher en plus des autres services
            FileServer fileServerThread = msg.toUpperCase().contains("FILE") ?
                    new FileServer(RootDirectory ,console)
                    : null;

            Socket_Serveur.getGroups();
            var log = "Server has started at : "+Socket_Serveur._srvSocket.getInetAddress().getHostAddress()+ " port :"+FactoryPort;
            var logger = new Logger();
            logger.writeLog(log,-666,"[INFO]");
            System.out.printf("[INFO] Server has started at : %s port :%d%n", Socket_Serveur._srvSocket.getInetAddress().getHostAddress(), FactoryPort);
            while (!Socket_Serveur.getServer().isClosed()) {
                try {
                    Socket client;
                    while (Socket_Serveur.getNbSocket() < Socket_Serveur.getMaxConnection()) {
                        client = Socket_Serveur.acceptClient();
                        if (client != null) {
                            try {

                                String hello_request = Socket_Serveur.readClientStream(client);
                                final User[] current_usr = {null};
                                String[] text = hello_request.split(",");
                                clientUsername = text[0];
                                clientMail = text[1];
                                HashMap<Socket, User> currentUser = new HashMap<>();
                                currentUser.put(client, new User(clientUsername));
                                Socket_Serveur.users.add(currentUser);
                                int userId = Socket_Serveur.getUserId(clientMail);
                                currentUser.get(client).Id = userId;
                                currentUser.get(client).userMail = clientMail;
                                currentUser.get(client).Groups = Socket_Serveur.getUserGroups(userId);
                                Socket finalClient = client;
                                Socket_Serveur.groupes.forEach(groupe -> {
                                    currentUser.get(finalClient).Groups.forEach(usrGroup ->{
                                        if(usrGroup.Id == groupe.Id)
                                            groupe.groupeUsers.add(currentUser);
                                    } );

                                });
                                log = "user : "+currentUser.get(client).Id+
                                        ", email :"+currentUser.get(client).userMail+
                                        " at address"+client.getInetAddress().getHostAddress()+" is connected";
                                logger.writeLog(log,-666,"[INFO]");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ClientServiceThread cliThread = new ClientServiceThread(client);


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



