import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Logger {

    private static FileWriter file;

    static {
        try {
            file = new FileWriter("src/logger(" + DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now()) + ").txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final BufferedWriter output = new BufferedWriter(file);

    public static void writeLog(String p_fileData) {
        try {
            // Writes the string to the file
            output.append(p_fileData);
            output.newLine();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static void closeLog() throws IOException {

        output.close();

    }

    /*public void readLog(String p_fileName){

        char[] array = new char[512];

        try {
            // Creates a FileReader
            FileReader file = new FileReader(p_fileName);

            // Creates a BufferedReader
            BufferedReader input = new BufferedReader(file);

            // Reads characters
            input.read(array);
            System.out.println("Data in the file: ");
            System.out.println(array);

            // Closes the reader
            input.close();
        }

        catch(Exception e) {
            e.getStackTrace();
        }
    }*/

}
