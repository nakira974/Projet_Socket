import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Principale {

    public static void main(String[] args) throws IOException {

        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

        do{
            msg = commandes.lireEcran();
            if(!msg.equals("quit")) {
                commandes.ecrireEcran(msg);
            }

        }while(!msg.equals("quit"));



        // write your code here
    }
}
