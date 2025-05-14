package top.endiexz.DiskOnline.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;


public class JwtUtil {

    //设置令牌时间
    private final static int timeOut = 30*60;
    // 从文件中读取私钥内容作为 secret
    private final static String secret = loadSecretKey();

    private static String loadSecretKey() {
        try {
            // 读取文件并转成字符串（去除换行）

            InputStream in = JwtUtil.class.getClassLoader().getResourceAsStream("Keys/jwt-private.key");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\R", "");
        } catch (IOException e) {
            throw new RuntimeException("读取密钥文件失败: Keys/jwt-private.key", e);
        }
    }

    //token状态码
    public static final int TOKEN_VALID = 1;
    public static final int TOKEN_EXPIRED = -1;
    public static final int SIGNATURE_INVALID = -2;
    public static final int ALGORITHM_MISMATCH = -3;
    public static final int TOKEN_DECODE_ERROR = -4;
    public static final int CLAIM_INVALID = -5;
    public static final int UNKNOWN_ERROR = -99;

    //设置token
    public static String getToken(Long userId, String userName){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, timeOut);
        String token = JWT.create()
                        .withClaim("userId", userId)
                        .withClaim("userName", userName)
                        .withExpiresAt(instance.getTime())
                        .sign(Algorithm.HMAC256(secret)); 
        return token;
    }

    //验证token
    public static int verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
            return TOKEN_VALID;
        } catch (TokenExpiredException e) {
            return TOKEN_EXPIRED;
        } catch (SignatureVerificationException e) {
            return SIGNATURE_INVALID;
        } catch (AlgorithmMismatchException e) {
            return ALGORITHM_MISMATCH;
        } catch (JWTDecodeException e) {
            return TOKEN_DECODE_ERROR;
        } catch (InvalidClaimException e) {
            return CLAIM_INVALID;
        } catch (JWTVerificationException e) {
            return UNKNOWN_ERROR; // 其他未知 JWT 异常
        }
    }

    //通过错误码获取状态
    public static String getErrorMessage(int code) {
        return switch (code) {
            case TOKEN_VALID -> "验证成功";
            case TOKEN_EXPIRED -> "Token 已过期";
            case SIGNATURE_INVALID -> "签名无效";
            case ALGORITHM_MISMATCH -> "加密算法不一致";
            case TOKEN_DECODE_ERROR -> "Token 解码失败";
            case CLAIM_INVALID -> "Token 声明无效";
            case UNKNOWN_ERROR -> "未知错误";
            default -> "未知返回码";
        };
    }

    //续租token
    public static String refreshToken(String oldToken) {
        // 只解码，不验证（假设已验证）
        DecodedJWT decodedJWT = getTokenInfo(oldToken);
    
        Long userId = decodedJWT.getClaim("userId").asLong();
        String userName = decodedJWT.getClaim("userName").asString();
    
        // 重新生成新的 token（续租）
        return getToken(userId, userName);
    }
    

    public static DecodedJWT getTokenInfo(String token){
        DecodedJWT TokenInfo = JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        return TokenInfo;
    }


    public static String getSecret(){
        return secret;
    }

}
