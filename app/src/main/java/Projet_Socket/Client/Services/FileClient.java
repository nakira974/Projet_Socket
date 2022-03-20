package Projet_Socket.Client.Services;

import Projet_Socket.Shared.WorkerService;
import Projet_Socket.Utils.File.CloudFile;
import Projet_Socket.Utils.File.Logger;
import Projet_Socket.Utils.InternalCommandsEnum;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Thread de client du serveur de fichier
 */
public final class FileClient extends WorkerService<FileClient> {

    private final Socket socket;
    public ArrayList<CloudFile> groupFiles;
    private final Logger _logger;

    public FileClient(Socket socket, Socket client) {
        super(socket,  "[CLOUD]");
        this.socket = client;
        this._logger = new Logger();

    }

    public void run(){
        try{
            do{
                checkForUpdates();
            }while (this.runState);

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    @Contract(pure = true)
    public Socket getSocket() {
        return socket;
    }

    /**
     * Envoie un message sur le serveur
     * @param texte message à envoyer sur le serveur
     * @throws IOException
     */
    public void writeSocket(String texte) throws IOException {

        var out = new PrintWriter(Objects.requireNonNull(this.socket).getOutputStream());
        out.println(texte);
        out.flush();


    }

    /**
     * Renvoie les messages en provenance du serveur
     * @return message provenant du serveur
     * @throws IOException
     */
    public String readSocket() throws IOException {

        return new BufferedReader(new InputStreamReader(this.socket.getInputStream())).readLine();

    }

    @Nullable
    public HashMap<String, HashMap<CloudFile.FileStateEnum, ArrayList<String>>> checkForUpdates(){
        var result = new HashMap<String, HashMap<CloudFile.FileStateEnum, ArrayList<String>>>();
        try {
            writeSocket(InternalCommandsEnum.FileSynchronisation.Label);
            var json = readSocket();
            var jsonObject = new JSONObject(json);
            var jsonArray = jsonObject.getJSONArray("server_files");
            jsonArray.forEach(object->{

//TODO get changed files and write to FileServer
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }
}
