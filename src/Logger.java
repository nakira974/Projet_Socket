import java.io.*;

public class Logger {

    private FileWriter file;
    private BufferedWriter output;

    public Logger(String p_fileName) throws IOException {

        // Creates a FileWriter
        this.file = new FileWriter(p_fileName);

        // Creates a BufferedWriter
        this.output = new BufferedWriter(this.file);

    }

    public void writeLog(String p_fileData) {
        try {
            // Writes the string to the file
            output.append(p_fileData);
            output.newLine();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void closeLog() throws IOException {

        this.output.close();

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
