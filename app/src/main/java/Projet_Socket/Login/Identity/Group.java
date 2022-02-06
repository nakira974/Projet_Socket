package Projet_Socket.Login.Identity;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Groupe de diffusion sur le serveur tcp
 */
public final class Group {
    public int Id;
    public User administrator;
    public int administratorId;
    public String name;
    public ArrayList<HashMap<Socket, User>> groupeUsers;

    public Group() {
        name = "";
        administrator = new User();
    }

    /**
     * Créer un groupe
     * @param name nom du groupe
     * @param administrator utilisateur ayant créé le groupe
     * @param admin_sock socket de l'administrateur
     */
    public Group(String name, @NotNull User administrator, Socket admin_sock) {
        administratorId = administrator.Id;
        groupeUsers = new ArrayList<>(10);
        var currentHash = new HashMap<Socket, User>();
        currentHash.put(admin_sock, administrator);
        this.name = name;
        this.administrator = administrator;
        groupeUsers.add(currentHash);
    }

}
