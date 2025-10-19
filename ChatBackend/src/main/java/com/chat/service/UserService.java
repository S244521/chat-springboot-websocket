package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.entity.UserEntity;

public interface UserService extends IService<UserEntity> {
    Page<UserEntity> getAllUser(Integer pageNum, Integer pageSize, String key);
}
