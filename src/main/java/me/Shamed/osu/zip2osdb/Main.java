package me.Shamed.osu.zip2osdb;

import com.github.junrar.exception.RarException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
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

        }

    }

    private static ArchiveStructure detectArchiveStructure(File file) throws IOException {

        ZipFile zipFile = new ZipFile(file);
        int totalFiles=0;
        int zipFiles=0;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry entry = entries.nextElement();
        while (entries.hasMoreElements()){
            totalFiles+=1;
            if(entry.getName().endsWith(".zip")||entry.getName().endsWith(".rar")||entry.getName().endsWith(".7z")){
                zipFiles+=1;
            }
            entry = entries.nextElement();
        }
        if(zipFiles>.5F){
            return ArchiveStructure.PACK_CONTAINER;
        } else{
            return ArchiveStructure.PACK;
        }

    }

    private static void buildGui(){

    }

    private enum ArchiveStructure{
        PACK_CONTAINER,
        PACK
    }
}
