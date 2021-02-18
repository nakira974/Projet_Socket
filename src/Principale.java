/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class Principale {


    public static void main(String[] args) throws IOException {

        SocketPerso socket_client = null;
        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

        ArrayList<String> userInfo = new ArrayList<>();


        msg = commandes.lireEcran();
        if (msg.equals("client")) {
            //FAIS SI LE LOGIN EST OK
            //SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));
            System.out.println(" (1) Se connecter || (2) S'enregistrer \n");
            msg = commandes.lireEcran();

            //LOGIN
            if (msg.equals("1")){
                System.out.println("Nom d'utilisateur : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                System.out.println("Mot de passe : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                try {
                    LogUser log = new LogUser();
                    //do{
                    socket_client = log.login(userInfo);

                    //}while(socket_client != null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //CREATE USER
            }else if(msg.equals("2")){
                System.out.println("Nom d'utilisateur : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                System.out.println("Mot de passe : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                System.out.println("Adresse email : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                try {
                    LogUser log = new LogUser();
                    //do{
                    log.createUser(userInfo);
                    System.out.println("Utilisateur : "+userInfo.get(0)+" enregistré dans la base.\n");
                    System.out.println("[Auto-login]");
                    for(int i=0; i<3 ;i ++){
                        sleep(500);
                        System.out.println("....");
                    }

                    socket_client = log.login(userInfo);

                    //}while(socket_client != null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //FIN LOGIN

            if (socket_client != null) {
                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();


                SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client);

                sender.start();
            } else {
                commandes.ecrireEcran("L'utilisateur n'existe pas!");
            }


        } else if (msg.equals("serveur")) {
            String clientUsername = null;
            Socket_Serveur.createServer(new ServerSocket(5000));
            while (!Socket_Serveur.getServer().isClosed()) {
                try {
                    Socket client;
                    while (Socket_Serveur.getNbSocket() < Socket_Serveur.getMaxConnection()) {
                        client = Socket_Serveur.acceptClient();
                        if (client != null) {
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

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }
    }
}


