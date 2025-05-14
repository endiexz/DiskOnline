package top.endiexz.DiskOnline.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyMessageUtil {
    private static long myId = 1;
    private static String myNodeName = "centralserver";
    private static final String FINGERPRINT_FILE_PATH = "/Keys/fingerprint-private.key";
    private static String myFingerPrint = getFingerprint();
    private static String myInterface = "localhost:8080";


    // 只读取文件中已有的指纹
    private static String getFingerprint() {
        try (InputStream is = MyMessageUtil.class.getResourceAsStream(FINGERPRINT_FILE_PATH)) {
            if (is == null) {
                throw new RuntimeException("无法读取指纹文件: " + FINGERPRINT_FILE_PATH);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.readLine(); // 只读取第一行作为指纹
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMyNodeName(){
        return myNodeName;
    }
    public static String getMyFingerPrint(){
        return myFingerPrint;
    }
    public static String getMyInterface(){
        return myInterface;
    }

    public static long getMyId(){
        return myId;
    }

}
