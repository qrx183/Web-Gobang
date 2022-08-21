package com.example.gobang.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.8.0
 */
@Component
public class OnlineUserManger {

    //哈希表表示当前用户在游戏大厅的上线,下线状态
    //为了保证线程安全,用ConcurrentHashMap
    private ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer,WebSocketSession> gameRoom = new ConcurrentHashMap<>();
    public void enterGameHall(int userId,WebSocketSession session){
        gameHall.put(userId,session);
    }

    public void exitGameHall(int userId){
        gameHall.remove(userId);
    }

    public WebSocketSession getFromGameHall(int userId){
        return gameHall.get(userId);
    }

    public void enterGameRoom(int userId,WebSocketSession session){
        gameRoom.put(userId,session);
    }
    public void exitGameRoom(int userId){
        gameRoom.remove(userId);
    }
    public WebSocketSession getFromGameRoom(int userId){
        return gameRoom.get(userId);
    }
}
