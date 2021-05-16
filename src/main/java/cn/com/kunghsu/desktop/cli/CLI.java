package cn.com.kunghsu.desktop.cli;

import cn.com.kunghsu.desktop.system.LocalComputer;
import cn.com.kunghsu.desktop.system.worker.CaptureWorker;
import cn.com.kunghsu.desktop.system.worker.CompressWorker;
import cn.com.kunghsu.desktop.util.Configs;
import cn.com.kunghsu.desktop.wss.WSSession;
import cn.com.kunghsu.desktop.wss.WSSessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CLI extends Thread {

    static String password;

    CaptureWorker captureWorker = null;
    CompressWorker compressWorker = null;

    String state = "idle";

    private void interact() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("*********************************************************");
        System.out.println("*                                                       *");
        System.out.println("*                   Kunghsu CLI                         *");
        System.out.println("*                                                       *");
        System.out.println("*********************************************************");

        //获取本机所有网卡的IP
        ArrayList<String> ipAddresses = LocalComputer.getLocalIP();
        System.out.println(String.format("server started at: %d, share the desktop with following urls: ", Configs.getInt("server.port", 5656)));
        for (String addr : ipAddresses) {
            echo(String.format("  http://%s:%d/", addr, Configs.getInt("server.port", 5656)));
        }
        //随机生成一个八位数的密码，用于登录验证
        password = String.valueOf(10000000 + (int)(Math.random() * 89999999));
        echo(String.format("access password: %s", password));

        loop : while (true) {
            System.out.print("> ");
            //用户输入的命令
            String text = reader.readLine().trim();
            boolean valid = true;
            if ("start".equals(text)) {
                // 开始共享
                if (!"idle".equals(state)) {
                    echo(String.format("server state: %s, command not executed", state));
                    continue loop;
                }
                state = "start";
                captureWorker = new CaptureWorker();
                compressWorker = new CompressWorker();

                captureWorker.start();
                compressWorker.start();
                echo("start sharing the desktop...");
            } else if ("stop".equals(text)) {
                // TODO: 停止共享
                if (!"start".equals(state))
                {
                    echo(String.format("server state: %s, command not executed", state));
                    continue loop;
                }
                state = "idle";
                captureWorker.terminate();
                compressWorker.terminate();
                echo("stop the desktop sharing...");
            } else if ("list".equals(text)) {
                // TODO: 列出全部听众
                echo("all listening members:");
                WSSession[] sessions = WSSessionManager.getInstance().list();
                for (WSSession session : sessions)
                {
                    echo(String.format("\tID: %11d, IP: %s", session.getId(), session.getRemoteAddr()));
                }
            } else if (text.matches("^password\\s+(\\w{8})$")) {
                // TODO: 设定访问密码
                password = text.replaceAll("^password\\s+(\\w{8})$", "$1");
                echo("new password applied");
            } else if (text.matches("^kick \\d+$")) {
                // TODO: 踢出指定的围观群众
                String id = text.replaceAll("^kick (\\d+)$", "$1");
                echo("kick the member: " + id);
                WSSessionManager.getInstance().kick(Integer.parseInt(id));
            } else if (text.matches("^\\d+$")) {
                // TODO: 由当前的模式决定是做什么

                // TODO: 由处理结果决定valid的结果
            } else if ("exit".equals(text)) {
                echo("exiting...");
                // TODO: 关闭所有应该要关闭的东西
                System.exit(0);
            } else if ("".equals(text)) {
                // do nothing here...
            } else {
                valid = false;
            }
            if (!valid) {
                echo("Commands: ");
                echo("\tstart - start to share the desktop");
                echo("\tstop - stop the desktop sharing");
                echo("\tlist - list all members");
                echo("\tkick <id> - kick the member with <id>");
                echo("\tpassword <password> - set password (8 characters)");
                echo("\texit - exit this program");
            }
        }
    }

    private void echo(String text)
    {
        System.out.println("  " + text);
    }

    public void run() {
        try {
            interact();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getPassword()
    {
        return password;
    }

    static CLI instance = null;

    /**
     * 启动命令行交互界面
     */
    public static void init() {
        instance = new CLI();
        //这里就是启动了一个线程，一直监听用户的命令行输入
        instance.start();
    }

    public static synchronized CLI getInstance() {
        return instance;
    }

    public static synchronized void resetScreen() {
        if (instance != null && instance.compressWorker != null) instance.compressWorker.resetScreen();
    }

    public static void main(String[] args) throws Exception {
        new CLI().start();
    }
}
