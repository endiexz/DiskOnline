package top.endiexz.DiskOnline.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import top.endiexz.DiskOnline.entity.FileChunk;
import top.endiexz.DiskOnline.entity.FileEntity;
import top.endiexz.DiskOnline.entity.StorageNode;
import top.endiexz.DiskOnline.entity.UserTask;
import top.endiexz.DiskOnline.mapper.ChunkMapper;
import top.endiexz.DiskOnline.mapper.FileMapper;
import top.endiexz.DiskOnline.mapper.StorageNodeMapper;
import top.endiexz.DiskOnline.mapper.TaskMapper;

@RestController
public class TaskProcess {

    @Autowired
    private StorageNodeMapper storageNodeMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ChunkMapper chunkMapper;

    @Autowired
    private WebClient.Builder webClientBuilder;


    //get file upload path
    @PostMapping("/gflpath")
    public ResponseEntity<Map<String, Object>>glfPath( @RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        UserTask userTask = new UserTask();
        userTask.setParentId( Long.parseLong(requestBody.get("parentid")));
        userTask.setUserId(Long.parseLong(requestBody.get("userid")));
        userTask.setFileName(requestBody.get("filename"));
        userTask.setFileSha256(requestBody.get("sha256"));
        userTask.setAbsolutePath(requestBody.get("absolutePath"));
        userTask.setFileSize(Long.parseLong(requestBody.get("filesize")));
        //每个文件分片大小为10M
        long chunkNum = (userTask.getFileSize() + (1024 * 1024 * 10L) - 1) / (1024 * 1024 * 10L);
        userTask.setChunkTotal((int) chunkNum);
        userTask.setChunkSize(1024*1024*10L);
        userTask.setChunkUploaded(0);
        //检查是否有存在文件或者上传任务

        //检查同目录下是否存在同名文件

        List<FileEntity> searchFileList = fileMapper.getFilesByParentId(userTask.getParentId());
        for( FileEntity file : searchFileList){
            if(file.getFileName().equals(userTask.getFileName())){
                result.put("status", false);
                result.put("message", "不允许同名文件");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        
        
        //检查是否有存在的上传任务
        if(taskMapper.countTasksBySha256(userTask.getFileSha256())>0){
            UserTask exitUserTask = taskMapper.getUserTaskBySha256(userTask.getFileSha256());
            String[] fileNames = exitUserTask.getFileName().split("\\|");
            String[] absolutePaths = exitUserTask.getAbsolutePath().split("\\|");
            if(Arrays.asList(absolutePaths).contains(userTask.getAbsolutePath())){
                int abindex = Arrays.asList(absolutePaths).indexOf(userTask.getAbsolutePath());
                System.out.println("exit name:"+fileNames[abindex]);
                System.out.println("upload name:"+userTask.getFileName());
                if(fileNames[abindex].equals(userTask.getFileName())){
                    result.put("status", false);
                    result.put("message", "上传任务存在请勿重复上传");
                }else{
                    result.put("status", false);
                    result.put("message", "检测到相同文件上传任务存在，请等待上传");
                    taskMapper.updateUserTaskPathAndName(exitUserTask.getTaskId(), exitUserTask.getFileName()+"|"+userTask.getFileName(), exitUserTask.getAbsolutePath()+"|"+userTask.getAbsolutePath());
                }
                return new ResponseEntity<>(result, HttpStatus.OK);
            }else{
                result.put("status", false);
                result.put("message", "检测到相同文件上传任务存在，请等待上传");
                taskMapper.updateUserTaskPathAndName(exitUserTask.getTaskId(), exitUserTask.getFileName()+"|"+userTask.getFileName(), exitUserTask.getAbsolutePath()+"|"+userTask.getAbsolutePath());
                return new ResponseEntity<>(result, HttpStatus.OK);
            }

        }



        //检查是否存在该文件 存在的话就不用上传了
        if(fileMapper.getAllFileByFileSha256(userTask.getFileSha256()).size()>0){
            //不需要给用户安排上传任务上传文件标记就行
            Map<String, Object> task = new HashMap<>();
            List<Map<String, Object>> chunkList = new ArrayList<>();
            task.put("taskId", taskMapper.getTaskIdBySha256(userTask.getFileSha256()));
            task.put("fileName", userTask.getFileName());
            task.put("fileSize", userTask.getFileSize());
            task.put("fileSha256", userTask.getFileSha256());
            task.put("chunkSize", userTask.getChunkSize());
            task.put("chunkTotal", chunkNum);
            task.put("chunkUploaded", chunkNum);
            task.put("absolutePath", userTask.getAbsolutePath());
            task.put("chunkList", chunkList);
            result.put("status", true);
            result.put("task", task);

            result.put("message", "已存在该文件备份不需要重传");
            userTask.setChunkUploaded((int)chunkNum);
            taskMapper.insertUserTask(userTask);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }




        List<StorageNode> nodeList = storageNodeMapper.listAllNodes();
        //剔除不适合的节点
        nodeList.removeIf(node ->
        "inactive".equalsIgnoreCase(node.getStatus()) ||
        "fault".equalsIgnoreCase(node.getRunStatus())
        );

        //对所有节点进行排序
        List<StorageNode> sortedNodeList = nodeList.stream()
        .sorted((a, b) -> Long.compare(
        (b.getTotalCapacity() - b.getUsedCapacity()),
        (a.getTotalCapacity() - a.getUsedCapacity())))
        .collect(Collectors.toList());

        List<Map<String, Object>> chunkList = new ArrayList<>();

        for(int i=0;i<chunkNum;i++){
            StorageNode node0 = sortedNodeList.get(0);
            StorageNode node1 = sortedNodeList.get(1);
            node0.setUsedCapacity(node0.getUsedCapacity() + 1024 * 1024 * 10);
            node1.setUsedCapacity(node1.getUsedCapacity() + 1024 * 1024 * 10);
            // 计算可用空间
            long avail0 = node0.getTotalCapacity() - node0.getUsedCapacity();
            long avail1 = node1.getTotalCapacity() - node1.getUsedCapacity();
            if(avail0<0 || avail1<0){
                result.put("status", false);
                result.put("message", "节点空间不足，请添加新的节点");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            //添加chunk任务节点
            FileChunk insertFileChunk = new FileChunk();
            insertFileChunk.setChunkOrder(i);
            insertFileChunk.setChunkSize(i==chunkNum-1?userTask.getFileSize()%(1024*1024*10L):1024*1024*10L);
            insertFileChunk.setFileSha256(userTask.getFileSha256());
            insertFileChunk.setIsLoaded(0);
            insertFileChunk.setNodeId(node0.getId());
            insertFileChunk.setNodeInterface(node0.getNodeInterface());
            insertFileChunk.setExtraNodeId(node1.getId());
            insertFileChunk.setExtraNodeInterface(node1.getNodeInterface());

            chunkMapper.insertChunk(insertFileChunk);


            //返回分片节点上传任务信息
            Map<String, Object> chunkPath = new HashMap<>();
            chunkPath.put("chunkOrder", insertFileChunk.getChunkOrder());
            chunkPath.put("nodeId", insertFileChunk.getNodeId());
            chunkPath.put("nodeInterface", insertFileChunk.getNodeInterface());
            chunkPath.put("extraNodeId", insertFileChunk.getExtraNodeId());
            chunkPath.put("extraNodeInterface", insertFileChunk.getExtraNodeInterface());
            chunkPath.put("chunkSize", insertFileChunk.getChunkSize());
            chunkList.add(chunkPath);
            // 按照剩余容量重新排序（大 -> 小）
            sortedNodeList.sort((a, b) -> Long.compare(
                (b.getTotalCapacity() - b.getUsedCapacity()),
                (a.getTotalCapacity() - a.getUsedCapacity())
            ));

        }

        taskMapper.insertUserTask(userTask);
        Map<String, Object> task = new HashMap<>();

        task.put("taskId", taskMapper.getTaskIdBySha256(userTask.getFileSha256()));
        task.put("fileName", userTask.getFileName());
        task.put("fileSize", userTask.getFileSize());
        task.put("fileSha256", userTask.getFileSha256());
        task.put("chunkSize", userTask.getChunkSize());
        task.put("chunkTotal", chunkNum);
        task.put("chunkUploaded", 0);
        task.put("absolutePath", userTask.getAbsolutePath());
        task.put("chunkList", chunkList);
        result.put("status", true);
        result.put("task", task);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping("getfileloadtask")
    public ResponseEntity<Map<String, Object>>getFileLoadTask(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        Long userId = Long.parseLong(requestBody.get("userid"));
        List<UserTask> userTasks = taskMapper.getUserTasksByUserId(userId);
        if(userTasks.size()==0){
            result.put("status", true);
            result.put("tasknum", 0);
        }else{
            result.put("tasknum", userTasks.size());
            List<Map<String, Object>> tasksList = new ArrayList<>();
            for (UserTask userTask : userTasks) { // 注意这里指定了类型并且使用了正确的变量名
                Map<String, Object> task = new HashMap<>();
                task.put("taskId", userTask.getTaskId());
                task.put("fileName", userTask.getFileName().split("\\|")[0]);
                task.put("fileSize", userTask.getFileSize());
                task.put("fileSha256", userTask.getFileSha256());
                task.put("chunkSize", userTask.getChunkSize());
                task.put("chunkTotal", userTask.getChunkTotal());
                task.put("chunkUploaded", userTask.getChunkUploaded());
                task.put("absolutePath", userTask.getAbsolutePath().split("\\|")[0]);
                List<Map<String, Object>> chunkList = new ArrayList<>();
                List<FileChunk> fileChunks = chunkMapper.getChunksByFileSha256(userTask.getFileSha256());
                for(FileChunk filechunk : fileChunks){
                    if(filechunk.getIsLoaded() == 0){
                        Map<String, Object> chunk = new HashMap<>();
                        chunk.put("chunkOrder", filechunk.getChunkOrder());
                        chunk.put("chunkSize", filechunk.getChunkSize());
                        chunk.put("nodeId", filechunk.getNodeId());
                        chunk.put("nodeInterface", filechunk.getNodeInterface());
                        chunk.put("extraNodeId", filechunk.getExtraNodeId());
                        chunk.put("extraNodeInterface", filechunk.getExtraNodeInterface());
                        chunkList.add(chunk);
                    }
                }
                task.put("chunkList", chunkList);
                result.put("status", true);
                tasksList.add(task);
            }
            result.put("taskList", tasksList);
        }
        

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("fileuploadedverify")
    public ResponseEntity<Map<String, Object>> fileUploadedVerify(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String fileSha256 = requestBody.get("fileSha256");
        UserTask userTask = taskMapper.getUserTaskBySha256(fileSha256);
        if(userTask.getChunkTotal()==userTask.getChunkUploaded()){
            //传输完成就把任务从任务列表中移除
            String[] fileNames = userTask.getFileName().split("\\|");
            String[] absolutePaths = userTask.getAbsolutePath().split("\\|");
            if(fileNames.length==absolutePaths.length){
                for(int i=0;i<fileNames.length;i++){
                    Long fileParentId;
                    //通过绝对路径查找文件夹信息
                    String[] paths = absolutePaths[i].split("/");
                    if(paths.length>1){
                        Long findParentId = Long.valueOf(1);
                        String findAbsolutePath = "/";
                        for(int j=1;j<paths.length;j++){
                            FileEntity findFolder = fileMapper.getFolderByNameAndParentId(paths[j], findParentId);
                            if(findFolder==null){
                                //发现为空就创建一个文件夹
                                FileEntity newFolder = new FileEntity();
                                newFolder.setFileName(paths[j]);
                                newFolder.setFileSha256(null);
                                newFolder.setCreatedBy(userTask.getUserId());
                                newFolder.setFileSize(Long.valueOf(0));
                                newFolder.setFileType("folder");
                                newFolder.setIsDirectory(true);
                                newFolder.setChunkSize(Long.valueOf(0));
                                newFolder.setChunkCount(0);
                                newFolder.setParentId(findParentId);
                                fileMapper.insertFolder(newFolder);
                                //获取刚刚创建文件夹的id
                                findFolder = fileMapper.getFolderByNameAndParentId(paths[j], findParentId);
                                if(findFolder!=null){
                                    findParentId = findFolder.getFileId();
                                }
                                if(!findAbsolutePath.equals("/")){
                                    findAbsolutePath=findAbsolutePath+"/"+paths[j];
                                }else{
                                    findAbsolutePath=findAbsolutePath+paths[j];
                                }
                            }else{
                                findParentId = findFolder.getFileId();
                                if(!findAbsolutePath.equals("/")){
                                    findAbsolutePath=findAbsolutePath+"/"+paths[j];
                                }else{
                                    findAbsolutePath=findAbsolutePath+paths[j];
                                }
                            }
                        }
                        fileParentId = findParentId;

                    }else{
                        fileParentId = Long.valueOf(1);
                    }
                    FileEntity newFile = new FileEntity();
                    newFile.setFileName(userTask.getFileName());
                    newFile.setFileSha256(userTask.getFileSha256());
                    newFile.setCreatedBy(userTask.getUserId());
                    newFile.setFileSize(userTask.getFileSize());
                    newFile.setFileType("file");
                    newFile.setIsDirectory(false);
                    newFile.setChunkSize(userTask.getChunkSize());
                    newFile.setChunkCount(userTask.getChunkTotal());
                    newFile.setParentId(fileParentId);
                    fileMapper.insertFile(newFile);
                }
            }
            if(taskMapper.deleteUserTaskBySha256(fileSha256)==1){
                result.put("status", true);
                result.put("message", "任务上传完成");
            }
        }
        else{
            result.put("status", false);
            result.put("message", "任务没有完成");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("deltask")
    public ResponseEntity<Map<String, Object>>delTask(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String fileSha256 = requestBody.get("fileSha256");
        UserTask userTask = taskMapper.getUserTaskBySha256(fileSha256);
        if(userTask!=null){
            List<FileChunk> chunkList = chunkMapper.getChunksByFileSha256(fileSha256);
            if(chunkList.size()>0){
                for(FileChunk chunk:chunkList){
                    if(chunk.getIsLoaded()==0){
                        //还没有上传直接删除
                        chunkMapper.deleteChunksByFileSha256(fileSha256, chunk.getChunkOrder());
                    }else{
                        //如果上传则通过node以及extranode删除改分片
                        Map<String, Object> chunkRequestBody = new HashMap<>();
                        chunkRequestBody.put("fileSha256", fileSha256);
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
                        if(Boolean.TRUE.equals(nodeResponse.get("status")) && Boolean.TRUE.equals(extraNodeResponse.get("status"))){
                            chunkMapper.deleteChunksByFileSha256(fileSha256, chunk.getChunkOrder());
                        }else{
                            result.put("status", false);
                            result.put("message", "删除分片失败"+chunk.getChunkOrder()+nodeResponse+extraNodeResponse);
                            return new ResponseEntity<>(result, HttpStatus.OK);
                        }
                    }
                }
                if(taskMapper.deleteUserTaskBySha256(fileSha256)>0){
                    result.put("status", true);
                    result.put("message", "删除任务成功");
                }
            }else{
                result.put("status", false);
                result.put("message", "没有找到分片信息");
            }
        }else{
            result.put("status", false);
            result.put("message", "没有找到任务信息");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
