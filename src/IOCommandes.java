import java.io.*;

public class IOCommandes {

    private BufferedReader _lectureEcran;

    private PrintStream _ecritureEcran;

    public IOCommandes(BufferedReader lectureEcran, PrintStream ecritureEcran) throws IOException {


        this._lectureEcran = lectureEcran;
        this._ecritureEcran = ecritureEcran;


    }

    public IOCommandes() {
    }

    public void ecrireEcran(String texte){
           _ecritureEcran.println(texte);
    }

    public String lireEcran() throws IOException {

        return _lectureEcran.readLine();
    }



}
