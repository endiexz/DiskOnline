package top.endiexz.DiskOnline.StoreStatus;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
@Service
public class StatusService {
    @Scheduled(fixedRate = 1000)
    public void test(){
        //System.out.println("test");
    }

}