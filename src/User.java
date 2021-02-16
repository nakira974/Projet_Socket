import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;

class LogUser {

    LogUser(){}

    public void login (ArrayList<String> args) {
        try {
            String url = "jdbc:msql://localhost/serveur_db";
            Connection conn = DriverManager.getConnection(url,"ServerMaster","Master2004$");
            Statement stmt = conn.createStatement();
            ResultSet rs;

            rs = stmt.executeQuery(
                    "SELECT pseudo, dt_last_connection " +
                            "FROM user WHERE password ="+ args.get(1) + " AND user="+ args.get(0));
            while ( rs.next() ) {
                String pseudo = rs.getString("pseudo");
                System.out.println(pseudo);
                User currentUser = new User(pseudo);
                //INSTANCIER CLIENT ICI
            }
            conn.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
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
