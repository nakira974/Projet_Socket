import java.io.*;

public class Socket {

    private java.net.Socket socket;

    public Socket(java.net.Socket socket){


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
