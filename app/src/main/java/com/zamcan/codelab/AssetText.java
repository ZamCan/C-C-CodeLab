package com.zamcan.codelab;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Reads a bundled UTF-8 asset, bounded so a corrupted package cannot exhaust memory. */
final class AssetText {
    private static final int MAX_BYTES = 512 * 1024;

    private AssetText() {}

    static String read(Context context, String path) throws IOException {
        try (InputStream input = context.getAssets().open(path);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] block = new byte[8192];
            int length;
            int total = 0;
            while ((length = input.read(block)) != -1) {
                total += length;
                if (total > MAX_BYTES) throw new IOException("Asset is too large: " + path);
                out.write(block, 0, length);
            }
            return out.toString("UTF-8");
        }
    }
}
