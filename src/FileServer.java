import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.Arrays;

public class FileServer extends Thread {
    private class FileSubscriber{

    }
    public String ServerRootDirectory;
    public Console Console;
    private boolean _isServerRunning;
    public FileServer(String serverRootDirectory, Console console) {
        _isServerRunning = true;
        try {
            Console = console;
            ServerRootDirectory = serverRootDirectory;

            do {
                try {

                }catch (Exception e){
                    e.printStackTrace();
                    Socket_Serveur.writeSocket(e.getMessage(),Socket_Serveur.sockets);
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

    public void CreateCloudSubscription(int currentGroupeId) throws Exception {
        String path = "C:\\temp\\"+currentGroupeId+"\\";
        //Creating a File object
        File file = new File(path);
        //Creating the directory
        boolean bool = file.mkdir();
        int groupId=0;
        SocketPerso socket_client = null;
        ResultSet rs = null;
        String pseudo = null;
        Statement stmt = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://mysql-wizle.alwaysdata.net/" +
                    "wizle_test?user=wizle&password=projettest123")) {
                //System.out.println("connected");
                stmt = conn.createStatement();

                rs = stmt.executeQuery(
                        "INSERT INTO tcpFileSharing(groupId, rootPath) VALUES ('" + groupId + "','" + path + "');");

            } catch (Exception e) {
                if (rs == null) {
                    System.err.println("Erreur de création d'un groupe de partage ! ");
                    System.err.println("Erreur :\n" + (stmt != null ? stmt.getWarnings().getSQLState() : null));
                }
                System.err.println("Erreur de connexion au serveur de fichier...");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
