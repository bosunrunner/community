package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversions(int userId,int offset,int limit) {
        return messageMapper.selectConversions(userId,offset,limit);
    }

    public int findConversionsCount(int userId) {
        return messageMapper.selectConversionsCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit) {
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLettersCount(String conversationId) {
        return messageMapper.selectLettersCount(conversationId);
    }

    public  int findLettersUnreadCount(int userId,String conversationId) {
        return messageMapper.selectLettersUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(message.getContent());
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids) {
        // 1为已读
        return messageMapper.updateMessage(ids,1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId,String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId,String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit) {
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }



}
