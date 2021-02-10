import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Principale {

    public static void main(String[] args) throws IOException {

        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;


        //commandes.readLog("log_client.txt");
        if(commandes.lireEcran().equals("client")) {
            SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",25565));


            do {
                msg = commandes.lireEcran();
                if (!msg.equals("quit")) {
                    socket_client.ecrireSocket(msg);
                    commandes.writeLog("src/log_client.txt", msg);
                    commandes.readLog("src/log_client.txt");
                    commandes.ecrireEcran(socket_client.lireSocket());
                }

            } while (!msg.equals("quit"));


        }else if(commandes.lireEcran().equals("serveur")){
            Socket_Serveur socket_serveur = new Socket_Serveur(new ServerSocket(25565));

            do {
                msg = commandes.lireEcran();
                if (!msg.equals("quit")) {
                    socket_serveur.acceptClient();
                    socket_serveur.lireSocket();
                    socket_serveur.ecrireSocket("echo> ");
                }

            } while (!msg.equals("quit"));

        }



        // write your code here
    }
}
