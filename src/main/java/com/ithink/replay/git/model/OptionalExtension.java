package com.ithink.replay.git.model;

import com.ithink.replay.git.util.Bytes;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional extension can
 *      be ignored if Git does not understand them.
 */
public class OptionalExtension extends Extension {

    private final byte[] data;

    public OptionalExtension(int signature, byte[] data) {
        super(signature, data);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public List<String> dataLines() {

        List<String> dataStrings = Bytes.toHexLines(data);

        List<String> lines = new ArrayList<>(dataStrings.size() + 1);

        lines.add("data: ");
        dataStrings.forEach( str -> lines.add('\t' + str));

        return lines;
    }

}
