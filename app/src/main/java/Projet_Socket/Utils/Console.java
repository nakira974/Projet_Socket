package Projet_Socket.Utils;/*
 --- creators : nakira974 && Weefle  ----
 */

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 */
public final class Console {

    private final BufferedReader _writeBuffer;

    private final PrintStream _readBuffer;

    /**
     * Utilitaire d'entrée/sortie
     * @param readStream flux de lecture
     * @param writeStream flux d'écriture
     * @throws IOException
     */
    public Console(@NotNull BufferedReader readStream,@NotNull PrintStream writeStream) throws IOException {
        try{
            this._writeBuffer = readStream;
            this._readBuffer = writeStream;
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException();
        }

    }

    /**
     * Sortie console standard
     * @param texte
     */
    public void writeLine(String texte) {
        _readBuffer.println(texte);
    }

    /**Lis l'entrée clavier
     * @return Chaine lu
     * @throws IOException
     */
    public String readKey() throws IOException {

        return _writeBuffer.readLine();
    }


}
