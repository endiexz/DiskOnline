package top.endiexz.DiskOnline.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class RsaUtil {

    private final static String publicKeyBase64 = loadSecretKey("Keys/rsa-public.key");
    private final static String privateKeyBase64 = loadSecretKey("Keys/rsa-private.key");
    // 直接读取并解码 base64 编码的密钥字符串
    private final static PublicKey publicKey = loadPublicKey("Keys/rsa-public.key");
    private final static PrivateKey privateKey = loadPrivateKey("Keys/rsa-private.key");


    // 错误码常量
    public static final int SUCCESS = 1;  // 成功状态码
    public static final int DECRYPTION_FAILURE = -1;  // 解密失败
    public static final int TIMESTAMP_INVALID = -2;  // 时间戳无效
    public static final int DECRYPTION_PARSE_ERROR = -3;  // 解密数据解析错误

    // 加载并解码公钥
    private static PublicKey loadPublicKey(String filePath) {
        try {
            String base64PublicKey = loadSecretKey(filePath);
            byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey); // 解码 base64
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            return keyFactory.generatePublic(keySpec); // 返回公钥
        } catch (Exception e) {
            throw new RuntimeException("读取和解析公钥失败: " + filePath, e);
        }
    }

    // 加载并解码私钥
    private static PrivateKey loadPrivateKey(String filePath) {
        try {
            String base64PrivateKey = loadSecretKey(filePath);
            byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey); // 解码 base64
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            return keyFactory.generatePrivate(keySpec); // 返回私钥
        } catch (Exception e) {
            throw new RuntimeException("读取和解析私钥失败: " + filePath, e);
        }
    }
    private static String loadSecretKey(String finePath) {
        try {
            // 读取文件并转成字符串（去除换行）

            InputStream in = JwtUtil.class.getClassLoader().getResourceAsStream(finePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\R", "");
        } catch (IOException e) {
            throw new RuntimeException("读取密钥文件失败: "+finePath, e);
        }
    }

    // 对外提供加密方法
    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey); // 使用公钥加密
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes); // 返回加密后的 base64 字符串
        } catch (Exception e) {
            throw new RuntimeException("RSA 加密失败", e);
        }
    }

    // 对外提供解密方法
    public static String decrypt(String cipherTextBase64) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey); // 使用私钥解密
            byte[] decodedCipherText = Base64.getDecoder().decode(cipherTextBase64); // 解码 base64 密文
            byte[] decryptedBytes = cipher.doFinal(decodedCipherText);
            return new String(decryptedBytes, StandardCharsets.UTF_8); // 返回解密后的明文
        } catch (Exception e) {
            throw new RuntimeException("RSA 解密失败", e);
        }
    }


    // 解密并验证时间戳，返回状态码
    public static int verifyDecryptedData(String encrypted) {
        String decrypted = decrypt(encrypted);
        if (decrypted == null) {
            return DECRYPTION_FAILURE;  // 解密失败
        }

        try {
            // 解析 JSON 格式的 password + timestamp
            JSONObject json = JSON.parseObject(decrypted);
            long timestamp = json.getLong("timestamp");

            // 验证时间戳（3 秒内有效）
            long now = System.currentTimeMillis();
            if (Math.abs(now - timestamp) > 3000) {
                return TIMESTAMP_INVALID;  // 时间戳无效
            }

            return SUCCESS;  // 解密成功
        } catch (Exception e) {
            return DECRYPTION_PARSE_ERROR;  // 解析失败
        }
    }
    
    // 获取解密状态码的错误消息
    public static String getDecryptionErrorMessage(int code) {
        return switch (code) {
            case SUCCESS -> "验证成功";
            case DECRYPTION_FAILURE -> "加密数据解析失败";
            case TIMESTAMP_INVALID -> "请求超时，请重试";
            case DECRYPTION_PARSE_ERROR -> "解析解密数据失败";
            default -> "未知错误";
        };
    }

    //获取加密内容
    public static String getEncryptContent(String encrypted){
        JSONObject json = JSON.parseObject(RsaUtil.decrypt(encrypted));
        return json.getString("content");
    }


    // 使用传递的 publicKey 和内容添加 timestamp 并加密
    public static String encryptWithTimestamp(String publicKeyBase64, String content) {
        try {
            // 解码并加载公钥
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyBase64);  // 解码 base64 公钥
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            PublicKey publicKey = keyFactory.generatePublic(keySpec); // 创建公钥对象

            // 添加时间戳
            long timestamp = System.currentTimeMillis();
            JSONObject json = new JSONObject();
            json.put("content", content);
            json.put("timestamp", timestamp);

            // 转换为 JSON 字符串
            String contentWithTimestamp = json.toJSONString();

            // 使用公钥加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(contentWithTimestamp.getBytes(StandardCharsets.UTF_8));

            // 返回加密后的 base64 字符串
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA 加密失败", e);
        }
    }


    // 获取解码后的公钥/私钥对象供其他用途
    public static String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public static String getPrivateKeyBase64() {
        return privateKeyBase64;
    }



}
