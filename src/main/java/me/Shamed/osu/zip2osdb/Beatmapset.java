package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Beatmapset {
    private static final Logger log = LogManager.getLogger();
    private final String fileName;
    private OSDBWritableBeatmap[] beatmaps;


    public Beatmapset(ZipInputStream inputStream, ZipEntry oszEntry) throws IOException, NoSuchAlgorithmException, BeatmapException, InterruptedException {

        this.fileName = oszEntry.getName();
        log.debug("Opening beatmapset: " + this.fileName);
        log.debug("Caching in tempdir...");
        File oszFile = new File(System.getProperty("java.io.tmpdir") + oszEntry.getName());
        log.debug("Cached path: " + oszFile.getPath());
        if (!oszFile.createNewFile()) {
            log.debug("Apparently the file already exists, replacing...");
            oszFile.delete();
            oszFile.createNewFile();
        }
        oszFile.deleteOnExit();

        log.debug("Writing content...");
        try(OutputStream oszOut = new FileOutputStream(oszFile)){
            IOUtils.copy(inputStream, oszOut);
            log.debug("Cached file created");
        }

        log.debug("Reading cached file");

        parseBeatmaps(oszFile);
    }

    public Beatmapset(InputStream sevenZipInputStream, SevenZArchiveEntry oszEntry) throws IOException, NoSuchAlgorithmException {
        this.fileName = oszEntry.getName();
        log.debug("Opening beatmapset: " + this.fileName);
        log.debug("Caching in tempdir...");
        File oszFile = new File(System.getProperty("java.io.tmpdir") + oszEntry.getName());
        if (!oszFile.createNewFile()) {
            log.debug("Apparently the file already exists, replacing...");
            oszFile.delete();
            oszFile.createNewFile();
        }
        oszFile.deleteOnExit();

        log.debug("Writing content...");
        try (OutputStream oszOut = new FileOutputStream(oszFile)) {
            IOUtils.copy(sevenZipInputStream, oszOut);
            log.debug("Cached file created");
        }

        log.debug("Reading cached file");

        parseBeatmaps(oszFile);
    }

    public Beatmapset(File osz) throws IOException, NoSuchAlgorithmException {
        this.fileName=osz.getName();
        parseBeatmaps(osz);
    }

    private static boolean isJUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    private void parseBeatmaps(File oszFile) throws IOException, NoSuchAlgorithmException {
        ArrayList<OSDBWritableBeatmap> detectedMaps = new ArrayList<>();
        ZipInputStream osz = new ZipInputStream(new FileInputStream(oszFile));

        ZipEntry osuEntry = osz.getNextEntry();
        while (osuEntry!=null){
            log.debug("Entry: " + osuEntry.getName());
            if(!osuEntry.isDirectory()){
                if(osuEntry.getName().endsWith(".osu")){
                    BeatmapParser parser = new BeatmapParser();
                    log.debug(".osu file found, caching in tmpdir...");
                    File beatmapFile = new File(System.getProperty("java.io.tmpdir")+osuEntry.getName());
                    if(!beatmapFile.createNewFile()){
                        log.debug("Replacing existing ");
                        beatmapFile.delete();
                        beatmapFile.createNewFile();
                    }


                    log.debug("Computing MD5 checksum on writing...");
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    DigestInputStream dis = new DigestInputStream(osz, md);
                    try(OutputStream osuOut = new FileOutputStream(beatmapFile)){
                        IOUtils.copy(dis, osuOut);
                    }


                    byte[] hash = md.digest();
                    log.debug("MD5: " + DatatypeConverter.printHexBinary(hash));

                    log.debug("Parsing beatmap...");

                    try{
                        Beatmap beatmap = parser.parse(beatmapFile);
                        if(beatmap!=null){
                            OSDBWritableBeatmap writableBeatmap = OSDBWritableBeatmap.BeatmapConverter.makeWritable(beatmap, hash);
                            log.debug("Beatmap added to set.");
                            detectedMaps.add(writableBeatmap);
                        }else{
                            throw new BeatmapException("NullPointer");
                        }



                    }catch (BeatmapException e){
                        log.warn("Failed to parse beatmap");
                    }

                    log.debug("Deleting cached map...");
                    if(beatmapFile.delete()){
                        log.debug("Deletion succeded...");
                    } else{
                        log.warn("Failed to delete cache");
                    }
                    beatmapFile.deleteOnExit();
                }


            }
            osz.closeEntry();
            osuEntry=osz.getNextEntry();
        }

        if (detectedMaps.isEmpty()){
            log.warn("Detected 0 beatmaps in set");
        }
        this.beatmaps=detectedMaps.toArray(new OSDBWritableBeatmap[detectedMaps.size()]);
        if(!isJUnitTest()) oszFile.delete(); // LOOOL My own program was deleting its own classpath wth
    }

    public void writeToBinary(LittleEndianDataOutputStream outputStream) throws IOException{
        log.debug(String.format("Writing beatmapset: %s...", this.fileName));
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


