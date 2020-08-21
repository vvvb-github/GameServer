package com.vvvb.game.websocket.controller;

import com.vvvb.game.websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "true")
public class WebSocketController {

    @Autowired
    private WebSocketServer webSocketServer;

    private Integer id = 1000;

    @RequestMapping(value = "/newroom", method = RequestMethod.GET)
    public String newRoom() {
        id = id + 1;
        if(id>9999) {
            id = 1000;
        }
        return id.toString();
    }

    @RequestMapping(value = "/joinroom", method = RequestMethod.GET)
    public Boolean joinRoom(String rid) {
        return webSocketServer.countSession(rid).equals(1);
    }

}
