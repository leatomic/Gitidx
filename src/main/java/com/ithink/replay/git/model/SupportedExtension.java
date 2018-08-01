package com.ithink.replay.git.model;

import com.ithink.replay.git.Displayable;

import java.util.ArrayList;
import java.util.List;

public abstract class SupportedExtension<E extends SupportedExtension.Entry>
        extends Extension {

    /**
     * A series of entries fill the entire extension;
     */
    private final List<E> entries = new ArrayList<>();

    protected SupportedExtension(int signature, byte[] data) {
        super(signature, data);
        entries.addAll(convertEntries(data));
    }

    protected abstract List<E> convertEntries(byte[] data);

    public List<? extends Entry> getEntries() {
        return entries;
    }

    @Override
    public List<String> dataLines() {

        List<String> strs = new ArrayList<>();

        strs.add("    entries:");
        for (Entry entry : entries) {
            entry.toLines()
                    .forEach(str -> strs.add("\t" + str));
        }

        return strs;
    }


    interface Entry extends Displayable {

    }
}
