package Projet_Socket.Utils.File;

import org.jetbrains.annotations.NotNull;

public class CloudFile {
    public String fileName;
    public FileStateEnum state;

    public enum FileStateEnum{
        Created("created"),
        Modified("modified"),
        Deleted("deleted"),
        Unchanged("unchanged");
        @NotNull
        public final String Label;

        FileStateEnum(@NotNull String command) {
            this.Label = command;
        }
    }
}
