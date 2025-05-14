package top.endiexz.DiskOnline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import top.endiexz.DiskOnline.entity.StorageNode;

@Mapper
public interface StorageNodeMapper {
    @Insert("INSERT INTO storage_node (id, nodeName, totalCapacity, usedCapacity, fingerprint, status, runStatus, nodeInterface, netFlow) " +
            "VALUES (#{id}, #{nodeName}, #{totalCapacity}, #{usedCapacity}, #{fingerprint}, #{status}, #{runStatus}, #{nodeInterface}, #{netFlow})")
    void insertStorageNode(StorageNode storageNode);

    //通过id查询
    @Select("SELECT * FROM storage_node WHERE id = #{id}")
    StorageNode getStorageNodeById(@Param("id") Long id);

    @Update("UPDATE storage_node SET status = #{status}, updatedAt = NOW() WHERE id = #{id}")
    void updateStatusById(@Param("id") Long id, @Param("status") String status);

    // 分页查询节点
    @Select("SELECT id, nodeName, totalCapacity, usedCapacity, fingerprint, status, runStatus, createdAt, updatedAt, nodeInterface, netFlow " +
    "FROM storage_node " +
    "WHERE id != 1 " +  // 排除 id = 1 的记录
    "LIMIT #{size} OFFSET #{offset}")
    List<StorageNode> listNodes(@Param("size") int size, 
                         @Param("offset") int offset);
    // 查询所有节点（排除 id = 1）
    @Select("SELECT id, nodeName, totalCapacity, usedCapacity, fingerprint, status, runStatus, createdAt, updatedAt, nodeInterface, netFlow " +
    "FROM storage_node " +
    "WHERE id != 1")
    List<StorageNode> listAllNodes();
    //获取除了id=1的所有节点的信息
    @Select("SELECT COUNT(*) FROM storage_node WHERE id != 1")
    int getTotalNodeCount();
    //根据节点名字查询除了id=1的所有节点的信息
    @Select("SELECT id, nodeName, totalCapacity, usedCapacity, fingerprint, status, runStatus, createdAt, updatedAt, nodeInterface, netFlow " +
    "FROM storage_node " +
    "WHERE nodeName = #{query} " +  // 精确查找节点名称
    "AND id != 1 " +  // 排除 id = 1 的记录
    "LIMIT 1")  // 限制查询结果只返回一条记录
    List<StorageNode> findNodeByName(@Param("query") String query);


    @Select("SELECT id, nodeName, totalCapacity, usedCapacity, fingerprint, status, runStatus, createdAt, updatedAt, nodeInterface, netFlow " +
    "FROM storage_node " +
    "WHERE nodeInterface = #{nodeInterface} " +  // 精确查找节点名称
    "LIMIT 1")  // 限制查询结果只返回一条记录
    StorageNode getNodeByInterface(@Param("nodeInterface") String nodeInterface);



    // 根据 nodeId 更新节点状态
    @Update("UPDATE storage_node SET status = #{status} WHERE id = #{nodeId}")
    void updateNodeStatus(@Param("status") String status, @Param("nodeId") long nodeId);

    //增加对应节点的使用容量
    @Update("UPDATE storage_node SET usedCapacity = usedCapacity + #{chunkSize} WHERE nodeInterface = #{nodeInterface}")
    int increaseUsedCapacityByInterface(
        @Param("nodeInterface") String nodeInterface, 
        @Param("chunkSize") long chunkSize
    );


    // 根据节点接口获取节点 ID
    @Select("SELECT id FROM storage_node WHERE nodeInterface = #{nodeInterface}")
    int getNodeIdByInterface(@Param("nodeInterface") String nodeInterface);
    // 根据节点接口获取 fingerprint
    @Select("SELECT fingerprint FROM storage_node WHERE nodeInterface = #{nodeInterface}")
    String getFingerprintByInterface(@Param("nodeInterface") String nodeInterface);









    // 根据节点接口获取公钥
    @Select("SELECT publicKey FROM nodePublicKey WHERE nodeInterface = #{nodeInterface}")
    String getPublicKeyByInterface(@Param("nodeInterface") String nodeInterface);
    // 插入一个节点的公钥记录
    @Insert("INSERT INTO nodePublicKey (nodeId, nodeInterface, publicKey) " +
    "VALUES (#{nodeId}, #{nodeInterface}, #{publicKey})")
    void insertNodePublicKey(@Param("nodeId") int nodeId,
             @Param("nodeInterface") String nodeInterface,
             @Param("publicKey") String publicKey);

    


}