package Projet_Socket.Utils.File;/*
 --- creators : nakira974 && Weefle  ----
 */

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Semaphore;


/**
 * Utilitaire de logs
 */
public class Logger {


    public BufferedWriter output;

    /**
     * Créer un logger qui créer s'il n'existe pas le fichier de journalisation
     */
    public Logger() {

        {
            try {
                var currentFile = new File(getLogFileName());
                if (currentFile.exists()) return;
                var buffer = new BufferedWriter(new FileWriter(getLogFileName(), true));
                var file = new FileWriter(getLogFileName());
                output = new BufferedWriter(file);
                buffer.newLine();
                buffer.append("{\"Logs\": [\n{}\n]\n}");
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Renvoie le nom du fichier de log du jour
     * @return nom du fichier
     */
    public String getLogFileName() {
        var currentDirectoryPath = FileSystems.getDefault().
                getPath("").
                toAbsolutePath().
                toString();

        return currentDirectoryPath + "\\logger(" + getDateNowShort() + ").json";
    }


    /**
     * Renvoie la date du jour
     * @return date du jour dd-MM-yyyy HH:MM:SS
     */
    public String getDateNow() {
        var currentDate = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now());
        currentDate += " " + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + ":" + LocalDateTime.now().getSecond();
        return currentDate;
    }

    /**
     * Renvoie la date du jour
     * @return date du jour dd-MM-yyyy
     */
    public String getDateNowShort() {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now());
    }

    /**
     * Ecris dans le fichier de log de l'application au format JSON
     * @param p_fileData contenu à journaliser
     * @param userId id de l'utilisateur concerné
     * @param domain type de l'information
     */
    public void writeLog(@NotNull String p_fileData,int userId, @NotNull String domain) {
        try {
            var reader = new BufferedReader(new FileReader(getLogFileName()));
            var jsonContent = "\n" +
                    "{\n" +
                    "   \"time\" : \"" + getDateNow() + "\",\n" +
                    "   \"domain\" : \"" + domain + "\",\n" +
                    "   \"content\" : " + p_fileData + ",\n" +
                    "   \"userId\" : " + userId + "\n" +
                    "},\n\n\t{}";
            var semaphore = new Semaphore(1);
            semaphore.acquire();
            // Writes the string to the file
            var line = reader.readLine();
            var oldContent = "";
            while (line != null) {
                oldContent = oldContent + line + System.lineSeparator();

                line = reader.readLine();
            }
            var newContent = oldContent.replace("{}", jsonContent);
            var writer = new FileWriter(getLogFileName());
            writer.write(newContent);
            reader.close();
            writer.close();
            semaphore.release(1);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * Ferme le log
     * @throws IOException
     */
   /* public void closeLog() throws IOException {

        this.output.close();

    }
*/
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
