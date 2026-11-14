package com.zamcan.codelab;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Local UTF-8 source files. Names cannot escape the project directory. */
public final class ProjectStore {
    public static final int MAX_BYTES = 256 * 1024;
    private final File root;
    public ProjectStore(File root) throws IOException {
        this.root = root;
        if (!root.isDirectory() && !root.mkdirs()) throw new IOException("Cannot open project storage");
    }
    private File resolve(String name) throws IOException {
        if (name == null || !name.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,47}\\.(c|cpp|h|hpp)"))
            throw new IOException("Use a short filename with letters, numbers, underscores, or hyphens and .c, .cpp, .h, or .hpp");
        File file = new File(root, name);
        if (!file.getCanonicalFile().getParentFile().equals(root.getCanonicalFile())) throw new IOException("Invalid source path");
        return file;
    }
    public List<String> list() {
        String[] names = root.list((dir, name) -> name.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,47}\\.(c|cpp|h|hpp)"));
        List<String> result = new ArrayList<>(names == null ? Collections.emptyList() : Arrays.asList(names));
        Collections.sort(result);
        return result;
    }
    public boolean exists(String name) throws IOException { return resolve(name).exists(); }
    public String read(String name) throws IOException {
        File file = resolve(name);
        if (file.length() > MAX_BYTES) throw new IOException("Source exceeds 256 KB");
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
    public void write(String name, String source) throws IOException {
        File target = resolve(name);
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IOException("Source exceeds 256 KB");
        File temp = File.createTempFile("source-", ".tmp", root);
        try {
            try (FileOutputStream out = new FileOutputStream(temp)) { out.write(bytes); out.getFD().sync(); }
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { if (temp.exists()) temp.delete(); }
    }
}
