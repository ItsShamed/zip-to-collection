package me.Shamed.osu.zip2osdb.beatmap;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.beatmap.ctb.CatchObject;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.DifficultyCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

import java.util.List;

public class WritableCatchBeatmap extends OSDBWritableBeatmap{

    private List<CatchObject> hitObjects;

    public WritableCatchBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
                        BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
                        List<CatchObject> hitObjects, byte[] hash) {
        super(generals, editorState, metadata, difficulties, breaks, timingPoints, hash);
        this.hitObjects = hitObjects;

        finalizeObjects(hitObjects);
    }

    public List<CatchObject> getHitObjects() {
        return hitObjects;
    }

    @Override
    public Gamemode getGamemode() {
        return Gamemode.CATCH;
    }

    @Override
    public Difficulty getDifficulty(Mods mods) {
        return null;
    }

    @Override
    public Difficulty getDifficulty() {
        return null;
    }

    @Override
    public DifficultyCalculator getDifficultyCalculator() {
        return null;
    }

    @Override
    public int getMaxCombo() {
        return 0;
    }

    @Override
    public int getObjectCount() {
        return 0;
    }

}
