import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class Socket_Serveur {
    private ServerSocket _srvSocket;

    public Socket_Serveur(java.net.ServerSocket socket){

        this._srvSocket=socket;
    }

    public Socket acceptClient() throws IOException {

            return _srvSocket.accept();


    }


    public ServerSocket getServer(){
        return this._srvSocket;
    }

    public void ecrireSocket(String texte, ArrayList<Socket> clients) throws IOException {

        for (Socket socket : clients) {

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(texte);
            out.flush();
        }


    }

    public String lireSocket(Socket client) throws IOException {

        return new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();

    }
    }


public class SocketPerso {

    private Socket socket;

    public SocketPerso(java.net.Socket socket){


        this.socket = socket;


    }

    public void ecrireSocket(String texte) throws IOException {


        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    class Thread_ClientReceive extends Thread {
        public void run(Socket socket, BufferedInputStream inputText) throws IOException {
            do{
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(inputText);
                out.flush();
            }while(!inputText.toString().equals("END"));

        }
    }

    class Thread_ClientSend extends Thread {
        public void run(SocketPerso socket) throws IOException {
            IOCommandes commandes = new IOCommandes(new BufferedReader(new InputStreamReader(System.in)), System.out);
            String msg;
            do {
                msg = commandes.lireEcran();
                if (!msg.equals("quit")) {
                    socket.ecrireSocket(msg);
                    commandes.writeLog("src/log_client.txt", msg);
                    commandes.readLog("src/log_client.txt");
                    commandes.ecrireEcran(socket.lireSocket());
                }
            } while (!msg.equals("quit"));
        }
    }


}
