import java.io.*;

public class Socket {

    private java.net.Socket socket;

    public Socket(java.net.Socket socket){


        this.socket = socket;


    }

    public void ecrireSocket(String texte) throws IOException {


        new BufferedWriter(new PrintWriter(this.socket.getOutputStream()));


    }

    public String lireSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }


}
