import java.sql.*;

class LogUser {


    public static void login (String[] args) {
        try {
            String url = "jdbc:msql://localhost/serveur_db";
            Connection conn = DriverManager.getConnection(url,"ServerMaster","Master2004$");
            Statement stmt = conn.createStatement();
            ResultSet rs;

            rs = stmt.executeQuery(
                    "SELECT pseudo, dt_last_connection " +
                            "FROM user WHERE password ="+args[1]+ " AND user="+args[0]);
            while ( rs.next() ) {
                String lastName = rs.getString("pseudo");
                System.out.println(lastName);
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
    public String username;
    public Date lastConnection;
}
