import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class Principale {

    private static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("MD5");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    static String _str;

    private static void register(ArrayList<String> args, IOCommandes command) throws Exception {
        byte[] sha256;
        System.out.println("------REGISTER-----");
        LogUser logger = new LogUser();
        System.out.println("Nom d'utilisateur : ");
        _str = command.lireEcran();
        //ON ENCRYPTE LE NOM USER EN SHA256
        sha256=getSHA(_str);
        _str=toHexString(sha256);
        args.add(_str);
        //FIN ENCRYPTAGE
        System.out.println("Mot de passe : ");
        _str= command.lireEcran();
        //ON ENCRYPTE LE MDP EN AES256(mdp, pseudo_en_sha256)
        _str=AES_Perso.encrypt(_str , toHexString(sha256));
        args.add(_str);
        //FIN AES 256
        System.out.println("email : ");
        _str = command.lireEcran();
        args.add(_str);
        logger.createUser(args);
    }



    public static void main(String[] args) throws Exception {

        SocketPerso socket_client = null;
        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

        ArrayList<String> userInfo = new ArrayList<>();


            msg = commandes.lireEcran();
            if(msg.equals("client")) {
                //FAIS SI LE LOGIN EST OK
                //SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));

                System.out.println("-(1) S'authentifier || S'enregistrer (2)-");
                msg = commandes.lireEcran();
                if(msg.equals("2")){
                   register(userInfo, commandes);
                }


                //LOGIN
                System.out.println("------LOGIN-----");
                System.out.println("Nom d'utilisateur : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                System.out.println("Mot de passe : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                try{
                    LogUser log= new LogUser();
                    //do{
                        socket_client = log.newLogin(userInfo);

                    //}while(socket_client != null);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                //FIN LOGIN

                if(socket_client!=null) {
                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();


                    SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client);

                    sender.start();
                }else{
                    commandes.ecrireEcran("L'utilisateur n'existe pas!");
                }


            }else if(msg.equals("serveur")) {
                String clientUsername = null;
                Socket_Serveur.createServer(new ServerSocket(5000));
                while (!Socket_Serveur.getServer().isClosed()) {
                    try{
                        Socket client;
                        while(Socket_Serveur.getNbSocket()<Socket_Serveur.getMaxConnection()){
                            client = Socket_Serveur.acceptClient();
                            if(client!=null) {
                                try {
                                    clientUsername = Socket_Serveur.lireSocket(client);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                HashMap<Socket, User> currentUser = new HashMap<>();
                                currentUser.put(client, new User(clientUsername));
                                Socket_Serveur.users.add(currentUser);
                                ClientServiceThread cliThread = new ClientServiceThread(client);
                                cliThread.start();

                            }
                        }

                    }catch(Exception  ex){
                        ex.printStackTrace();
                    }

                }

            }
    }
}


