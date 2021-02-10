import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
