package top.endiexz.DiskOnline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import top.endiexz.DiskOnline.entity.UserShare;

@Mapper
public interface UserShareMapper {
    @Insert({
        "INSERT INTO userShare(userId, fileName, fileId, days, isDirectory, visitTimes)",
        "VALUES(#{userId}, #{fileName}, #{fileId}, #{days}, #{isDirectory}, #{visitTimes})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserShare userShare);


    //
    @Select("SELECT * FROM userShare WHERE id = #{id}")
    UserShare getUserShareById(@Param("id") Long id);

    //获取用户的share
    @Select("SELECT * FROM userShare WHERE userId = #{userId}")
    List<UserShare> getUserShareListByUserId(@Param("userId") Long userId);

    /**
     * 根据fileId删除记录
     */
    @Delete({
        "DELETE FROM userShare",
        "WHERE id = #{id}"
    })
    int deleteById(@Param("id") Long id);


}
