import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Principale {


    public static void main(String[] args) throws IOException {

        SocketPerso socket_client = null;
        IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
        String msg;

        ArrayList<String> userInfo = new ArrayList<>();
        ArrayList<Socket> _clientList = new ArrayList<>();



            msg = commandes.lireEcran();
            if(msg.equals("client")) {
                //FAIS SI LE LOGIN EST OK
                //SocketPerso socket_client = new SocketPerso(new Socket("127.0.0.1",5000));

                //LOGIN
                System.out.println("Nom d'utilisateur : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                System.out.println("Mot de passe : ");
                msg = commandes.lireEcran();
                userInfo.add(msg);
                try{
                    LogUser log= new LogUser();
                    do{
                        socket_client = log.login(userInfo);

                    }while(socket_client != null);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                //FIN LOGIN

                SocketPerso.Thread_Client_Receive receiver = new SocketPerso.Thread_Client_Receive(socket_client);

                receiver.start();

                SocketPerso.Thread_Client_Send sender = new SocketPerso.Thread_Client_Send(socket_client);

                sender.start();


            }else if(msg.equals("serveur")) {
                Socket_Serveur socket_serveur = new Socket_Serveur(new ServerSocket(5000));
                while (!socket_serveur.getServer().isClosed()) {
                        try{
                            Socket client = socket_serveur.acceptClient();
                            ClientServiceThread cliThread = new ClientServiceThread(client, socket_serveur);
                            cliThread.start();
                        }catch(Exception  ex){
                            ex.printStackTrace();
                        }

                }

            }
    }
}