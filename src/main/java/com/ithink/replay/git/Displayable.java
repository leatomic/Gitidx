package com.ithink.replay.git;

import java.io.*;
import java.util.List;

/**
 * <p>表示对象可以转化为一行一行的字符串，用于整齐地打印出来<p/>
 * <p>可用于输出到控制台或文件中，风格建议类似yml<p/>
 *
 */
public interface Displayable {

    /**
     * @return 只用于遍历的字符串列表，不需要支持添加删除等操作，但不能为null
     */
    List<String> toLines();

    /**
     * 打印到控制台
     */
    default void display() {
        toLines().forEach(System.out::println);
    }

    /**
     * 打印到控制台
     */
    default void dump(String filename) throws IOException {
        File file = new File(filename);

        if (!file.exists())
            file.createNewFile();

        try (
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            for (String l : toLines()) {
                bw.write(l);
                bw.newLine();
            }
            bw.flush();
        }

    }

}
