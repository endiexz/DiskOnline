package top.endiexz.DiskOnline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import top.endiexz.DiskOnline.entity.FileEntity;

@Mapper
public interface FileMapper {

    //插入文件
    /**
     * 插入一个文件记录
     * 使用 @Options 获取自动生成的主键，并赋值给 fileEntity.fileId
     */
    @Insert({
        "INSERT INTO fileInfo (",
        "fileName, fileSha256, createdBy, fileSize, fileType,",
        "isDirectory, chunkSize, chunkCount, parentId",
        ") VALUES (",
        "#{fileName}, #{fileSha256}, #{createdBy}, #{fileSize}, #{fileType},",
        "#{isDirectory}, #{chunkSize}, #{chunkCount}, #{parentId}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    void insertFile(FileEntity fileEntity);

    //插入文件夹
    @Insert({
        "INSERT INTO fileInfo (",
        "fileName, fileSha256, createdBy, fileSize, fileType,",
        "isDirectory, chunkSize, chunkCount, parentId",
        ") VALUES (",
        "#{fileName}, #{fileSha256}, #{createdBy}, #{fileSize}, #{fileType},",
        "#{isDirectory}, #{chunkSize}, #{chunkCount}, #{parentId}",
        ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    void insertFolder(FileEntity newFolder);


    //通过文件名以及parentid获取 类型是 文件夹的文件信息

    @Select("SELECT * FROM fileInfo WHERE parentId = #{parentId} AND fileName = #{fileName} AND isDirectory = 1 LIMIT 1")
    FileEntity getFolderByNameAndParentId(  @Param("fileName") String fileName,
                                            @Param("parentId") Long parentId);


    //通过absolutePath以及文件名检查是否存在同一个文件夹下面同名的文件

    //通过用户id以及父文件id对应绝对路径文件夹下面的文件
    @Select("SELECT * FROM fileInfo WHERE createdBy = #{userId} AND parentId = #{parentId}")
    List<FileEntity> getFilesByUserAndParentId(
        @Param("userId") Long userId, 
        @Param("parentId") Long parentId
    );

    @Select("SELECT * FROM fileInfo WHERE parentId = #{parentId}")
    List<FileEntity> getFilesByParentId(
        @Param("parentId") Long parentId
    );

    //通过文件id获取文件信息
    @Select("SELECT * FROM fileInfo WHERE fileId = #{fileId}")
    FileEntity getFileByFileId(
        @Param("fileId") Long fileId
    );

    //通过文件id删除文件

    @Delete("DELETE FROM fileInfo WHERE fileId = #{fileId}")
    int deleteFileByFileId(@Param("fileId") Long fileId);

    //通过fileSha256获取全部文件
    @Select("SELECT * FROM fileInfo WHERE fileSha256 = #{fileSha256}")
    List<FileEntity> getAllFileByFileSha256(
        @Param("fileSha256") String fileSha256
    );

    //根据文件id修改文件名字

    @Update({
        "UPDATE fileInfo",
        "SET fileName = #{newFileName}",
        "WHERE fileId = #{fileId}"
    })
    int updateFileNameById(@Param("fileId") Long fileId, @Param("newFileName") String newFileName);

    @Update({
        "UPDATE fileInfo",
        "SET parentId = #{parentId},",
        " originalParentPath = #{originalParentPath}",
        "WHERE fileId = #{fileId}"
    })
    int updateFileParentIdAndOriginalParentPathById(@Param("fileId") Long fileId, 
                                                     @Param("parentId") Long parentId,
                                                     @Param("originalParentPath") String originalParentPath);


}
