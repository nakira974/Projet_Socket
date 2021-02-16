import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Principale {


    public static void main(String[] args) throws IOException {

        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

            msg = commandes.lireEcran();
            if(msg.equals("client")) {
                SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));
                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();

                SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client);

                sender.start();


            }else if(msg.equals("serveur")) {
                Socket_Serveur socket_serveur = new Socket_Serveur(new ServerSocket(5000));
                while (!socket_serveur.getServer().isClosed()) {
                        try{
                            Socket client = socket_serveur.acceptClient();
                            ClientServiceThread cliThread = new ClientServiceThread(client);
                            cliThread.start();
                        }catch(Exception  ex){
                            ex.printStackTrace();
                        }

                }

            }
    }
}