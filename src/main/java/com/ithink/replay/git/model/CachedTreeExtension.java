package com.ithink.replay.git.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cached tree extension contains pre-computed hashes for trees that can
 *   be derived from the index. It helps speed up tree object generation
 *   from index for a new commit.
 *
 *   When a path is updated in index, the path must be invalidated and
 *   removed from tree cache.
 */
public class CachedTreeExtension extends SupportedExtension<CachedTreeExtension.Entry> {

    /**
     * The signature for this extension is { 'T', 'R', 'E', 'E' }.
     */
    public static final int SIGNATURE = 0x54_52_45_45;

    public CachedTreeExtension(byte[] data) {
        super(SIGNATURE, data);
    }

    @Override
    protected List<Entry> convertEntries(byte[] data) {

        List<Entry> entries = new ArrayList<>();

        int pos = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x0a) {
                /*                                                                               v
                  [0]...[pos].[    (i - 4)   ][  (i - 3)  ][    (i - 2)   ][    (i - 1)   ][     i    ].[data.length-1]
                              |0x00(nil byte)|             |0x20(A space) |                |0x0a('\n')|
                        |<--path component-->||entry count|                |subtreesCount |
                        |<------------------------------- this entry -------------------------------->|

                  忽略data[i-4]即尾部的空字节,所以是pathComponent字节数组长度为(i - 5) + 1 -pos
                */
                byte[] pathComponent    = new byte[(i - 5) + 1 - pos];
                System.arraycopy(data, pos, pathComponent, 0, (i - 5) + 1 - pos);
                byte entryCount         = data[i - 3];
                byte subtreesCount      = data[i - 1];

                /*
                  entry count为负数（最高位为1）时，说明接下来的20个字节存储的仍是该树节点(entry)的数据
                  将接下来的20个字节拷贝为其object name，并在下次循环中跳过这20个字节
                */
                byte[] objectName       = null;
                if (!(entryCount < 0)) {
                    int size = GitObject.Id.GIT_SHA1_RAWSZ;
                    objectName = new byte[size];
                    System.arraycopy(data, pos, objectName, 0, size);
                    i += size;
                }

                Entry entry = new Entry(objectName, pathComponent, entryCount, subtreesCount);
                entries.add(entry);

                pos = i + 1;
            }

        }

        return entries;

    }


    /**
     * <p>each of which consists of:</p>
     *
     * <ul>
     *      <li>
     *          NUL-terminated path component (relative to its parent directory);
     *      </li>
     *      <li>
     *          ASCII decimal number of entries in the index that is covered by the
     *          tree this entry represents (entry_count);
     *      </li>
     *      <li>
     *          A space (ASCII 32);
     *      </li>
     *      <li>
     *          ASCII decimal number that represents the number of subtrees this
     *          tree has;
     *      </li>
     *      <li>
     *          A newline (ASCII 10); and
     *      </li>
     *      <li>
     *          160-bit object name for the object that would result from writing
     *          this span of index as a tree.
     *      </li>
     * </ul>
     *
     * <p>
     *      An entry can be in an invalidated state and is represented by having
     * a negative number in the entry_count field. In this case, there is no
     * object name and the next entry starts immediately after the newline.
     * When writing an invalid entry, -1 should always be used as entry_count.
     * </p>
     *
     * <p>
     *      The entries are written out in the top-down, depth-first order.  The
     * first entry represents the root level of the repository, followed by the
     * first subtree--let's call this A--of the root level (with its name
     * relative to the root level), followed by the first subtree of A (with
     * its name relative to A), ...
     * </p>
     */
    public static class Entry extends GitObject
            implements SupportedExtension.Entry {

        private final byte[] name;
        private final byte entryCount;
        private final byte subtreesCount;

        private Entry(byte[] hash, byte[] name, byte entryCount, byte subtreesCount) {
            super(hash);
            this.name           = name;
            this.entryCount     = entryCount;
            this.subtreesCount  = subtreesCount;
        }

        @Override
        public List<String> toLines() {
            return Arrays.asList(
                    "-   name_path       : " + new String(name),
                    "    object_id       : " + getOid(),
                    "    entry_count     : " + (char) entryCount,
                    "    subtrees_count  : " + (char) subtreesCount
            );
        }

    }

}