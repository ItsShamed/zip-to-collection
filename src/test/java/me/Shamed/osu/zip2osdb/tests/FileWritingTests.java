package me.Shamed.osu.zip2osdb.tests;

import com.github.junrar.exception.RarException;
import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.MapsetPack;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@DisplayName("File writing tests")
public class FileWritingTests {

    private final Logger log = Logger.getLogger("zip2osdb");

    @Test
    @DisplayName("Converting a single beatmap into its OSDB format")
    public void SingleBeatmapSerializationTest() throws URISyntaxException, IOException, BeatmapException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");

        File osuFile = new File(this.getClass().getResource("/Beatmaps/beatmap.osu").toURI().getPath());
        BeatmapParser parser = new BeatmapParser();
        Beatmap ekBeatmap = parser.parse(osuFile);
        OSDBWritableBeatmap beatmap = OSDBWritableBeatmap.BeatmapConverter.makeWritable(ekBeatmap, DigestUtils.md5(new FileInputStream(osuFile)));
        ByteArrayOutputStream osdbSample = new ByteArrayOutputStream();
        LittleEndianDataOutputStream outputStream = new LittleEndianDataOutputStream(osdbSample);
        beatmap.writeToBinary(outputStream);
        outputStream.close();
        osdbSample.close();

        printBinaryData(osdbSample.toByteArray());



    }

    @Test
    @DisplayName("Converting beatmaps from a beatmapset into their OSDB format")
    public void BeatmapsetSerializationTest() throws IOException, NoSuchAlgorithmException, URISyntaxException {
        String filePath = this.getClass().getClassLoader().getResource("Beatmapsets/beatmapSet.osz").toURI().getPath();

        log.info("Testing beatmapset: "+filePath);
        Beatmapset beatmapset = new Beatmapset(new File(filePath));

        ByteArrayOutputStream osdbSample = new ByteArrayOutputStream();
        LittleEndianDataOutputStream os = new LittleEndianDataOutputStream(osdbSample);

        beatmapset.writeToBinary(os);

        os.close();
        osdbSample.close();

        printBinaryData(osdbSample.toByteArray());

    }

    @Test
    @DisplayName("Converting a MapPack into its OSDB equivalent")
    public void PackSerializationTest() throws URISyntaxException, RarException, BeatmapException, IOException,
                                               NoSuchAlgorithmException, InterruptedException {
        log.config("java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] [%2$s] %5$s %n");
        MapsetPack pack = new MapsetPack(this.getClass().getResource("/MapPacks/Beatmap Pack #1054.7z").toURI().getPath());

        ByteArrayOutputStream osdbSample = new ByteArrayOutputStream();
        LittleEndianDataOutputStream os = new LittleEndianDataOutputStream(osdbSample);

        pack.writeToBinary(os);

        os.close();
        osdbSample.close();

        printBinaryData(osdbSample.toByteArray());
    }

    @DisplayName("Print final result")
    private void printBinaryData(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(data);
        System.out.println("================================================================");
        int address = 0x000000;
        int column = 0;
        boolean endOfMemoryAddress;
        for (byte b:
                data) {

            if(column == 0){
                System.out.printf("0x%08x ||", address);
            }else if (column == 8){
                System.out.print(" |");
            }
            endOfMemoryAddress = column == 15;
            System.out.printf(" %02X", b);

            if(endOfMemoryAddress){
                System.out.print("\n");
                column = 0;
                endOfMemoryAddress=false;
            } else {
                column+=1;
            }
            address+=1;

        }
        System.out.println("\n================================================================\n");


        System.out.println();
        System.out.println("Raw hexadecimal data:");
        System.out.println(DatatypeConverter.printHexBinary(data));

        System.out.println();
        System.out.println("Raw data: \n");
        System.out.println(byteArrayOutputStream.toString("UTF-8"));
        byteArrayOutputStream.close();
    }

}
