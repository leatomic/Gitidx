package com.ithink.replay.git.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对字节数组({@code byte[]})进行操作的工具类<br>
 */
public class Bytes {

    private final static char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     *  将字节数组转化为十六进制的字符串，从高到底位。<br>
     *  例如：以下代码的效果是等效的
     *  <pre>
     *  {@code
     *  Bytes.toHexString(bytes);
     *  new BigInteger(bytes).toString(16);
     *  }
     *  </pre>
     *  主要用于将160bit的SHA-1值，转化成40个16进制数组成的字符串显示
     */
    public static String toHexString(byte[] bytes) {

        if (bytes == null)
            return "null";

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes)
            sb.append(digits[b >>> 4 & 0xf])
                .append(digits[b & 0xf]);

        return sb.toString();

    }

    /**
     * 将字节数组转化为多个(16进制数)行。最常见的在编辑器中以二进制的方式打开文件。<br>
     * 其中每两个字节（4个16进制数）一组，不同组之间用空格隔开，每行8组（16字节）。<br>
     * 例如:
     * <pre>
     * 4449 5243 0000 0002 0000 0005 0000 0000
     * 0000 0000 0000 0000 0000 0000 0000 0000
     * 0000 0000 0000 81a4 0000 0000 0000 0000
     * 0000 0000 fe38 0c49 b818 647b 3a22 cf9d
     * 8d00 6f9e c95b 5688
     * </pre>
     */
    public static List<String> toHexLines(byte[] bytes) {

        if (bytes == null)
            throw new IllegalArgumentException("bytes cannot be null");

        if (bytes.length == 0)
            return Collections.singletonList("");


        StringBuilder sb = new StringBuilder();
        List<String> strs = new ArrayList<>(bytes.length >> 3 + 1);

        for (int i = 0; i < bytes.length; i++) {
            char firstHex   = digits[bytes[i] >>> 4 & 0xf],
                 secondHex  = digits[bytes[i] & 0xf];
            sb.append(firstHex).append(secondHex);

            boolean isEvenOne          = (i & 0x1) == 0x1,
                    isLastOneInLine    = (i & 0xf) == 0xf,
                    isLastOne          = i == bytes.length - 1;
            if (isLastOneInLine || isLastOne) {
                strs.add(sb.toString());
                sb.setLength(0);
            } else if (isEvenOne) {
                sb.append(' ');
            }
        }


        return strs;
    }

    /**
     * 比较两个字节数组的大小。<br>
     * 规则：<br>
     *     从下标为0的开始往后比较相同下标对应的元素，若得出结果则直接返回，否则继续往后比较<br>
     *     若两个数组中下标相同的两个元素都相等，则判断两个数组的长度<br>
     */
    public static int compare(byte[] bytesA, byte[] bytesB) {

        if (bytesA == null || bytesB == null)
            throw new IllegalArgumentException("both byteA and byteB cannot be null");

        int la = bytesA.length, lb = bytesB.length;

        for (int i = 0; i < Math.min(la, lb); i++) {
            int r = Byte.compare(bytesA[i], bytesB[i]);
            if (r != 0) return r;
        }

        return Integer.compare(la, lb);
    }

}
