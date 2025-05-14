package top.endiexz.DiskOnline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import top.endiexz.DiskOnline.entity.FileChunk;

@Mapper
public interface ChunkMapper {
    @Select("SELECT * FROM chunkInfo WHERE fileSha256 = #{fileSha256}")
    List<FileChunk> getChunksByFileSha256(@Param("fileSha256") String fileSha256);

    @Insert("INSERT INTO chunkInfo (fileSha256, chunkOrder, chunkSize, nodeId, nodeInterface, extraNodeId, extraNodeInterface, isLoaded) " +
        "VALUES (#{fileSha256}, #{chunkOrder}, #{chunkSize}, #{nodeId}, #{nodeInterface}, #{extraNodeId}, #{extraNodeInterface}, #{isLoaded})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertChunk(FileChunk fileChunk);


    //更新文件分片的状态
    @Update("UPDATE chunkInfo SET isLoaded = 1 WHERE fileSha256 = #{fileSha256} AND chunkOrder = #{chunkIndex}")
    int markChunkAsUploaded(@Param("fileSha256") String fileSha256, @Param("chunkIndex") int chunkIndex);

    //删除 分片文件信息根据fileSha256删除分片文件信息
    // 删除某个文件的所有分片信息
    @Delete("DELETE FROM chunkInfo WHERE fileSha256 = #{fileSha256} AND chunkOrder = #{chunkOrder}")
    int deleteChunksByFileSha256(@Param("fileSha256") String fileSha256, @Param("chunkOrder") int chunkOrder);

}
