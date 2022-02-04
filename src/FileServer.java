import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class FileServer extends Thread {
    public String ServerRootDirectory;
    public Console Console;
    public FileServer(String serverRootDirectory, Console console ) {
        try{
            Console = console;
            ServerRootDirectory = serverRootDirectory;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
