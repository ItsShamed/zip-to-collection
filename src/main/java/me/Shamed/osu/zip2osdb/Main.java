package me.Shamed.osu.zip2osdb;

import com.github.junrar.exception.RarException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import me.Shamed.osu.zip2osdb.beatmap.OSDBWritableBeatmap;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main{

    public static final Logger log = LogManager.getLogger();
    private static final MainActionListener listener = new MainActionListener();
    private final static Pattern debugPattern = Pattern.compile("-Xdebug|jdwp");
    private static final JFrame jFrame = new JFrame("osu! Beatmap Pack to Collection converter");
    private static JTextField inputFileField;
    private static JTextField outputFileField;
    private static JButton chooseInFileButton;
    private static JButton chooseOutFileButton;
    private static JButton convertButton;
    private static JProgressBar progressBar;

    public static void main(String[] args) throws BeatmapException, IOException, NoSuchAlgorithmException, InterruptedException, RarException, URISyntaxException, ParseException {

        if (isDebugging()) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.DEBUG);
            ctx.updateLoggers();
        }

        if (args.length == 0) {
            buildGui();
        } else if (args.length == 1) {
            File zipFile = new File(args[0]);
            if (!zipFile.exists()) {
                log.error(String.format("File %s does not exist.%n", args[0]));
                return;
            }

            switch (detectArchiveStructure(zipFile)){
                case PACK:
                    MapsetPack mapsetPack = new MapsetPack(args[0]);
                    log.info(String.format("Mapset pack %s:%n", mapsetPack.getName()));
                    for (Beatmapset set :
                            mapsetPack.getBeatmapsets()) {
                        log.info(String.format("     Beatmapset %s:%n", set.getFileName()));
                        for (OSDBWritableBeatmap beatmap : set.getBeatmaps()){
                            log.info(String.format("         Beatmap:%n" +
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
                            ));
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
                        log.error("Unknown archive format.");
                        return;
                    }


                    for (MapsetPack mapsetPack1 : packs){
                        log.info(String.format("Mapset pack %s:%n", mapsetPack1.getName()));
                        for (Beatmapset set :
                                mapsetPack1.getBeatmapsets()) {
                            log.info(String.format("     Beatmapset %s:%n", set.getFileName()));
                            for (OSDBWritableBeatmap beatmap : set.getBeatmaps()){
                                log.info(String.format("         Beatmap:%n" +
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
                                ));
                            }
                        }
                    }
                    break;

                case UNKNOWN:
                default:
                    log.error("Can't determine the str(ucture of the archive.");
                    return;

            }

        } else if(args.length==2){
            run(args[0], args[1], false);
        }

    }

    private static ArchiveStructure detectArchiveStructure(File file) throws IOException {

        log.debug(String.format("Detecting archive structure for %s", file.getName()));
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
            log.debug(String.format("Total files: %s\n.osz files:%s\nZIP Files:%s", totalFiles, oszFiles, zipFiles));
            log.debug("osz ratio: " + (((float) oszFiles) / totalFiles));
            log.debug("zip ratio: " + (((float) zipFiles) / totalFiles));
            if (((float) zipFiles) / totalFiles > .5) {
                return ArchiveStructure.PACK_CONTAINER;
            } else if (((float) oszFiles) / totalFiles > .5) {
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
            log.debug(String.format("Total files: %s\n.osz files:%s\nZIP Files:%s", totalFiles, oszFiles, zipFiles));
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

        log.info("Using arguments: in:" + inPath + " \nout:" + outPath + " \ngui?:" + gui);
        log.debug("Opening " + inPath);
        File zipFile = new File(inPath);
        OSDB collection = gui ? new OSDB(new File(outPath), jFrame) : new OSDB(new File(outPath));
        progressBar.setIndeterminate(true);
        switch (detectArchiveStructure(zipFile)) {
            case PACK:
                log.debug("Input file is a map pack");
                MapsetPack mapsetPack = new MapsetPack(inPath);
                collection.add(mapsetPack);
                break;
            case PACK_CONTAINER:
                log.debug("Input file is a container for map packs");
                if (zipFile.getName().endsWith(".zip")){
                    log.debug("Detected ZIP Archive");
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
                    log.debug("Detected 7Zip archive");
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
                    log.error("Unknown archive format.");
                    return;
                }
                break;

            case UNKNOWN:
                JOptionPane.showMessageDialog(jFrame, "Can't determine the structure of the archive.",
                        "Parsing error", JOptionPane.ERROR_MESSAGE);
                log.error("Can't determine the structure of the archive.");
                return;

        }
        progressBar.setIndeterminate(false);
        collection.write(progressBar);
    }

    public static boolean isDebugging() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (debugPattern.matcher(arg).find()) {
                return true;
            }
        }
        return false;
    }

    private static void buildGui() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        jFrame.setSize(400, 250);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
        progressBar = new JProgressBar(0);
        progressBar.setMaximumSize(new Dimension(325, 50));
        progressBar.setValue(0);
        progressBar.setEnabled(false);
        outputFileGroup.add(outputFileField);
        outputFileGroup.add(chooseOutFileButton);


        inputPanel.add(inputFileGroup);
        inputPanel.add(outputFileGroup);
        inputPanel.add(progressBar);

        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
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
                Task task = new Task();
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
                    progressBar.setEnabled(true);
                    progressBar.setMaximum(100);
                    task.execute();
                }catch (Exception ex) {
                    if (ex instanceof CancellationException) {
                        log.warn("User cancelled operation.");
                        JOptionPane.showMessageDialog(jFrame, "Conversion cancelled.", "Cancelled",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        ex.printStackTrace();
                        StringBuilder stack = new StringBuilder();
                        stack.append(ex.getMessage() + "\n\n");
                        for (StackTraceElement element : ex.getStackTrace()) {
                            stack.append(element.toString() + "\n");
                        }
                        JOptionPane.showMessageDialog(jFrame, stack.toString(), "Unexpected error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }


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

    private static class Task extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            Main.run(inputFileField.getText(), outputFileField.getText(), true);
            return null;
        }

        @Override
        protected void done() {
            progressBar.setEnabled(false);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chooseInFileButton.setEnabled(true);
            chooseOutFileButton.setEnabled(true);
            inputFileField.setEnabled(true);
            outputFileField.setEnabled(true);
            convertButton.setEnabled(true);
        }
    }
}
