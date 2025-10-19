package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.Vo.UserVo;
import com.chat.entity.UserEntity;
import com.chat.mapper.UserMapper;
import com.chat.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Override
    public Page<UserVo> getAllUser(Integer pageNum, Integer pageSize, String key) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        if (key != null) {
            queryWrapper.like("username", key)
                    .or() // 表示“或”关系
                    .like("name", key);
        }
        Page<UserEntity> userEntityPage = this.page(page,queryWrapper);
        // 创建一个UserVoPage对象，将userEntityPage中的数据复制到userVoPage中
        Page<UserVo> userVoPage = new Page<>();
        userVoPage.setCurrent(userEntityPage.getCurrent());
        userVoPage.setSize(userEntityPage.getSize());
        userVoPage.setTotal(userEntityPage.getTotal());
        userVoPage.setPages(userEntityPage.getPages());

        List<UserVo> userVos = userEntityPage.getRecords().stream().map(userEntity -> {
            UserVo userVo = new UserVo();
            userVo.setId(userEntity.getId());
            userVo.setUsername(userEntity.getUsername());
            userVo.setName(userEntity.getName());
            userVo.setSex(userEntity.getSex());
            return userVo;
        }).toList();
        userVoPage.setRecords(userVos);

        return userVoPage;
    }
}
