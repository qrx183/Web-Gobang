package com.example.gobang.game;

import lombok.Data;

/**
 * @author qiu
 * @version 1.8.0
 */
@Data
public class MatchResponse {
    private boolean ok;
    private String reason;
    private String message;
}
