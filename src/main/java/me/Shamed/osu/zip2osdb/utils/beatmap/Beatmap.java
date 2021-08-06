package me.Shamed.osu.zip2osdb.utils.beatmap;

import com.google.common.io.LittleEndianDataOutputStream;
import com.google.gson.*;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Beatmap{

    @Nullable private Integer beatmapId;
    @Nullable private Integer beatmapSetId;
    @Nullable private String artist;
    @Nullable private String title;
    @Nullable private String diffName;
    @NotNull private String hash;
    @Nullable private GameMode gameMode;
    @Nullable private Double diffRating;
    private Boolean minimal;


    @Deprecated
    public Beatmap(){
    }

    private Beatmap(String md5){
        this.hash=md5;
        this.minimal=true;
    }

    public Boolean isMinimal(){
        return minimal;
    }


    private Beatmap(Integer beatmapId, Integer beatmapSetId, String artist,
    String title, String diffName, String hash, GameMode gameMode, Double diffRating){
        this.beatmapId=beatmapId;
        this.beatmapSetId=beatmapSetId;
        this.artist=artist;
        this.title=title;
        this.diffName=diffName;
        this.hash=hash;
        this.gameMode=gameMode;
        this.diffRating=diffRating;
        this.minimal = false;
    }

    public static Beatmap getFromMD5(String hash, String apiKey){
        Logger logger = Logger.getLogger("zip2osdb");

        logger.info("Searching beatmap with MD5 Hash "+hash+"...");

        logger.info("Attempting to reach osu!...");
        try (CloseableHttpClient client = HttpClients.createDefault()){

            logger.fine("Instantiating HTTP GET request to "+String.format("https://osu.ppy.sh/api/get_beatmaps?k=%s&h=%s", apiKey, hash));
            HttpGet request = new HttpGet(String.format("https://osu.ppy.sh/api/get_beatmaps?k=%s&h=%s", apiKey, hash));

            logger.fine("Attempting to execute request...");
            return client.execute(request, classicHttpResponse -> {
                logger.fine("Attempting to read response...");
                Gson gson = new GsonBuilder().registerTypeAdapter(Beatmap.class, new BeatmapJsonDeserializer()).setPrettyPrinting().create();
                String content = new BufferedReader(new InputStreamReader(classicHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                logger.fine("Response reading successful.");

                logger.fine("Converting...");

                Beatmap beatmap = gson.fromJson(content, Beatmap.class);

                logger.info("osu! responded with:\n"+content);

                return beatmap;
            });

        } catch (Exception e){
            logger.warning("Failed to create beatmap object from osu! api: "+e.toString()+": "+e.getMessage());
            StringBuilder errorBuilder = new StringBuilder();
            for (StackTraceElement st :
                    e.getStackTrace()) {
                errorBuilder.append(st.toString()).append("\n");
            }
            logger.warning(errorBuilder.toString());
            return new Beatmap(hash);
        }
    }


    public static class BeatmapJsonDeserializer implements JsonDeserializer<Beatmap>{

        public BeatmapJsonDeserializer(){

        }

        @Override
        public Beatmap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonArray().get(0).getAsJsonObject();

            Logger logger = Logger.getLogger("zip2osdb");

            logger.fine("Deserializing json...");
            return new Beatmap(
                    Integer.parseInt(json.get("beatmap_id").getAsString()),    // beatmap ID
                    Integer.parseInt(json.get("beatmapset_id").getAsString()), // beatmapset ID
                    json.get("artist").getAsString(),                          // Artist name
                    json.get("title").getAsString(),                           // Title
                    json.get("version").getAsString(),                         // Diff name
                    json.get("file_md5").getAsString(),                        // MD5 Hash
                    GameMode.getById((byte)Integer.parseInt(json.get("mode").getAsString())), // Mode
                    json.get("difficultyrating").getAsDouble()
            );
        }
    }

    public String getTitle() {
        return title;
    }

    public Double getDiffRating() {
        return diffRating;
    }

    public Integer getBeatmapId() {
        return beatmapId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Integer getBeatmapSetId() {
        return beatmapSetId;
    }

    public String getArtist() {
        return artist;
    }

    public String getDiffName() {
        return diffName;
    }

    public String getHash() {
        return hash;
    }

    // See https://gist.github.com/ItsShamed/c3c6c83903653d72d1f499d7059fe185#beatmap-format
    public void writeToBinary(LittleEndianDataOutputStream outputStream) throws IOException {

        outputStream.writeInt(this.beatmapId);
        outputStream.writeInt(this.beatmapSetId);
        BinaryEditing.writeCSUTF(outputStream, artist);
        BinaryEditing.writeCSUTF(outputStream, title);
        BinaryEditing.writeCSUTF(outputStream, diffName);
        BinaryEditing.writeCSUTF(outputStream, hash);
        BinaryEditing.writeCSUTF(outputStream, "");
        outputStream.write(this.gameMode.getByte());
        outputStream.writeDouble(this.diffRating);
    }


}
