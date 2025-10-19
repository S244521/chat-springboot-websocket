package com.chat.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}") // 注入配置中的文件存储路径
    private String uploadDir;

    private Path finalUploadPath;// 最终文件存储目录
    private Path chunkStoragePath;// 块文件临时存储目录

    @Resource
    private ExecutorService  fileUploadExecutor;

    /**
     * 初始化文件存储服务
     *
     * @return true 表示成功，false 表示失败
     */
    @PostConstruct // 在Bean初始化后自动调用
    public void initialize() { // 方法名可以任意，例如 setup, initDirectories
        if (!this.init()) { // 调用下面的init方法
            logger.error("!!!!!!!!!! File storage initialization failed during PostConstruct. Uploads may not work. !!!!!!!!!!");
            // 可以抛出异常阻止应用启动，或采取其他措施
            // throw new RuntimeException("File storage initialization failed.");
        } else {
            logger.info("File storage initialized successfully via PostConstruct.");
        }
    }

    /**
     * 初始化文件存储服务
     *
     * @return true 表示成功，false 表示失败
     */
    public boolean init() { // 这个方法现在主要被 initialize() 调用
        try {
            this.finalUploadPath = Paths.get(uploadDir);
            // 修正 chunkStoragePath 的构建
            this.chunkStoragePath = this.finalUploadPath.resolve("temp_chunks"); // 作为 uploadDir 的子目录

            // 检查并创建最终上传目录
            if (!Files.exists(finalUploadPath)) {
                Files.createDirectories(finalUploadPath);
                logger.info("Created final upload directory(最终上传目录创建成功): {}", finalUploadPath);
            } else if (!Files.isDirectory(finalUploadPath)) {
                logger.error("Final upload path exists but is not a directory(最终上传路径已存在但不是目录): {}", finalUploadPath);
                return false;
            }
            // 检查并创建分块存储目录
            if (!Files.exists(chunkStoragePath)) {
                Files.createDirectories(chunkStoragePath);
                logger.info("Created chunk storage directory(分块存储目录创建成功): {}", chunkStoragePath);
            } else if (!Files.isDirectory(chunkStoragePath)) {
                logger.error("Chunk storage path exists but is not a directory(分块存储路径已存在但不是目录): {}", chunkStoragePath);
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("Could not initialize storage directories(无法初始化存储目录)", e);
            return false;
        }
    }

    /**
     * 存储单个文件块
     *
     * @param file           文件块
     * @param fileIdentifier 整个文件的唯一标识符
     * @param chunkNumber    当前块的编号 (从0开始)
     * @param totalChunks    总块数
     * @param originalFilename 原始文件名 (用于最终合并)
     * @return 如果所有块都已上传并合并成功，则返回最终文件的路径；否则返回null或部分信息。
     * @throws IOException IO异常
     */
    public CompletableFuture<String> storeChunkAndMerge(MultipartFile file, String fileIdentifier, int chunkNumber, int totalChunks, String originalFilename)
            throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty chunk.(无法存储空块)");
        }
        if (!StringUtils.hasText(fileIdentifier)) {
            throw new IllegalArgumentException("File identifier cannot be empty.(文件标识符不能为空)");
        }
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("Original filename cannot be empty.(原始文件名不能为空)");
        }


        Path specificChunkDir = chunkStoragePath.resolve(fileIdentifier);//路径结合
        Files.createDirectories(specificChunkDir); // 确保特定文件的块目录存在

        Path chunkFile = specificChunkDir.resolve(String.valueOf(chunkNumber)); // 块文件命名为它的编号

        // 清理原始文件名，防止路径遍历攻击
        String cleanOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(originalFilename));
        if (cleanOriginalFilename.contains("..")) {
            throw new RuntimeException("Cannot store file with relative path outside current directory(无法存储当前目录外具有相对路径的文件) " + cleanOriginalFilename);
        }

        logger.info("Storing chunk: identifier={}, chunkNumber={}, totalChunks={}, originalFilename={}, tempChunkFile={}",
                fileIdentifier, chunkNumber, totalChunks, cleanOriginalFilename, chunkFile);

        // 异步上传文件块，并在完成后检查是否需要合并
        return CompletableFuture.runAsync(() -> {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, chunkFile, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Chunk {} uploaded successfully", chunkNumber);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store chunk", e);
            }
        }, fileUploadExecutor)
        .thenApplyAsync(v -> {
            try {
                // 检查是否所有块都已上传
                long uploadedChunkCount;
                try (Stream<Path> stream = Files.list(specificChunkDir)) {
                    uploadedChunkCount = stream.count();
                }

                logger.info("Uploaded chunk count: {}/{}", uploadedChunkCount, totalChunks);

                if (uploadedChunkCount == totalChunks) {
                    // 使用原子操作确保只有一个线程执行合并
                    return mergeChunksIfAllPresent(specificChunkDir, cleanOriginalFilename, totalChunks);
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Failed to check chunks", e);
            }
        }, fileUploadExecutor);
    }


    /**
     * 使用分布式锁或原子操作确保合并逻辑只执行一次
     * @param chunkDir
     * @param originalFilename
     * @param totalChunks
     * @return
     * @throws IOException
     */
    private String mergeChunksIfAllPresent(
            Path chunkDir,
            String originalFilename,
            int totalChunks
    ) throws IOException {
        // 使用分布式锁或文件标记防止重复合并
        Path lockFile = chunkDir.resolve(".merge.lock");
        if (Files.exists(lockFile)) {
            logger.info("Merge already in progress, skipping");
            return null;
        }

        try {
            Files.createFile(lockFile); // 创建锁文件

            // 再次确认所有块都存在
            long actualChunkCount;
            try (Stream<Path> stream = Files.list(chunkDir)) {
                actualChunkCount = stream.filter(p -> !p.getFileName().toString().startsWith(".")).count();
            }

            if (actualChunkCount != totalChunks) {
                logger.warn("Chunk count mismatch after acquiring lock: {}/{}", actualChunkCount, totalChunks);
                return null;
            }

            // 执行合并操作
            return mergeChunks(chunkDir, originalFilename, totalChunks);
        } finally {
            Files.deleteIfExists(lockFile); // 释放锁
        }
    }


    /**
     * 合并指定标识符的所有块
     *
     * @param chunkDir      特定文件的块所在目录
     * @param finalFileName 最终合并后的文件名
     * @param totalChunks   预期的总块数 (用于校验)
     * @return 合并后文件的完整路径
     * @throws IOException IO异常
     */
    private String mergeChunks(Path chunkDir, String finalFileName, int totalChunks) throws IOException {
        Path finalFilePath = this.finalUploadPath.resolve(finalFileName);

        // 筛选并排序块文件（仅保留数字命名的文件，避免锁文件等干扰）
        List<Path> chunkFiles;
        try (Stream<Path> stream = Files.list(chunkDir)) {
            chunkFiles = stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        // 只处理纯数字命名的块文件，排除.lock等隐藏文件
                        return name.matches("\\d+");
                    })
                    .sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString())))
                    .collect(Collectors.toList());
        }

        // 校验块数量
        if (chunkFiles.size() != totalChunks) {
            logger.warn("Chunk count mismatch for {}: expected {}, found {}. Aborting.",
                    chunkDir.getFileName(), totalChunks, chunkFiles.size());
            throw new IOException("Chunk count mismatch during merge");
        }

        logger.info("Merging {} chunks into: {}", chunkFiles.size(), finalFilePath);

        // 合并文件：用Files.copy实现流拷贝
        try (OutputStream fos = Files.newOutputStream(finalFilePath)) {
            for (Path chunkFile : chunkFiles) {
                logger.debug("Appending chunk: {}", chunkFile);
                // 核心修改点：用Files.copy完成文件内容写入输出流
                Files.copy(chunkFile, fos);
            }
        }

        // 清理临时文件（先删文件再删空目录）
        cleanTempChunks(chunkDir);

        logger.info("Merge success: {}", finalFilePath);
        return finalFilePath.toString();
    }


    /**
     * 抽离清理逻辑，复用性更好
     *
     * @param chunkDir
     */
    private void cleanTempChunks(Path chunkDir) {
        try (Stream<Path> stream = Files.walk(chunkDir)) {
            // 逆序遍历，确保先删文件再删目录
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            if (Files.isRegularFile(path)) {
                                Files.delete(path);
                                logger.debug("Deleted temp file: {}", path);
                            }
                        } catch (IOException e) {
                            logger.error("Failed to delete temp file: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Failed to walk temp dir: {}", chunkDir, e);
        }
    }

    /**
     * 获取最终上传路径的字符串表示
     *
     * @return 最终上传路径的字符串表示
     */
    public String getFinalUploadPathString() {
        if (this.finalUploadPath == null) {
            // 如果 finalUploadPath 可能未初始化 (例如 init 失败或尚未调用)
            // 应该确保它在使用前被正确设置，通常通过 @PostConstruct 的 init
            // 这里可以加一个警告或尝试重新初始化，但最好是保证它已初始化
            logger.warn("finalUploadPath is null in getFinalUploadPathString(). Attempting to re-initialize paths.");
            this.init(); // 确保路径被设置 (如果init是幂等的且安全的)
            if(this.finalUploadPath == null) {
                throw new IllegalStateException("Final upload path is not initialized.");
            }
        }
        return this.finalUploadPath.toString();
    }

    /**
     * 删除指定文件名（包含后缀）的文件。
     *
     * @param filename 要删除的文件名（包含后缀）
     * @return 如果成功删除文件，则返回 true；否则返回 false。
     * @throws IOException 如果在删除文件时发生 I/O 错误。
     */
    public boolean deleteFileHou(String filename) throws IOException {
        return Files.deleteIfExists(this.finalUploadPath.resolve(filename));
    }
    /**
     * 删除指定基准名称的文件（忽略后缀名）。
     * 如果找到多个匹配项（例如 file.txt, file.jpg），则都会被删除。
     *
     * @param baseFilename 要删除的文件的基准名称（不含后缀）
     * @return 如果至少删除了一个文件，则返回 true；否则返回 false。
     * @throws IOException 如果在列出或删除文件时发生 I/O 错误。
     */
    public boolean deleteFileByName(String baseFilename) throws IOException {
        if (baseFilename == null || baseFilename.trim().isEmpty()) {
            logger.warn("Base filename for deletion is null or empty.");
            return false;
        }
        // 防止路径遍历攻击，确保 baseFilename 不包含路径分隔符
        if (baseFilename.contains("/") || baseFilename.contains("\\") || baseFilename.contains("..")) {
            logger.error("Invalid base filename for deletion (contains path characters): {}", baseFilename);
            throw new IllegalArgumentException("Base filename cannot contain path characters.");
        }

        AtomicBoolean deletedAtLeastOne = new AtomicBoolean(false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.finalUploadPath)) {
            System.out.println("this.finalUploadPath = " + this.finalUploadPath);
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    String currentFileName = entry.getFileName().toString();
                    String currentBaseName = getMainFileName(currentFileName);
                    System.out.println("currentFileName = " + currentFileName);
                    System.out.println("currentBaseName = " + currentBaseName);
                    if (baseFilename.equalsIgnoreCase(currentBaseName)) { // 不区分大小写匹配
                        logger.info("Attempting to delete file matching base name '{}': {}", baseFilename, entry);
                        try {
                            Files.delete(entry);
                            logger.info("Successfully deleted: {}", entry);
                            deletedAtLeastOne.set(true);
                        } catch (IOException e) {
                            logger.error("Failed to delete file: {}", entry, e);
                            // 根据需求决定是否继续删除其他匹配项或立即抛出异常
                            // 这里选择记录日志并继续
                        }
                    }
                }
            }
        }
        return deletedAtLeastOne.get();
    }


    /**
     * 获取文件的主文件名（不包含扩展名）
     *
     * @param path
     * @return 主文件名，如果没有扩展名，则返回原始路径
     */
    public static String getMainFileName(String path) {
        if (path == null) {
            return null;
        }
        // 找到最后一个点的位置（确保点不在开头或结尾）
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
            return path.substring(0, lastDotIndex); // 提取主文件名
        } else {
            return path; // 无有效扩展名时返回原路径
        }
    }

    /**
     * 获取文件的扩展名
     *
     * @param path
     * @return 扩展名，如果没有扩展名，则返回 null
     */
    public static String  getExtension(String path) {
        if (path == null) {
            return null;
        }
        // 找到最后一个点位置
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1); // 提取扩展名
        } else {
            return null; // 无有效扩展名时返回 null
        }
    }

    public Boolean deleteFile(String path) throws IOException {
        // 1. 构建 File 对象，对应目标文件
        File targetFile = new File(path);

        // 2. 先判断文件是否存在，且是文件（不是目录）
        if (!targetFile.exists()) {
            System.out.println("文件不存在：" + path);
            return false;
        }
        if (targetFile.isDirectory()) {
            System.out.println("路径是目录，不是文件：" + path);
            return false;
        }

        // 3. 执行删除，返回删除结果
        boolean deleteResult = targetFile.delete();
        if (deleteResult) {
            System.out.println("文件删除成功：" + path);
        } else {
            System.out.println("文件删除失败（可能无权限）：" + path);
        }
        return deleteResult;
    }

    public static void main(String[] args) throws IOException {
        String path = "D:/upload/63653958-1485-43bc-9a67-e9851ad3fa03.png";
        FileStorageService fileStorageService = new FileStorageService();
        fileStorageService.deleteFile(path);
    }
}
