package me.Shamed.osu.zip2osdb.beatmap;

import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.beatmap.ctb.CatchBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.DifficultyCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public abstract class OSDBWritableBeatmap extends Beatmap {


    protected static final Logger log = Logger.getLogger("zip2osdb");
    protected byte[] md5;

    protected OSDBWritableBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata, BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints) {
        super(generals, editorState, metadata, difficulties, breaks, timingPoints);
    }

    protected OSDBWritableBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata, BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints, byte[] hash) {
        super(generals, editorState, metadata, difficulties, breaks, timingPoints);
        this.md5=hash;
    }

    // See https://gist.github.com/ItsShamed/c3c6c83903653d72d1f499d7059fe185#beatmap-format
    public void writeToBinary(LittleEndianDataOutputStream outputStream) throws IOException {
        log.info("Writing contents to output stream...");

        outputStream.writeInt(Integer.parseInt(this.metadata.getBeatmapId()));
        outputStream.writeInt(Integer.parseInt(this.metadata.getBeatmapSetId()));
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getArtistRomanized());
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getTitleRomanized());
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getVersion());
        BinaryEditing.writeCSUTF(outputStream, DatatypeConverter.printHexBinary(this.md5));
        BinaryEditing.writeCSUTF(outputStream, "");
        outputStream.write(GameMode.convertEKModeToByte(this.getGamemode()));
        outputStream.writeDouble(this.getDifficulty().getStars());
    }

    public byte[] getHash(){
        return this.md5;
    }

    public String getStringHash(){
        return DatatypeConverter.printHexBinary(this.md5);
    }



    public static class BeatmapConverter{

        public static OSDBWritableBeatmap makeWritable(Beatmap beatmap, byte[] hash){
            log.info("Making beatmap writable for to .osdb");
            if(beatmap instanceof OsuBeatmap){
                return new WritableOsuBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((OsuBeatmap) beatmap).getHitObjects(),
                        hash
                );
            }
            else if(beatmap instanceof TaikoBeatmap){
                return new WritableTaikoBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((TaikoBeatmap) beatmap).getHitObjects(),
                        hash
                );
            }
            else if(beatmap instanceof ManiaBeatmap){
                return new WritableManiaBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((ManiaBeatmap) beatmap).getHitObjects(),
                        hash
                );
            }
            else if (beatmap instanceof CatchBeatmap){
                return new WritableCatchBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((CatchBeatmap) beatmap).getHitObjects(),
                        hash
                );
            }
            else {
                throw new IllegalStateException();
            }
        }

        public static Beatmap makeNonWritable(OSDBWritableBeatmap beatmap){
            if(beatmap instanceof WritableOsuBeatmap){
                return new OsuBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((WritableOsuBeatmap) beatmap).getHitObjects()
                );
            }
            else if(beatmap instanceof WritableTaikoBeatmap){
                return new TaikoBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((WritableTaikoBeatmap) beatmap).getHitObjects()
                );
            }
            else if(beatmap instanceof WritableManiaBeatmap){
                return new ManiaBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((WritableManiaBeatmap) beatmap).getHitObjects()
                );
            }
            else if (beatmap instanceof WritableCatchBeatmap){
                return new CatchBeatmap(
                        beatmap.getGenerals(),
                        beatmap.getEditorState(),
                        beatmap.getMetadata(),
                        beatmap.getDifficultySettings(),
                        beatmap.getBreaks(),
                        beatmap.getTimingPoints(),
                        ((WritableCatchBeatmap) beatmap).getHitObjects()
                );
            }
            else {
                throw new IllegalStateException();
            }
        }

    }
}
