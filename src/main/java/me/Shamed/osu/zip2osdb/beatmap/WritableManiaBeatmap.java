package me.Shamed.osu.zip2osdb.beatmap;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaObject;
import lt.ekgame.beatmap_analyzer.difficulty.ManiaDifficulty;
import lt.ekgame.beatmap_analyzer.difficulty.ManiaDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

import java.util.List;

public class WritableManiaBeatmap extends OSDBWritableBeatmap{

    private List<ManiaObject> hitObjects;

    public WritableManiaBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
                        BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
                        List<ManiaObject> hitObjects, byte[] hash) {
        super(generals, editorState, metadata, difficulties, breaks, timingPoints, hash);
        this.hitObjects = hitObjects;

        finalizeObjects(hitObjects);
    }

    @Override
    public Gamemode getGamemode() {
        return Gamemode.MANIA;
    }

    @Override
    public ManiaDifficultyCalculator getDifficultyCalculator() {
        return new ManiaDifficultyCalculator();
    }

    @Override
    public ManiaDifficulty getDifficulty(Mods mods) {
        return getDifficultyCalculator().calculate(mods, BeatmapConverter.makeNonWritable(this));
    }

    @Override
    public ManiaDifficulty getDifficulty() {
        return getDifficulty(Mods.NOMOD);
    }

    public List<ManiaObject> getHitObjects() {
        return hitObjects;
    }

    public int getCollumns() {
        return (int)difficulties.getCS();
    }

    @Override
    public int getMaxCombo() {
        return hitObjects.stream().mapToInt(o->o.getCombo()).sum();
    }

    @Override
    public int getObjectCount() {
        return hitObjects.size();
    }

}
