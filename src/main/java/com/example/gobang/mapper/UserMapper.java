package com.example.gobang.mapper;

import com.example.gobang.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author qiu
 * @version 1.8.0
 */
@Mapper
public interface UserMapper {
    void insert(User user);


    User selectByName(String username);
}
