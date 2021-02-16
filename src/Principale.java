import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Principale {


    public static void main(String[] args) throws IOException {

        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        ArrayList<Socket> _clientList = new ArrayList<>();
        String msg;

            msg = commandes.lireEcran();
            if(msg.equals("client")) {
                SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));
                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();

                SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client);

                sender.start();

                /*do {
                    msg = commandes.lireEcran();
                    if (msg.equals("quit")) {
                        socket_client.ecrireSocket(msg);
                        System.exit(0);
                    }else{
                        socket_client.ecrireSocket(msg);
                    }


                } while (true);*/


            }else if(msg.equals("serveur")) {
                Socket_Serveur socket_serveur = new Socket_Serveur(new ServerSocket(5000));
                while (!socket_serveur.getServer().isClosed()) {
                        try{
                            Socket client = socket_serveur.acceptClient();
                            _clientList.add(client);
                            ClientServiceThread cliThread = new ClientServiceThread(client);
                            cliThread.start();
                        }catch(Exception  ex){
                            ex.printStackTrace();
                        }
                        /*else{
                        if (!_clientList.isEmpty()) {
                            String response = socket_serveur.lireSocket(client);
                            if (!response.equals("quit")) {
                                logger.writeLog("Reception: " + response);
                                socket_serveur.ecrireSocket("Serveur> " + response, _clientList);
                            } else {
                                logger.closeLog();
                                System.exit(0);
                            }

                        }

                    }*/

                }

            }
    }
}