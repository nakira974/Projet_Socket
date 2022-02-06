package Projet_Socket.Login.Identity;

import Projet_Socket.Login.Identity.User;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Group {
    public int Id;
    public User administrator;
    public int administratorId;
    public String name;
    public ArrayList<HashMap<Socket, User>> groupeUsers;

    public Group() {
        name = "";
        administrator = new User();
    }

    public Group(String name, User administrator, Socket admin_sock) {
        administratorId = administrator.Id;
        groupeUsers = new ArrayList<>(10);
        var currentHash = new HashMap<Socket, User>();
        currentHash.put(admin_sock, administrator);
        this.name = name;
        this.administrator = administrator;
        groupeUsers.add(currentHash);
    }

    /*
    INSERT INTO groupes  (nom, administrator)  VALUES (1804,5);
     */
    public void createGroup() {

    }
}
