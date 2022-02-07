package Projet_Socket.Utils.File;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
