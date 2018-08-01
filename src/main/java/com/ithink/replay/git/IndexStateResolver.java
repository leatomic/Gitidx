package com.ithink.replay.git;

import com.ithink.replay.git.model.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.SortedSet;


/**
 * <p>当前仅解析版本2的内容，部分版本3或版本4中出现的内容以及改动暂时不支持</p>
 * <p>另外扩展列表中，当前只对Cached tree和Resolve undo进行详细解析，其余的皆按可选扩展解析，后续再添加更新</p>
 */
public class IndexStateResolver {

    /**
     * 将指定index文件解析为{@link IndexState}类型的对象
     * @param filename 要解析的index文件的文件名，包含完整路径
     * @return 表示index文件内容的对象
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     * @see IndexState
     */
    public IndexState resolve(String filename) throws IOException {

        RandomAccessFile file = new RandomAccessFile(filename, "r");

        checkSignature(file);
        checkSum(file);

        CacheHeader cacheHeader = resolveHeader(file);
        IndexState theIndex = new IndexState(cacheHeader);

        resolveIndexEntries(file, theIndex);
        resolveExtensions(file, theIndex);

        file.readFully(theIndex.getChecksum());

        file.close();

        return theIndex;

    }

    /**
     * 检查魔数，检查魔数也可以考虑挪出解析文件头部
     * @param file 打开的要解析的文件
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     */
    private void checkSignature(RandomAccessFile file) throws IOException {

        int signature = file.readInt();
        if (signature != CacheHeader.CACHE_SIGNATURE)
            throw new IllegalStateException("当前文件并非index文件，请确保打开的是index文件！");

        file.seek(0);
    }

    /**
     * <p>检查文件的是否已被篡改或损坏。<p/>
     * <p>通过比较前面所有数据的SHA-1值，与最后20字节表示的是否一致来确定</p>
     * @param file 要进行检查的文件
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     */
    private void checkSum(RandomAccessFile file) throws IOException {

        // 保存上次指针指向的位置，方便下面重新恢复到该指针的位置
        long last = file.getFilePointer();
        // 需要计算的是除最后20字节外的所有内容，需要从第一个字节开始计算起
        file.seek(0);

        /*
        增量地计算文件中前file.length() - 20字节内容的SHA-1值
         */
        byte[] sum;
        try {

            long dataSize = file.length() - 20;

            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            // 先1KB，1KB地计算，步长1024byte
            int stepSizeLength = 1024;
            byte[] buf = new byte[stepSizeLength];
            for (int i = 0; i < dataSize >> 10; i++) {
                file.readFully(buf);
                digest.update(buf);
            }

            // 之后再计算最后不超过1KB的数据的
            stepSizeLength = (int) (dataSize & (stepSizeLength - 1));
            file.readFully(buf, 0, stepSizeLength);
            digest.update(buf, 0, stepSizeLength);

            sum = digest.digest();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // never happen
        }

        byte[] tarSum  = new byte[20];
        file.readFully(tarSum);

        if (! Arrays.equals(tarSum, sum)) {
            throw new IllegalStateException("文件的内容已被篡改或文件已损坏!");
        }

        // 将指针重新恢复到之前保存的指向的位置
        file.seek(last);
    }

    /**
     * 解析index文件中的头部部分的内容<br/>
     * 该步骤在打开文件后就可以执行
     * @param file 要解析的index文件
     * @return 表示被解析的index文件中头部部分内容，的{@link CacheHeader}对象
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     * @see CacheHeader
     */
    private CacheHeader resolveHeader(RandomAccessFile file) throws IOException {

        int signature               = file.readInt();
        int versionNumber           = file.readInt();
        int numberOfIndexEntries    = file.readInt();

        return new CacheHeader(signature, versionNumber, numberOfIndexEntries);

    }

    /**
     * 解析index文件中的索引条目列表部分的内容<br/>
     * 该步骤应该紧接在解析文件头部之后，否则需要手动跳过头部部分的字节
     * @param flie 要解析的index文件
     * @param output 按照某个头部来解析，解析完保存到那个对象
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     */
    private void resolveIndexEntries(RandomAccessFile flie, IndexState output) throws IOException {

        SortedSet<CacheEntry> entries = output.getSortedIndexEntries();

        for (int i = 0; i < output.getHeader().getEntryCount(); i++) {

            int ctimeSeconds                    = flie.readInt();
            int ctimeNanosecondFractions        = flie.readInt();
            int mtimeSeconds                    = flie.readInt();
            int mtimeNanosecondFractions        = flie.readInt();
            int dev                             = flie.readInt();
            int ino                             = flie.readInt();

            CacheEntry.Mode mode            = new CacheEntry.Mode(flie.readInt());

            int uid                             = flie.readInt();
            int gid                             = flie.readInt();
            int fileSize                        = flie.readInt();

            CacheEntry.StatData statdata    = new CacheEntry.StatData(
                    ctimeSeconds, ctimeNanosecondFractions,
                    mtimeSeconds, mtimeNanosecondFractions,
                    dev, ino, uid, gid, fileSize
            );

            byte[] hash = new byte[20];
            flie.readFully(hash);
            GitObject.Id oid                = new GitObject.Id(hash);

            CacheEntry.Flags flags          = new CacheEntry.Flags(flie.readShort());

            byte[] name                     = new byte[flags.getNameLength()];
            flie.readFully(name);

            CacheEntry entry = new CacheEntry(oid, name, statdata, mode, flags);
            entries.add(entry);

            /*
                skip the padding nil Bytes
             */
            int entryByteCount  = 4*4 + 4*2 + 4 + 4*3 + 20 + 2 + flags.getNameLength();
            int nilBytesCount   = 8 - (entryByteCount & 0x7);
            flie.skipBytes(nilBytesCount);

        }

    }

    /**
     * <p>解析index文件中的扩展列表部分的内容<p/>
     * <p>该步骤通常在解析完索引条目部分的内容后才执行<p/>
     * @param file 要解析的index文件
     * @param output 解析完保存到哪个对象中
     * @exception EOFException 若未读取足够的字节却已经到文件尾部
     * @exception IOException  若I/O发生错误
     */
    private void resolveExtensions(RandomAccessFile file, IndexState output) throws IOException {

        SortedSet<Extension> extensions = output.getExtensions();

        /*
            由于前面并未发现任何数据指示本index文件中，当前存有多少个扩展，
            读取时只能通过判断下一个要读的字节是否不为倒数第20个字节，来确定是否要继续往下读
         */
        long positionToTheFirstByteOfSHA1Checksum = file.length() - 20;
        while (file.getFilePointer() < positionToTheFirstByteOfSHA1Checksum) {

            int signature       = file.readInt();
            int size            = file.readInt();
            byte[] data         = new byte[size];
            file.readFully(data);

            Extension extension = convertExtension(signature, data);

            extensions.add(extension);

        }

    }

    /**
     * 根据扩展项的signature，判断其类型，并将其数据转化成相应的{@link Extension}对象
     * @param signature 扩展项的signature
     * @param data 扩展项的数据
     * @return 对应的Extension对象
     */
    private Extension convertExtension(int signature, byte[] data) {

        switch (signature) {
            case CachedTreeExtension.SIGNATURE :
                return new CachedTreeExtension(data);
            case ResolveUndoExtension.SIGNATURE:
                return new ResolveUndoExtension(data);
            default:
                return new OptionalExtension(signature, data);
        }

    }

}
