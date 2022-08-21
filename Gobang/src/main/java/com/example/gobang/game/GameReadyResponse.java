package com.example.gobang.game;

import lombok.Data;

/**
 * @author qiu
 * @version 1.8.0
 */
@Data
public class GameReadyResponse {
    private String message;
    private boolean ok;
    private String reason;
    private String roomId;
    private int thisUserId;
    private int thatUserId;
    private int whiteUser;
}
