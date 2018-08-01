package com.ithink.replay.git.model;

import com.ithink.replay.git.util.Bytes;
import com.ithink.replay.git.Displayable;

import java.util.Arrays;
import java.util.List;

/**
 *   IndexState entries are sorted in ascending order on the name field,
 *   interpreted as a string of unsigned bytes (i.e. memcmp() order, no
 *   localization, no special casing of directory separator '/'). Entries
 *   with the same name are sorted by their stage field.
 */
public class CacheEntry extends GitObject
        implements Comparable<CacheEntry>, Displayable {

    private final byte[] name;
    private final StatData statdata;
    private final Mode mode;
    private final Flags flags;

    public CacheEntry(GitObject.Id oid, byte[] name, StatData statdata, Mode mode, Flags flags) {
        super(oid);
        this.name       = name;
        this.statdata = statdata;
        this.mode       = mode;
        this.flags      = flags;
    }

    public StatData getStatData() {
        return statdata;
    }

    public Mode getMode() {
        return mode;
    }

    public Flags getFlags() {
        return flags;
    }

    public byte[] getName() {
        return name;
    }

    /**
     * IndexState entries are sorted in ascending order on the name field,
     * and entries with the same name are sorted by their stage field.
     */
    @Override
    public int compareTo(CacheEntry other) {

        int result = Bytes.compare(name, other.name);

        if (result == 0) {
            result = Byte.compare(flags.getStage(), other.getFlags().getStage());
        }

        return result;
    }

    @Override
    public List<String> toLines() {
        return Arrays.asList(
                "-   name_path  : " + new String(name),
                "    object_id  : " + getOid(),
                "    mode       : " + mode,
                "    flags      : " + flags
        );
    }

    /**
     * @author le
     * @since v_0.1.0
     */
    public static class StatData {

        private final CacheTime ctime;
        private final CacheTime mtime;
        private final int dev;
        private final int ino;
        private final int uid;
        private final int gid;
        private final int size;

        public StatData(
                int ctimeSeconds, int ctimeNanosecondFractions,
                int mtimeSeconds, int mtimeNanosecondFractions,
                int dev,
                int ino,
                int uid,
                int gid,
                int size
        ) {
            this.ctime = new CacheTime(ctimeSeconds, ctimeNanosecondFractions);
            this.mtime = new CacheTime(mtimeSeconds, mtimeNanosecondFractions);
            this.dev = dev;
            this.ino = ino;
            this.uid = uid;
            this.gid = gid;
            this.size = size;
        }

        public CacheTime getCtime() {
            return ctime;
        }

        public CacheTime getMtime() {
            return mtime;
        }

        public int getDev() {
            return dev;
        }

        public int getIno() {
            return ino;
        }

        public int getUid() {
            return uid;
        }

        public int getGid() {
            return gid;
        }

        public int getSize() {
            return size;
        }
    }

    /**
     * 32-bit mode, split into (high to low bits)
     *
     *     4-bit object type
     *       valid values in binary are 1000 (regular file), 1010 (symbolic link)
     *       and 1110 (gitlink)
     *
     *     3-bit unused
     *
     *     9-bit unix permission. Only 0755 and 0644 are valid for regular files.
     *     Symbolic links and gitlinks have value 0 in this field.
     */
    public static class Mode {

        /*
            valid values in binary are 1000 (regular file),
            1010 (symbolic link) and 1110 (gitlink)
         */
        private final byte objectType;

        /*
            Only 0755 and 0644 are valid for regular files.
            Symbolic links and gitlinks have value 0 in this field.
         */
        private final short unixPermission;

        public Mode(int mode) {
            this.objectType     = (byte)  (mode << 16 >>> 28);
            this.unixPermission = (short) (mode & 0x1ff);
        }

        @Override
        public String toString() {

            if (objectType == 0)
                return "0000 | 000";

            String type = Integer.toBinaryString(objectType);
            String permission = "" + (unixPermission >>> 6 & 0x7)
                    + (unixPermission >>> 3 & 0x7)
                    + (unixPermission & 0x7);
            return type + " | " + permission;
        }
    }

    /**
     *   A 16-bit 'flags' field split into (high to low bits)
     *
     *     1-bit assume-valid flag
     *
     *     1-bit extended flag (must be zero in version 2)
     *
     *     2-bit stage (during merge)
     *
     *     12-bit name length if the length is less than 0xFFF; otherwise 0xFFF
     *     is stored in this field.
     *
     * @author le
     * @since v_0.1.0
     */
    public static class Flags {

        private final byte assumeValid;
        private final byte extendedFlag;
        private final byte stage;
        private final short nameLength;

        public Flags(short flagsBits) {
            this.assumeValid    =    (byte) (flagsBits >>> 15);
            this.extendedFlag   =    (byte) (flagsBits << 1 >>> 14);
            this.stage          =    (byte) (flagsBits << 2 >>> 12);
            this.nameLength     =   (short) (flagsBits & 0xFFF);
        }

        public byte getAssumeValid() {
            return assumeValid;
        }

        public byte getExtendedFlag() {
            return extendedFlag;
        }

        public byte getStage() {
            return stage;
        }

        public short getNameLength() {
            return nameLength;
        }

        @Override
        public String toString() {
            return "{" +
                    "assume_valid: " + assumeValid +
                    ", extended_flag: " + extendedFlag +
                    ", stage: " + stage +
                    ", nameLength: " + nameLength +
                    '}';
        }

    }

}
