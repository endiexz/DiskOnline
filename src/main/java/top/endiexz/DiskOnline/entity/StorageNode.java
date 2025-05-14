package top.endiexz.DiskOnline.entity;

import java.time.LocalDateTime;

public class StorageNode {
    private Long id;
    private String nodeName;
    private Long totalCapacity;
    private Long usedCapacity;
    private Long netFlow;
    private String fingerprint;
    private String status;
    private String runStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String nodeInterface;

    // Getters & Setters


    public Long getId() {
        return id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Long getUsedCapacity() {
        return usedCapacity;
    }

    public void setUsedCapacity(Long usedCapacity) {
        this.usedCapacity = usedCapacity;
    }


    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNodeInterface() {
        return nodeInterface;
    }

    public void setNodeInterface(String nodeInterface) {
        this.nodeInterface = nodeInterface;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Long getNetFlow() {
        return netFlow;
    }

    public void setNetFlow(Long netFlow) {
        this.netFlow = netFlow;
    }
}
