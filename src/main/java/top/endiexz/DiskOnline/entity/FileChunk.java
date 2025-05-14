package top.endiexz.DiskOnline.entity;

public class FileChunk {
    private Long id;
    private String fileSha256;
    private int chunkOrder;
    private Long chunkSize;
    private Long nodeId;
    private String nodeInterface;
    private Long extraNodeId;
    private String extraNodeInterface;
    private int isLoaded;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFileSha256() {
        return fileSha256;
    }
    public void setFileSha256(String fileSha256) {
        this.fileSha256 = fileSha256;
    }
    public int getChunkOrder() {
        return chunkOrder;
    }
    public void setChunkOrder(int chunkOrder) {
        this.chunkOrder = chunkOrder;
    }
    public Long getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }
    public String getNodeInterface() {
        return nodeInterface;
    }
    public void setNodeInterface(String nodeInterface) {
        this.nodeInterface = nodeInterface;
    }
    public Long getNodeId() {
        return nodeId;
    }
    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
    public Long getExtraNodeId() {
        return extraNodeId;
    }
    public void setExtraNodeId(Long extraNodeId) {
        this.extraNodeId = extraNodeId;
    }
    public String getExtraNodeInterface() {
        return extraNodeInterface;
    }
    public void setExtraNodeInterface(String extraNodeInterface) {
        this.extraNodeInterface = extraNodeInterface;
    }
    public int getIsLoaded() {
        return isLoaded;
    }
    public void setIsLoaded(int isLoaded) {
        this.isLoaded = isLoaded;
    }

    
     
}