package labs.demo.chaos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Random;
import java.util.function.ToIntBiFunction;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

/** OOM simulado: descompacta um payload gerado em memória num buffer limitado e o libera; nunca derruba a JVM. */
final class OomFault implements ToIntBiFunction<Scenario, Random> {

    static final int LIMIT = 16 * 1024 * 1024;

    @Override
    public int applyAsInt(Scenario s, Random random) {
        try {
            return unzip(zip(s.getBytes()), LIMIT) < 0 ? 507 : s.getStatus();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Zip com uma entrada de size bytes zerados (comprime muito: payload pequeno, descompactado grande). */
    static byte[] zip(int size) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(out)) {
            zip.putArchiveEntry(new ZipArchiveEntry("payload.bin"));
            byte[] chunk = new byte[64 * 1024];
            for (int left = size; left > 0; left -= chunk.length) {
                zip.write(chunk, 0, Math.min(left, chunk.length));
            }
            zip.closeArchiveEntry();
        }
        return out.toByteArray();
    }

    /** Total descompactado, ou -1 assim que passaria do limite. */
    static long unzip(byte[] zip, int limit) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[64 * 1024];
        try (ZipFile file = new ZipFile(new SeekableInMemoryByteChannel(zip))) {
            for (ZipArchiveEntry entry : Collections.list(file.getEntries())) {
                try (InputStream in = file.getInputStream(entry)) {
                    for (int n; (n = in.read(chunk)) > 0; ) {
                        if (buffer.size() + n > limit) {
                            return -1;
                        }
                        buffer.write(chunk, 0, n);
                    }
                }
            }
        }
        return buffer.size();
    }
}
