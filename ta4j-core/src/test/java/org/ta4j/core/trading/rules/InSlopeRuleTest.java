/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.trading.rules;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.FixedDecimalIndicator;
import org.ta4j.core.num.Num;

import static org.junit.Assert.assertTrue;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.num.NaN;

public class InSlopeRuleTest {

    private InSlopeRule rulePositiveSlope;  // 正斜率规则
    private InSlopeRule ruleNegativeSlope;  // 负斜率规则
    
    private PreviousValueIndicator prev;	
    private InSlopeRule rulePositiveSlope2;  // 负斜率规则

    @Before
    public void setUp() {
        BarSeries series = new BaseBarSeries();
        Indicator<Num> indicator = new FixedDecimalIndicator(series, 50, 70, 80, 90, 99, 60, 30, 20, 10, 0);
        rulePositiveSlope = new InSlopeRule(indicator, series.numOf(0), NaN.NaN);
        ruleNegativeSlope = new InSlopeRule(indicator, NaN.NaN, series.numOf(-0));
	
	prev = new PreviousValueIndicator(indicator);
	rulePositiveSlope2 = new InSlopeRule(prev, series.numOf(0), NaN.NaN);
    }

    @Test
    public void isSatisfied() {
//	50, 70, 80, 90, 99, 60, 30, 20, 10, 0	->ref
//	50, 50, 70, 80, 90, 99, 60, 30, 20, 10	->prev
//	0,  20, 10, 10, 9, -39,-30,-10,-10,-10	->diff
        assertTrue(rulePositiveSlope.isSatisfied(0));
        assertTrue(rulePositiveSlope.isSatisfied(1));
        assertTrue(rulePositiveSlope.isSatisfied(2));
        assertTrue(rulePositiveSlope.isSatisfied(3));
	assertTrue(rulePositiveSlope.isSatisfied(4));
	
        assertTrue(ruleNegativeSlope.isSatisfied(5));
        assertTrue(ruleNegativeSlope.isSatisfied(6));
        assertTrue(ruleNegativeSlope.isSatisfied(7));
        assertTrue(ruleNegativeSlope.isSatisfied(8));
	assertTrue(ruleNegativeSlope.isSatisfied(9));
	
	assertTrue(ruleNegativeSlope.isSatisfied(5));
	assertTrue(rulePositiveSlope2.isSatisfied(5));
	assertTrue(ruleNegativeSlope.isSatisfied(5) && rulePositiveSlope2.isSatisfied(5));
    }
}
