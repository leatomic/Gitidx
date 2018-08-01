package com.ithink.replay.git.model;

import com.ithink.replay.git.util.Bytes;

/**
 * @author le
 * @since v_0.1.0
 */
public class GitObject {

    private final Id oid;

    public GitObject(byte[] hash) {
        this.oid = hash == null ? null
                                : new Id(hash);
    }

    public GitObject(Id oid) {
        this.oid = oid;
    }

    public Id getOid() {
        return oid;
    }

    /**
     * SHA-1 value
     */
    public static class Id {

        /**
         *  The length in bytes and in hex digits of an object name (SHA-1 value).
         */
        public static final int GIT_SHA1_RAWSZ = 20;

        /**
         * The length in byte and in hex digits of the largest possible hash value.
         */
        public static final int GIT_SHA1_HEXSZ = (2 * GIT_SHA1_RAWSZ);

        private final byte[] hash;

        public Id(byte[] hash) {

            if (hash == null)
                throw new IllegalArgumentException("the hash can not be null");

            if (hash.length != GIT_SHA1_RAWSZ) {
                String msg = "hash size must match to GIT_SHA1_RAWSZ(" + GIT_SHA1_RAWSZ + ")"
                        + " against " + hash.length;
                throw new IllegalArgumentException(msg);
            }

            this.hash = hash;

        }

        public byte[] getHash() {
            return hash;
        }

        @Override
        public String toString() {
            return Bytes.toHexString(hash);
        }
    }
}
