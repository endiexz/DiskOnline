package top.endiexz.DiskOnline.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import top.endiexz.DiskOnline.entity.UserShare;

@Mapper
public interface UserShareMapper {
    @Insert({
        "INSERT INTO userShare(userId, fileName, fileId, days, code, isDirectory, visitTimes)",
        "VALUES(#{userId}, #{fileName}, #{fileId}, #{days}, #{code}, #{isDirectory} #{visitTimes})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserShare userShare);


    /**
     * 根据fileId删除记录
     */
    @Delete({
        "DELETE FROM userShare",
        "WHERE fileId = #{fileId}"
    })
    int deleteByFileId(@Param("fileId") Long fileId);


}
