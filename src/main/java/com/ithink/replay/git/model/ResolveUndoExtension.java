package com.ithink.replay.git.model;

import java.util.*;

/**
 *  <p>
 *      A conflict is represented in the index as a set of higher stage entries.
 *   When a conflict is resolved (e.g. with "git add path"), these higher
 *   stage entries will be removed and a stage-0 entry with proper resolution
 *   is added.
 *  </p>
 *  <br>
 *  <p>
 *      When these higher stage entries are removed, they are saved in the
 *   resolve undo extension, so that conflicts can be recreated (e.g. with
 *   "git checkout -m"), in case users want to redo a conflict resolution
 *   from scratch.
 *  </p>
 */
public class ResolveUndoExtension extends SupportedExtension<ResolveUndoExtension.Entry> {

    /**
     * The signature for this extension is { 'R', 'E', 'U', 'C' }.
     */
    public static final int SIGNATURE = 0x52_45_55_43;

    public ResolveUndoExtension(byte[] data) {

        super(SIGNATURE, data);

    }

    @Override
    protected List<Entry> convertEntries(byte[] data) {

        ArrayList<Entry> entries = new ArrayList<>();
        byte[] stage0 = {0x30};    // Integer.toOctalString(0).getBytes();

        byte[][] byteses = new byte[4][];
        int mark = 0, pos = 1;
        while (pos < data.length) {

            /*
             * 读取path name以及3个entry mode
             */
            for (int i = 0; i < 4; i++) {

                while (pos < data.length && data[pos] != 0x0)
                    pos ++;

                int offset = pos - mark;
                byte[] bytes = byteses[i] = new byte[offset];
                System.arraycopy(data, mark, bytes, 0, offset);

                mark = pos + 1;
                pos = mark + 1;
            }

            byte[] pathName = byteses[0];
            Entry entry = new Entry(pathName);

            Entry.ResolveUndoInfoItem[] info = entry.getInfo();
            /*
             * 遍历3个entry mode，并生成对应的对象的object name（根据其转化为32bit整形时是否为0，来判断额外读取20字节还是空字节）
             * 再讲每个entry mode,及生成的object name组合成info item，依次设置为entry的 info item
             */
            for (int i = 1; i < 4; i++) {

                int mode                    = Integer.valueOf(new String(byteses[i]), 8);
                byte[] objectName           = null;

                if (!Arrays.equals(byteses[i], stage0)) {
                    int size = GitObject.Id.GIT_SHA1_RAWSZ;

                    objectName = new byte[size];
                    System.arraycopy(data, mark, objectName, 0, size);

                    mark += size;
                    pos = mark + 1;
                }

                info[i - 1] = new Entry.ResolveUndoInfoItem(objectName, mode);
            }

            entries.add(entry);
        }


        return entries;
    }

    /**
     * A series of entries fill the entire extension; each of which
     *   consists of:
     *  <ul>
     *      <li>
     *          NUL-terminated pathname the entry describes (relative to the root of
     *     the repository, i.e. full pathname);
     *      </li>
     *      <li>
     *          Three NUL-terminated ASCII octal numbers, entry mode of entries in
     *     stage 1 to 3 (a missing stage is represented by "0" in this field);
     *     and
     *      </li>
     *      <li>
     *          At most three 160-bit object names of the entry in stages from 1 to 3
     *     (nothing is written for a missing stage).
     *      </li>
     *  </ul>
     */
    public static class Entry implements SupportedExtension.Entry {

        private final byte[] lost;
        private final ResolveUndoInfoItem[] ui = new ResolveUndoInfoItem[3];

        public Entry(byte[] pathName) {
            this.lost = pathName;
        }

        public ResolveUndoInfoItem[] getInfo() {
            return ui;
        }

        @Override
        public List<String> toLines() {

            List<String> strs = new ArrayList<>(Collections.singletonList(
                    "-   name_path     : " + new String(lost)
            ));

            for (int i = 1; i <= 3; i ++) {
                ResolveUndoInfoItem infoItem = ui[i-1];
                strs.add("\tmode[" + i + "]       : " + infoItem.getMode());
                strs.add("\tobject_id[" + i + "]  : " + infoItem.getOid());
            }
            return strs;
        }

        /**
         *
         */
        public static class ResolveUndoInfoItem extends GitObject {

            private final CacheEntry.Mode mode;

            public ResolveUndoInfoItem(byte[] hash, int mode) {
                super(hash);
                this.mode = new CacheEntry.Mode(mode);
            }

            public CacheEntry.Mode getMode() {
                return mode;
            }
        }

    }
}
