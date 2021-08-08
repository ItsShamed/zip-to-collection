package me.Shamed.osu.zip2osdb.beatmap;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoCircle;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoObject;
import lt.ekgame.beatmap_analyzer.difficulty.TaikoDifficulty;
import lt.ekgame.beatmap_analyzer.difficulty.TaikoDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

import java.util.List;

public class WritableTaikoBeatmap extends OSDBWritableBeatmap{

    private List<TaikoObject> hitObjects;

    public WritableTaikoBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
                        BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
                        List<TaikoObject> hitObjects, byte[] hash) {
        super(generals, editorState, metadata, difficulties, breaks, timingPoints, hash);
        this.hitObjects = hitObjects;
        finalizeObjects(hitObjects);
    }

    @Override
    public Gamemode getGamemode() {
        return Gamemode.TAIKO;
    }

    @Override
    public int getMaxCombo() {
        return (int) hitObjects.stream().filter(o->o instanceof TaikoCircle).count();
    }

    public List<TaikoObject> getHitObjects() {
        return hitObjects;
    }

    @Override
    public int getObjectCount() {
        return hitObjects.size();
    }

    @Override
    public TaikoDifficultyCalculator getDifficultyCalculator() {
        return new TaikoDifficultyCalculator();
    }

    @Override
    public TaikoDifficulty getDifficulty(Mods mods) {
        return getDifficultyCalculator().calculate(mods, BeatmapConverter.makeNonWritable(this));
    }

    @Override
    public TaikoDifficulty getDifficulty() {
        return getDifficulty(Mods.NOMOD);
    }

}
