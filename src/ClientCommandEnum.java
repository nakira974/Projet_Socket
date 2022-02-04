public enum ClientCommandEnum {
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
    ClientCommandEnum(String command) {
        this.Label = command;
    }
    public boolean contains(String right){
        boolean result = false;
        if(!this.Label.contains(right)) return result;
        return true;
    }
}
