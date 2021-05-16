package cn.com.kunghsu.desktop;

import cn.com.kunghsu.desktop.cli.CLI;
import cn.com.kunghsu.desktop.system.LocalComputer;
import cn.com.kunghsu.desktop.util.Configs;
import cn.com.kunghsu.desktop.util.logger.StdOutDelegate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * 应用入口
 * Created by xuyaokun On 2021/5/16 15:32
 * @desc:
 */
@SpringBootApplication
public class ServerApp {

    public static void main(String[] args) throws Exception {

        System.setProperty("java.awt.headless", "false");
        StdOutDelegate stdWriter = StdOutDelegate.newInstance(System.out);
        System.setOut(stdWriter);
        System.setErr(stdWriter);

        ApplicationContext context = SpringApplication.run(ServerApp.class, args);
        Configs.init("/application.properties");
        LocalComputer.init();
        CLI.init();
    }

//    @Bean
//    public DataSource dataSource() {
//        SQLiteDataSource dataSource = new SQLiteDataSource();
//        dataSource.setUrl(env.getProperty("spring.datasource.url"));
//        return dataSource;
//    }

}
