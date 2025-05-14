package top.endiexz.DiskOnline.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import top.endiexz.DiskOnline.Utils.RsaUtil;

@RestController
public class SecretKey {
    @GetMapping("/publickey")
    public ResponseEntity<Map<String, Object>> getPublicKey(){
        Map<String, Object> result = new HashMap<>();
        result.put("publickey", RsaUtil.getPublicKeyBase64());
        result.put("status", true);
        return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
    }
}
