package com.ithink.replay.git.model;

import com.ithink.replay.git.Displayable;
import com.ithink.replay.git.util.Int;

import java.util.Arrays;
import java.util.List;

/**
 *  参考git源码中cache.h
 */
public class CacheHeader implements Displayable {

    /**
     * The signature is { 'D', 'I', 'R', 'C' } (stands for "dircache")
     */
    public static final int CACHE_SIGNATURE = 0x44_49_52_43;

    private final int signature;

    /**
     * 4-byte version number:
     *  The current supported versions are 2, 3 and 4.
     */
    private final int version;

    /**
     * number of index entries.
     */
    private final int entryCount;

    public CacheHeader(int signature, int version, int entryCount) {
        this.signature = signature;
        this.version = version;
        this.entryCount = entryCount;
    }

    public int getSignature() {
        return signature;
    }

    public int getVersion() {
        return version;
    }

    public int getEntryCount() {
        return entryCount;
    }

    @Override
    public List<String> toLines() {
        return Arrays.asList(
                "signature  : " + Int.toSignatureString(signature),
                "version    : " + version,
                "entry count: " + entryCount
        );
    }

}
