package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CancellationException;

public class OSDB {

    public final String version = "o!dm8";
    private final File out;
    private final List<MapsetPack> packs;
    private final Date creationDate;
    private final Logger log = LogManager.getLogger(Main.class);
    private JFrame jFrame;

    public OSDB(File file) throws IOException {
        this.out = file;
        this.packs = new ArrayList<>();
        this.creationDate = new Date();
        if (out.exists() && out.isFile()) {
            System.out.printf("%s already exists. Do you want to overwrite? (y/n): ", out.getName());
            Scanner scanner = new Scanner(System.in);
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                out.delete();
                out.createNewFile();
            } else {
                System.exit(0);
            }
        }
    }

    public int computeTotalBeatmaps() {
        int total = 0;
        for (MapsetPack pack : packs) {
            for (Beatmapset set : pack.getBeatmapsets()) {
                for (OSDBWritableBeatmap beatmap : set.getBeatmaps()) {
                    total += 1;
                }
            }
        }
        return total;
    }

    public OSDB(File file, JFrame jFrame) throws IOException {
        this.out = file;
        this.packs = new ArrayList<>();
        this.creationDate = new Date();
        this.jFrame = jFrame;
        if (out.exists() && out.isFile()) {
            int r = JOptionPane.showConfirmDialog(jFrame, "File already exists, do you want to overwrite?",
                    "File overwrite", JOptionPane.YES_NO_OPTION);

            if (r == JOptionPane.YES_OPTION) {
                out.delete();
                out.createNewFile();
            } else {
                throw new CancellationException("Conversion cancelled");
            }
        }
    }

    public void add(MapsetPack pack){
        log.debug(String.format("Adding MapPack %s to osdb", pack.getName()));
        packs.add(pack);
    }

    public MapsetPack[] getPacks() {
        return packs.toArray(new MapsetPack[packs.size()]);
    }

    //TODO: Remove useless collection folder
    public void write(JProgressBar progressBar) throws IOException, ParseException {

        progressBar.setMaximum((computeTotalBeatmaps() + 3) * 2);
        progressBar.setIndeterminate(false);
        progressBar.setValue(computeTotalBeatmaps() + 3);
        // /tmp/collection/
        File osdbFolder = new File(System.getProperty("java.io.tmpdir") + "collection");
        if (osdbFolder.exists() && !osdbFolder.isDirectory()) {
            osdbFolder.delete();
            osdbFolder.mkdir();
        } else {
            osdbFolder.mkdir();
        }
        progressBar.setValue(progressBar.getValue() + 1);

        // /tmp/collection/collection.osdb
        log.debug("Creating uncompressed .osdb file");
        File uncompressedOsdb = new File(System.getProperty("java.io.tmpdir") + "collection/collection.osdb");
        if (!uncompressedOsdb.createNewFile()) {
            uncompressedOsdb.delete();
            uncompressedOsdb.createNewFile();
        }

        LittleEndianDataOutputStream uncompressedOutputStream = new LittleEndianDataOutputStream(new FileOutputStream(uncompressedOsdb));
        BinaryEditing.writeCSUTF(uncompressedOutputStream, this.version);
        uncompressedOutputStream.writeDouble(BinaryEditing.convertToOADate(creationDate));
        BinaryEditing.writeCSUTF(uncompressedOutputStream, System.getProperty("user.name"));
        uncompressedOutputStream.writeInt(1);
        for (MapsetPack pack :
                this.packs) {
            pack.writeAsCollection(uncompressedOutputStream, progressBar);
        }
        BinaryEditing.writeCSUTF(uncompressedOutputStream, "By Piotrekol");
        uncompressedOutputStream.close();
        //

        // /tmp/collection.osdb.gz
        log.debug("Compressing raw .osdb");
        File compressedOsdb = new File(System.getProperty("java.io.tmpdir") + " collection.osdb.gz");
        if (!compressedOsdb.createNewFile()) {
            compressedOsdb.delete();
            compressedOsdb.createNewFile();
        }
        FileInputStream uncompressedStream = new FileInputStream(uncompressedOsdb);
        GzipParameters gzipHeader = new GzipParameters();
        gzipHeader.setFilename("collection.osdb");
        gzipHeader.setModificationTime(new Date().getTime());
        GzipCompressorOutputStream cOsdbStream = new GzipCompressorOutputStream(new FileOutputStream(compressedOsdb), gzipHeader);
        IOUtils.copy(uncompressedStream, cOsdbStream);
        cOsdbStream.close();
        progressBar.setValue(progressBar.getValue() + 1);
        //

        // out.osdb
        log.debug(String.format("Saving final data to %s", out.getPath()));
        LittleEndianDataOutputStream outStream = new LittleEndianDataOutputStream(new FileOutputStream(out));
        BinaryEditing.writeCSUTF(outStream, "o!dm8");
        IOUtils.copy(new FileInputStream(compressedOsdb), outStream);
        outStream.close();
        uncompressedOsdb.delete();
        compressedOsdb.delete();
        osdbFolder.delete();
        progressBar.setValue(progressBar.getValue() + 1);
        log.info("OSDB File written!!");
        JOptionPane.showMessageDialog(jFrame, "Conversion finished!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        //

    }

    public void write() throws IOException, ParseException {

        // /tmp/collection/
        File osdbFolder = new File(System.getProperty("java.io.tmpdir") + "collection");
        if (osdbFolder.exists() && !osdbFolder.isDirectory()) {
            osdbFolder.delete();
            osdbFolder.mkdir();
        } else {
            osdbFolder.mkdir();
        }

        // /tmp/collection/collection.osdb
        log.debug("Creating uncompressed .osdb file");
        File uncompressedOsdb = new File(System.getProperty("java.io.tmpdir") + "collection/collection.osdb");
        if (!uncompressedOsdb.createNewFile()) {
            uncompressedOsdb.delete();
            uncompressedOsdb.createNewFile();
        }

        LittleEndianDataOutputStream uncompressedOutputStream = new LittleEndianDataOutputStream(new FileOutputStream(uncompressedOsdb));
        BinaryEditing.writeCSUTF(uncompressedOutputStream, this.version);
        uncompressedOutputStream.writeDouble(BinaryEditing.convertToOADate(creationDate));
        BinaryEditing.writeCSUTF(uncompressedOutputStream, System.getProperty("user.name"));
        uncompressedOutputStream.writeInt(1);
        for (MapsetPack pack :
                this.packs) {
            pack.writeAsCollection(uncompressedOutputStream);
        }
        BinaryEditing.writeCSUTF(uncompressedOutputStream, "By Piotrekol");
        uncompressedOutputStream.close();
        //

        // /tmp/collection.osdb.gz
        log.debug("Compressing raw .osdb");
        File compressedOsdb = new File(System.getProperty("java.io.tmpdir") + " collection.osdb.gz");
        if (!compressedOsdb.createNewFile()) {
            compressedOsdb.delete();
            compressedOsdb.createNewFile();
        }
        FileInputStream uncompressedStream = new FileInputStream(uncompressedOsdb);
        GzipParameters gzipHeader = new GzipParameters();
        gzipHeader.setFilename("collection.osdb");
        gzipHeader.setModificationTime(new Date().getTime());
        GzipCompressorOutputStream cOsdbStream = new GzipCompressorOutputStream(new FileOutputStream(compressedOsdb), gzipHeader);
        IOUtils.copy(uncompressedStream, cOsdbStream);
        cOsdbStream.close();
        //

        // out.osdb
        log.debug(String.format("Saving final data to %s", out.getPath()));
        LittleEndianDataOutputStream outStream = new LittleEndianDataOutputStream(new FileOutputStream(out));
        BinaryEditing.writeCSUTF(outStream, "o!dm8");
        IOUtils.copy(new FileInputStream(compressedOsdb), outStream);
        outStream.close();
        uncompressedOsdb.delete();
        compressedOsdb.delete();
        osdbFolder.delete();
        log.info("OSDB File written!!");
        //

    }


}