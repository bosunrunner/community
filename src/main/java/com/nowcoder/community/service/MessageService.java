package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public List<Message> findConversions(int userId,int offset,int limit) {
        return messageMapper.selectConversions(userId,offset,limit);
    }

    public int findConversionsCount(int userId) {
        return messageMapper.selectConversionsCount(userId);
    }

    public List<Message> findLetters(String conversionId,int offset,int limit) {
        return messageMapper.selectLetters(conversionId,offset,limit);
    }

    public int findLettersCount(String conversionId) {
        return messageMapper.selectLettersCount(conversionId);
    }

    public  int findLettersUnreadCount(int userId,String conversionId) {
        return messageMapper.selectLettersUnreadCount(userId,conversionId);
    }

}
