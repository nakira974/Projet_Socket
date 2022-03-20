package Projet_Socket.Utils.File;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe servant à l'envoi et réception de fichiers en json
 */
public final class FileObject {

    private String name;
    private int size;
    private byte[] content;


    /**
     * Constructeur permettant de créer un fichier pour l'envoi
     * @param name nom du fichier
     * @param size taille du fichier
     * @param content contenu du fichier
     */
    public FileObject(@NotNull String name, int size,@NotNull byte[] content) {
        this.name = name;
        this.size = size;
        this.content = content;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return name;
    }

    @Contract(value = "this")
    public void setName(String name) {
        this.name = name;
    }

    @Contract(pure = true)
    public int getSize() {
        return size;
    }

    @Contract(value = "this")
    public void setSize(int size) {
        this.size = size;
    }

    @Contract(pure = true)
    public byte[] getContent() {
        return content;
    }

    @Contract(value = "this")
    public void setContent(byte[] content) {
        this.content = content;
    }

    public void saveFile(String fileToReceived) {
        // TODO Auto-generated method stub
        Path path = Paths.get(fileToReceived);
        try {
            Files.write(path, content);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
