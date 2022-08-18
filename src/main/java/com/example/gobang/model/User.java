package com.example.gobang.model;

import lombok.Data;

/**
 * @author qiu
 * @version 1.8.0
 */
@Data
public class User {
    private int userId;
    private String username;
    private String password;
    private int score;
    private int totalCount;
    private int winCount;
}
