package com.vvvb.game.websocket.server;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/websocket/{rid}")
@Component
public class WebSocketServer {

    private static AtomicInteger connections = new AtomicInteger();

    private static ConcurrentHashMap<String, ArrayList<Session> > sessionPool = new ConcurrentHashMap<>();

    public void addConnect() {
        connections.incrementAndGet();
    }

    public void subConnect() {
        connections.decrementAndGet();
    }

    public void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void broadcastMessage(String rid, String message) {
        ArrayList<Session> sessionList = sessionPool.get(rid);

        try {
            for(Session session : sessionList) {
                sendMessage(session, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "rid") String rid) {
        if(sessionPool.containsKey(rid)) {
            sessionPool.get(rid).add(session);
        } else {
            ArrayList<Session> sessionList = new ArrayList<>();
            sessionList.add(session);
            sessionPool.put(rid, sessionList);
        }

        addConnect();
        System.out.println(connections);
    }

    @OnClose
    public void onClose(Session session, @PathParam(value = "rid") String rid) {
        if(!sessionPool.containsKey(rid)) {
            return;
        }

        ArrayList<Session> sessionList = sessionPool.get(rid);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 0);

        try {
            for (Session session1 : sessionList) {
                subConnect();
                if (!session1.getId().equals(session.getId())) {
                    sendMessage(session1, jsonObject.toString());
                }
            }
            sessionPool.remove(rid);
            System.out.println(connections);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("There's an Error in WebSocket Connection !!!");
        System.out.println(session);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, @PathParam(value = "rid") String rid) {
        System.out.println(message);

        JSONObject data = JSONObject.fromObject(message);
        int id = data.getInt("id");

        switch (id) {
            case 1:
                JSONObject res = new JSONObject();
                res.put("id", 1);
                broadcastMessage(rid, res.toString());
                break;
        }
    }

    public Integer countSession(String rid) {
        if(sessionPool.containsKey(rid)) {
            return sessionPool.get(rid).size();
        }else{
            return 0;
        }
    }

}
