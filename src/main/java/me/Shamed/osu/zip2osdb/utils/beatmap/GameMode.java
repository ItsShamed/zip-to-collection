package me.Shamed.osu.zip2osdb.utils.beatmap;

import java.util.HashMap;
import java.util.Map;

public enum GameMode {

    STANDARD((byte)0x0, "osu!standard"),
    TAIKO((byte)0x1,"Taiko"),
    CTB((byte)0x2,"Catch The Beat"),
    MANIA((byte)0x3,"osu!mania");

    private final Byte mode;
    private final String name;
    private static final Map<Byte, GameMode> BY_ID = new HashMap<>();

    private GameMode(final Byte mode, final String name){
        this.mode=mode;
        this.name=name;
    }

    public final Byte getByte(){
        return this.mode;
    }

    public static GameMode getById(Byte id) {
        return BY_ID.get(id);
    }

    @Override
    public String toString(){
        return this.name;
    }

    static {
        for (GameMode gamemode :
                values()) {
            BY_ID.put(gamemode.mode, gamemode);
        }
    }

}
