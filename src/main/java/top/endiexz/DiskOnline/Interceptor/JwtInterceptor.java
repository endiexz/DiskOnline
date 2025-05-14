package top.endiexz.DiskOnline.Interceptor;


import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.endiexz.DiskOnline.Utils.JwtUtil;
import top.endiexz.DiskOnline.Utils.MyMessageUtil;
import top.endiexz.DiskOnline.Utils.RsaUtil;

public class JwtInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {


    //判断是否是来自节点的请求
    //如果是来自节点的请求需要验证fingerprint是否吻合

    

        String token = request.getHeader("token");

        // 优先判断 token，有 token 就验证
        if (token != null) {
            if (JwtUtil.verifyToken(token) == JwtUtil.TOKEN_VALID) {
                return true; // token 合法，放行
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Invalid token");
                return false;
            }
        }

        // 判断是否是来自节点的请求：即 header 包含 X-Fingerprint
        String encryptedFingerprint = request.getHeader("X-Fingerprint");
        System.out.println("处理X-Fingerprint请求");
        if(encryptedFingerprint!=null){
            //验证fingerPrint是否正确
            int decryptCode  = RsaUtil.verifyDecryptedData(encryptedFingerprint);
            if(decryptCode!=1){
                //验证不通过可能超时或者其他错误
                System.out.println("处理X-Fingerprint请求验证不通过可能超时或者其他错误");
                return false;
            }
            String nodeFingerprint = RsaUtil.getEncryptContent(encryptedFingerprint);
            if(!nodeFingerprint.equals(MyMessageUtil.getMyFingerPrint())){
                //指纹验证不通过
                System.out.println("处理X-Fingerprint请求指纹验证不通过");
                return false;
            }
            return true;
        }
        //没有检查得到token不通过
        return false;
    }
}
