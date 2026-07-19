package com.chekrol.dms.model;

import java.io.Serializable;

public class DocumentFile implements Serializable {
    private long id;
    private long documentId;
    private String originalName;
    private String storageName;
    private String storagePath;
    private String mimeType;
    private long fileSize;
    private boolean primaryFile;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getDocumentId() { return documentId; }
    public void setDocumentId(long documentId) { this.documentId = documentId; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStorageName() { return storageName; }
    public void setStorageName(String storageName) { this.storageName = storageName; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public boolean isPrimaryFile() { return primaryFile; }
    public void setPrimaryFile(boolean primaryFile) { this.primaryFile = primaryFile; }
}
