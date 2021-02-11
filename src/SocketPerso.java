import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

class Socket_Serveur {

    private ServerSocket _srvSocket;

    public Socket_Serveur(java.net.ServerSocket socket) {

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

class ClientServiceThread extends Thread {
    Socket client;
    boolean runState = true;
    boolean ServerOn= true;
    public ClientServiceThread() {
        super();
    }

    ClientServiceThread(Socket s) {
        client = s;
    }

    public void run() {
        Logger logger = null;
        try {
            logger = new Logger("src/logger.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        PrintWriter out = null;
        System.out.println(
                "Accepted Client Address - " + client.getInetAddress().getHostName());
        try {
            in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(
                    new OutputStreamWriter(client.getOutputStream()));

            while(runState) {
                String clientCommand = in.readLine();
                if(clientCommand!=null) {
                    System.out.println("Client Says :" + clientCommand);
                    assert logger != null;
                    logger.writeLog(client.getInetAddress().getHostName() + "(" + new Date() + ") : " + clientCommand);
                }

                if(!ServerOn) {
                    System.out.print("Server has already stopped");
                    out.println("Server has already stopped");
                    out.flush();
                    runState = false;
                }
                assert clientCommand != null;
                if(clientCommand.equalsIgnoreCase("quit")) {
                    runState = false;
                    System.out.print("Stopping client thread for client : ");
                    logger.closeLog();
                } else if(clientCommand.equalsIgnoreCase("END")) {
                    runState = false;
                    System.out.print("Stopping client thread for client : ");
                    ServerOn = false;
                    System.exit(0);
                } else {
                    out.println("Server Says : " + clientCommand);
                    out.flush();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert in != null;
                in.close();
                assert out != null;
                out.close();
                client.close();
                assert logger != null;
                logger.closeLog();
                System.out.println("...Stopped");
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
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

    class ThreadServiceCLient extends Thread {
        IOCommandes commandes;

        public void run(SocketPerso socket) throws IOException {
            try{}catch(Exception ex){

            }
            String msg;
            do {
                msg = commandes.lireEcran();
                if (!msg.equals("quit")) {
                    socket.ecrireSocket(msg);
                    commandes.ecrireEcran(socket.lireSocket());
                }
            } while (!msg.equals("quit"));
        }
    }


}