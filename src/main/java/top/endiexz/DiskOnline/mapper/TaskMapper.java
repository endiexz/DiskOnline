package top.endiexz.DiskOnline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import top.endiexz.DiskOnline.entity.UserTask;

public interface TaskMapper {
    @Insert("INSERT INTO userTask (userId, parentId, fileName, fileSha256, fileSize, chunkSize, chunkTotal, chunkUploaded, absolutePath) " +
        "VALUES (#{userId}, #{parentId}, #{fileName}, #{fileSha256}, #{fileSize}, #{chunkSize}, #{chunkTotal}, #{chunkUploaded}, #{absolutePath})")
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    int insertUserTask(UserTask userTask);

    //通过sha256获取id值
    @Select("SELECT taskId FROM userTask WHERE fileSha256 = #{fileSha256}")
    Long getTaskIdBySha256(@Param("fileSha256") String fileSha256);

    //查找相同sha256文件数量
    @Select("SELECT COUNT(*) FROM userTask WHERE fileSha256 = #{fileSha256}")
    int countTasksBySha256(@Param("fileSha256") String fileSha256);
    
    
    //根据sha256获取到该任务信息
    @Select("SELECT * FROM userTask WHERE fileSha256 = #{fileSha256}")
    UserTask getUserTaskBySha256(@Param("fileSha256") String fileSha256);

    //更新文件名与绝对路径
    @Update("UPDATE userTask SET fileName = #{fileName}, absolutePath = #{absolutePath} WHERE taskId = #{taskId}")
    int updateUserTaskPathAndName(@Param("taskId") Long taskId,
                                @Param("fileName") String fileName,
                                @Param("absolutePath") String absolutePath);

    //通过用户id获取用户文件上传任务
    @Select("SELECT * FROM userTask WHERE userId = #{userId}")
    List<UserTask> getUserTasksByUserId(@Param("userId") Long userId);

    //更新uploaded
    @Update("UPDATE userTask SET chunkUploaded = chunkUploaded + 1 WHERE fileSha256 = #{fileSha256}")
    int updateChunkUploadedBySha256(@Param("fileSha256") String fileSha256);

    //任务完成后用于删除userTask 通过fileSha256

    @Delete("DELETE FROM userTask WHERE fileSha256 = #{fileSha256}")
    int deleteUserTaskBySha256(@Param("fileSha256") String fileSha256);
}
