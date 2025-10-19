package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.entity.UserEntity;
import com.chat.mapper.UserMapper;
import com.chat.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Override
    public Page<UserEntity> getAllUser(Integer pageNum, Integer pageSize, String key) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (key != null) {
            queryWrapper.like("username", key)
                    .or() // 表示“或”关系
                    .like("name", key);
        }
        Page<UserEntity> userEntityPage = this.page(page,queryWrapper);
        return userEntityPage;
    }
}
