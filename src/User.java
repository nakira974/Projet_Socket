import java.net.Socket;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;

class LogUser {

    LogUser(){

    }


    public SocketPerso createUser(ArrayList<String> args){
        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3307/serveur_db?user=ServerMaster&password=Master2004$"))
            {
                System.out.println("connected");
                Statement stmt = conn.createStatement();


                rs = stmt.executeQuery(
                        "INSERT INTO users (pseudo, password) VALUES("+ args.get(0)+","+args.get(1)+");");

                if(!rs.wasNull()){

                    socket_client = login(args);
                }
                else{
                    return null;}
            }
            catch (SQLException ex1){
                ex1.printStackTrace();
            }
        }catch(Exception ex2){
            ex2.printStackTrace();
        }
        return socket_client;
    }




    public SocketPerso login (ArrayList<String> args) throws SQLException {

        SocketPerso socket_client = null;
        ResultSet rs = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3307/serveur_db?user=ServerMaster&password=Master2004$")){
                System.out.println("connected");
                Statement stmt = conn.createStatement();


                rs = stmt.executeQuery(
                        "SELECT pseudo, dt_last_connection " +
                                "FROM users WHERE password ="+ args.get(1) + " AND pseudo="+ args.get(0));

                while ( rs.next() ) {
                    String pseudo = rs.getString("pseudo");
                    System.out.println(pseudo);

                    //INSTANCIER CLIENT ICI
                }
                User currentUser = new User(rs.getString("pseudo"));
                socket_client = new SocketPerso(new Socket("127.0.0.1",5000));
                Socket_Serveur.users.put(currentUser, socket_client.getSocket());
                conn.close();
            }

            //String url = "jdbc:mariadb://localhost:3307/serveur_db";
            //Connection conn = DriverManager.getConnection(url,"ServerMaster","Master2004$");

        } catch (Exception e) {
            System.err.println("Erreur d'authenfication ! ");
            System.err.println("Nom d'utilisateur ou mot de passe incorrect(s) ! ");
            System.err.println(e.getMessage());
        }
        return socket_client;
        }
    }

public class User {

    User(){}
    User(String username){
        _username = username;
        LocalTime time = LocalTime.now();
        _lastConnection = time;
    }

    public String _username;
    public LocalTime _lastConnection;


}
