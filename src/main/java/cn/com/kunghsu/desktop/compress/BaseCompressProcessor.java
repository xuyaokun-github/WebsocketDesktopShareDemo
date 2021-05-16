package cn.com.kunghsu.desktop.compress;

/**
 */
public abstract class BaseCompressProcessor {
    public abstract byte[] compress(int[] bitmap, int from, int to);
}
