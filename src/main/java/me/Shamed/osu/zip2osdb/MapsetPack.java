package me.Shamed.osu.zip2osdb;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MapsetPack {

    private final Logger log = LogManager.getLogger();

    private final String version = "o!dm8";
    private final String name;
    private final Beatmapset[] beatmapsets;
    private Date creationDate;

    public MapsetPack(String filePath) throws IOException, BeatmapException, NoSuchAlgorithmException, InterruptedException, RarException {
        this.name = filePath.split(File.pathSeparator)[filePath.split(File.pathSeparator).length-1].replaceAll("\\..{2,3}(.\\d{3})?$","");

        List<Beatmapset> mapsets = new ArrayList<>();


        log.info("Searching for beatmaps...");
        File mappackFile = new File(filePath);

        if (!mappackFile.exists()){
            log.error(String.format("File %s not found.", filePath));
            System.exit(1);
        }

       if(mappackFile.getName().endsWith(".zip")){
           log.debug("Detected ZIP archive");
           ZipInputStream zipIn = null;
           try{
               zipIn = new ZipInputStream(new FileInputStream(mappackFile));
           } catch (FileNotFoundException e){
               System.err.printf("File %s not found.", filePath);
               System.exit(1);
           }
           assert zipIn!=null;
           ZipEntry entry;
           entry = zipIn.getNextEntry();
           while (entry!=null){

               if (entry.getName().endsWith(".osz")) {
                   mapsets.add(new Beatmapset(zipIn, entry));
               }
               entry= zipIn.getNextEntry();
           }
           zipIn.closeEntry();
           zipIn.close();
       }
       else if(mappackFile.getName().endsWith(".7z")){ // || mappackFile.getName().matches("\\.7z(.\\d{3})?$")
           log.debug("Detected 7Zip archive");
           SevenZFile zipFile=null;
           try{
               zipFile = new SevenZFile(mappackFile);
           } catch (FileNotFoundException e){
               System.err.printf("File %s not found.", filePath);
               System.exit(1);
           }
           assert zipFile!=null;
           SevenZArchiveEntry entry;
           entry = zipFile.getNextEntry();
           while(entry!=null){

               if (entry.getName().endsWith(".osz")) {
                   mapsets.add(new Beatmapset(zipFile.getInputStream(entry), entry));
               }
               entry= zipFile.getNextEntry();
           }
           zipFile.close();

        }
       else if(mappackFile.getName().endsWith(".rar")){
           log.debug("Detected RAR archive");
           Archive mapPackRar = new Archive(new FileInputStream(mappackFile));
           FileHeader header = mapPackRar.nextFileHeader();
           while (header!=null){
               File oszFile = new File(System.getProperty("java.io.tmpdir")+header.getFileName());
               if(!oszFile.createNewFile()){
                   oszFile.delete();
                   oszFile.createNewFile();
               }
               FileOutputStream oszFileOut = new FileOutputStream(oszFile);
               mapPackRar.extractFile(header, oszFileOut);
               oszFileOut.close();
               if(oszFile.getName().endsWith(".osz")) {
                   mapsets.add(new Beatmapset(oszFile));
               }
               oszFile.delete();
               header = mapPackRar.nextFileHeader();
           }
        }

        this.beatmapsets=mapsets.toArray(mapsets.toArray(new Beatmapset[mapsets.size()]));

    }

    public String getName() {
        return name;
    }

    public Beatmapset[] getBeatmapsets() {
        return beatmapsets;
    }

    private int computeTotalBeatmaps(){
        int total=0;
        for (Beatmapset set :
                this.beatmapsets) {
            total+=set.getBeatmaps().length;
        }

        return total;
    }

    public void writeToBinary(LittleEndianDataOutputStream outputStream, JProgressBar progressBar) throws IOException {
        log.info(String.format("Writing map pack: %s", this.name));
        for (Beatmapset beatmapset :
                this.beatmapsets) {
            beatmapset.writeToBinary(outputStream, progressBar);
        }
    }

    public void writeToBinary(LittleEndianDataOutputStream outputStream) throws IOException {
        log.info(String.format("Writing map pack: %s", this.name));
        for (Beatmapset beatmapset :
                this.beatmapsets) {
            beatmapset.writeToBinary(outputStream);
        }
    }

    public void writeAsCollection(LittleEndianDataOutputStream outputStream, JProgressBar progressBar) throws IOException {
        BinaryEditing.writeCSUTF(outputStream, this.name);
        outputStream.writeInt(0);
        outputStream.writeInt(computeTotalBeatmaps());
        this.writeToBinary(outputStream, progressBar);
        outputStream.writeInt(0);

    }

    public void writeAsCollection(LittleEndianDataOutputStream outputStream) throws IOException {
        BinaryEditing.writeCSUTF(outputStream, this.name);
        outputStream.writeInt(0);
        outputStream.writeInt(computeTotalBeatmaps());
        this.writeToBinary(outputStream);
        outputStream.writeInt(0);
    }
}
