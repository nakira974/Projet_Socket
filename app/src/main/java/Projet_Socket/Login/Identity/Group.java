package Projet_Socket.Login.Identity;

import Projet_Socket.Utils.File.CloudFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Groupe de diffusion sur le serveur tcp
 */
public final class Group {
    public int Id;
    public User administrator;
    public int administratorId;
    public String name;
    public ArrayList<HashMap<Socket, User>> groupeUsers;
    public HashMap<CloudFile.FileStateEnum, ArrayList<String>> Files;

    public Group() {
        name = "";
        administrator = new User();
        Files = new HashMap<>();
    }

    /**
     * Créer un groupe
     * @param name nom du groupe
     * @param administrator utilisateur ayant créé le groupe
     * @param admin_sock socket de l'administrateur
     */
    public Group(@NotNull String name, @NotNull User administrator, @NotNull Socket admin_sock) {
        administratorId = administrator.Id;
        groupeUsers = new ArrayList<>(10);
        var currentHash = new HashMap<Socket, User>();
        currentHash.put(admin_sock, administrator);
        this.name = name;
        this.administrator = administrator;
        groupeUsers.add(currentHash);
        Files = new HashMap<>();
    }
    /**
     * Renvoie la liste des fichiers d'un espace cloud d'un groupe
     * @param path chemin du dossier
     * @return liste des fichiers d'un espace cloud de groupe
     */
    @NotNull
    private ArrayList<String> checkGroupFiles(@NotNull String path) {
        var result = new ArrayList<String>();
        //Creating a File object for directory
        var directoryPath = new File(path);
        var textFilefilter = new FileFilter() {
            public boolean accept(@NotNull File file) {
                return file.isFile();
            }
        };
        //List of all the text files
        var filesList = directoryPath.listFiles(textFilefilter);
        System.out.println("List of the text files in the specified directory:");

        for (var file : Objects.requireNonNull(filesList)) {
            result.add(file.getAbsolutePath());
            System.out.println("File name: " + file.getName());
            System.out.println("File path: " + file.getAbsolutePath());
            System.out.println("Size :" + file.getTotalSpace());
            System.out.println(" ");
        }
        return result;
    }

}
