package com.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.entity.RootEntity;
import com.chat.mapper.RootMapper;
import com.chat.service.RootService;
import org.springframework.stereotype.Service;

@Service
public class RootServiceImpl extends ServiceImpl<RootMapper, RootEntity> implements RootService {
}
