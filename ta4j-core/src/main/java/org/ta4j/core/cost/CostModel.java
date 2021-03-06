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
package org.ta4j.core.cost;

import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;

import java.io.Serializable;

/**
 * 成本模型
 */
public interface CostModel extends Serializable {

    /**
     * 计算单笔交易的交易成本
     * @param trade      the trade
     * @param finalIndex final index of consideration for open trades
     * @return Calculates the trading cost of a single trade
     */
    Num calculate(Trade trade, int finalIndex);

    /**
     * 计算单笔交易的交易成本
     * @param trade the trade
     * @return Calculates the trading cost of a single trade
     */
    Num calculate(Trade trade);

    /**
     * 计算某一交易数量的交易成本
     * @param price  the price per asset
     * @param amount number of traded assets
     * @return Calculates the trading cost for a certain traded amount
     */
    Num calculate(Num price, Num amount);

    boolean equals(CostModel model);
}