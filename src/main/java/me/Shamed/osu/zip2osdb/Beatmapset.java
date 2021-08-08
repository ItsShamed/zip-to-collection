package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import me.Shamed.osu.zip2osdb.beatmap.WritableOsuBeatmap;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Beatmapset {
    private String fileName;
    private OSDBWritableBeatmap[] beatmaps;


    public Beatmapset(ZipInputStream inputStream, ZipEntry oszEntry) throws IOException, NoSuchAlgorithmException, BeatmapException {
        Logger log = Logger.getLogger("zip2osdb");

        ArrayList<OSDBWritableBeatmap> detectedMaps = new ArrayList<>();
        this.fileName=oszEntry.getName();
        log.info("Opening beatmapset: "+this.fileName);
        log.info("Caching in tempdir...");
        File oszFile = new File(System.getProperty("java.io.tmpdir")+oszEntry.getName());
        if(!oszFile.createNewFile()){
            log.info("Apparently the file already exists, replacing...");
            oszFile.delete();
            oszFile.createNewFile();
        }
        oszFile.deleteOnExit();

        log.info("Writing content...");
        try(OutputStream oszOut = new FileOutputStream(oszFile)){
            IOUtils.copy(inputStream, oszOut);
            log.info("Cached file created");
        }

        log.info("Reading cached file");
        ZipInputStream osz = new ZipInputStream(new FileInputStream(oszFile));

        ZipEntry osuEntry = osz.getNextEntry();
        BeatmapParser parser = new BeatmapParser();
        while (osuEntry!=null){
            log.info("Entry: "+osuEntry.getName());
            if(!osuEntry.isDirectory()){
                if(osuEntry.getName().endsWith(".osu")){
                    log.info(".osu file found, caching in tmpdir...");
                    File beatmapFile = new File(System.getProperty("java.io.tmpdir")+osuEntry.getName());
                    if(!beatmapFile.createNewFile()){
                        log.info("Replacing existing ");
                        beatmapFile.delete();
                        beatmapFile.createNewFile();
                    }
                    beatmapFile.deleteOnExit();

                    log.info("Computing MD5 checksum on writing...");
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    DigestInputStream dis = new DigestInputStream(osz, md);
                    try(OutputStream osuOut = new FileOutputStream(beatmapFile)){
                        IOUtils.copy(dis, osuOut);
                    }


                    byte[] hash = md.digest();
                    log.info("MD5: "+ DatatypeConverter.printHexBinary(hash));

                    log.info("Parsing beatmap...");
                    Beatmap beatmap = parser.parse(beatmapFile);

                    OSDBWritableBeatmap writableBeatmap = OSDBWritableBeatmap.BeatmapConverter.makeWritable(beatmap, hash);

                    log.info("Beatmap added to set.");
                    detectedMaps.add(writableBeatmap);
                }


            }
            osz.closeEntry();
            osuEntry=osz.getNextEntry();
        }

        if (detectedMaps.isEmpty()){
            throw new IOException("Not valid osz file.");
        }
        this.beatmaps=detectedMaps.toArray(new OSDBWritableBeatmap[detectedMaps.size()]);

    }

    public void writeToBinary(LittleEndianDataOutputStream outputStream) throws IOException{
        for (OSDBWritableBeatmap writableBeatmap :
                this.beatmaps) {
            writableBeatmap.writeToBinary(outputStream);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public OSDBWritableBeatmap[] getBeatmaps() {
        return beatmaps;
    }
}
