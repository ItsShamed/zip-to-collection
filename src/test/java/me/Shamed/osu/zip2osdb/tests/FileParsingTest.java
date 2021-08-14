package me.Shamed.osu.zip2osdb.tests;

import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.MapsetPack;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@DisplayName("File parsing tests")
public class FileParsingTest {

    private static final Logger log = LogManager.getLogger();

    @BeforeAll
    public static void setupLog(){
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();
    }

    @Test
    @DisplayName("Single beatmapset (.osz) parsing test case")
    public void mapsetParsingTest() throws IOException, BeatmapException, NoSuchAlgorithmException, InterruptedException, URISyntaxException {

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
    @DisplayName("Beatmap pack (.zip) parsing test")
    public void smallZipPackParsingTest() throws BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, RarException, URISyntaxException {

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
    @DisplayName("Beatmap pack (.7z) parsing test")
    public void sevenZipPackParsingTest() throws RarException, BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, URISyntaxException {
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
    @Disabled
    @DisplayName("Beatmap pack (.rar) parsing test")
    public void largeRarPackParsingTest() throws URISyntaxException, RarException, BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException {
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
