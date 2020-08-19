package com.baiyf.springbootwebsocket.controller;

import com.alibaba.fastjson.JSON;
import com.baiyf.springbootwebsocket.bean.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.shiro.subject.Subject;

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
        jackInfo.setAvatar("static/images/jack.jpg");
        jackInfo.setStatus("online");

        UserBean tomInfo = new UserBean();
        tomInfo.setUsername("tom");
        tomInfo.setId(2);
        tomInfo.setSign("我是tom");
        tomInfo.setAvatar("static/images/tom.jpg");
        tomInfo.setStatus("online");

        UserBean mikeInfo = new UserBean();
        mikeInfo.setUsername("mike");
        mikeInfo.setId(3);
        mikeInfo.setSign("我是mike");
        mikeInfo.setAvatar("static/images/group.jpg");
        mikeInfo.setStatus("online");

        List<UserBean> friendList = new ArrayList<>();
        List<FriendBean> friendBeanList = new ArrayList<>();
        List<GroupBean> groupList = new ArrayList<>();

        FriendBean friend = new FriendBean();
        GroupBean group = new GroupBean();

        group.setGroupname("websocket交流");
        group.setId(1000);
        group.setAvatar("static/images/group.jpg");
        groupList.add(group);

        infoMap.setGroup(groupList);

        friend.setGroupname("我的好友");
        friend.setId(100);

        if ("jack".equals(userName)) {
            friendList.add(tomInfo);
            friendList.add(mikeInfo);
            infoMap.setMine(jackInfo);
        } else if ("mike".equals(userName)){
            friendList.add(jackInfo);
            friendList.add(tomInfo);
            infoMap.setMine(mikeInfo);
        } else if ("tom".equals(userName)){
            friendList.add(jackInfo);
            friendList.add(mikeInfo);
            infoMap.setMine(tomInfo);
        }

        friend.setList(friendList);
        friendBeanList.add(friend);
        infoMap.setFriend(friendBeanList);

        String jsonString = JSON.toJSONString(infoMap);

        model.addAttribute("mine", jsonString);

        return "chat";
    }

    @MessageMapping("/chat.group")
    @SendTo("/topic/group1000")
    public ChatMessageBean sendGroupMessage(@Payload SendMessageBean msg) {

        ChatMessageBean groupMsg = new ChatMessageBean();

        groupMsg.setUsername(msg.getData().getMine().getUsername());
        groupMsg.setAvatar(msg.getData().getMine().getAvatar());
        groupMsg.setId(1000);
        groupMsg.setType("group");
        groupMsg.setContent(msg.getData().getMine().getContent());
        groupMsg.setCid(0);
        groupMsg.setMine(false);
        groupMsg.setFromid(msg.getData().getMine().getId());
        groupMsg.setTimestamp(System.currentTimeMillis());

        return groupMsg;
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload SendMessageBean msg) {

        ChatMessageBean chatMsg = new ChatMessageBean();

        chatMsg.setUsername(msg.getData().getMine().getUsername());
        chatMsg.setAvatar(msg.getData().getMine().getAvatar());
        chatMsg.setId(msg.getData().getMine().getId());
        chatMsg.setType("friend");
        chatMsg.setContent(msg.getData().getMine().getContent());
        chatMsg.setCid(0);
        chatMsg.setMine(false);
        chatMsg.setFromid(msg.getData().getMine().getId());
        chatMsg.setTimestamp(System.currentTimeMillis());

        /**
         * 服务端推送消息到指定用户得客户端
         * 例如以下将会推送到
         * 如果设置了setUserDestinationPrefix("/user")，因此推送到/user/msg.getData().getTo().getName()/topic/chat
         * 如果设置了setUserDestinationPrefix("/destination/prefix")，因此推送到/destination/prefix/msg.getData().getTo().getName()/topic/chat
         * 如果没有设置setUserDestinationPrefix()，则默认前端为user，将会推送到/user/msg.getData().getTo().getName()/topic/chat
         * 客户端订阅/userTest/topic/chat将会收到服务端推送得消息
         */
        messagingTemplate.convertAndSendToUser(msg.getData().getTo().getName(), "/topic/chat", chatMsg);
//        messagingTemplate.convertAndSend("/topic/group1000", chatMsg);
    }
}