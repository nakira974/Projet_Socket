import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Principale {

    public static void main(String[] args) throws IOException {

        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;
        SocketPerso socket = new SocketPerso(new Socket("grit.esiee-amiens.fr",8599));


        //commandes.readLog("log_client.txt");

        do{
            msg = commandes.lireEcran();
            socket.ecrireSocket(msg);
            commandes.writeLog("log_client.txt", msg);
            if(!msg.equals("quit")) {
                commandes.ecrireEcran(socket.lireSocket());
            }

        }while(!msg.equals("quit"));



        // write your code here
    }
}
