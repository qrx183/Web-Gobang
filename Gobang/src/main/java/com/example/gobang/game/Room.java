package com.example.gobang.game;

import com.example.gobang.GobangApplication;
import com.example.gobang.mapper.UserMapper;
import com.example.gobang.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.UUID;

/**
 * @author qiu
 * @version 1.8.0
 */
@Data
public class Room {
    private String roomId;
    private User user1;
    private User user2;
    private int whiteUser;

    private  static int MAX_ROW = 15;
    private  static int MAX_COL = 15;
    private int[][] board = new int[MAX_ROW][MAX_COL];

    //这里如果用注解注入,需要将Roon类存储到Spring中.但如果将Roon存储到Spring中默认是单例,而逻辑上Room应该是多例的,如果通过Scope设置成
    //多例,但Room被RoomManager管理,所以即使在Spring中设置成多例也不太合适.所以考虑手动注入的方式

    private OnlineUserManger onlineUserManger;
    private RoomManager roomManager;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UserMapper userMapper;
    public Room(){

        //UUID:生成唯一的字符串作为身份标识
        roomId = String.valueOf(UUID.randomUUID());
        //手动注入
        onlineUserManger = GobangApplication.context.getBean(OnlineUserManger.class);
        roomManager = GobangApplication.context.getBean(RoomManager.class);
        userMapper = GobangApplication.context.getBean(UserMapper.class);
    }

    //处理落子操作:0表示未落子,1表示user1的落子,2表示user2的落子

    public void putChess(String jS){
        try {
            GameRequest request = objectMapper.readValue(jS,GameRequest.class);
            GameResponse response = new GameResponse();
            int userId = request.getUserId();
            int row = request.getRow();
            int col = request.getCol();
            int chess = 0;
            if(user1.getUserId() == userId){
                chess = 1;
            }else if(user2.getUserId() == userId){
                chess = 2;
            }
            if(board[row][col] == 0){
                board[row][col] = chess;
            }else{
                System.out.println(row+","+col+"已经落子");
                return;
            }
            //打印棋盘
            printChess();
            //判断胜负
            int winner = winner(row,col,chess);
            //返回响应  注意:响应返回的不是一个玩家,而是广播给房间的所有玩家
            response.setMessage("putChess");
            response.setRow(row);
            response.setCol(col);
            response.setUserId(request.getUserId());
            response.setWinner(winner);

            WebSocketSession session1 = onlineUserManger.getFromGameRoom(user1.getUserId());
            WebSocketSession session2 = onlineUserManger.getFromGameRoom(user2.getUserId());
            if(session1 == null){
                response.setWinner(user2.getUserId());
                System.out.println("玩家1掉线");
            }
            if(session2 == null){
                response.setWinner(user1.getUserId());
                System.out.println("玩家2掉线");
            }
            //如果玩家1和玩家2都掉线就没有意义了,不需要处理了
            if(session1 != null) {
                session1.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            if(session2 != null) {
                session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            //如果游戏对局结束则要销毁房间
            if(winner != 0){
                String winnerName = "";
                if(winner == 1){
                    winnerName = user1.getUsername();
                }else if(winner == 2){
                    winnerName = user2.getUsername();
                }else if(winner == -1){
                    winnerName = "和棋";
                    userMapper.userOver(user1.getUserId());
                    userMapper.userOver(user2.getUserId());
                }
                int winnerId = winner;
                int loserId = user1.getUserId() == winnerId ? user2.getUserId() : user1.getUserId();
                userMapper.userWin(winnerId);
                userMapper.userLose(loserId);
                if(winner != -1){
                    System.out.println("游戏结束,房间" + roomId+ "即将销毁"+",玩家" + winnerName + "获胜");
                }else{
                    System.out.println("游戏结束,房间" + roomId+ "即将销毁.本局游戏和棋");
                }

                roomManager.remove(roomId,user1.getUserId(),user2.getUserId());
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }
    public void printChess(){
        //打印棋盘这里可以将不同房间的棋盘存储到不同的文件中
        System.out.println("打印棋盘信息" + roomId);
        System.out.println("=============================");
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length;j++){
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("=============================");
    }
    public int winner(int row,int col,int chess){
       for(int i = col-4; i <= col; i++){
           if(i >= 0 && i + 4 < MAX_COL){
               if(board[row][i] == chess
                && board[row][i+1] == board[row][i]
                && board[row][i+2] == chess
                && board[row][i+3] == chess
                && board[row][i+4] == chess){
                    return chess == 1 ? user1.getUserId() : user2.getUserId();
               }
           }
       }
       for(int i = row-4; i <= row; i++){
           if(i >= 0 && i + 4 < MAX_ROW){
               if(board[i][col] == chess
               && board[i+1][col] == chess
               && board[i+2][col] == chess
               && board[i+3][col] == chess
               && board[i+4][col] == chess){
                   return chess == 1 ? user1.getUserId() : user2.getUserId();
               }
           }
       }
       for(int i = -4; i <= 0; i++){
           if(row+i >= 0 && col + i >= 0 && row + i + 4 < MAX_ROW && col + i +4 < MAX_COL){
               if(board[row+i][col+i] == chess
               && board[row+i+1][col+i+1] == chess
               && board[row+i+2][col+i+2] == chess
               && board[row+i+3][col+i+3] == chess
               && board[row+i+4][col+i+4] == chess){
                   return chess == 1 ? user1.getUserId() : user2.getUserId();
               }
           }
       }
       for(int i = row+4,j = col-4;i >= row && j <= col;i--,j++){
           if(i < MAX_ROW && i-4 >= 0 && j >= 0 && j + 4 < MAX_COL){
               if(board[i][j] == chess
               && board[i-1][j+1] == chess
               && board[i-2][j+2] == chess
               && board[i-3][j+3] == chess
               && board[i-4][j+4] == chess){
                   return chess == 1 ? user1.getUserId() : user2.getUserId();
               }
           }
       }
       boolean flag = true;
        for(int i = 0; i < MAX_ROW; i++){
            for(int j = 0; j < MAX_COL;j++){
                if(board[i][j] == 0){
                    flag = false;
                    break;
                }
            }
            if(!flag){
                break;
            }
        }
        if(flag){
            return -1;
        }
       //这里判断和棋,和棋后需要向前端传递一个和棋的消息
        return 0;
    }
}
