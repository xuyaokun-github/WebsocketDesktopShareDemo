package cn.com.kunghsu.desktop.wss;

import cn.com.kunghsu.desktop.cli.CLI;
import cn.com.kunghsu.desktop.config.GetHttpSessionConfigurator;
import cn.com.kunghsu.desktop.util.Configs;
import cn.com.kunghsu.desktop.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * websocket相关
 * 作用：
 * 1.建立会话
 * 2.校验密码
 *
 * Created by xuyaokun On 2021/5/16 15:35
 * @desc:
 */
@Component
@Scope("prototype")
@ServerEndpoint(value = "/desktop/wss", configurator = GetHttpSessionConfigurator.class)
public class DesktopWSS {

    private static AtomicInteger sequence = new AtomicInteger(1);

    private int id;

    /**
     * websocket会话
     */
    private Session session;

    private HttpSession httpSession = null;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.id = sequence.getAndAdd(1);
        this.session = session;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    }

    public int getId() {
        return this.id;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        String type = json.get("type").getAsString();
        if ("command".equals(type)) {
            String cmd = json.get("command").getAsString();
            if ("login".equals(cmd)) {
                //身份校验
                //假如设置了跳过密码校验，则不做校验，直接返回isLogin为true
                boolean isNeedSkipPassword = "true".equals(Configs.get("desktop.websocket.enter.skip-password"));
                if (!isNeedSkipPassword && !CLI.getPassword().equals(json.get("password").getAsString())) {
                    //后续可以扩展为更具体的身份角色校验
                    this.sendResponse("login", "密码错误");
                    return;
                }
                this.httpSession.setAttribute("isLogin", true);
                this.sendResponse("login", "success");
                requestDesktopSharing();
            }
        }
    }

    private void requestDesktopSharing() {
        try {
            //注册websocket会话
            int sessionId = WSSessionManager.getInstance().register(this);
            this.sendResponse("request-desktop", "success", String.valueOf(sessionId));
            // TODO: 需要发送最近的完整画面，以及最近一次的压缩祯
        }
        catch(Exception ex) {
            this.sendResponse("request-desktop", ex.getMessage());
        }
    }

    // 下发控制响应
    public void sendControlResponse(int compressMethod, int bandWidth, int colorBits, int screenWidth, int screenHeight) {
        // sendText("{ \"action\" : \"setup\", \"compressMethod\" : " + compressMethod + ", \"bandWidth\" : " + bandWidth + ", \"colorBits\" : " + colorBits + ", \"screenWidth\" : " + screenWidth + ", \"screenHeight\" : " + screenHeight + " }");
    }

    // 下发屏幕截图
    public boolean sendScreenshot(byte[] screenshot) {
        try {
            if (!this.session.isOpen()) {
                return false;
            }
            sendBinary(screenshot);
        }
        catch(Exception e) {
            Log.error(e);
        }
        return true;
    }

    private void sendResponse(String action, String result) {
        sendResponse(action, result, null);
    }

    private void sendResponse(String action, String result, String extra) {
        JsonObject resp = new JsonObject();
        resp.addProperty("action", action);
        resp.addProperty("result", result);
        if (extra != null) resp.addProperty("extra", extra);
        sendText(resp.toString());
    }

    private void sendMessage(String action, String data) {
        JsonObject resp = new JsonObject();
        resp.addProperty("action", action);
        resp.addProperty("result", data);
        sendText(resp.toString());
    }

    public void sendText(String text) {
        try {
            this.session.getBasicRemote().sendText(text);
        }
        catch(IOException e) {
            try { this.session.close(); } catch(Exception ex) { }
        }
    }

    private void sendBinary(byte[] data) {
        try {
            //向客户端返回二进制
            this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        } catch(IOException e) {
            try { this.session.close(); } catch(Exception ex) { }
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("websocket session closed...");
        try {
            this.httpSession.removeAttribute("isLogin");
        } catch(Exception ex) {
            System.out.println("onClose出现异常");
//            ex.printStackTrace();
        }
        WSSessionManager.getInstance().unregister(this);
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        //会话出现异常
        ex.printStackTrace();
    }

    public void shutdown() {
        this.sendMessage("status", "kicked");
    }

    public String getRemoteAddr() {
        return (String)this.httpSession.getAttribute("remote-addr");
    }

}
