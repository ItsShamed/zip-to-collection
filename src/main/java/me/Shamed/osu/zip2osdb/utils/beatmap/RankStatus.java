package me.Shamed.osu.zip2osdb.utils.beatmap;

import java.util.HashMap;
import java.util.Map;

public enum RankStatus {

    GRAVEYARD(-2),
    WIP(-1),
    PENDING(0),
    RANKED(1),
    APPROVED(2),
    QUALIFIED(3),
    LOVED(4);

    private final Integer id;
    private static final Map<Integer, RankStatus> BY_ID = new HashMap<>();

    private RankStatus(final Integer id){
        this.id = id;
    }

    public Integer getId(){
        return this.id;
    }

    public static RankStatus getById(Integer id) {
        return BY_ID.get(id);
    }

    static {
        for (RankStatus status :
                values()) {
            BY_ID.put(status.id, status);
        }
    }

}
