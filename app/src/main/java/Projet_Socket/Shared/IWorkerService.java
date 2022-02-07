package Projet_Socket.Shared;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @param <T> WorkerType
 */
public interface IWorkerService<T> extends Runnable{
    /**
     * Extinction du serveur
     *
     */
     void serverStop() throws IOException;

    /**
     * Fermeture de la connexion avec le client
     */
    void clientExit() throws NoSuchAlgorithmException;

    /**
     * Distribue un message dans un groupe donné
     * @param clientCommand commande du client
     */
    void sendGroup(@NotNull String clientCommand);

    /**
     * Distribue le message privé à un utilisateur donné
     * @param clientCommand contenu la commande client
     */
    void sendPrivate(@NotNull String clientCommand);

    /**
     * Déconnecte l'utilisateur en base de données
     * @throws NoSuchAlgorithmException
     */
    void setUserDown() throws NoSuchAlgorithmException;

    /**
     * Transforme une chaine de caractères en md5
     * @param input contenu à transformer
     * @return md5 de la chaine passée en paramètres
     * @throws NoSuchAlgorithmException
     */
    byte[] getMd5(@NotNull String input) throws NoSuchAlgorithmException;

    /**
     * Converti un byte array en string
     * @param hash tableau de byte à convertir en string
     * @return
     */
    @NotNull
    String toHexString(@NotNull byte[] hash);

    /**
     * Distribue le message privé d'un utilisateur à un autre
     * @param sender envoyeur
     * @param destination destinataire
     * @param msg contenu du message
     */
    void privateSend(@NotNull String[] sender, @NotNull String destination, @NotNull String msg);

    /**
     * Déconnecte tous les utilisateurs en base de données
     */
    void setUsersDown();
}
