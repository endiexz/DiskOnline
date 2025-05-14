package top.endiexz.DiskOnline.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import top.endiexz.DiskOnline.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    //通过username获取用户信息
    @Select("SELECT * FROM user WHERE username = #{username}")
    public User findByUsername(String username);


    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    public User findByUsernameAndPassword(String username, String password);

    @Select("SELECT * FROM user WHERE id = #{userid}")
    public User findByUserId(Long userid);
    
    @Select("select * from user")
    public List<User> userFind();
    //增添用户
    @Insert("INSERT INTO user(username, password, userRank, availCapacity, usedCapacity) VALUES(#{username}, #{password}, #{userRank}, #{availCapacity}, #{usedCapacity})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 如果你有自增主键 id
    int insertUser(User user);



}
