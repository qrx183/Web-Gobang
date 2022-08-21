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

    //总比赛场数+1,获胜场数+1,天梯分数+50
    void userWin(int userId);

    //总比赛场数-1,总场数不变,天梯分数-30
    void userLose(int userId);

    //总比赛场数+1
    void userOver(int userId);
}
