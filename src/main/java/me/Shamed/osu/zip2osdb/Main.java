package me.Shamed.osu.zip2osdb;

import com.github.junrar.exception.RarException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    public static final Logger log = Logger.getLogger("zip2osdb");
    public JFrame jFrame;

    public static void main(String[] args) throws BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, RarException, URISyntaxException {

        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");

        if(args.length==0){
            buildGui();
        }
        else if(args.length==1){
            File zipFile = new File(args[0]);
            if(!zipFile.exists()){
                System.err.printf("File %s does not exist.%n", args[0]);
                return;
            }

            switch (detectArchiveStructure(zipFile)){
                case PACK:
                    MapsetPack mapsetPack = new MapsetPack(args[0]);
                    System.out.printf("Mapset pack %s:%n", mapsetPack.getName());
                    for (Beatmapset set :
                            mapsetPack.getBeatmapsets()) {
                        System.out.printf("     Beatmapset %s:%n", set.getFileName());
                        for (OSDBWritableBeatmap beatmap : set.getBeatmaps()){
                            System.out.printf("         Beatmap:%n" +
                                              "             Title: %s%n" +
                                              "             Artist: %s%n" +
                                              "             Beatmap ID: %s%n" +
                                              "             Beatmapset ID: %s%n" +
                                              "             Difficulty name: %s%n" +
                                              "             Difficulty rating: %4s%n",
                                    beatmap.getMetadata().getTitleRomanized(),
                                    beatmap.getMetadata().getArtistRomanized(),
                                    beatmap.getMetadata().getBeatmapId(),
                                    beatmap.getMetadata().getBeatmapSetId(),
                                    beatmap.getMetadata().getVersion(),
                                    beatmap.getDifficulty().getStars()
                            );
                        }
                    }
                    break;
                case PACK_CONTAINER:
                    List<MapsetPack> packs = new ArrayList<>();
                    if (zipFile.getName().endsWith(".zip")){
                        ZipFile packContainer = new ZipFile(zipFile);
                        Enumeration<? extends ZipEntry> entries = packContainer.entries();
                        ZipEntry entry = entries.nextElement();
                        while(entries.hasMoreElements()){
                            File packFile = new File(System.getProperty("java.io.tmpdir")+entry.getName());
                            if(!packFile.createNewFile()){
                                packFile.delete();
                                packFile.createNewFile();
                            }
                            FileOutputStream os = new FileOutputStream(packFile);
                            IOUtils.copy(packContainer.getInputStream(entry), os);
                            os.close();
                            packs.add(new MapsetPack(System.getProperty("java.io.tmpdir")+entry.getName()));
                            packFile.delete();
                            entry = entries.nextElement();
                        }
                    } else if(zipFile.getName().endsWith(".7z")){
                        SevenZFile packContainer = new SevenZFile(zipFile);
                        SevenZArchiveEntry entry = packContainer.getNextEntry();
                        while (entry!=null){
                            File packFile = new File(System.getProperty("java.io.tmpdir")+entry.getName());
                            if(!packFile.createNewFile()){
                                packFile.delete();
                                packFile.createNewFile();
                            }
                            FileOutputStream os = new FileOutputStream(packFile);
                            IOUtils.copy(packContainer.getInputStream(entry), os);
                            os.close();
                            packs.add(new MapsetPack(System.getProperty("java.io.tmpdir")+entry.getName()));
                            packFile.delete();
                            entry = packContainer.getNextEntry();
                        }
                    } else {
                        System.err.println("Unknown archive format.");
                        return;
                    }


                    for (MapsetPack mapsetPack1 : packs){
                        System.out.printf("Mapset pack %s:%n", mapsetPack1.getName());
                        for (Beatmapset set :
                                mapsetPack1.getBeatmapsets()) {
                            System.out.printf("     Beatmapset %s:%n", set.getFileName());
                            for (OSDBWritableBeatmap beatmap : set.getBeatmaps()){
                                System.out.printf("         Beatmap:%n" +
                                                "             Title: %s%n" +
                                                "             Artist: %s%n" +
                                                "             Beatmap ID: %s%n" +
                                                "             Beatmapset ID: %s%n" +
                                                "             Difficulty name: %s%n" +
                                                "             Difficulty rating: %4s%n",
                                        beatmap.getMetadata().getTitleRomanized(),
                                        beatmap.getMetadata().getArtistRomanized(),
                                        beatmap.getMetadata().getBeatmapId(),
                                        beatmap.getMetadata().getBeatmapSetId(),
                                        beatmap.getMetadata().getVersion(),
                                        beatmap.getDifficulty().getStars()
                                );
                            }
                        }
                    }
                    break;

                case UNKNOWN:
                default:
                    System.err.println("Can't determine the structure of the archive.");
                    return;

            }

        }

    }

    private static ArchiveStructure detectArchiveStructure(File file) throws IOException {


        if(file.getName().endsWith(".zip")){
            ZipFile zipFile = new ZipFile(file);
            int totalFiles = 0;
            int zipFiles = 0;
            int oszFiles = 0;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry = entries.nextElement();
            while (entries.hasMoreElements()) {
                totalFiles += 1;
                if (entry.getName().endsWith(".zip") || entry.getName().endsWith(".rar") || entry.getName().endsWith(".7z")) {
                    zipFiles += 1;
                } else if (entry.getName().endsWith(".osz")) {
                    oszFiles += 1;
                }
                entry = entries.nextElement();
            }
            if (zipFiles / totalFiles > .5) {
                return ArchiveStructure.PACK_CONTAINER;
            } else if (oszFiles / totalFiles > .5) {
                return ArchiveStructure.PACK;
            } else {
                return ArchiveStructure.UNKNOWN;
            }
        }else if(file.getName().endsWith(".7z")){
            SevenZFile zipFile = new SevenZFile(file);
            int totalFiles = 0;
            int zipFiles = 0;
            int oszFiles = 0;
            SevenZArchiveEntry entry = zipFile.getNextEntry();
            while(entry!=null){
                totalFiles += 1;
                if (entry.getName().endsWith(".zip") || entry.getName().endsWith(".rar") || entry.getName().endsWith(".7z")) {
                    zipFiles += 1;
                } else if (entry.getName().endsWith(".osz")) {
                    oszFiles += 1;
                }
                entry = zipFile.getNextEntry();
            }
            if (zipFiles / totalFiles > .5) {
                return ArchiveStructure.PACK_CONTAINER;
            } else if (oszFiles / totalFiles > .5) {
                return ArchiveStructure.PACK;
            } else {
                return ArchiveStructure.UNKNOWN;
            }
        } else {
            return ArchiveStructure.UNKNOWN;
        }


    }

    private static void buildGui(){

    }

    private enum ArchiveStructure{
        PACK_CONTAINER,
        PACK,
        UNKNOWN
    }
}
