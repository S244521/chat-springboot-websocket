package com.chat.controller;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.common.FileQuery;
import com.chat.common.Result;
import com.chat.entity.FileEntity;
import com.chat.service.FileService;
import com.chat.service.FileStorageService;
import com.chat.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "*") // 允许所有来源（开发环境使用，生产环境建议指定具体域名）
@RequestMapping("/file")
public class FileController {
    @Value("${file.url}")
    private String fileUrl;

    @Value("${file.upload-dir}") // 注入配置中的文件存储路径
    private String uploadDir;
    
    @Resource
    private FileStorageService fileStorageService;

    @Autowired
    private FileService fileService;

    /**
     * 处理分块文件上传的端点
     *
     * @param file             文件块
     * @param fileIdentifier   整个文件的唯一标识符 (例如：文件名-文件大小-最后修改时间戳)
     * @param chunkNumber      当前块的编号 (从0开始)
     * @param totalChunks      总块数
     * @param originalFilename 原始文件名
     * @param authorizationHeader 认证头
     * @return Result 对象
     */
    @PostMapping("/upload")
    @ResponseBody // 确保返回的是JSON
    public Result<?> uploadChunk(
        @RequestParam("file") MultipartFile file,
        @RequestParam("fileIdentifier") String fileIdentifier,
        @RequestParam("chunkNumber") int chunkNumber,
        @RequestParam("totalChunks") int totalChunks,
        @RequestParam("originalFilename") String originalFilename,
        @RequestHeader("Authorization") String authorizationHeader
    ){
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }

        // 在实际调用服务前，可以添加 fileStorageService.init() 的检查，
        // 但通常在 @PostConstruct 中初始化失败应该有更全局的处理。
//        if (!fileStorageService.init()){
//            return Result.FAIL("初始化失败无法上传",500);
//        }

        try {
            CompletableFuture<String> future = fileStorageService.storeChunkAndMerge(
                    file, fileIdentifier, chunkNumber, totalChunks, originalFilename
            );

            // 可选：设置超时时间，避免长时间等待
            String mergedFilePath = future.get(30, TimeUnit.SECONDS);

            if (mergedFilePath != null) {
                // 所有块已上传并合并成功
                java.io.File  rename = new java.io.File(mergedFilePath);
                String fileExtension = fileStorageService.getExtension(originalFilename);
                String uniqueFileName = UUID.randomUUID().toString() +
                        (fileExtension != null ? "." + fileExtension : "");
                // 构建新的文件路径
                Path directory = Paths.get(rename.getParent());
                Path newFilePath = directory.resolve(uniqueFileName);

                // 重命名文件
                Files.move(rename.toPath(), newFilePath, StandardCopyOption.REPLACE_EXISTING);

//                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()// 获取当前上下文路径http://localhost:9999
//                        .path("/file/download/") // 假设你有一个下载端点
//                        .path(uniqueFileName) // 或者使用某种方式从mergedFilePath提取文件名
//                        .toUriString();
                String fileDownloadUri = fileUrl+uniqueFileName;

                // 保存文件信息到数据库
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFilename(originalFilename);
                fileEntity.setFileurl(fileDownloadUri);
                fileEntity.setNum(0);
                fileEntity.setUploadtime(java.time.LocalDateTime.now());

                fileService.save(fileEntity);
                return Result.ok(fileDownloadUri+ "文件上传并合并成功: " + originalFilename);
            } else {
                // 块已上传，但文件尚未合并完成
                return Result.ok("Chunk " + chunkNumber + "/" + (totalChunks -1) + " for " + originalFilename + " uploaded successfully. Waiting for remaining chunks."+ "块上传成功");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Illegal argument during chunk upload: {}"+e.getMessage());
            return Result.error("上传参数错误: " + e.getMessage()); // Bad Request
        } catch (IOException e) {
            System.out.println("Error during chunk upload or merge for identifier {}: {}"+fileIdentifier + e.getMessage()+ e);
            return Result.error("块上传或合并失败: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error during chunk upload for identifier {}: {}"+fileIdentifier + e.getMessage()+ e);
            return Result.error("上传过程中发生未知错误: " + e.getMessage());
        }
    }


    /**
     * 文件下载接口
     *
     * @param filename 文件名
     * @param request  HttpServletRequest 用于动态获取MIME类型
     * @return ResponseEntity 包含文件资源
     */
    @GetMapping("/download/{filename:.+}") // :.+ 用于匹配带有点号的文件名
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable("filename") String filename, HttpServletRequest request
    ) {
        // 从 FileStorageService 获取最终文件存储的根路径
        // 假设 FileStorageService 有一个方法可以获取 finalUploadPath
        // 或者直接在这里构造，但更好的做法是从service获取或配置中读取
        Path fileStorageLocation = Paths.get(fileStorageService.getFinalUploadPathString()); // 你需要在FileStorageService中添加此方法
        Path filePath = fileStorageLocation.resolve(filename).normalize();

        try {
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // 尝试确定文件的内容类型
                String contentType = null;
                try {
                    // contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath()); // 可能因安全限制不可用
                    contentType = Files.probeContentType(filePath); // Java 7+ 推荐
                } catch (IOException ex) {
                    System.out.println("Could not determine file type for {}."+ filename);
                }

                // 如果无法确定，则使用默认的二进制流类型
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                System.out.println("Downloading file: {} with content type: {}"+ filename+ contentType);

                // 下载次数加一
                UpdateWrapper<FileEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.like("fileurl", filename);

                // 执行 num = num + 1 的自增操作（SQL层面直接更新，无需查询）
                updateWrapper.setSql("num = num + 1");
                fileService.update(updateWrapper);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // Content-Disposition 使得浏览器提示下载而不是直接显示（对于某些类型如图片/PDF）
                        // attachment 表示下载，inline 表示尝试内联显示
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        // .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // 如果想尝试内联显示
                        .body(resource);
            } else {
                System.out.println("File not found or not readable: {}"+ filename);
                // 可以返回自定义的错误Result对象，或者标准的404
                // return ResponseEntity.notFound().build();
                // 为了统一，可以返回Result对象，但下载通常直接返回ResponseEntity
                return ResponseEntity.status(404).body(null); // 或者一个错误提示的 Resource
            }
        } catch (MalformedURLException ex) {
            System.out.println("Error creating URL for file: {}"+ filename+ ex);
            // return ResponseEntity.badRequest().build(); // 或者更详细的错误
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 文件信息查询
     * @param authorizationHeader
     * @param pageNum
     * @param pageSize
     * @param fileQuery
     */
    @PostMapping("/query")
    public Result<?> query(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestBody FileQuery fileQuery
    ) {
        String verifyToken = JwtUtils.verifyToken(authorizationHeader);
        if(verifyToken!=null){
            return Result.error(verifyToken);
        }
        Page<FileEntity> query = fileService.query(pageNum, pageSize, fileQuery);
        return Result.ok(query);
    }

    /**
     * 文件删除
     * @param authorizationHeader
     * @param fileId
     */
    @PostMapping("/deleteFileById")
    public Result<?> deleteFileById(
            @RequestHeader("Root") String authorizationHeader,
            @RequestParam("fileId") Integer fileId
    ) throws IOException {
        // 校验 token（保持原有逻辑）
        if (authorizationHeader == null || !authorizationHeader.startsWith("Shang ")) {
            return Result.error("Authorization 头格式错误");
        }
        String token = authorizationHeader.substring(6);

        if (JwtUtils.isTokenExpired(token)) {
            return Result.error("token 已过期，请重新登录"); // 明确提示“过期”，比“无效”更精准
        }

        String username = JwtUtils.parseUsername(token);
        if (username == null) {
            return Result.error("token 无效");
        }

        FileEntity fileEntity = fileService.getById(fileId);
        // 解析文件url里面的文件名  将fileurl从url中剔除

        String localurl = fileEntity.getFileurl().substring(fileUrl.length());

        // 删除本地文件
        String path=uploadDir+localurl;
        boolean delete = fileStorageService.deleteFile(path);
        if (delete){
            fileService.removeById(fileId);
            return Result.ok("删除成功");
        }
        return Result.error("删除失败");
    }
}
