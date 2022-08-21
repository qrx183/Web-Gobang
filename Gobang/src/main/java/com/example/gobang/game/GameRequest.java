package com.example.gobang.game;

import lombok.Data;

/**
 * 落子请求
 * @author qiu
 * @version 1.8.0
 */
@Data
public class GameRequest {
    private String message;
    private int userId;
    private int row;
    private int col;
}
