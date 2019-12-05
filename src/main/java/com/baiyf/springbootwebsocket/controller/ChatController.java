package com.baiyf.springbootwebsocket.controller;

import com.alibaba.fastjson.JSON;
import com.baiyf.springbootwebsocket.bean.FriendBean;
import com.baiyf.springbootwebsocket.bean.GroupBean;
import com.baiyf.springbootwebsocket.bean.InfoBean;
import com.baiyf.springbootwebsocket.bean.UserBean;
import com.baiyf.springbootwebsocket.bean.SendMessageBean;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.shiro.subject.Subject;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping("/")
    public String index() {

        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return "redirect:/chat";
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping("/chat")
    public String chat(Model model) {

        Subject subject = SecurityUtils.getSubject();
        String userName = (String) subject.getPrincipal();

        InfoBean infoMap = new InfoBean();

        UserBean jackInfo = new UserBean();
        jackInfo.setUsername("jack");
        jackInfo.setId(1);
        jackInfo.setSign("我是jack");
        jackInfo.setAvatar("//res.layui.com/images/fly/avatar/00.jpg");
        jackInfo.setStatus("online");

        UserBean tomInfo = new UserBean();
        tomInfo.setUsername("tom");
        tomInfo.setId(2);
        tomInfo.setSign("我是tom");
        tomInfo.setAvatar("//tva1.sinaimg.cn/crop.0.0.118.118.180/5db11ff4gw1e77d3nqrv8j203b03cweg.jpg");
        tomInfo.setStatus("online");

        List<UserBean> friendList = new ArrayList<>();
        List<FriendBean> friendBeanList = new ArrayList<>();
        List<GroupBean> groupList = new ArrayList<>();

        FriendBean friend = new FriendBean();
        GroupBean group = new GroupBean();

        group.setGroupname("websocket交流");
        group.setId(1000);
        group.setAvatar("//tva1.sinaimg.cn/crop.0.0.200.200.50/006q8Q6bjw8f20zsdem2mj305k05kdfw.jpg");
        groupList.add(group);

        infoMap.setGroup(groupList);

        friend.setGroupname("我的好友");
        friend.setId(100);

        if ("jack".equals(userName)) {

            friendList.add(tomInfo);
            infoMap.setMine(jackInfo);
        } else {

            friendList.add(jackInfo);
            infoMap.setMine(tomInfo);
        }

        friend.setList(friendList);
        friendBeanList.add(friend);
        infoMap.setFriend(friendBeanList);

        String jsonString = JSON.toJSONString(infoMap);
        System.out.println(jsonString);
        model.addAttribute("mine", jsonString);

        return "chat";
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public SendMessageBean sendMessage(@Payload SendMessageBean chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public SendMessageBean addUser(@Payload SendMessageBean chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @MessageMapping("/sendPrivateMessage")
    public void sendPrivateMessage(@Payload SendMessageBean msg, Principal principal) {
        msg.setSender(principal.getName());

        messagingTemplate.convertAndSendToUser(msg.getSender(), "topic/chat", msg);
    }
}