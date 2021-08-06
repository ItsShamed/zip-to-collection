import com.google.common.io.LittleEndianDataOutputStream;
import me.Shamed.osu.zip2osdb.utils.beatmap.Beatmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeatmapGatheringTest {
    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger("zip2osdb");
        logger.setLevel(Level.ALL);

        Beatmap beatmap = Beatmap.getFromMD5("c8f08438204abfcdd1a748ebfae67421", "7342f16a1e3f8cbf23b63dcb63d6adbb14d6eb6d");
        System.out.println(beatmap.getBeatmapId());
        System.out.println(beatmap.getBeatmapSetId());
        System.out.println(beatmap.getArtist());
        System.out.println(beatmap.getDiffName());
        System.out.println(beatmap.getGameMode());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LittleEndianDataOutputStream reader = new LittleEndianDataOutputStream(baos);
        beatmap.writeToBinary(reader);
        reader.flush();
        reader.close();
        baos.flush();
        baos.close();
        int i = 1;
        for(byte b : baos.toByteArray()){

            if(i<=15){
                System.out.printf("%02x ", b);
            } else{
                System.out.printf("%02x%n", b);
                i=0;
            }
            i++;


        }

    }
}
