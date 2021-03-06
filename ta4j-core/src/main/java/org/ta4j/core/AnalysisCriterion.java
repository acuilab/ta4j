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
package org.ta4j.core;

import org.ta4j.core.num.Num;

import java.util.List;

/**
 * An analysis criterion.
 *
 * Can be used to:
 * <ul>
 * <li>Analyze the performance of a {@link Strategy strategy}
 * <li>Compare several {@link Strategy strategies} together
 * </ul>
 * 
 * 分析标准，用于：
 *  分析策略的性能
 *  一起比较几种策略
 */
public interface AnalysisCriterion {

    /**
     * 计算交易的标准值
     * @param series a bar series, not null
     * @param trade  a trade, not null
     * @return the criterion value for the trade
     */
    Num calculate(BarSeries series, Trade trade);

    /**
     * @param series        a bar series, not null
     * @param tradingRecord a trading record, not null
     * @return the criterion value for the trades
     */
    Num calculate(BarSeries series, TradingRecord tradingRecord);

    /**
     * @param manager    the bar series manager
     * @param strategies a list of strategies
     * @return the best strategy (among the provided ones) according to the criterion
     *	    根据标准得出的最佳策略（在所提供的策略中）
     */
    default Strategy chooseBest(BarSeriesManager manager, List<Strategy> strategies) {

        Strategy bestStrategy = strategies.get(0);
        Num bestCriterionValue = calculate(manager.getBarSeries(), manager.run(bestStrategy));

        for (int i = 1; i < strategies.size(); i++) {
            Strategy currentStrategy = strategies.get(i);
            Num currentCriterionValue = calculate(manager.getBarSeries(), manager.run(currentStrategy));

            if (betterThan(currentCriterionValue, bestCriterionValue)) {
                bestStrategy = currentStrategy;
                bestCriterionValue = currentCriterionValue;
            }
        }

        return bestStrategy;
    }

    /**
     * @param criterionValue1 the first value
     * @param criterionValue2
     * 
     * 
     *                        the second value
     * @return true if the first value is better than (according to the criterion)
     *         the second one, false otherwise
     */
    boolean betterThan(Num criterionValue1, Num criterionValue2);
}