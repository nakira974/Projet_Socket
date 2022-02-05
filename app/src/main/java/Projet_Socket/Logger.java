package Projet_Socket;/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Logger {


    public BufferedWriter output;
    private FileWriter file;

    public Logger() {

        {
            try {
                String currentDirectoryPath = FileSystems.getDefault().
                        getPath("").
                        toAbsolutePath().
                        toString();
                System.out.println(currentDirectoryPath);
                var currentDate =  DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now());
                this.file = new FileWriter(currentDirectoryPath+"\\app\\src\\main\\java\\Projet_Socket\\logger(" +currentDate + ").txt");
                output = new BufferedWriter(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void writeLog(String p_fileData) {
        try {
            // Writes the string to the file
            this.output.append(p_fileData);
            this.output.newLine();
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
