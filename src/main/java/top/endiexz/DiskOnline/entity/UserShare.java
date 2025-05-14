package top.endiexz.DiskOnline.entity;

import java.sql.Date;

public class UserShare {
    private Long id;
    private Long userId;
    private String fileName;
    private String code;
    private Long fileId;
    private int days;
    private int visitTimes;
    private Boolean isDirectory;
    private Date createTime;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public Long getFileId() {
        return fileId;
    }
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
    public int getDays() {
        return days;
    }
    public void setDays(int days) {
        this.days = days;
    }
    public int getVisitTimes() {
        return visitTimes;
    }
    public void setVisitTimes(int visitTimes) {
        this.visitTimes = visitTimes;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Boolean getIsDirectory() {
        return isDirectory;
    }
    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
