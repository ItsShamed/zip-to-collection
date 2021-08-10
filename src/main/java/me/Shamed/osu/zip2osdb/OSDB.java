package me.Shamed.osu.zip2osdb;

import com.google.common.io.LittleEndianDataOutputStream;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class OSDB {

    public final String version = "o!dm8";
    private final File out;
    private final List<MapsetPack> packs;
    private final Date creationDate;
    private String lastEditor;

    public OSDB(File file){
        this.out = file;
        this.packs = new ArrayList<>();
        this.creationDate=new Date();
    }

    public void add(MapsetPack pack){
        packs.add(pack);
    }

    public MapsetPack[] getPacks() {
        return packs.toArray(new MapsetPack[packs.size()]);
    }

    public void write() throws IOException, ParseException {
        if(!out.createNewFile()){
            System.out.printf("%s already exists. Do you want to overwrite? (y/n): ", out.getName());
            Scanner scanner = new Scanner(System.in);
            if(scanner.nextLine().equalsIgnoreCase("y")){
                out.delete();
                out.createNewFile();
            } else{
                System.exit(0);
            }
            LittleEndianDataOutputStream outputStream = new LittleEndianDataOutputStream(new FileOutputStream(this.out));
            BinaryEditing.writeCSUTF(outputStream, this.version);
            outputStream.writeDouble(BinaryEditing.convertToOADate(creationDate));
            BinaryEditing.writeCSUTF(outputStream, System.getProperty("user.name"));
            outputStream.writeInt(1);
            for (MapsetPack pack :
                    this.packs) {
                pack.writeAsCollection(outputStream);
            }
            BinaryEditing.writeCSUTF(outputStream, "By Piotrekol");
            outputStream.close();

        }


    }


}