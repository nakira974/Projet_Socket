package Projet_Socket;/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.*;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class Logger {


    public BufferedWriter output;
    private FileWriter file;

    public Logger() {

        {
            try {

                var buffer = new BufferedWriter(new FileWriter(getLogFileName(), true));
                this.file = new FileWriter(getLogFileName());
                output = new BufferedWriter(file);
                buffer.newLine();
                buffer.append("{\"Logs\": [\n{}\n]\n}");
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getLogFileName(){
        String currentDirectoryPath = FileSystems.getDefault().
                getPath("").
                toAbsolutePath().
                toString();

        return currentDirectoryPath+"\\app\\src\\main\\java\\Projet_Socket\\logger(" + getDateNowShort() + ").json";
    }


    public String getDateNow(){
        var currentDate =  DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now());
        currentDate+=" "+LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute()+":"+LocalDateTime.now().getSecond();
        return currentDate;
    }

    public String getDateNowShort(){
        return DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE).format(LocalDateTime.now());
    }
    public void writeLog(String p_fileData, int userId, String domain) {
        try {
            var reader = new BufferedReader(new FileReader(getLogFileName()));
            String jsonContent = "\n" +
                    "{\n" +
                    "     \"time\" : \""+ getDateNow()+"\",\n" +
                    "     \"domain\" : \""+domain+"\",\n" +
                    "\t  \"content\" : \""+p_fileData+"\",\n" +
                    "\t\t\"userId\" : \""+userId+"\"\n" +
                    "},\n{}\n" +
                    "";
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquire();
            // Writes the string to the file
            var line = reader.readLine();
            String oldContent = "";
            while (line != null)
            {
                oldContent = oldContent + line + System.lineSeparator();

                line = reader.readLine();
            }
            var newContent = oldContent.replace("{}", jsonContent);
            var writer = new FileWriter(getLogFileName());
            writer.write(newContent);
            reader.close();
            writer.close();
            semaphore.release();
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
