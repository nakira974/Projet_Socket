package Projet_Socket.Utils;/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Console {

    private final BufferedReader _writeBuffer;

    private final PrintStream _readBuffer;

    public Console(BufferedReader readStream, PrintStream writeStream) throws IOException {
        try{
            this._writeBuffer = readStream;
            this._readBuffer = writeStream;
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException();
        }

    }

    public void writeLine(String texte) {
        _readBuffer.println(texte);
    }

    public String readKey() throws IOException {

        return _writeBuffer.readLine();
    }


}
