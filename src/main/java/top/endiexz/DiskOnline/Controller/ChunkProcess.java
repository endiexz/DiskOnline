package top.endiexz.DiskOnline.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import top.endiexz.DiskOnline.mapper.ChunkMapper;
import top.endiexz.DiskOnline.mapper.StorageNodeMapper;
import top.endiexz.DiskOnline.mapper.TaskMapper;

@CrossOrigin(origins = "*")
@RestController
public class ChunkProcess {
    @Autowired
    private ChunkMapper chunkMapper;

    @Autowired 
    private TaskMapper taskMapper;

    @Autowired
    private StorageNodeMapper storageNodeMapper;

    @PostMapping("/chunkuploadedverify")
    public ResponseEntity<Map<String, Object>> chunkUploadedVerify(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String fileSha256 = requestBody.get("fileSha256");
        int chunkIndex = Integer.parseInt(requestBody.get("chunkIndex"));
        String nodeInterface = requestBody.get("nodeInterface");
        String extraNodeInterface = requestBody.get("extraNodeInterface");
        Long chunkSize = Long.parseLong(requestBody.get("chunkSize"));

        //String action = requestBody.get("uploaded");

        //修改分片文件状态
        chunkMapper.markChunkAsUploaded(fileSha256, chunkIndex);
        //修改任务中上传分片数量记录
        //UserTask userTask = fileMapper.getUserTaskBySha256(fileSha256);
        taskMapper.updateChunkUploadedBySha256(fileSha256);
        result.put("status", true);
        //增加对应节点的存储容量
        storageNodeMapper.increaseUsedCapacityByInterface(nodeInterface, chunkSize);
        storageNodeMapper.increaseUsedCapacityByInterface(extraNodeInterface, chunkSize);


        result.put("message", "修改分片状态成功");


        return new ResponseEntity<>(result, HttpStatus.OK);
    }



}
