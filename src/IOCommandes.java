import java.io.*;

public class IOCommandes {

    private BufferedReader _lectureEcran;

    private PrintStream _ecritureEcran;

    public IOCommandes(BufferedReader lectureEcran, PrintStream ecritureEcran){


        this._lectureEcran = lectureEcran;
        this._ecritureEcran = ecritureEcran;


    }

    public void ecrireEcran(String texte){
        try {
           _ecritureEcran.println(texte);
        }catch(Exception ex){
            writeLog("log_client.txt", ex.getMessage());
        }
    }

    public String lireEcran() {

        String res =null;
        try{
            res= _lectureEcran.readLine();
        }catch(IOException ex){
            writeLog("log_client.txt", ex.getMessage());
        }
        return res;
    }

    public void writeLog(String p_fileName, String p_fileData) {
        try {
            // Creates a FileWriter
            FileWriter file = new FileWriter(p_fileName);

            // Creates a BufferedWriter
            BufferedWriter output = new BufferedWriter(file);

            // Writes the string to the file
            output.write(p_fileData);

            // Closes the writer
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void readLog(String p_fileName){

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
    }

}
