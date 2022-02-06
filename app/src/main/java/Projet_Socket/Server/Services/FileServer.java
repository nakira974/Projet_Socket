package Projet_Socket.Server.Services;

import Projet_Socket.Server.ServerTcp;
import Projet_Socket.Utils.Console;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class FileServer extends Thread {
    public String ServerRootDirectory;
    public Projet_Socket.Utils.Console Console;
    private boolean _isServerRunning;
    public FileServer(String serverRootDirectory, Console console) {
        _isServerRunning = true;
        try {
            Console = console;
            ServerRootDirectory = serverRootDirectory;

            do {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    ServerTcp.writeSocket(e.getMessage(), ServerTcp.sockets);
                }
                sleep(5000);
            } while (_isServerRunning);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Contract(pure = true)
    private static @NotNull
    String getRootPathFactory() {
        return "C:\\temp\\CdProject";
    }

    private static File[] getRootFiles(String location) {
        //Creating a File object for directory
        File directoryPath = new File(location);
        //List of all files and directories
        return directoryPath.listFiles();
    }

    private static void checkFilesFromServer() {
        File[] remoteFiles = null;
        File[] missingFiles = new File[]{};
        int count = 0;
        //TODO récupérer la liste des fichiers sur le serveur

        var localFiles = getRootFiles(getRootPathFactory());
        for (var localFile : localFiles) {
            for (var remoteFile : remoteFiles) {
                count = !localFile.getPath().equals(remoteFile.getPath()) ? count++ : count;
                Arrays.stream(missingFiles).toList().add(localFile);
            }
        }

        if (missingFiles.length == 0) return;
        for (var i = 0; i < count; i++) {
            //TODO télécharger les fichiers manquants

        }

    }

    public void closeFileProcess() {
        _isServerRunning = false;
    }

    private class FileSubscriber {

    }


}
