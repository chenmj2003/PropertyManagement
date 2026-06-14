package com.msb.mapper;

import com.msb.pojo.LoginToken;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TokenMapper {
    @Insert("INSERT INTO login_token(user_id, user_type, token, expire_time) VALUES(#{userId}, #{userType}, #{token}, #{expireTime})")
    int insert(LoginToken loginToken);
    @Select("SELECT * FROM login_token WHERE token = #{token}")
    LoginToken selectByToken(String token);
    @Delete("DELETE FROM login_token WHERE token = #{token}")
    int deleteByToken(String token);
    @Delete("DELETE FROM login_token WHERE user_id = #{userId} AND user_type = #{userType}")
    int deleteByUser(@Param("userId") int userId, @Param("userType") String userType);
    @Select("SELECT * FROM login_token WHERE user_id = #{userId} AND user_type = #{userType}")
    java.util.List<LoginToken> selectByUser(@Param("userId") int userId, @Param("userType") String userType);
}