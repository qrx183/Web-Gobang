package com.example.gobang.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.8.0
 */
@Component
public class RoomManager {
    private ConcurrentHashMap<String,Room> rooms = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer,String> userIdToRoomId = new ConcurrentHashMap<>();

    public void add(Room room,int userId1,int userId2){
        userIdToRoomId.put(userId1, room.getRoomId());
        userIdToRoomId.put(userId2, room.getRoomId());
        rooms.put(room.getRoomId(), room);
    }
    public void remove(String id,int userId1,int userId2){
        userIdToRoomId.remove(userId1);
        userIdToRoomId.remove(userId2);
        rooms.remove(id);
    }

    public Room getRoomByRoomId(String id){
        return rooms.get(id);
    }

    public Room getRoomByUserId(Integer userId){
        String roomId = userIdToRoomId.get(userId);
        if(roomId == null){
            return null;
        }
        return rooms.get(roomId);
    }
}
