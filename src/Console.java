/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class Console {

    private BufferedReader _writeBuffer;

    private PrintStream _readBuffer;

    public Console(BufferedReader readStream, PrintStream writeStream) throws IOException {
        this._writeBuffer = readStream;
        this._readBuffer = writeStream;
    }

    public Console() {
    }

    public void writeLine(String texte) {
        _readBuffer.println(texte);
    }

    public String readKey() throws IOException {

        return _writeBuffer.readLine();
    }


}
