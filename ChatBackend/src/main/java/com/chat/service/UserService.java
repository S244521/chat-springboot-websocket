package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.Vo.UserVo;
import com.chat.entity.UserEntity;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<UserEntity> {
    Page<UserVo> getAllUser(Integer pageNum, Integer pageSize, String key,Integer id);

    List<UserVo> getUserList(Set<Integer> ids);
}
