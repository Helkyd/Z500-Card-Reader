package ao.metagest.z500cardreader.utils;


import java.io.Closeable;
import java.io.IOException;

public final class IOUtil {
    private IOUtil() {
        throw new AssertionError("create IOUtil instance is forbidden");
    }

    /**
     * 关闭IO对象
     *
     * @param src 源IO对象
     */
    public static void close(Closeable src) {
        if (src != null) {
            try {
                src.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
