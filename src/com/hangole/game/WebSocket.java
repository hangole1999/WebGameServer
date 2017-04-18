package com.hangole.game;

import com.hangole.game.common.Player;
import com.hangole.game.common.Room;
import com.hangole.game.controller.GameController;
import com.hangole.server.session.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Hangole on 2017-03-20.
 */

@ServerEndpoint(value = "/game", configurator = GetHttpSessionConfigurator.class)
public class WebSocket {
    private static ArrayList<Session> list = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        System.out.println("onOnpen()");

        list.add(session);
        session.getBasicRemote().sendText(Room.getRoomListAsJSON());
        HttpSession httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());

        Player player = new Player(Util.findEqualSessionId(httpSession), false, session);
        Player.addPlayerToList(player);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("onMessage(" + message + ")");
        JSONObject jsonObject = new JSONObject(message);
        Room targetRoom;
        switch (jsonObject.getString("type")) {

            case "create_room":
                Room created_room = GameController.createRoom(jsonObject.getString("name"), jsonObject.getBoolean("lock"), jsonObject.getString("password"), session);
                session.getBasicRemote().sendText(created_room.getRoomDetailInfomToJSON().put("type", "room_detail").toString());
                break;
            case "enter_room":
                Room entered_room = GameController.enterRoom(jsonObject.getInt("roomNum"), session);
                if (entered_room != null) {
                    ArrayList<Session> roomMembers = entered_room.getPlayerSession();
                    for (Session member : roomMembers) {
                        member.getBasicRemote().sendText(entered_room.getRoomInfomToJSON().put("type", "room_detail").toString());
                    }
                } else {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("방 인원이 가득 찼습니다."));
                }
                break;
            case "change_master":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));

                if (targetRoom != null) {
                    Boolean isSuccess = targetRoom.changeRoomMaster(Player.getPlayerEqualSession(session));

                    if (isSuccess == true) {
                        session.getBasicRemote().sendText(targetRoom.getRoomInfomToJSON().put("type", "room_detail").toString());
                    } else {
                        session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("방장 변경에 실패했습니다."));
                    }
                }
                break;
            case "change_ready":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));
                targetRoom.getPlayerEqualSession(session).changeReadyState();
                break;
            case "get_out_room":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));
                if (targetRoom.removePlayer(targetRoom.getPlayerEqualSession(session))) {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeSuccessLog("나가기 성공"));
                } else {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("나가기 성공"));
                }
                break;
            case "game_start":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));
                if (targetRoom.isGamePossible()) {
                    for (Player player : targetRoom.getPlayerList()) {
                        player.setKillCount(0);
                        player.setHp(100);
                        player.getSession().getBasicRemote().sendText(GameController.getGameStartInform(targetRoom));
                    }
                    targetRoom.changeRoomToPlaying(targetRoom);
                } else {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("레디를 안한 팀원이 있습니다."));
                }
                break;
            case "change_map":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));
                if (targetRoom.chanegMap(jsonObject.getString("name"))) {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeSuccessLog("Map 변경 성공"));
                } else {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("올바르지 않은 Map 이름입니다."));
                }
                break;
            case "characterPosition":
                targetRoom = GameController.findRoomFromRoomList(jsonObject.getInt("roomNum"));
                if (targetRoom != null) {
                    double characterX = jsonObject.getInt("x");
                    double characterY = jsonObject.getInt("y");

                    session.getBasicRemote().sendText(Player.getPositionAsJSON(targetRoom, session));
                } else {
                    session.getBasicRemote().sendText(com.hangole.game.Util.makeErrorLog("좌표 보내기 실패"));
                }
                break;
            case "lose_hp":
                targetRoom = GameController.findRoomFromPlayingRoomList(jsonObject.getInt("roomNum"));
                if (targetRoom.getPlayerEqualSession(session).minusHp(15)) {
                    Player player = targetRoom.getPlayerEqualId(jsonObject.getString("bullet_attacker"));
                    player.upCountKill();
                }
                for (Player player : targetRoom.getPlayerList()) {
                    player.getSession().getBasicRemote().sendText(GameController.getPlayersHPInfo(targetRoom));
                }
                break;
            case "end_game":
                targetRoom = GameController.findRoomFromPlayingRoomList(jsonObject.getInt("roomNum"));
                String result = GameController.getPlayerResult(targetRoom);

                for (Player player : targetRoom.getPlayerList()) {
                    player.getSession().getBasicRemote().sendText(result);
                }
                break;
            case "bullet_position":
                targetRoom = GameController.findRoomFromPlayingRoomList(jsonObject.getInt("roomNum"));
                for (Player player : targetRoom.getPlayerList()) {
                    player.getSession().getBasicRemote().sendText(message);
                }
                break;
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose()");
        Player targetPlayer = Player.getPlayerEqualSession(session);

        if (targetPlayer != null) {

        }

    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        System.out.println("onError()");
        throwable.printStackTrace();
    }
}
