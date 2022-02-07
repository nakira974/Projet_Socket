package Projet_Socket.Utils;

import org.jetbrains.annotations.NotNull;

/**
 * Enum comprenant l'ensemble des commandes interne de l'application
 */
public enum InternalCommandsEnum {
    FileSynchronisation("/fileSynchronisation"),
    CreateSharingSpace("/createSharingSpace"),
    Quit("quit"),
    WeatherInfo("/weather"),
    Translate("/translate:"),
    PrivateMessage("/@"),
    SendFile("/file"),
    GroupMessage("/G"),
    JoinGroupRequest("/JG"),
    GroupCreationRequest("/CG"),
    EndProcess("END"),
    Lazy(""),
    ;
    @NotNull
    public final String Label;

    InternalCommandsEnum(@NotNull String command) {
        this.Label = command;
    }

}
