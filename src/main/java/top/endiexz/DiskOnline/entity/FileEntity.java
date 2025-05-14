package top.endiexz.DiskOnline.entity;

import java.sql.Date;

public class FileEntity {
    private Long fileId;
    private String fileName;
    private String fileSha256;
    private Long createdBy;
    private Long fileSize;
    private String fileType;
    private Boolean isDirectory; // 将 tinyint(1) 映射为 Boolean 类型
    private Long chunkSize;
    private int chunkCount;
    private Long parentId;
    private Date updatedAt;
    private String originalParentPath;
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Long getFileId() {
        return fileId;
    }
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileSha256() {
        return fileSha256;
    }
    public void setFileSha256(String fileSha256) {
        this.fileSha256 = fileSha256;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public String getFileType() {
        return fileType;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public Boolean getIsDirectory() {
        return isDirectory;
    }
    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
    public Long getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }
    public String getOriginalParentPath() {
        return originalParentPath;
    }
    public void setOriginalParentPath(String originalParentPath) {
        this.originalParentPath = originalParentPath;
    }
    public int getChunkCount() {
        return chunkCount;
    }
    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }
    public Long getParentId() {
        return parentId;
    }
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
