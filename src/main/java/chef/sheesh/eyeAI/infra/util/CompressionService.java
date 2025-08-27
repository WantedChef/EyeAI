package chef.sheesh.eyeAI.infra.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;

/**
 * Service for compressing and decompressing data
 */
public class CompressionService {

    /**
     * Compress data using GZIP
     */
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutput = new GZIPOutputStream(outputStream)) {

            gzipOutput.write(data);
            gzipOutput.finish();
            return outputStream.toByteArray();
        }
    }

    /**
     * Decompress GZIP data
     */
    public byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length == 0) {
            return new byte[0];
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInput = new GZIPInputStream(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = gzipInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        }
    }

    /**
     * Get compression ratio
     */
    public double getCompressionRatio(byte[] original, byte[] compressed) {
        if (original == null || original.length == 0) {
            return 0.0;
        }
        if (compressed == null || compressed.length == 0) {
            return 0.0;
        }

        return (double) compressed.length / original.length;
    }

    /**
     * Check if data is likely compressed (simple heuristic)
     */
    public boolean isLikelyCompressed(byte[] data) {
        if (data == null || data.length < 2) {
            return false;
        }

        // Check for GZIP magic bytes
        return data[0] == (byte) 0x1f && data[1] == (byte) 0x8b;
    }
}
