package top.endiexz.DiskOnline.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;

import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.Utils.RsaUtil;
import top.endiexz.DiskOnline.entity.FileEntity;
import top.endiexz.DiskOnline.entity.User;
import top.endiexz.DiskOnline.mapper.FileMapper;
import top.endiexz.DiskOnline.mapper.UserMapper;


@RestController
public class UserSession {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMapper fileMapper;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> UserLogin(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();

        String username = requestBody.get("username");
        String encrypted = requestBody.get("encrypted");
        int decryptCode = RsaUtil.verifyDecryptedData(encrypted);
        if (decryptCode!=1) {
            result.put("status", false);
            result.put("message", RsaUtil.getDecryptionErrorMessage(decryptCode));
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        JSONObject json = JSON.parseObject(RsaUtil.decrypt(encrypted));
        String password = json.getString("content");
        
        User user = userMapper.findByUsernameAndPassword(username, password);
        
        
        
        
        if (user != null) {
            result.put("status", true);
            result.put("message", "登录成功");
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userid", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("user_rank", user.getUserRank());
            userInfo.put("availcapacity", user.getAvailCapacity());
            userInfo.put("usedcapacity", user.getUsedCapacity());
            if(!user.getUserRank().equals("admin")){
                FileEntity homeFolder = fileMapper.getFolderByNameAndParentId(username, 3l);
                userInfo.put("homeFolderId", homeFolder.getFileId());
            }
            result.put("userinfo", userInfo);
            String token = JwtUtil.getToken(user.getId(), username);
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", token);
            return new ResponseEntity<Map<String, Object>>(result, headers, HttpStatus.OK);

            // 可以继续添加其他信息，如 token、用户数据等
        } else {
            result.put("status", false);
            result.put("message", "用户名或密码错误");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }

    }
    //用户注册
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> UserSignin(@RequestBody Map<String, String> requestBody){

        
        Map<String, Object> result = new HashMap<>();
        String username = requestBody.get("username");
        String encrypted = requestBody.get("encrypted");
        // 1. 解密 encrypted
        int decryptCode = RsaUtil.verifyDecryptedData(encrypted);
        if (decryptCode!=1) {
            result.put("status", false);
            result.put("message", RsaUtil.getDecryptionErrorMessage(decryptCode));
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        JSONObject json = JSON.parseObject(RsaUtil.decrypt(encrypted));
        String password = json.getString("content");
        
        // 检查用户名和密码是否为空或仅为空白字符
        if (username == null || username.trim().isEmpty()) {
            result.put("status", false);
            result.put("message", "用户名不能为空");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }

        if (password == null || password.trim().isEmpty()) {
            result.put("status", false);
            result.put("message", "密码不能为空");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }
        User user = userMapper.findByUsername(username);
        if(user!=null){
            result.put("status", false);
            result.put("message", "用户已存在");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }
        User insertUser = new User();
        insertUser.setUsername(username);
        insertUser.setPassword(password);
        insertUser.setUserRank("user");
        insertUser.setAvailCapacity(10*1024*1024*1024l);
        insertUser.setUsedCapacity(0l);
        userMapper.insertUser(insertUser);
        result.put("status", true);
        result.put("message", "成功注册");
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);


    }

    //token续租
    @GetMapping("/aftoken")
    public ResponseEntity<Map<String, Object>> applyForToken(@RequestHeader("token") String oldToken){
        Map<String, Object> result = new HashMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", JwtUtil.refreshToken(oldToken));
        result.put("status", true);
        result.put("message", "apply for token successfully");
        return new ResponseEntity<Map<String, Object>>(result, headers, HttpStatus.OK);
    }

    //用户刷新时获取pinia中的信息通过tokne
    @GetMapping("gmsgttoken")
    public ResponseEntity<Map<String, Object>> getMessageFromToken(@RequestHeader("token") String oldToken){
        Map<String, Object> result = new HashMap<>();
        DecodedJWT decodedJWT = JwtUtil.getTokenInfo(oldToken);
    
        String userName = decodedJWT.getClaim("userName").asString();

        User user = userMapper.findByUsername(userName);
        
        //返回用户信息
        if (user != null) {
            result.put("status", true);
            result.put("message", "获取信息成功");
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userid", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("user_rank", user.getUserRank());
            userInfo.put("availcapacity", user.getAvailCapacity());
            userInfo.put("usedcapacity", user.getUsedCapacity());
            if(!user.getUserRank().equals("admin")){
                FileEntity homeFolder = fileMapper.getFolderByNameAndParentId(user.getUsername(), 3l);
                userInfo.put("homeFolderId", homeFolder.getFileId());
            }

            result.put("userinfo", userInfo);
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);

            // 可以继续添加其他信息，如 token、用户数据等
        } else {
            result.put("status", false);
            result.put("message", "token error or other error");
            return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
        }




    }


    @PostMapping("/test")
    public String tokentest(){
        System.out.println("true");
        return "true";
    }
    
}
