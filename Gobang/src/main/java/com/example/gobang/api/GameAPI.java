package com.example.gobang.api;

import com.example.gobang.game.*;
import com.example.gobang.mapper.UserMapper;
import com.example.gobang.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.xml.ws.soap.Addressing;
import java.io.IOException;

/**
 * @author qiu
 * @version 1.8.0
 */
@Component
public class GameAPI extends TextWebSocketHandler {

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManger onlineUserManger;

    @Autowired
    private UserMapper userMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse response = new GameReadyResponse();
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            response.setOk(true);
            response.setReason("用户尚未登录");
            response.setMessage("notLogin");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            response.setOk(false);
            response.setReason("当前用户尚未匹配到");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        WebSocketSession curSession = onlineUserManger.getFromGameRoom(user.getUserId());
        WebSocketSession curSession1 = onlineUserManger.getFromGameHall(user.getUserId());
        if (curSession != null || curSession1 != null) {
            response.setOk(true);
            response.setReason("当前用户已经登陆,禁止多开");
            response.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }

        onlineUserManger.enterGameRoom(user.getUserId(), session);
        System.out.println("玩家" + user.getUsername() + "进入了比赛中");
        //在这里将两个玩家放入Room中,因为从game_hall.html到game_room.html涉及到页面跳转,
        //而页面跳转有可能失败
        //这里需要处理多线程问题,竞争的对象是同一个房间
        synchronized (room) {
            if (room.getUser1() == null) {
                room.setUser1(user);
                System.out.println("玩家1" + user.getUsername() + "准备就绪");
                //先连接到服务器的玩家先执子,玩家1会一直等待玩家2的进入
                room.setWhiteUser(user.getUserId());
                return;
            }
            if (room.getUser2() == null) {
                room.setUser2(user);
                System.out.println("玩家2" + user.getUsername() + "准备就绪");
                //两个玩家均进入房间后,通知玩家可以开战了.
                noticeGameReady(room, room.getUser1(), room.getUser2());
                noticeGameReady(room, room.getUser2(), room.getUser1());
                return;

            }
            //房间已满,再连接的用户不能进入
            response.setOk(false);
            response.setReason("当前房间已满,请稍后再试");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

    }

    private void noticeGameReady(Room room, User thisUser, User thatUser) {
        GameReadyResponse response = new GameReadyResponse();
        response.setOk(true);
        response.setMessage("gameReady");
        response.setRoomId(room.getRoomId());
        response.setThisUserId(thisUser.getUserId());
        response.setThatUserId(thatUser.getUserId());
        response.setWhiteUser(room.getWhiteUser());
        WebSocketSession session = onlineUserManger.getFromGameRoom(thisUser.getUserId());
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User)session.getAttributes().get("user");
        if(user == null){
            System.out.println("当前用户尚未登录");
            return;
        }
        Room room = roomManager.getRoomByUserId(user.getUserId());
        String jS = message.getPayload();
        room.putChess(jS);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        WebSocketSession curSession = onlineUserManger.getFromGameRoom(user.getUserId());
        if (session == curSession) {
            onlineUserManger.exitGameRoom(user.getUserId());
            System.out.println("当前用户" + user.getUsername() + "游戏房间连接异常");
        }
        noticeThatWinnerWin(user);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        WebSocketSession curSession = onlineUserManger.getFromGameRoom(user.getUserId());
        if (session == curSession) {
            onlineUserManger.exitGameRoom(user.getUserId());
            System.out.println("当前用户" + user.getUsername() + "退出了游戏房间");
        }
        noticeThatWinnerWin(user);
    }

    private void noticeThatWinnerWin(User user){
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            return;
        }
        User user1 = room.getUser1();
        User user2 = room.getUser2();
        User thatUser;
        if(user1.equals(user)){
            thatUser = user2;
        }else{
            thatUser = user1;
        }
        WebSocketSession session = onlineUserManger.getFromGameRoom(thatUser.getUserId());
        if(session == null){
            System.out.println("对手同时也掉线了");
            return;
        }
        GameResponse response = new GameResponse();
        response.setUserId(thatUser.getUserId());
        response.setWinner(thatUser.getUserId());
        response.setMessage("putChess");
        response.setRow(-1);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        userMapper.userWin(thatUser.getUserId());
        userMapper.userLose(user.getUserId());
        roomManager.remove(room.getRoomId(),user1.getUserId(),user2.getUserId());
    }
}
