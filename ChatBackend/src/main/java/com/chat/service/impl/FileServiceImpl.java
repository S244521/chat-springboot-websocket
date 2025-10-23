package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.common.FileQuery;
import com.chat.entity.FileEntity;
import com.chat.mapper.FileMapper;
import com.chat.service.FileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {


    @Override
    public Page<FileEntity> query(Integer pageNum, Integer pageSize, FileQuery fileQuery) {
        Page<FileEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<FileEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(fileQuery.getFilename() != null, "filename", fileQuery.getFilename());
        queryWrapper.ge(fileQuery.getStartTime() != null, "uploadtime", fileQuery.getStartTime());
        queryWrapper.le(fileQuery.getEndTime() != null, "uploadtime", fileQuery.getEndTime());
        // 判断是否不为空且为true
        if (fileQuery.getIsNum() != null && fileQuery.getIsNum()){
            queryWrapper.orderByDesc("num");
        }
        Page<FileEntity> re = page(page, queryWrapper);
        return re;
    }
}
