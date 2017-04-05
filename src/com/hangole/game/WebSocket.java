package com.hangole.game;

import com.hangole.game.common.Player;
import com.hangole.game.common.Room;
import com.hangole.game.controller.MainPageController;
import com.hangole.server.session.Util;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Hangole on 2017-03-20.
 */

@ServerEndpoint("/game")
public class WebSocket {
    private static ArrayList<Session> list = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("onOnpen()");
        list.add(session);
        session.getBasicRemote().sendText(Room.getRoomListAsJSON());

        Player player = new Player(Util.findEqualSessionId(session), false, session);
        Player.addPlayerToList(player);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("onMessage("+message+")");
        JSONObject jsonObject = new JSONObject(message);
        switch (jsonObject.getString("type")){
            case "create_room" :
                Room created_room = MainPageController.createRoom(jsonObject.getString("name"), jsonObject.getBoolean("lock"), jsonObject.getString("password"), session);
                session.getBasicRemote().sendText(created_room.getRoomInfomToJSON().put("type", "room_detail").toString());
                break;
            case "enter_room" :
                Room entered_room = MainPageController.enterRoom(jsonObject.getInt("roomNum"), session);
                if(entered_room != null){
                    ArrayList<Session> roomMembers = entered_room.getPlayerSession();
                    for(Session member : roomMembers){
                        member.getBasicRemote().sendText(entered_room.getRoomInfomToJSON().put("type", "room_detail").toString());
                    }
                }else{
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("방 인원이 가득 찼습니다."));
                }
                break;
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose()");
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        System.out.println("onError()");
        throwable.printStackTrace();
    }
}