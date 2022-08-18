package com.example.gobang.game;

import lombok.Data;

/**
 * 落子响应
 * @author qiu
 * @version 1.8.0
 */
@Data
public class GameResponse {
    private String message;
    private int userId;
    private int row;
    private int col;
    private int winner;
}
