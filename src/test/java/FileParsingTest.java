import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileParsingTest {
    @Test
    public void mapsetParsingTest() throws IOException, BeatmapException, NoSuchAlgorithmException {
        Logger logger = Logger.getLogger("zip2osdb");
        logger.setLevel(Level.ALL);

        System.out.println("java.io.tmpdir: "+System.getProperty("java.io.tmpdir"));
        String filePath = "test_resources/MapsetParsingTest/beatmapPack.zip";

        logger.info("Testing beatmap pack: "+filePath);

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(filePath));
        ZipEntry entry = zipIn.getNextEntry();
        Beatmapset beatmapset = new Beatmapset(zipIn, entry);

        for (OSDBWritableBeatmap beatmap : beatmapset.getBeatmaps()){
            System.out.printf(
                    "Beatmap %n" +
                            "Artist: %s%n" +
                            "Title: %s%n" +
                            "Difficulty name: %s%n" +
                            "Difficulty rating: %s%n" +
                            "MD5 Hash: %s%n",
                    beatmap.getMetadata().getArtistRomanized(),
                    beatmap.getMetadata().getTitleRomanized(),
                    beatmap.getMetadata().getVersion(),
                    beatmap.getDifficulty().getStars(),
                    beatmap.getStringHash()
            );
        }


    }
}
