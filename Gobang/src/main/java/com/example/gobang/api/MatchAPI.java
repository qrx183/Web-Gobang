package com.example.gobang.api;

import com.example.gobang.game.Matcher;
import com.example.gobang.game.OnlineUserManger;
import com.example.gobang.game.MatchRequest;
import com.example.gobang.game.MatchResponse;
import com.example.gobang.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author qiu
 * @version 1.8.0
 */
@Component
public class MatchAPI extends TextWebSocketHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private Matcher matcher;
    @Autowired
    private OnlineUserManger onlineUserManger;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //玩家上线
        //WebSocket的session中提前注册了HttpSession

            User user = (User) session.getAttributes().get("user");
            if(user == null){
                MatchResponse response = new MatchResponse();
                response.setOk(true);
                response.setReason("当前用户未登录");
                response.setMessage("notLogin");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                //session.close();
                return;
            }
            WebSocketSession curSession = onlineUserManger.getFromGameHall(user.getUserId());
            WebSocketSession curSession1 = onlineUserManger.getFromGameRoom(user.getUserId());
            if(curSession != null || curSession1 != null){
                MatchResponse matchResponse = new MatchResponse();
                matchResponse.setOk(true);
                matchResponse.setReason("当前用户已经登录,禁止多开");
                matchResponse.setMessage("repeatConnection");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
                //会调用下线函数
                //session.close();
                return;
            }
            onlineUserManger.enterGameHall(user.getUserId(),session);
            System.out.println("玩家" + user.getUsername() + "进入了游戏大厅");

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User)session.getAttributes().get("user");
        String data = message.getPayload();
        MatchRequest request = objectMapper.readValue(data,MatchRequest.class);
        MatchResponse response = new MatchResponse();

        if("startMatch".equals(request.getMessage())){
            matcher.add(user);
            response.setOk(true);
            response.setMessage("startMatch");
        }else if("stopMatch".equals(request.getMessage())){
            matcher.remove(user);
            response.setOk(true);
            response.setMessage("stopMatch");
        }else{
            response.setOk(false);
            response.setReason("非法情况");
        }
        System.out.println(response.getMessage());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //玩家异常下线
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            System.out.println("当前用户未登录");
            return;
        }
        WebSocketSession curSession = onlineUserManger.getFromGameHall(user.getUserId());
        //防止多开导致已经登陆的用户因为多开而被迫下线
        if(curSession == session){
            onlineUserManger.exitGameHall(user.getUserId());
            System.out.println("玩家" + user.getUsername() + "退出了游戏大厅");
        }
        matcher.remove(user);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //玩家正常下线
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            System.out.println("当前用户未登录");
            return;
        }

        WebSocketSession curSession = onlineUserManger.getFromGameHall(user.getUserId());
        //防止多开导致已经登陆的用户因为多开而被迫下线
        if(curSession == session){
            onlineUserManger.exitGameHall(user.getUserId());
            System.out.println("玩家" + user.getUsername() + "退出了游戏大厅");
        }
        matcher.remove(user);
    }
}
