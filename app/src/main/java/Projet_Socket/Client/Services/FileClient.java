package Projet_Socket.Client.Services;

import Projet_Socket.Server.ServerTcp;
import Projet_Socket.Shared.WorkerService;
import Projet_Socket.Utils.File.Logger;

import java.net.Socket;

/**
 * Thread de client du serveur de fichier
 */
public final class FileClient extends WorkerService {

    private final Logger _logger;

    public FileClient(Socket socket) {
        super(socket,  "[CLOUD]");
        this._logger = new Logger();
    }

    public void run(){
        do{

        }while (this.runState);
    }

}
