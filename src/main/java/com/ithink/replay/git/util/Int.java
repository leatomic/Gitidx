package com.ithink.replay.git.util;

/**
 * @author le
 * @since v_0.1.0
 */
public class Int {

    /**
     * 将整数变量转化为魔数的表现形式的字符串<br>
     * 每个字节都转化成相应的字符并用单引号包裹起来，字符之间用“, ”隔开，最后首尾用大括号包裹起来<br>
     * 例如：
     * <pre>
     * {@code
     *  int signature = 0x44_49_52_43; // DIRC
     *  Int.toSignatureString(signature); // {'D', 'I', 'R', 'C'}
     * }
     * </pre>
     */
    public static String toSignatureString(int i) {

        char c1 = (char) (i >>> 24 & 0xff);
        char c2 = (char) (i >>> 16 & 0xff);
        char c3 = (char) (i >>> 8  & 0xff);
        char c4 = (char) (i & 0xff);

        return "{" +
                    "'" + c1 + "', " +
                    "'" + c2 + "', " +
                    "'" + c3 + "', " +
                    "'" + c4 + "'" +
               "}";
    }

}
