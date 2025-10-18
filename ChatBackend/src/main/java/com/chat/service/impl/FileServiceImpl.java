package com.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.entity.FileEntity;
import com.chat.mapper.FileMapper;
import com.chat.service.FileService;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {
}
