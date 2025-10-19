package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.Vo.UserVo;
import com.chat.entity.UserEntity;

public interface UserService extends IService<UserEntity> {
    Page<UserVo> getAllUser(Integer pageNum, Integer pageSize, String key);


}
