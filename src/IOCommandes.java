/*
 --- creators : nakira974 && Weefle  ----
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class IOCommandes {

    private BufferedReader _lectureEcran;

    private PrintStream _ecritureEcran;

    public IOCommandes(BufferedReader lectureEcran, PrintStream ecritureEcran) throws IOException {


        this._lectureEcran = lectureEcran;
        this._ecritureEcran = ecritureEcran;


    }

    public IOCommandes() {
    }

    public void ecrireEcran(String texte) {
        _ecritureEcran.println(texte);
    }

    public String lireEcran() throws IOException {

        return _lectureEcran.readLine();
    }


}
