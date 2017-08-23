package com.hk.game;

import com.hk.base.BaseSamplerClient;
import com.hk.net.DataCallBack;
import com.hk.net.DataEvent;
import com.hk.net.DataListener;
import com.hk.net.SocketClient;
import com.hk.web.IUserSrv;
import com.hk.web.Web;
import com.hk.web.entity.HttpRet;
import com.hk.web.entity.LoginBody;
import com.hk.web.entity.UserInfo;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class SamplerClient extends BaseSamplerClient {
    private static final Logger log = LoggerFactory.getLogger(SamplerClient.class);
    private static final String SERVER_IP = "ServerIP";                             // 游戏服务器IP
    private static final String SERVER_PORT = "ServerPort";                         // 游戏服务器端口号
    private static final String WEB_BASE_URL = "WebBaseUrl";                        // web服务器baseUrl
    private static final String ROBOT_PREFIX = "RobotPrefix";                       // 机器人名字前缀
    private static final String ROBOT_PASSWORD = "RobotPassword";                   // 机器人密码

    private static final int DEFAULT_SERVER_PORT = 9999;
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final String DEFAULT_WEB_BASE_URL = "http://127.0.0.1:8080/api/v1/";
    private static final String DEFAULT_ROBOT_PREFIX = "robot";
    private static final String DEFAULT_ROBOT_PASSWORD = "123456";
    private static final AtomicInteger robotIndex = new AtomicInteger(0); // 机器人索引号

    private String serverIP;                                                        // 游戏服务器IP
    private int serverPort;                                                         // 游戏服务器端口
    private String webBaseUrl;                                                      // web服务器baseUrl
    private String robotUserName;                                                   // 机器人名字
    private String robotPassword;                                                   // 机器人默认密码
    private SocketClient client;                                                    // 游戏服务器连接的socket套接字
    private String sessionToken;                                                    // 用户登录第三方web服务器获取token


    /**
     * 初始化Jmeter "Java请求"Sampler界面显示参数
     *
     * @param params
     */
    @Override
    protected void onInitParameters(Arguments params) {
        log.debug(whoAmI() + "\tonInitParameters()");
        params.addArgument(SERVER_IP, DEFAULT_SERVER_IP);
        params.addArgument(SERVER_PORT, String.valueOf(DEFAULT_SERVER_PORT));
        params.addArgument(WEB_BASE_URL, DEFAULT_WEB_BASE_URL);
        params.addArgument(ROBOT_PREFIX, DEFAULT_ROBOT_PREFIX);
        params.addArgument(ROBOT_PASSWORD, DEFAULT_ROBOT_PASSWORD);
    }

    /**
     * 获取用户在Jmeter "Java请求"Sampler界面配置的参数
     *
     * @param context
     */
    @Override
    protected void onSetUp(JavaSamplerContext context) {
        log.info(whoAmI() + "\tonSetUp()");
        serverIP = context.getParameter(SERVER_IP, DEFAULT_SERVER_IP);
        serverPort = context.getIntParameter(SERVER_PORT, DEFAULT_SERVER_PORT);
        webBaseUrl = context.getParameter(WEB_BASE_URL, DEFAULT_WEB_BASE_URL);
        String prefix = context.getParameter(ROBOT_PREFIX, DEFAULT_ROBOT_PREFIX);
        robotUserName = prefix + robotIndex.getAndAdd(1);
        robotPassword = context.getParameter(ROBOT_PASSWORD, DEFAULT_ROBOT_PASSWORD);
        Web.getInstance().init(webBaseUrl, false);
        client = new SocketClient(serverIP, serverPort);

        // 服务器回调通知, 可以在回调方法里面做一些相关的逻辑测试
        // 游戏开始
        client.on(Const.onGameStart, new DataListener() {
            public void receiveData(DataEvent event) {
            }
        });
        // 游戏结束
        client.on(Const.onGameOver, new DataListener() {
            public void receiveData(DataEvent event) {
                stop();
            }
        });
    }

    /**
     * 测试开始
     */
    @Override
    protected void onStart() {
        // 默认的流程
        // 1. 登录web/第三方服务器获取token （如果没有该步骤可以省略）
        // 2. 使用token登录游戏服务器
        // 3. 实现游戏服务器对应的机器人逻辑
        // 4. 结束测试时 记得调用stop方法
        log.debug(whoAmI() + "\tonStart()");
        if (!testWebLogin()
                || !testGameLogin()) {
            stop();
        }
    }

    @Override
    protected void onInterrupt() {
        stop();
    }

    protected void stop() {
        if (client != null) {
            try {
                client.disConnect();
            } catch (IOException e) {
                log.error("", e);
            }
            client = null;
        }
        // Important! 测试结束需要调用finish方法
        finish();
    }

    /**
     * 测试web服务器登录性能
     *
     * @return 没有异常发生则返回true, 否则返回false
     */
    protected boolean testWebLogin() {
        log.debug(whoAmI() + "\ttestWebLogin()");
        SampleResult sub = new SampleResult();
        addRawSubResult(sub);
        sub.setSampleLabel("webLogin");
        sub.setSuccessful(true);

        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long costTime = 0;
        // 登录web服务器获取用户SessionToken
        IUserSrv service = Web.getInstance().getRetrofit().create(IUserSrv.class);
        Call<HttpRet<UserInfo>> call = service.login(new LoginBody(robotUserName, robotPassword));
        try {
            Response<HttpRet<UserInfo>> ret = call.execute();
            // 使用Retrofit记录的时间更加准确
            startTime = ret.raw().sentRequestAtMillis();
            endTime = ret.raw().receivedResponseAtMillis();
            sessionToken = ret.body().getData().getData().get("Token");
            log.info(String.format("[%s] SessionToken => %s", robotUserName, sessionToken));
        } catch (IOException e) {
            log.error("", e);
            sub.setResponseMessage(e.toString());
            sub.setSuccessful(false);
            endTime = System.currentTimeMillis();
        }
        costTime = endTime - startTime;
        if (sub.isStampedAtStart()) {
            sub.setStampAndTime(startTime, costTime);
        } else {
            sub.setStampAndTime(endTime, costTime);
        }
        return sub.isSuccessful();
    }

    /**
     * 测试游戏服务器登录
     *
     * @return
     */
    protected boolean testGameLogin() {
        log.debug(whoAmI() + "\ttestGameLogin()");
        final SampleResult sub = new SampleResult();
        addRawSubResult(sub);
        sub.setSampleLabel("GameLogin");
        sub.setSuccessful(true);
        sub.sampleStart();

        // todo 用户自定义数据为reqData, 可以自定义数据的编码与解码, 推荐使用protobuf
        byte[] reqData = {0};
        try {
            client.connect();
            client.request(Const.reqLogin, reqData, new DataCallBack() {
                public void responseData(byte[] data) {
                    sub.sampleEnd();
                    // todo 数据的解码
                }
            });
        } catch (Exception e) {
            log.error("", e);
            sub.setSuccessful(false);
            sub.setResponseMessage(e.toString());
            sub.sampleEnd();
        }
        return sub.isSuccessful();
    }
}
