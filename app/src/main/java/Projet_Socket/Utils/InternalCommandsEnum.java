package Projet_Socket.Utils;

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
    public final String Label;

    InternalCommandsEnum(String command) {
        this.Label = command;
    }

}
