package me.Shamed.osu.zip2osdb.tests;

import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.MapsetPack;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileParsingTest {

    private static final Logger log = Logger.getLogger("zip2osdb");

    @Test
    public void mapsetParsingTest() throws IOException, BeatmapException, NoSuchAlgorithmException, InterruptedException, URISyntaxException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");
        log.setLevel(Level.ALL);

        String filePath = this.getClass().getClassLoader().getResource("Beatmapsets/beatmapSet.osz").toURI().getPath();

        log.info("Testing beatmapset: "+filePath);
        Beatmapset beatmapset = new Beatmapset(new File(filePath));

        for (OSDBWritableBeatmap beatmap : beatmapset.getBeatmaps()){
            System.out.printf(
                    "Beatmap %n" +
                    "   Artist: %s%n" +
                    "   Title: %s%n" +
                    "   Difficulty name: %s%n" +
                    "   Difficulty rating: %s%n" +
                    "   MD5 Hash: %s%n",
                    beatmap.getMetadata().getArtistRomanized(),
                    beatmap.getMetadata().getTitleRomanized(),
                    beatmap.getMetadata().getVersion(),
                    beatmap.getDifficulty().getStars(),
                    beatmap.getStringHash()
            );
        }


    }

    @Test
    public void smallZipPackParsingTest() throws BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, RarException, URISyntaxException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");

        MapsetPack pack = new MapsetPack(this.getClass().getResource("/MapPacks/TestBeatmapPack.zip").toURI().getPath());
        System.out.printf("Map pack: %s%n", pack.getName());
        for (Beatmapset beatmapset : pack.getBeatmapsets()){
            System.out.printf("     Beatmapset: %s%n", beatmapset.getFileName());
            for (OSDBWritableBeatmap beatmap : beatmapset.getBeatmaps()){
                System.out.println("         Beatmap:");
                System.out.printf("             Title: %s%n", beatmap.getMetadata().getTitleRomanized());
                System.out.printf("             Artist: %s%n", beatmap.getMetadata().getArtistRomanized());
                System.out.printf("             Beatmap ID: %s%n", beatmap.getMetadata().getBeatmapId());
                System.out.printf("             Beatmapset ID: %s%n", beatmap.getMetadata().getBeatmapSetId());
                System.out.printf("             Difficulty name: %s%n", beatmap.getMetadata().getVersion());
                System.out.printf("             Difficulty rating: %s%n", beatmap.getDifficulty().getStars());
                System.out.printf("             MD5: %s%n", DatatypeConverter.printHexBinary(beatmap.getHash()).toLowerCase(Locale.ROOT));
            }
        }
    }

    @Test
    public void sevenZipPackParsingTest() throws RarException, BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, URISyntaxException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");
        MapsetPack pack = new MapsetPack(this.getClass().getResource("/MapPacks/Beatmap Pack #1054.7z").toURI().getPath());
        System.out.printf("Map pack: %s%n", pack.getName());
        for (Beatmapset beatmapset : pack.getBeatmapsets()){
            System.out.printf("     Beatmapset: %s%n", beatmapset.getFileName());
            for (OSDBWritableBeatmap beatmap : beatmapset.getBeatmaps()){
                System.out.println("         Beatmap:");
                System.out.printf("             Title: %s%n", beatmap.getMetadata().getTitleRomanized());
                System.out.printf("             Artist: %s%n", beatmap.getMetadata().getArtistRomanized());
                System.out.printf("             Beatmap ID: %s%n", beatmap.getMetadata().getBeatmapId());
                System.out.printf("             Beatmapset ID: %s%n", beatmap.getMetadata().getBeatmapSetId());
                System.out.printf("             Difficulty name: %s%n", beatmap.getMetadata().getVersion());
                System.out.printf("             Difficulty rating: %s%n", beatmap.getDifficulty().getStars());
                System.out.printf("             MD5: %s%n", DatatypeConverter.printHexBinary(beatmap.getHash()).toLowerCase(Locale.ROOT));
            }
        }
    }

    @Test
    public void largeRarPackParsingTest() throws URISyntaxException, RarException, BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");
        System.out.println(this.getClass().getResource("/MapPacks/TestBeatmapPack.rar").toURI().getPath());
        MapsetPack pack = null;
        try {
            pack = new MapsetPack(this.getClass().getResource("/MapPacks/TestBeatmapPack.rar").toURI().getPath());
        }catch (UnsupportedRarV5Exception e){
            System.err.println("RAR5 file can't test...");
            return;
        }
        System.out.printf("Map pack: %s%n", pack.getName());
        for (Beatmapset beatmapset : pack.getBeatmapsets()){
            System.out.printf("     Beatmapset: %s%n", beatmapset.getFileName());
            for (OSDBWritableBeatmap beatmap : beatmapset.getBeatmaps()){
                System.out.println("         Beatmap:");
                System.out.printf("             Title: %s%n", beatmap.getMetadata().getTitleRomanized());
                System.out.printf("             Artist: %s%n", beatmap.getMetadata().getArtistRomanized());
                System.out.printf("             Beatmap ID: %s%n", beatmap.getMetadata().getBeatmapId());
                System.out.printf("             Beatmapset ID: %s%n", beatmap.getMetadata().getBeatmapSetId());
                System.out.printf("             Difficulty name: %s%n", beatmap.getMetadata().getVersion());
                System.out.printf("             Difficulty rating: %s%n", beatmap.getDifficulty().getStars());
                System.out.printf("             MD5: %s%n", DatatypeConverter.printHexBinary(beatmap.getHash()).toLowerCase(Locale.ROOT));
            }
        }
    }
}
