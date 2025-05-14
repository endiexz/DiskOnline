package top.endiexz.DiskOnline.entity;

import java.sql.Date;

public class UserTask {
    long taskId;
    long userId;
    long parentId;
    String fileName;
    String fileSha256;
    String absolutePath;
    long fileSize;
    long chunkSize;
    int chunkTotal;
    int chunkUploaded;
    Date createdAt;
    Date updateAt;

    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    public long getTaskId() {
        return taskId;
    }
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getParentId() {
        return parentId;
    }
    public void setParentId(long parentId) {
        this.parentId = parentId;
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
    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public long getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }
    public int getChunkTotal() {
        return chunkTotal;
    }
    public void setChunkTotal(int chunkTotal) {
        this.chunkTotal = chunkTotal;
    }
    public int getChunkUploaded() {
        return chunkUploaded;
    }
    public void setChunkUploaded(int chunkUploaded) {
        this.chunkUploaded = chunkUploaded;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdateAt() {
        return updateAt;
    }
    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }


}
