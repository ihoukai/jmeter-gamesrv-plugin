package com.hk.visualizers;

import com.hk.utils.ReflectionUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SummaryReportEx extends org.apache.jmeter.visualizers.SummaryReport {
    private static final Logger LOG = LoggerFactory.getLogger(SummaryReportEx.class);

    private final transient Object lock = new Object();
    private Map<String, Calculator> superTableRows;
    private ObjectTableModel superModel;

    public SummaryReportEx() {
        superModel = (ObjectTableModel) com.hk.utils.ReflectionUtils.getFieldValue(this, "model");
        superTableRows = (Map<String, Calculator>) ReflectionUtils.getFieldValue(this, "tableRows");
    }

    @Override
    public String getStaticLabel() {
        return "[custom] Summary Report";
    }

    @Override
    public void add(final SampleResult res) {
        JMeterUtils.runSafe(false, new Runnable() {
            public void run() {
                SampleResult[] subResults = res.getSubResults();
                synchronized (lock) {
                    addSampleResult(res);
                    for (int i=0; i<subResults.length; i++) {
                        addSampleResult(subResults[i]);
                    }
                }
            }
        });
    }

    public void addSampleResult(SampleResult result) {
        String sampleLabel = result.getSampleLabel();
        Calculator row = superTableRows.get(sampleLabel);
        if (row == null) {
            row = new Calculator(sampleLabel);
            superTableRows.put(row.getLabel(), row);
            superModel.insertRow(row, superModel.getRowCount() - 1);
        }
        row.addSample(result);
    }
}
