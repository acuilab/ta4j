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

import org.ta4j.core.Order;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;

/**
 * 线性交易成本模型
 */
public class LinearTransactionCostModel implements CostModel {

    /**
     * Slope of the linear model - fee per trade
     * 线性模型的斜率-每笔交易的费用
     */
    private final double feePerTrade;

    /**
     * Constructor. (feePerTrade * x)
     * 
     * @param feePerTrade the feePerTrade coefficient (e.g. 0.005 for 0.5% per {@link Order order})
     */
    public LinearTransactionCostModel(double feePerTrade) {
        this.feePerTrade = feePerTrade;
    }

    /**
     * Calculates the transaction cost of a trade.
     * 计算交易的交易成本。
     * 
     * @param trade        the trade
     * @param currentIndex current bar index (irrelevant for the
     *                     LinearTransactionCostModel)
     * @return the absolute order cost
     */
    @Override
    public Num calculate(Trade trade, int currentIndex) {
        return this.calculate(trade);
    }

    /**
     * Calculates the transaction cost of a trade.
     * 计算交易的交易成本。
     * 
     * @param trade the trade
     * @return the absolute order cost
     */
    @Override
    public Num calculate(Trade trade) {
        Num totalTradeCost = null;
        Order entryOrder = trade.getEntry();
        if (entryOrder != null) {
            // transaction costs of entry order
            totalTradeCost = entryOrder.getCost();
            if (trade.getExit() != null) {
                totalTradeCost = totalTradeCost.plus(trade.getExit().getCost());
            }
        }
        return totalTradeCost;
    }

    /**
     * @param price  execution price
     * @param amount order amount
     * @return the absolute order transaction cost
     */
    @Override
    public Num calculate(Num price, Num amount) {
        return amount.numOf(feePerTrade).multipliedBy(price).multipliedBy(amount);
    }

    /**
     * Evaluate if two models are equal
     * 
     * @param otherModel model to compare with
     * @return 
     */
    @Override
    public boolean equals(CostModel otherModel) {
        boolean equality = false;
        if (this.getClass().equals(otherModel.getClass())) {
            equality = ((LinearTransactionCostModel) otherModel).feePerTrade == this.feePerTrade;
        }
        return equality;
    }
}
