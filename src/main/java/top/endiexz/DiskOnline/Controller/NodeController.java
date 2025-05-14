package top.endiexz.DiskOnline.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;


import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.Utils.MyMessageUtil;
import top.endiexz.DiskOnline.Utils.RsaUtil;
import top.endiexz.DiskOnline.entity.StorageNode;
import top.endiexz.DiskOnline.mapper.StorageNodeMapper;

@RestController
public class NodeController {

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private StorageNodeMapper storageNodeMapper;

    @PostMapping("afaddingnode")
    public ResponseEntity<Map<String, Object>> afAddingNode(@RequestHeader("token") String token, @RequestBody Map<String, String> requestBody) {

        Map<String, Object> result = new HashMap<>();
        String username = requestBody.get("username");
        String nodeinterface = requestBody.get("nodeinterface");
        String encryptedFingerprint = requestBody.get("encryptedFingerprint");


        // 验证申请状态是否是admin进行的操作
        String token_username = JwtUtil.getTokenInfo(token).getClaim("userName").asString();
        if (!username.equals("admin") || !token_username.equals("admin")) {
            result.put("status", false);
            result.put("message", "非法用户访问");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        //然后验证节点是否可靠
        try {
            String url = "http://" + nodeinterface + "/verifynode";

            // 创建新的请求体，包含 publickey
            Map<String, Object> nodeRequestBody = new HashMap<>();
            nodeRequestBody.put("publickey", RsaUtil.getPublicKeyBase64());
            nodeRequestBody.put("id", MyMessageUtil.getMyId());

            // 发起 POST 请求并获取响应
            Map<String, Object> response = webClientBuilder.baseUrl(url)
                    .build()
                    .post()
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON)) // 设置 Content-Type 为 JSON
                    .bodyValue(nodeRequestBody)  // 发送带有公钥的请求体
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // 阻塞等待返回结果

            // 从返回的 JSON 中解析出 publickey
            if (response != null) {
                // 直接解析为 Map<String, Object>
                if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                    String encryptedFingerprintNode = (String) response.get("encryptedFingerprint");

                    //验证发送过来的fingerprint
                    int decryptCode = RsaUtil.verifyDecryptedData(encryptedFingerprintNode);
                    if (decryptCode!=1) {
                        result.put("status", false);
                        result.put("message", RsaUtil.getDecryptionErrorMessage(decryptCode));
                        return new ResponseEntity<>(result, HttpStatus.OK);
                    }
                    if(!RsaUtil.getEncryptContent(encryptedFingerprintNode).equals(MyMessageUtil.getMyFingerPrint())){
                        result.put("status", false);
                        result.put("message", "指纹验证失败");
                        return new ResponseEntity<>(result, HttpStatus.OK);
                    }

                }
            }
        } catch (Exception e) {
            result.put("status", false);
            result.put("message", "尝试添加节点失败");
            System.out.println(e);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        //通过认证开始发送token以及让对方进行指纹验证
        String nodePublicKey;
        //获取对方的公钥
        try{    
            // 发起 POST 请求并获取响应
            String url = "http://" + nodeinterface + "/publickey";
            Map<String, Object> response = webClientBuilder.baseUrl(url)
                    .build()
                    .get()
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON)) // 设置 Content-Type 为 JSON
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // 阻塞等待返回结果
            if(response!=null){
                nodePublicKey = (String) response.get("publickey");
            }
            else{
                result.put("status", false);
                result.put("message", "获取节点公钥失败");
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            result.put("status", false);
            result.put("message", "获取节点公钥失败");
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        //验证fingerPrint是否正确
        int decryptCode  = RsaUtil.verifyDecryptedData(encryptedFingerprint);
        if(decryptCode!=1){
            result.put("status", false);
            result.put("message", "nodefingerprint 解码失败");
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        String nodeFingerprint = RsaUtil.getEncryptContent(encryptedFingerprint);

        try{
            String url = "http://" + nodeinterface + "/joincentral";
            Map<String, Object> requestBody1 = new HashMap<>();
            requestBody1.put("encryptedSecret", RsaUtil.encryptWithTimestamp(nodePublicKey, JwtUtil.getSecret()));
            requestBody1.put("encryptedFingerprint", RsaUtil.encryptWithTimestamp(nodePublicKey, nodeFingerprint));
            Map<String, Object> response = webClientBuilder.baseUrl(url)
                    .build()
                    .post()
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON)) // 设置 Content-Type 为 JSON
                    .bodyValue(requestBody1)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // 阻塞等待返回结果
            if(response!=null){
                if(!Boolean.TRUE.equals(response.get("status"))){
                    result.put("status", false);
                    result.put("message", "加入节点失败");
                    return new ResponseEntity<>(result, HttpStatus.OK);
                }
                StorageNode storageNode = new StorageNode();
                storageNode.setId(Long.parseLong(response.get("id").toString()));
                storageNode.setNodeName((String)response.get("nodeName"));
                storageNode.setNodeInterface((String)response.get("interface"));
                storageNode.setTotalCapacity(Long.parseLong(response.get("totalcapatity").toString()));
                storageNode.setUsedCapacity(Long.parseLong(response.get("usedcapacity").toString()));
                storageNode.setNetFlow(0l);
                storageNode.setFingerprint(nodeFingerprint);
                storageNode.setRunStatus("good");
                storageNode.setStatus((String) response.get("nodestatus"));
                storageNodeMapper.insertStorageNode(storageNode);

                result.put("status", true);
                result.put("message", "加入节点成功");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }

        } catch(Exception e){
            result.put("status", false);
            System.out.println(e);
            result.put("message", "未知错误");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    

        result.put("status", false);
        result.put("message", "未知错误");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("getallstoragenode")
    public ResponseEntity<Map<String, Object>> getAllStorageNode(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();

        int page = Integer.parseInt(requestBody.get("page"));
        int size = Integer.parseInt(requestBody.get("size"));
        String query = requestBody.get("query");
        int total;
        List<StorageNode> nodes;
        if(query == null || query.isEmpty()){
            nodes = storageNodeMapper.listNodes(size, (page - 1) * size);
            total = storageNodeMapper.getTotalNodeCount(); 
        }else{
            nodes = storageNodeMapper.findNodeByName(query);
            total=nodes.size();
        }

        result.put("status", true);
        result.put("total", total);

        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (StorageNode node : nodes) {
            // 创建一个 Map 来封装每个 StorageNode
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("id", node.getId());
            nodeMap.put("nodeName", node.getNodeName());
            nodeMap.put("totalCapacity", node.getTotalCapacity());
            nodeMap.put("usedCapacity", node.getUsedCapacity());
            nodeMap.put("netFlow", node.getNetFlow());
            //nodeMap.put("fingerprint", node.getFingerprint());
            nodeMap.put("status", node.getStatus());
            nodeMap.put("runStatus", node.getRunStatus());
            nodeMap.put("createdAt", node.getCreatedAt());
            nodeMap.put("updatedAt", node.getUpdatedAt());
            nodeMap.put("nodeInterface", node.getNodeInterface());
    
            // 将每个节点的 Map 加入到 nodeList 中
            nodeList.add(nodeMap);
        }
        result.put("nodes", nodeList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("modifystate") 
    public ResponseEntity<Map<String, Object>> modifyState(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String status = requestBody.get("status");
        long nodeId = Long.parseLong(requestBody.get("nodeid"));
        if(status.equals("active")){
            storageNodeMapper.updateNodeStatus("inactive", nodeId);
        }else{
            storageNodeMapper.updateNodeStatus("active", nodeId);
        }
        result.put("status", true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    //分站点向中心站定请求其他站点的信息
    @PostMapping("getnodeinfo")
    public  ResponseEntity<Map<String, Object>> getNodeInfo(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        String nodeInterface = requestBody.get("nodeInterface");
        String myNodeInterface = requestBody.get("myNodeInterface");
        StorageNode storageNode = storageNodeMapper.getNodeByInterface(nodeInterface);
        if(storageNode!=null){
            Map<String, Object> nodeInfo = new HashMap<>();

            String nodePublicKey =  storageNodeMapper.getPublicKeyByInterface(myNodeInterface);
            if(nodePublicKey == null){
                String url = "http://" + myNodeInterface + "/publickey";
                Map<String, Object> response = webClientBuilder.baseUrl(url)
                    .build()
                    .get()
                    .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON)) // 设置 Content-Type 为 JSON
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // 阻塞等待返回结果
                    if(response!=null){
                        nodePublicKey = (String) response.get("publickey");
                        int nodeId = storageNodeMapper.getNodeIdByInterface(myNodeInterface);
                        storageNodeMapper.insertNodePublicKey(nodeId, myNodeInterface, nodePublicKey);

                    }
            }
            nodeInfo.put("id", storageNode.getId());
            nodeInfo.put("nodeName", storageNode.getNodeName());
            nodeInfo.put("encryptedTotalCapacity", RsaUtil.encryptWithTimestamp(nodePublicKey, String.valueOf(storageNode.getTotalCapacity())));
            nodeInfo.put("encryptedUsedCapacity", RsaUtil.encryptWithTimestamp(nodePublicKey, String.valueOf(storageNode.getUsedCapacity())));
            nodeInfo.put("encryptedFingerprint", RsaUtil.encryptWithTimestamp(nodePublicKey, String.valueOf(storageNode.getFingerprint())));
            nodeInfo.put("nodeInterface", nodeInterface);
            nodeInfo.put("status", storageNode.getStatus());
            result.put("status", true);
            result.put("nodeInfo", nodeInfo);
            result.put("message", "获取节点信息成功");
        }
        else{
            result.put("status", false);
            result.put("message", "没有改节点的信息");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
