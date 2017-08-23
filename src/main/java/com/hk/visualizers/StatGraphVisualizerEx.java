package com.hk.visualizers;

import com.hk.utils.ReflectionUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jmeter.visualizers.StatGraphVisualizer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StatGraphVisualizerEx extends StatGraphVisualizer {
    private static final Logger LOG = LoggerFactory.getLogger(StatGraphVisualizerEx.class);

    private final transient Object lock = new Object();
    private Map<String, SamplingStatCalculator> superTableRows;
    private ObjectTableModel superModel;

    public StatGraphVisualizerEx() {
        superModel = (ObjectTableModel) com.hk.utils.ReflectionUtils.getFieldValue(this, "model");
        superTableRows = (Map<String, SamplingStatCalculator>) ReflectionUtils.getFieldValue(this, "tableRows");
    }

    @Override
    public String getStaticLabel() {
        return "[custom] Aggregate Graph";
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
        SamplingStatCalculator row = superTableRows.get(sampleLabel);
        if (row == null) {
            row = new SamplingStatCalculator(sampleLabel);
            superTableRows.put(row.getLabel(), row);
            superModel.insertRow(row, superModel.getRowCount() - 1);
        }
        row.addSample(result);
    }
}
