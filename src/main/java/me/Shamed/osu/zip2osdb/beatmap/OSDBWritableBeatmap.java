package me.Shamed.osu.zip2osdb.beatmap;

import com.google.common.io.LittleEndianDataOutputStream;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.beatmap.ctb.CatchBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import me.Shamed.osu.zip2osdb.Beatmapset;
import me.Shamed.osu.zip2osdb.utils.BinaryEditing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public abstract class OSDBWritableBeatmap extends Beatmap {


    protected static final Logger log = LogManager.getLogger(Beatmapset.class);
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
        log.debug("Writing beatmap: " + this.metadata.getBeatmapId() +
                "...");

        outputStream.writeInt(Integer.parseInt(this.metadata.getBeatmapId()));
        outputStream.writeInt(Integer.parseInt(this.metadata.getBeatmapSetId()));
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getArtistRomanized());
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getTitleRomanized());
        BinaryEditing.writeCSUTF(outputStream, this.metadata.getVersion());
        BinaryEditing.writeCSUTF(outputStream, DatatypeConverter.printHexBinary(this.md5).toLowerCase(Locale.ROOT));
        BinaryEditing.writeCSUTF(outputStream, "");
        outputStream.write(GameMode.convertEKModeToByte(this.getGamemode()));
        outputStream.writeDouble(this.getDifficulty().getStars());
    }

    public byte[] getHash(){
        return this.md5;
    }

    public String getStringHash(){
        return DatatypeConverter.printHexBinary(this.md5).toLowerCase(Locale.ROOT);
    }

    protected void finalizeObjects(List<? extends HitObject> objects) {
        ListIterator<TimingPoint> timingIterator = timingPoints.listIterator();
        ListIterator<? extends HitObject> objectIterator = objects.listIterator();

        // find first parent point
        TimingPoint parent = null;
        while (parent == null || parent.isInherited())
            parent = timingIterator.next();

        while (true) {
            TimingPoint current = timingIterator.hasNext() ? timingIterator.next() : null;
            TimingPoint previous = timingPoints.get(timingIterator.previousIndex() - (current == null ? 0 : 1));
            if (!previous.isInherited()) parent = previous;

            while (objectIterator.hasNext()) {
                HitObject object = objectIterator.next();
                if (current == null || object.getStartTime() < current.getTimestamp()) {
                    object.finalize(previous, parent, BeatmapConverter.makeNonWritable(this));
                }
                else {
                    objectIterator.previous();
                    break;
                }
            }

            if (current == null) break;
        }
    }

    public static class BeatmapConverter{

        public static OSDBWritableBeatmap makeWritable(Beatmap beatmap, byte[] hash){
            log.debug("Making beatmap writable for to .osdb");
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
                throw new IllegalStateException("Got: "+beatmap.getClass().getName());
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
