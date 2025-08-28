package com.my.hf.app;

public class FileInfo {
    private String filename;
    private long sizeBytes;

    public FileInfo(String filename, long sizeBytes) {
        this.filename = filename;
        this.sizeBytes = sizeBytes;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    /**
     * Formats the file size in human-readable format
     * @return Formatted string like "1.2 MB", "456 KB", etc.
     */
    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return filename + " (" + getFormattedSize() + ")";
    }
}
