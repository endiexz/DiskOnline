package top.endiexz.DiskOnline.Configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import top.endiexz.DiskOnline.Utils.RsaUtil;
import top.endiexz.DiskOnline.mapper.StorageNodeMapper;

@Configuration
public class WebClientConfig {

    @Autowired
    private StorageNodeMapper storageNodeMapper;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(addFingerprintHeader());
    }

    private ExchangeFilterFunction addFingerprintHeader(){
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->{
            try{
                //对添加头进行筛选
                String path = clientRequest.url().getPath();  // 注意是getPath()，不是getHost()

                // 判断路径是否需要加Fingerprint
                if (!shouldAddFingerprint(path)) {
                    return Mono.just(clientRequest);
                }
                String host = clientRequest.url().getHost();
                int port = clientRequest.url().getPort();
                String nodeInterface = host + ":" + port;

                String nodePublicKey = storageNodeMapper.getPublicKeyByInterface(nodeInterface);
                if(nodePublicKey == null){
                    String url = "http://" + nodeInterface + "/publickey";
                    Map<String, Object> response = WebClient.builder().baseUrl(url)
                            .build()
                            .get()
                            .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON)) // 设置 Content-Type 为 JSON
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block(); // 阻塞等待返回结果
                    if(response!=null){
                        nodePublicKey = (String) response.get("publickey");
                        int nodeId = storageNodeMapper.getNodeIdByInterface(nodeInterface);
                        storageNodeMapper.insertNodePublicKey(nodeId, nodeInterface, nodePublicKey);
                    }
                }

                String encryptedFingerprint = RsaUtil.encryptWithTimestamp(nodePublicKey, storageNodeMapper.getFingerprintByInterface(nodeInterface));


                //添加header
                ClientRequest newRequest = ClientRequest.from(clientRequest)
                        .header("X-Fingerprint", encryptedFingerprint)
                        .build();

                return Mono.just(newRequest);

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate fingerprint", e);
            }
        });
    }

    private boolean shouldAddFingerprint(String path) {
        // 精确匹配不加
        if (path.equals("/verifynode") || path.equals("/joincentral") || path.equals("/publickey")) {
            return false;
        }
    
        // 其他路径都加
        return true;
    }
}
