package com.ithink.replay.git.model;

import com.ithink.replay.git.util.Bytes;
import com.ithink.replay.git.Displayable;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <pre>
 *     == The Git index file has the following format
 *
 *         All binary numbers are in network byte order. Version 2 is described
 *         here unless stated otherwise.
 *
 *         - A 12-byte header consisting of
 *
 *         4-byte signature:
 *         The signature is { 'D', 'I', 'R', 'C' } (stands for "dircache")
 *
 *         4-byte version number:
 *         The current supported versions are 2, 3 and 4.
 *
 *         32-bit number of index entries.
 *
 *         - A number of sorted index entries (see below).
 *
 *         - Extensions
 *
 *         Extensions are identified by signature. Optional extensions can
 *         be ignored if Git does not understand them.
 *
 *         Git currently supports cached tree and resolve undo extensions.
 *
 *         4-byte extension signature. If the first byte is 'A'..'Z' the
 *         extension is optional and can be ignored.
 *
 *         32-bit size of the extension
 *
 *         Extension data
 *
 *         - 160-bit SHA-1 over the content of the index file before this
 *         checksum.
 * </pre>
 * <a href="https://github.com/git/git/blob/master/Documentation/technical/index-format.txt">
 *      参考github上git项目源码中的模板描述
 * </a>
 */

public class IndexState implements Displayable {

    private final CacheHeader header;
    private final SortedSet<CacheEntry> sortedIndexEntries;
    private final SortedSet<Extension> extensions = new TreeSet<>();
    private final byte[] checksum = new byte[20];

    public IndexState(CacheHeader header) {
        this.header = header;
        this.sortedIndexEntries = new TreeSet<>();
    }

    public CacheHeader getHeader() {
        return header;
    }

    public SortedSet<CacheEntry> getSortedIndexEntries() {
        return sortedIndexEntries;
    }

    public SortedSet<Extension> getExtensions() {
        return extensions;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    @Override
    public List<String> toLines() {

        List<String> strs = new ArrayList<>(128);

        strs.add("[header]");
        strs.addAll(header.toLines());
        strs.add("");
        strs.add("");

        strs.add("[index entries]");
        for (CacheEntry entry : sortedIndexEntries) {
            strs.addAll(entry.toLines());
        }
        strs.add("");
        strs.add("");

        strs.add("[extensions]");
        for (Extension extension : extensions) {
            strs.addAll(extension.toLines());
            strs.add("");
        }
        strs.add("");
        strs.add("");

        strs.add("[SHA-1 checksum]");
        strs.add(Bytes.toHexString(checksum));

        return strs;
    }

}
