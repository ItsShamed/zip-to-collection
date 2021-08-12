package me.Shamed.osu.zip2osdb;

import com.github.junrar.exception.RarException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main{

    public static final Logger log = Logger.getLogger("zip2osdb");
    private static final MainActionListener listener = new MainActionListener();
    private static final JFrame jFrame = new JFrame("osu! Beatmap Pack to Collection converter");
    private static JTextField inputFileField;
    private static JTextField outputFileField;
    private static JButton chooseInFileButton;
    private static JButton chooseOutFileButton;
    private static JButton convertButton;

    public static void main(String[] args) throws BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, RarException, URISyntaxException, ParseException {
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
                    System.err.println("Can't determine the str(ucture of the archive.");
                    return;

            }

        } else if(args.length==2){
            run(args[0], args[1], false);
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

    private static void run(String inPath, String outPath, boolean gui) throws IOException, RarException, BeatmapException, NoSuchAlgorithmException, InterruptedException, ParseException {
        File zipFile = new File(inPath);
        OSDB collection = gui ? new OSDB(new File(outPath), jFrame) : new OSDB(new File(outPath));
        switch (detectArchiveStructure(zipFile)){
            case PACK:
                MapsetPack mapsetPack = new MapsetPack(inPath);
                collection.add(mapsetPack);
                break;
            case PACK_CONTAINER:
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
                        collection.add(new MapsetPack(System.getProperty("java.io.tmpdir")+entry.getName()));
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
                        collection.add(new MapsetPack(System.getProperty("java.io.tmpdir")+entry.getName()));
                        packFile.delete();
                        entry = packContainer.getNextEntry();
                    }
                } else {
                    System.err.println("Unknown archive format.");
                    return;
                }
                break;

            case UNKNOWN:
                System.err.println("Can't determine the structure of the archive.");
                return;

        }
        collection.write();
    }

    private static void buildGui(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception ex) {
            ex.printStackTrace();
        }

        jFrame.setSize(400, 200);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));

        JPanel inputFileGroup = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder("Input Archive");
        inputFileGroup.setBorder(border);
        inputFileGroup.setLayout(new FlowLayout());
        inputFileField = new JTextField(20);
        chooseInFileButton = new JButton("Choose...");
        chooseInFileButton.setActionCommand("chooseInput");
        chooseInFileButton.addActionListener(listener);
        inputFileGroup.add(inputFileField);
        inputFileGroup.add(chooseInFileButton);

        JPanel outputFileGroup = new JPanel();
        TitledBorder border1 = BorderFactory.createTitledBorder("Output .osdb");
        outputFileGroup.setBorder(border1);
        outputFileGroup.setLayout(new FlowLayout());
        outputFileField = new JTextField(20);
        chooseOutFileButton = new JButton("Choose...");
        chooseOutFileButton.setActionCommand("chooseOutput");
        chooseOutFileButton.addActionListener(listener);
        outputFileGroup.add(outputFileField);
        outputFileGroup.add(chooseOutFileButton);



        inputPanel.add(inputFileGroup);
        inputPanel.add(outputFileGroup);

        JPanel bottom = new JPanel();
        convertButton = new JButton("Convert");
        convertButton.setActionCommand("run");
        convertButton.addActionListener(listener);
        bottom.add(convertButton);




        jFrame.getContentPane().add(BorderLayout.CENTER, inputPanel);
        jFrame.getContentPane().add(BorderLayout.SOUTH, bottom);
        jFrame.setVisible(true);
    }

    private enum ArchiveStructure{
        PACK_CONTAINER,
        PACK,
        UNKNOWN
    }

    private static class MainActionListener implements ActionListener{

        public MainActionListener(){}

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equalsIgnoreCase("chooseInput")){
                final JFileChooser inputFileChooser = new JFileChooser();
                inputFileChooser.addChoosableFileFilter(new OpenFileFilter(".zip","Zip Archive"));
                inputFileChooser.addChoosableFileFilter(new OpenFileFilter(".7z","7Zip Archive"));
                int r = inputFileChooser.showOpenDialog(jFrame);
                if(r==JFileChooser.APPROVE_OPTION){
                    inputFileField.setText(inputFileChooser.getSelectedFile().getPath());
                }
            } else if (e.getActionCommand().equalsIgnoreCase("chooseOutput")){
                final JFileChooser outputFileChooser = new JFileChooser();
                int r = outputFileChooser.showSaveDialog(jFrame);
                if(r==JFileChooser.APPROVE_OPTION){
                    if(outputFileChooser.getSelectedFile().getName().endsWith(".osdb")) {
                        outputFileField.setText(outputFileChooser.getSelectedFile().getPath());
                    }else {
                        outputFileField.setText(outputFileChooser.getSelectedFile().getPath()+".osdb");
                    }
                }
            } else if(e.getActionCommand().equalsIgnoreCase("run")){

                if((inputFileField.getText().equalsIgnoreCase("")||outputFileField.getText().equalsIgnoreCase(""))
                        ||(new File(inputFileField.getText()).isDirectory()||new File(outputFileField.getText()).isDirectory())){
                    JOptionPane.showMessageDialog(jFrame, "Invalid path for input/output file", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                chooseInFileButton.setEnabled(false);
                chooseOutFileButton.setEnabled(false);
                inputFileField.setEnabled(false);
                outputFileField.setEnabled(false);
                convertButton.setEnabled(false);
                jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                try {
                    run(inputFileField.getText(), outputFileField.getText(), true);
                    JOptionPane.showMessageDialog(jFrame, "Conversion finished!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
                }catch (Exception ex){
                    ex.printStackTrace();
                    StringBuilder stack = new StringBuilder();
                    stack.append(ex.getMessage()+"\n\n");
                    for (StackTraceElement element : ex.getStackTrace()){
                        stack.append(element.toString()+"\n");
                    }
                    JOptionPane.showMessageDialog(jFrame, stack.toString(), "Unexpected error",
                            JOptionPane.ERROR_MESSAGE);
                }
                jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                chooseInFileButton.setEnabled(true);
                chooseOutFileButton.setEnabled(true);
                inputFileField.setEnabled(true);
                outputFileField.setEnabled(true);
                convertButton.setEnabled(true);

            }
        }
    }

    private static class OpenFileFilter extends FileFilter {

        String description = "";
        String fileExt = "";

        public OpenFileFilter(String extension) {
            fileExt = extension;
        }

        public OpenFileFilter(String extension, String typeDescription) {
            fileExt = extension;
            this.description = typeDescription;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            return (f.getName().toLowerCase().endsWith(fileExt));
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
