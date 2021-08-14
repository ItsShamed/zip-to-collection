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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;
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
import java.io.*;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@DisplayName("File writing tests")
public class FileWritingTests {

    private final Logger log = LogManager.getLogger();

    @BeforeAll
    public static void setupLog(){
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();
    }

    @Test
    @DisplayName("Converting a single beatmap into its OSDB format")
    public void SingleBeatmapSerializationTest() throws URISyntaxException, IOException, BeatmapException {

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
        MapsetPack pack = new MapsetPack(this.getClass().getResource("/MapPacks/Beatmap Pack #1054.7z").toURI().getPath());

        ByteArrayOutputStream osdbSample = new ByteArrayOutputStream();
        LittleEndianDataOutputStream os = new LittleEndianDataOutputStream(osdbSample);

        pack.writeToBinary(os);

        os.close();
        osdbSample.close();

        printBinaryData(osdbSample.toByteArray());
    }

    @Test
    @DisplayName("Converting an archive containing packs to a set of collections")
    @Disabled // RIP LFS quota
    public void PackContainerSerializationTest() throws IOException, RarException, BeatmapException, NoSuchAlgorithmException, InterruptedException, URISyntaxException {
        File zipFile = new File(this.getClass().getClassLoader().getResource("PacksPacks/TestPacksPacks.zip").toURI().getPath());
        List<MapsetPack> packs = new ArrayList<>();
        if (zipFile.getName().endsWith(".zip")){
            ZipInputStream packContainer = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = packContainer.getNextEntry();
            while(entry!=null){
                File packFile = new File(System.getProperty("java.io.tmpdir")+entry.getName());
                if(!packFile.createNewFile()){
                    packFile.delete();
                    packFile.createNewFile();
                }
                FileOutputStream os = new FileOutputStream(packFile);
                IOUtils.copy(packContainer, os);
                os.close();
                packs.add(new MapsetPack(System.getProperty("java.io.tmpdir")+entry.getName()));
                packFile.delete();
                entry = packContainer.getNextEntry();
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
            log.error("Unknown archive format.");
            System.exit(1);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LittleEndianDataOutputStream leos = new LittleEndianDataOutputStream(baos);
        for (MapsetPack pack :
                packs) {
            pack.writeAsCollection(leos);
        }
        leos.close();
        baos.close();

        printBinaryData(baos.toByteArray());
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
