package com.ithink.replay.git.model;

import com.ithink.replay.git.Displayable;
import com.ithink.replay.git.util.Int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extensions are identified by signature.
 *
 *      Git currently supports cached tree and resolve undo extension.
 *
 *      4-byte extension signature. If the first byte is 'A'..'Z' the
 *      extension is optional and can be ignored.
 *
 *      32-bit size of the extension
 *
 *      extension data
 */
public abstract class Extension implements Comparable<Extension>, Displayable {

    private int signature;
    private int size;

    protected Extension(int signature, byte[] data) {
        this.signature  = signature;
        this.size       = data.length;
    }

    /**
     *  除可选的扩展（内部的保存为字节数组）外，其余的皆不冗余地保存内容对应的字节数组
     *  <br>
     *  内容暂时由字符串数组表示
     * @return 表示内容的字符串数组
     */
    protected abstract List<String> dataLines();

    @Override
    public int compareTo(Extension other) {
        return Integer.compare(signature, other.signature);
    }

    @Override
    public List<String> toLines() {
        List<String> strs = new ArrayList<>(Arrays.asList(
                "-   signature  : " + Int.toSignatureString(signature),
                "    size       : " + size
        ));
        strs.addAll(dataLines());
        return strs;
    }

}




