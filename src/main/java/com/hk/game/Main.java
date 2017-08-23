package com.hk.game;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) {
        log.info("main\tbegin");
        for (int i = 0; i < 4; i++) {
            TaskTread t = new TaskTread();
            t.start();
        }
        log.info("main\nend");
    }

    static class TaskTread extends Thread {
        @Override
        public void run() {
            SamplerClient poker = new SamplerClient();
            JavaSamplerContext context = new JavaSamplerContext(new Arguments());
            poker.setupTest(context);
            poker.runTest(context);
        }
    }
}
