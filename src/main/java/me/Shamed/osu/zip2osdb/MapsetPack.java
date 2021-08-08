package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MapsetPack {

    private String name;
    private Beatmapset[] beatmapsets;

    public MapsetPack(String filePath) throws IOException, BeatmapException, NoSuchAlgorithmException {
        List<Beatmapset> mapsets = new ArrayList<>();
        Logger log = Logger.getLogger("zip2osdb");
        ZipInputStream zipIn = null;

        File mappackFile = new File(filePath);

        if (!mappackFile.exists()){
            System.err.printf("File %s not found.", filePath);
            System.exit(1);
        }

        try{
            zipIn = new ZipInputStream(new FileInputStream(mappackFile));
        } catch (FileNotFoundException e){
            System.err.printf("File %s not found.", filePath);
            System.exit(1);
        }
        assert zipIn!=null;
        ZipEntry entry;

        do {
            entry = zipIn.getNextEntry();
            mapsets.add(new Beatmapset(zipIn, entry));
        } while (entry!=null);

        this.beatmapsets=mapsets.toArray(mapsets.toArray(new Beatmapset[mapsets.size()]));

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
