package com.example.gobang.game;

import com.example.gobang.model.User;
import lombok.Data;

import java.util.UUID;

/**
 * @author qiu
 * @version 1.8.0
 */
@Data
public class Room {
    private String roomId;
    private User user1;
    private User user2;
    private int whiteUser;
    public Room(){

        //UUID:生成唯一的字符串作为身份标识
        roomId = String.valueOf(UUID.randomUUID());
    }
}
