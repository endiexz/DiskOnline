package top.endiexz.DiskOnline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@MapperScan("top.endiexz.DiskOnline.mapper")
public class DiskOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiskOnlineApplication.class, args);
	}

}
