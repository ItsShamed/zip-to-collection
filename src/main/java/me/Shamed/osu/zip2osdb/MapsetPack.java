package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;

public class MapsetPack {

    private String name;
    private Beatmapset[] beatmapsets;

    public MapsetPack(String filePath){

    }

    public String getName() {
        return name;
    }

    public Beatmapset[] getBeatmapsets() {
        return beatmapsets;
    }

    public void writeToBinary(LittleEndianDataOutputStream outputStream){

    }

}
