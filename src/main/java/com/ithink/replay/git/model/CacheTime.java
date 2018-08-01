package com.ithink.replay.git.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 *
 * The "cache_time" is just the low 32 bits of the
 * time. It doesn't matter if it overflows - we only
 * check it for equality in the 32 bits we save.
 *
 * @author le
 * @since v_0.1.0
 */
public class CacheTime {

    private final int sec;
    private final int nsec;

    public CacheTime(int sec, int nsec) {
        this.sec = sec;
        this.nsec = nsec;
    }

    @Override
    public String toString() {
        // TODO 8-16 确认获取正确的失去偏移
        ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(sec, nsec, zoneOffset);
        return dateTime.toString();
    }
}
