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
 * 线性借贷成本模型
 */
public class LinearBorrowingCostModel implements CostModel {

    /**
     * Slope of the linear model - fee per period
     * 线性模型的斜率-每期费用
     */
    private final double feePerPeriod;

    /**
     * Constructor. (feePerPeriod * nPeriod)
     * 
     * @param feePerPeriod the coefficient (e.g. 0.0001 for 1bp per period)
     */
    public LinearBorrowingCostModel(double feePerPeriod) {
        this.feePerPeriod = feePerPeriod;
    }

    @Override
    public Num calculate(Num price, Num amount) {
        // borrowing costs depend on borrowed period
	// 借款费用取决于借款期限
        return price.numOf(0);
    }

    /**
     * Calculates the borrowing cost of a closed trade.
     * 计算已关闭交易的借贷成本。
     * 
     * @param trade the trade
     * @return the absolute order cost
     */
    @Override
    public Num calculate(Trade trade) {
        if (trade.isOpened()) {
            throw new IllegalArgumentException("Trade is not closed. Final index of observation needs to be provided.");
        }
        return calculate(trade, trade.getExit().getIndex());
    }

    /**
     * Calculates the borrowing cost of a trade.
     * 计算交易的借贷成本。
     * 
     * @param trade        the trade
     * @param currentIndex final bar index to be considered (for open trades)	最后的柱索引（对打开的交易）
     * @return the absolute order cost
     */
    @Override
    public Num calculate(Trade trade, int currentIndex) {
        Order entryOrder = trade.getEntry();	// 进入订单
        Order exitOrder = trade.getExit();	// 退出订单
        Num borrowingCost = trade.getEntry().getNetPrice().numOf(0);

        // borrowing costs apply for short positions only
        if (entryOrder != null && entryOrder.getType().equals(Order.OrderType.SELL) && entryOrder.getAmount() != null) {
            int tradingPeriods = 0;
            if (trade.isClosed()) {
		// 关闭的交易
                tradingPeriods = exitOrder.getIndex() - entryOrder.getIndex();
            } else if (trade.isOpened()) {
		// 打开的交易
                tradingPeriods = currentIndex - entryOrder.getIndex();
            }
            borrowingCost = getHoldingCostForPeriods(tradingPeriods, trade.getEntry().getValue());
        }
        return borrowingCost;
    }

    /**
     * @param tradingPeriods number of periods
     * @param tradedValue    value of the trade initial trade position
     * @return the absolute borrowing cost
     */
    private Num getHoldingCostForPeriods(int tradingPeriods, Num tradedValue) {
        return tradedValue
                .multipliedBy(tradedValue.numOf(tradingPeriods).multipliedBy(tradedValue.numOf(feePerPeriod)));
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
            equality = ((LinearBorrowingCostModel) otherModel).feePerPeriod == this.feePerPeriod;
        }
        return equality;
    }
}
