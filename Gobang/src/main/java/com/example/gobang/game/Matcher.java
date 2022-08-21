package com.example.gobang.game;

import com.example.gobang.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author qiu
 * @version 1.8.0
 */
@Component
public class Matcher {
    private Queue<User> normalQueue = new LinkedList<>();
    private Queue<User> highQueue = new LinkedList<>();
    private Queue<User> superQueue = new LinkedList<>();

    @Autowired
    private OnlineUserManger onlineUserManger;

    @Autowired
    private RoomManager roomManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void add(User user) {
        int score = user.getScore();
        if (score < 2000) {
            synchronized (normalQueue) {
                normalQueue.add(user);
                normalQueue.notify();
                System.out.println("玩家" + user.getUsername() + "加入normal队列");
            }
        } else if (score < 3000) {
            synchronized (highQueue) {
                highQueue.add(user);
                highQueue.notify();
                System.out.println("玩家" + user.getUsername() + "加入high队列");
            }
        } else {
            synchronized (superQueue) {
                superQueue.add(user);
                superQueue.notify();
                System.out.println("玩家" + user.getUsername() + "加入super队列");
            }

        }
    }

    public void remove(User user) {
        int score = user.getScore();
        if (score < 2000) {
            synchronized (normalQueue) {
                normalQueue.remove(user);
                System.out.println("玩家" + user.getUsername() + "退出normal队列");
            }

        } else if (score < 3000) {
            synchronized (highQueue) {
                highQueue.remove(user);
                System.out.println("玩家" + user.getUsername() + "退出high队列");
            }

        } else {
            synchronized (superQueue){
                superQueue.remove(user);
                System.out.println("玩家" + user.getUsername() + "退出super队列");
            }
        }
    }

    public Matcher() {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    handlerMatch(normalQueue);
                }
            }
        };
        t1.start();
        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    handlerMatch(highQueue);
                }
            }
        };
        t2.start();
        Thread t3 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    handlerMatch(superQueue);
                }
            }
        };
        t3.start();
    }

    private void handlerMatch(Queue<User> queue) {
        synchronized (queue){
            try {
                while (queue.size() < 2) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                User user1 = queue.poll();
                User user2 = queue.poll();
                System.out.println("匹配出的两个玩家" + user1.getUsername() + " VS " + user2.getUsername());

                WebSocketSession session1 = onlineUserManger.getFromGameHall(user1.getUserId());
                WebSocketSession session2 = onlineUserManger.getFromGameHall(user2.getUserId());

                if (session1 == null) {
                    System.out.println("玩家1未登录");
                    queue.offer(user2);
                    return;
                }
                if (session2 == null) {
                    System.out.println("玩家2未登录");
                    queue.offer(user1);
                    return;
                }
                if (session1 == session2) {
                    System.out.println("异常情况");
                    queue.offer(user1);
                    return;
                }

                // TODO 将2个玩家放到一个游戏房间中
                Room room = new Room();
                roomManager.add(room,user1.getUserId(),user2.getUserId());

                //给用户反馈匹配到对手信息.
                MatchResponse response1 = new MatchResponse();
                response1.setOk(true);
                response1.setMessage("matchSuccess");
                session1.sendMessage(new TextMessage(objectMapper.writeValueAsString(response1)));

                MatchResponse response2 = new MatchResponse();
                response2.setOk(true);
                response2.setMessage("matchSuccess");
                session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response2)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
