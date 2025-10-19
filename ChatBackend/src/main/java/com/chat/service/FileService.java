package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.common.FileQuery;
import com.chat.entity.FileEntity;
import com.chat.entity.UserEntity;

import java.util.List;

public interface FileService extends IService<FileEntity> {
    Page<FileEntity> query(Integer pageNum, Integer pageSize, FileQuery fileQuery);

}
