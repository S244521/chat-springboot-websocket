package com.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.entity.HistoryEntity;
import com.chat.mapper.HistoryMapper;
import com.chat.service.HistoryService;
import org.springframework.stereotype.Service;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, HistoryEntity> implements HistoryService {
}
