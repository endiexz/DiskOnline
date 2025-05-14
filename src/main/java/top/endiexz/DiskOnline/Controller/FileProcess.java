package top.endiexz.DiskOnline.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.entity.FileChunk;
import top.endiexz.DiskOnline.entity.FileEntity;
import top.endiexz.DiskOnline.entity.StorageNode;
import top.endiexz.DiskOnline.entity.User;
import top.endiexz.DiskOnline.mapper.ChunkMapper;
import top.endiexz.DiskOnline.mapper.FileMapper;
import top.endiexz.DiskOnline.mapper.StorageNodeMapper;
import top.endiexz.DiskOnline.mapper.UserMapper;

@RestController
public class FileProcess {



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private ChunkMapper chunkMapper;

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private StorageNodeMapper storageNodeMapper;

    @PostMapping("getuserfilebyparentid")
    public ResponseEntity<Map<String, Object>> getUserFile(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long userId = Long.parseLong(requestBody.get("userId").toString());
        Long parentId = Long.parseLong(requestBody.get("parentId"));

        User user = userMapper.findByUserId(userId);

        if(user.getUserRank()!="admin"){
            //前端生成绝对路径有点难后端产生吧
            FileEntity folder = fileMapper.getFileByFileId(parentId);
            FileEntity homeFolder = fileMapper.getFolderByNameAndParentId(user.getUsername(), 3l);
            if(folder.getParentId() == 3){
                result.put("parentId", parentId);
                result.put("absolutePath", "~");
            }else{
                result.put("parentId", folder.getParentId());
                String absolutePath = "/"+folder.getFileName();
                while(folder.getParentId()!=homeFolder.getFileId()){
                    folder = fileMapper.getFileByFileId(folder.getParentId());
                    absolutePath = "/"+folder.getFileName()+absolutePath;
                }
                absolutePath = "~"+absolutePath;
                result.put("absolutePath", absolutePath);
            }
        }
        List<FileEntity> fileList = fileMapper.getFilesByUserAndParentId(userId, parentId);

        List<Map<String, Object>> sendFileList = new ArrayList<>();
        for(FileEntity file : fileList ){
            Map<String, Object> sendFile = new HashMap<>();
            sendFile.put("fileId", file.getFileId());
            sendFile.put("fileName", file.getFileName());
            sendFile.put("fileSha256", file.getFileSha256());
            sendFile.put("fileSize", file.getFileSize());
            sendFile.put("fileType", file.getFileType());
            sendFile.put("isDirectory", file.getIsDirectory());
            sendFile.put("updatedAt", file.getUpdatedAt());
            sendFileList.add(sendFile); 
        }
        result.put("status", true);
        result.put("message", "获取文件信息成功");
        //因为前段需要维护一个parentId因此在这里返回
        result.put("filelist", sendFileList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping("getuserrecyclefilebyparentid")
    public ResponseEntity<Map<String, Object>> getUserRecycleFile(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long userId = Long.parseLong(requestBody.get("userId").toString());
        Long parentId = Long.parseLong(requestBody.get("parentId"));

        User user = userMapper.findByUserId(userId);

        if(user.getUserRank()!="admin"){
            //前端生成绝对路径有点难后端产生吧
            FileEntity folder = fileMapper.getFileByFileId(parentId);
            if(folder.getParentId() == 1){
                result.put("parentId", parentId);
                result.put("absolutePath", "RecycleDir/");
            }else{
                result.put("parentId", folder.getParentId());
                String absolutePath = "/"+folder.getFileName();
                while(folder.getParentId()!=2){
                    folder = fileMapper.getFileByFileId(folder.getParentId());
                    absolutePath = "/"+folder.getFileName()+absolutePath;
                }
                absolutePath = "RecycleDir"+absolutePath;
                result.put("absolutePath", absolutePath);
            }
        }
        List<FileEntity> fileList = fileMapper.getFilesByUserAndParentId(userId, parentId);

        List<Map<String, Object>> sendFileList = new ArrayList<>();
        for(FileEntity file : fileList ){
            Map<String, Object> sendFile = new HashMap<>();
            sendFile.put("fileId", file.getFileId());
            sendFile.put("fileName", file.getFileName());
            sendFile.put("fileSha256", file.getFileSha256());
            sendFile.put("fileSize", file.getFileSize());
            sendFile.put("fileType", file.getFileType());
            sendFile.put("isDirectory", file.getIsDirectory());
            sendFile.put("updatedAt", file.getUpdatedAt());
            sendFileList.add(sendFile); 
        }
        result.put("status", true);
        result.put("message", "获取文件信息成功");
        //因为前段需要维护一个parentId因此在这里返回
        result.put("filelist", sendFileList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }



    //删除文件
    @PostMapping("delfile")
    public ResponseEntity<Map<String, Object>>delFile(
        @RequestBody Map<String, String> requestBody,
        @RequestHeader("token") String token
    ){

        Map<String, Object> result = new HashMap<>();
        Long userId = JwtUtil.getTokenInfo(token).getClaim("userId").asLong();
        Long fileId = Long.parseLong(requestBody.get("fileId"));
        User user = userMapper.findByUserId(userId);
        FileEntity file = fileMapper.getFileByFileId(fileId);
        //首先判断用户
        if(user.getUserRank().equals("user")){
            //判断是否是用户自己的文件防止越权删除文件
            if(file.getCreatedBy()==userId){
                if(deleteFileUtile(fileId)){
                    result.put("status", true);
                    result.put("message", "删除文件成功");
                }else{
                    result.put("status", false);
                    result.put("message", "删除文件链出现错误");
                }
                
            }else{
                result.put("status", false);
                result.put("message", "越权访问");
            }
        }else{
            //如果是admin防止用户删除其他用户的家目录
            if(file.getParentId()!=3){
                if(deleteFileUtile(fileId)){
                    result.put("status", true);
                    result.put("message", "删除文件成功");
                }else{
                    result.put("status", false);
                    result.put("message", "删除文件链出现错误");
                }
            }else{
                result.put("status", false);
                result.put("message", "禁止删除用户目录");
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @PostMapping("addfolder")
    public ResponseEntity<Map<String, Object>> addFolder(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String folderName = requestBody.get("folderName");
        Long parentId = Long.parseLong(requestBody.get("parentId"));
        Long userId = Long.parseLong(requestBody.get("userId"));
        //检查是否有同名文件的存在
        List<FileEntity> fileList = fileMapper.getFilesByParentId(parentId);
        for(FileEntity file: fileList){
            if(file.getFileName().equals(folderName)){
                result.put("status", false);
                result.put("message", "不允许同名文件的存在");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        FileEntity newFolder = new FileEntity();
        newFolder.setFileName(folderName);
        newFolder.setFileSha256(null);
        newFolder.setCreatedBy(userId);
        newFolder.setFileSize(Long.valueOf(0));
        newFolder.setFileType("folder");
        newFolder.setIsDirectory(true);
        newFolder.setChunkSize(Long.valueOf(0));
        newFolder.setChunkCount(0);
        newFolder.setParentId(parentId);
        fileMapper.insertFolder(newFolder);
        result.put("status", true);
        result.put("message", "创建文件夹成功");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("modifyfilename")
    public ResponseEntity<Map<String, Object>> modifyFileName(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long fileId = Long.parseLong(requestBody.get("fileId"));
        String mFileName = requestBody.get("mfilename");
        Long parentId = Long.parseLong(requestBody.get("parentId"));
        List<FileEntity> fileList = fileMapper.getFilesByParentId(parentId);
        for(FileEntity file: fileList){
            if(file.getFileName().equals(mFileName)){
                result.put("status", false);
                result.put("message", "不允许同名文件的存在");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        if(fileMapper.updateFileNameById(fileId, mFileName)>0){
            result.put("status", true);
            result.put("message", "修改文件名成功");
        }else{
            result.put("status", false);
            result.put("message", "修改文件名失败");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);

    }
    @PostMapping("getdownloadpath")
    public ResponseEntity<Map<String, Object>> getDownloadPath(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long fileId  = Long.parseLong(requestBody.get("fileId"));
        FileEntity file = fileMapper.getFileByFileId(fileId);
        if(file.getIsDirectory()){
            result.put("status", false);
            result.put("message", "文件夹不支持下载");
        }else{
            Map<String, Object> task = new HashMap<>();
            List<FileChunk> chunkList = chunkMapper.getChunksByFileSha256(file.getFileSha256());
            List<StorageNode> nodeList = storageNodeMapper.listAllNodes();
            List<Map<String, Object>> chunkPathList = new ArrayList<>();
            for(FileChunk chunk : chunkList){
                StorageNode mainNode=null, extraNode=null;
                Map<String, Object> chunkPath = new HashMap<>();
                for(StorageNode node : nodeList){
                    if(node.getNodeInterface().equals(chunk.getNodeInterface())){
                        mainNode = node;
                    }
                    if(node.getNodeInterface().equals(chunk.getExtraNodeInterface())){
                        extraNode = node;
                    }
                    if(mainNode!=null && extraNode!=null){
                        break;
                    }
                }
                StorageNode tempNode;
                if(mainNode.getStatus().equals("active")&&mainNode.getRunStatus().equals("good")&&extraNode.getStatus().equals("active")&&extraNode.getRunStatus().equals("good")){
                    if(mainNode.getNetFlow()<extraNode.getNetFlow()){
                        tempNode = mainNode;
                    }else{
                        tempNode = extraNode;
                    }

                }else{
                    if(mainNode.getStatus().equals("active")&&mainNode.getRunStatus().equals("good")){
                        tempNode = mainNode;
                    }else{
                        tempNode = extraNode;
                    }
                }

                chunkPath.put("chunkOrder", chunk.getChunkOrder());
                chunkPath.put("chunkSize", chunk.getChunkSize());
                chunkPath.put("nodeId", tempNode.getId());
                chunkPath.put("nodeInterface", tempNode.getNodeInterface());
                chunkPathList.add(chunkPath);
            }
            task.put("fileId", file.getFileId());
            task.put("fileName", file.getFileName());
            task.put("fileSize", file.getFileSize());
            task.put("fileSha256", file.getFileSha256());
            task.put("chunkSize", file.getChunkSize());
            task.put("chunkTotal", file.getChunkCount());
            task.put("chunkList", chunkPathList);
            result.put("task", task);
            result.put("status", true);
            result.put("message", "下载路径获取成功");
            System.out.println("返回数据成功了啊");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("movetorecyclebin")
    public ResponseEntity<Map<String, Object>> moveToRecycleBin(@RequestHeader("token") String token,@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long userId = JwtUtil.getTokenInfo(token).getClaim("userId").asLong();
        Long fileId = Long.parseLong(requestBody.get("fileId"));
        FileEntity file = fileMapper.getFileByFileId(fileId);
        //权限判定
        if(userId!=file.getCreatedBy()){
            result.put("status",false);
            result.put("message", "权限错误");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        //同名文件判定
        List<FileEntity> ShareFileList = fileMapper.getFilesByParentId(2l);

        for(FileEntity tempFile : ShareFileList){
            if(tempFile.getFileName().equals(file.getFileName())){
                result.put("status",false);
                result.put("message", "不允许同名文件存在");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        FileEntity tempFile = fileMapper.getFileByFileId(file.getParentId());
        String origionalParentPath = "";
        while(tempFile.getFileId()!=1){
            
            origionalParentPath = "/"+tempFile.getFileName()+origionalParentPath;
            tempFile = fileMapper.getFileByFileId(tempFile.getParentId());
        }
        if(fileMapper.updateFileParentIdAndOriginalParentPathById(fileId, 2l, origionalParentPath)==1){
            result.put("status",true);
            result.put("message", "move to recycleDir successfully");
        }else{
            result.put("status",false);
            result.put("message", "修改失败");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("recoverfile")
    public ResponseEntity<Map<String, Object>>recoverFile(@RequestHeader("token") String token,@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long userId = JwtUtil.getTokenInfo(token).getClaim("userId").asLong();
        Long fileId = Long.parseLong(requestBody.get("fileId"));
        FileEntity file = fileMapper.getFileByFileId(fileId);
        //权限判定
        if(userId!=file.getCreatedBy()){
            result.put("status",false);
            result.put("message", "权限错误");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        //是否是回收站文件判定 外加获取文件路径
        String origionalParentPath = "";
        if(file.getParentId() == 2){
            origionalParentPath = file.getOriginalParentPath();
        }else{
            FileEntity tempFile = fileMapper.getFileByFileId(file.getParentId());
            while(tempFile.getParentId()!=2&&tempFile.getParentId()!=1){
                origionalParentPath="/"+tempFile.getFileName()+origionalParentPath;
                tempFile = fileMapper.getFileByFileId(tempFile.getParentId());
            }
            if(tempFile.getFileId()==1){
                result.put("status",false);
                result.put("message", "非回收站文件");
                return new ResponseEntity<>(result, HttpStatus.OK); 
            }
            origionalParentPath=tempFile.getOriginalParentPath()+"/"+tempFile.getFileName()+origionalParentPath;
        }
        //然后从家目录递归处理文件

        String[] paths = origionalParentPath.split("/");
        FileEntity tpFile = fileMapper.getFileByFileId(1l);
        for(int i=1;i<paths.length;i++){
            List<FileEntity> fileList = fileMapper.getFilesByParentId(tpFile.getFileId());
            Boolean continueLog = false;
            for(FileEntity tfile: fileList){
                if(tfile.getFileName().equals(paths[i])){
                    tpFile = tfile;
                    continueLog = true;
                    break;
                }
            }
            if(continueLog){
                continue;
            }
            FileEntity tFolder = new FileEntity();

            tFolder.setFileName(paths[i]);
            tFolder.setFileSha256(null);
            tFolder.setCreatedBy(userId);
            tFolder.setFileSize(Long.valueOf(0));
            tFolder.setFileType("folder");
            tFolder.setIsDirectory(true);
            tFolder.setChunkSize(Long.valueOf(0));
            tFolder.setChunkCount(0);
            tFolder.setParentId(tpFile.getFileId());
            fileMapper.insertFolder(tFolder);  

            tpFile = fileMapper.getFolderByNameAndParentId(paths[i], tpFile.getFileId());
        }


        List<FileEntity> fileList = fileMapper.getFilesByParentId(tpFile.getFileId());
        //检查是否有重名的文件
        for(FileEntity tfile: fileList){
            if(tfile.getFileName().equals(file.getFileName())){
                result.put("status", false);
                result.put("message", "存在同名文件恢复失败");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        //然后就能恢复文件了
        if(fileMapper.updateFileParentIdAndOriginalParentPathById(file.getFileId(), tpFile.getFileId(), null)==1){
            result.put("status", true);
            result.put("message", "恢复文件成功");
        }else{
            result.put("status", false);
            result.put("message", "恢复文件失败");
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }







    //删除文件utils
    public Boolean deleteFileUtile(Long fileId){
        FileEntity file = fileMapper.getFileByFileId(fileId);
        if(file.getIsDirectory()==true){
            List<FileEntity> fileList = fileMapper.getFilesByParentId(fileId);
            for(FileEntity cfile : fileList){
                if(!deleteFileUtile(cfile.getFileId())){
                    return false;
                }
            }
        }else{
            //判断是否是最后一个文件
            if(fileMapper.getAllFileByFileSha256(file.getFileSha256()).size()<=1){
                List<FileChunk> chunkList = chunkMapper.getChunksByFileSha256(file.getFileSha256());
                if(chunkList.size()>0){
                    for(FileChunk chunk : chunkList){
                        Map<String, Object> chunkRequestBody = new HashMap<>();
                        chunkRequestBody.put("fileSha256", file.getFileSha256());
                        chunkRequestBody.put("chunkOrder", chunk.getChunkOrder());
                        Map<String, Object> nodeResponse = webClientBuilder
                            .baseUrl("http://"+chunk.getNodeInterface()) // baseUrl 里不要带路径
                            .build()
                            .post()
                            .uri("/delchunk") // 单独写路径部分
                            .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                            .bodyValue(chunkRequestBody)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block(); // 阻塞等待响应

                            Map<String, Object> extraNodeResponse = webClientBuilder
                            .baseUrl("http://"+chunk.getExtraNodeInterface()) // baseUrl 里不要带路径
                            .build()
                            .post()
                            .uri("/delchunk") // 单独写路径部分
                            .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                            .bodyValue(chunkRequestBody)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block(); // 阻塞等待响应
                        if(Boolean.FALSE.equals(nodeResponse.get("status"))||Boolean.FALSE.equals(extraNodeResponse.get("status"))){
                            return false;
                        }else{
                            chunkMapper.deleteChunksByFileSha256(file.getFileSha256(), chunk.getChunkOrder());
                        }
                    }
                }else{
                    return false;
                }
            }
        }
        if(fileMapper.deleteFileByFileId(fileId)>0){
            return true;
        }else{
            return false;
        }
    }

    


}
