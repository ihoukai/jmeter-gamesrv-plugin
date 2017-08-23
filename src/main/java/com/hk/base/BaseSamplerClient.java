package com.hk.base;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public abstract class BaseSamplerClient extends AbstractJavaSamplerClient implements Serializable, Interruptible {
    private static final Logger log = LoggerFactory.getLogger(BaseSamplerClient.class);

    private Semaphore available;
    private String sampleLabel;
    private SampleResult results;

    public BaseSamplerClient() {
        log.debug(whoAmI() + "\tConstruct()");
        available = new Semaphore(0, true);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        onInitParameters(params);
        return params;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        if (log.isDebugEnabled()) {
            log.debug(whoAmI() + "\tsetupTest()");
            listParameters(context);
        }
        sampleLabel = context.getParameter(TestElement.NAME);
        onSetUp(context);
    }

    /**
     * 初始化jmeter界面参数
     *
     * @param params
     */
    abstract protected void onInitParameters(Arguments params);

    /**
     * jmeter线程启动时调用该方法进行初始化配置
     */
    abstract protected void onSetUp(JavaSamplerContext context);

    /**
     * 子类实现该方法,在方法内部测试子业务
     *
     */
    abstract protected void onStart();

    /**
     * 中断通知
     */
    abstract protected void onInterrupt();

    /**
     * 子类完成测试调用finish方法结束
     */
    protected void finish() {
        log.debug(whoAmI() + "\tfinish()");
        available.release();
    }

    /**
     * jmeter框架调用,开始运行测试用例
     *
     * @param context
     * @return
     */
    public SampleResult runTest(JavaSamplerContext context) {
        log.debug(whoAmI() + "\trunTest()");
        results = new SampleResult();
        results.setSampleLabel(sampleLabel);
        results.sampleStart();

        try {
            onStart();
            // 拦截runTest 直到子类调用finish 方法
            available.acquire();
            results.setSuccessful(true);
        } catch (Exception e) {
            results.setSuccessful(false);
            results.setResponseMessage(e.toString());
        } finally {
            results.sampleEnd();
        }
        if (log.isDebugEnabled()) {
            log.debug(whoAmI() + "\trunTest()" + "\tTime:\t" + results.getTime());
            listParameters(context);
        }
        return results;
    }

    /**
     * 添加子测试结果
     *
     * @param sampleResult
     */
    protected void addRawSubResult(SampleResult sampleResult) {
        results.addRawSubResult(sampleResult);
    }

    /**
     * 列出context所有参数
     *
     * @param context
     */
    private void listParameters(JavaSamplerContext context) {
        Iterator<String> argsIt = context.getParameterNamesIterator();
        while (argsIt.hasNext()) {
            String lName = argsIt.next();
            log.debug(lName + "=" + context.getParameter(lName));
        }
    }

    /**
     * 为测试的线程生成字符串标识.
     *
     * @return 字符串标识
     */
    protected String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().toString());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    /**
     * 中断
     *
     * @return
     */
    public boolean interrupt() {
        onInterrupt();
        available.release();
        return false;
    }
}
