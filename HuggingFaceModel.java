package com.my.hf.app;

public class HuggingFaceModel {
    private String id;
    private int downloads;
    private String lastModified;
    private String tags;

    public HuggingFaceModel(String id, int downloads, String lastModified, String tags) {
        this.id = id;
        this.downloads = downloads;
        this.lastModified = lastModified;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return id + " (" + downloads + " downloads)";
    }
}