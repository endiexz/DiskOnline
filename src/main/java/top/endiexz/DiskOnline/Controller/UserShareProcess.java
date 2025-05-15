package top.endiexz.DiskOnline.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.Utils.RsaUtil;
import top.endiexz.DiskOnline.entity.FileEntity;
import top.endiexz.DiskOnline.entity.User;
import top.endiexz.DiskOnline.entity.UserShare;
import top.endiexz.DiskOnline.mapper.FileMapper;
import top.endiexz.DiskOnline.mapper.UserMapper;
import top.endiexz.DiskOnline.mapper.UserShareMapper;

@RestController
public class UserShareProcess {
    @Autowired
    private UserShareMapper userShareMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/createshare")
    public ResponseEntity<Map<String, Object>> createShare( 
        @RequestHeader("token") String token,
        @RequestBody Map<String, String> requestBody){

        Map<String, Object> result = new HashMap<>();
        Long userId = JwtUtil.getTokenInfo(token).getClaim("userId").asLong();
        User user = userMapper.findByUserId(userId);                                                           
        Long fileId = Long.parseLong(requestBody.get("fileId"));
        int days =  Integer.parseInt(requestBody.get("days"));
        FileEntity file = fileMapper.getFileByFileId(fileId);

        //首先验证用户权限是否正确
        if(user.getUserRank().equals("normal")){
            if(user.getId()!=file.getCreatedBy()){
                result.put("status", false);
                result.put("message", "权限访问失败");
                return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
            }
        }

        UserShare userShare = new UserShare();
        userShare.setDays(days);
        userShare.setFileId(fileId);
        userShare.setFileName(file.getFileName());
        userShare.setIsDirectory(file.getIsDirectory());
        userShare.setUserId(userId);
        userShare.setVisitTimes(0);

        if(userShareMapper.insert(userShare)>0){
            userShare = userShareMapper.getUserShareById(userShare.getId());
            result.put("status", true);
            result.put("message", "创建链接成功");
            result.put("code", RsaUtil.encrypt(String.valueOf(userShare.getId())));
        }else{
            result.put("status", false);
            result.put("message", "插入数据失败");
        }
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
    }

    @PostMapping("/getShareList")
    public ResponseEntity<Map<String, Object>> getShareList(@RequestHeader("token") String token){
        Map<String, Object> result = new HashMap<>();
        Long userId = JwtUtil.getTokenInfo(token).getClaim("userId").asLong();

        List<UserShare> userShareList = userShareMapper.getUserShareListByUserId(userId);
        List<Map<String, Object>> sendUserShareList= new ArrayList<>();
        for(UserShare share : userShareList){
            Map<String, Object> tempShare = new HashMap<>();
            tempShare.put("id", share.getId());
            tempShare.put("userId", share.getUserId());
            tempShare.put("fileName", share.getFileName());
            tempShare.put("fileId", share.getFileId());
            tempShare.put("days", share.getDays());
            tempShare.put("visitTimes", share.getVisitTimes());
            tempShare.put("code", RsaUtil.encrypt(String.valueOf(share.getId())));
            tempShare.put("isDirectory", share.getCreateTime());
            tempShare.put("createTime", share.getCreateTime());
            sendUserShareList.add(tempShare);
        };
        result.put("shareList", sendUserShareList);
        result.put("status", true);
        result.put("message", "get userShareList success");
        
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
    }

    @PostMapping("/deletesharebyid")
    public ResponseEntity<Map<String, Object>> deleteShareById(@RequestBody Map<String, String> requestBody){
        Map<String, Object> result = new HashMap<>();
        if(userShareMapper.deleteById(Long.parseLong(requestBody.get("id")))>0){
            result.put("status", true);
            result.put("message", "删除成功");
        }else{
            result.put("status", false);
            result.put("message", "删除失败");
        }
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
    }

}


