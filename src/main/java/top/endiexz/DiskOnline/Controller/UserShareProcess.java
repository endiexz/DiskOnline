package top.endiexz.DiskOnline.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.entity.FileEntity;
import top.endiexz.DiskOnline.entity.User;
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
        FileEntity file = fileMapper.getFileByFileId(fileId);

        //首先验证用户权限是否正确
        if(user.getUserRank().equals("normal")&&user.getId()==fileId){

        }
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
    }

}


