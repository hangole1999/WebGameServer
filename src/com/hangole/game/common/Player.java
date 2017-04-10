package com.hangole.game.common;


import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.ArrayList;

/**
 * Created by dsm_025 on 2017-04-03.
 */
public class Player {
    private static ArrayList<Player> playerList = new ArrayList<>();
    private String id;
    private boolean roomMaster;
    private Session session;
    private boolean readyState;
    private double characPositionX = 0;
    private double characPositionY = 0;

    public Player(String id, boolean roomMaster, Session session) {
        this.id = id;
        this.roomMaster = roomMaster;
        this.session = session;
    }

    public double getPositionX() {
        return characPositionX;
    }

    public double getPositionY() {
        return characPositionY;
    }

    public void setPositionX(double x) {
        this.characPositionY = x;
    }

    public void setPosition(double y) {
        this.characPositionY = y;
    }

    public static String getPositionAsJSON(Session session){
        JSONObject message = new JSONObject();
        JSONArray array = new JSONArray();
        message.put("type","Position");
        message.put("x",getPlayerEqualSession(session).getPositionX());
        message.put("y",getPlayerEqualSession(session).getPositionY());

        return message.toString();
    }

    public String getId() {
        return id;
    }

    public boolean isRoomMaster() {
        return roomMaster;
    }

    public void setRoomMaster(boolean roomMaster) {
        this.roomMaster = roomMaster;
    }

    public Session getSession() {
        return session;
    }

    public static void addPlayerToList(Player player) {
        playerList.add(player);
    }

    public static ArrayList<Player> getPlayerList() {
        return playerList;
    }

    public boolean isReadyState() {
        return readyState;
    }

    public void setReadyState(boolean readyState) {
        this.readyState = readyState;
    }

    public void changeReadyState() {
        this.readyState = !readyState;
    }

    public static Player getEqualPlayer(Player player) {
        for (Player temp : Player.getPlayerList()) {
            if (temp.getSession().equals(player.getSession())) {
                return temp;
            }
        }
        return null;
    }

    public static Player getPlayerEqualSession(Session session) {
        for (Player player : Player.getPlayerList()) {
            if (player.getSession().equals(session)) {
                return player;
            }
        }
        return null;
    }
}
