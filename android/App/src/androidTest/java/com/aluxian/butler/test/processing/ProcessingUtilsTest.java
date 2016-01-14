package com.aluxian.butler.test.processing;

import com.aluxian.butler.processing.ProcessingUtils;

import junit.framework.TestCase;

public class ProcessingUtilsTest extends TestCase {

    public void testLogicalEval() throws Exception {
        // Should evaluate to true
        assertTrue(ProcessingUtils.logicalEval("(true & (true | false)) & true"));
        assertTrue(ProcessingUtils.logicalEval("(true | !(true & false)) & (true | false)"));

        // Should evaluate to false
        assertFalse(ProcessingUtils.logicalEval("(true & (true | false)) & false"));
    }

}
