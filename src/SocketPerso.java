import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



class Socket_Serveur extends Thread {
    private ServerSocket _srvSocket;
    private ArrayList<Socket> _clientList;

    public Socket_Serveur(java.net.ServerSocket socket){

        this._srvSocket=socket;
    }

    public void acceptClient(Socket client){
        try {
            client = _srvSocket.accept();
            _clientList.add(client);
        }catch(Exception ex){}

    }

    public void ecrireSocket(String texte, Socket client) throws IOException {


        PrintWriter out = new PrintWriter(this._srvSocket.getOutputStream());
        out.println(texte);
        out.flush();


    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this._srvSocket.getInputStream())).readLine();

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
