import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class Thread_ClientRecieve extends Thread {
    public void run(SocketPerso socket, BufferedInputStream inputText) throws IOException {
        do{
            PrintWriter out = new PrintWriter(socket.lireSocket());
            out.println(inputText);
            out.flush();
        }while(inputText.toString() != "END");

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

class Thread_ServerSend extends Thread {
    public void run(){
    }
}

class Thread_ServerRecieve extends Thread {
    public void run() {}

}



class Socket_Serveur {
    private ServerSocket _srvSocket;
    //private ArrayList<Socket> _clientList;
    private Socket client_socket;

    public Socket_Serveur(java.net.ServerSocket socket){

        this._srvSocket=socket;
    }

    public Socket acceptClient() throws IOException {

            return _srvSocket.accept();


    }

    public void ecrireSocket(String texte) throws IOException {


        PrintWriter out = new PrintWriter(client_socket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(client_socket.getInputStream())).readLine();

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


}
