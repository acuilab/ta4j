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

import org.ta4j.core.Order.OrderType;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

import java.io.Serializable;
import java.util.Objects;

import static org.ta4j.core.num.NaN.NaN;

/**
 * Pair of two {@link Order orders}.
 *
 * The exit order has the complement type of the entry order.<br>
 * I.e.: entry == BUY --> exit == SELL entry == SELL --> exit == BUY
 * 
 * 交易：一对（买和卖）订单。出口订单是入口订单的补充类型
 * 例如：入口==买 --> 出口==卖	入口==卖 --> 出口==买
 */
public class Trade implements Serializable {

    private static final long serialVersionUID = -5484709075767220358L;

    /** The entry order */
    // 进入订单
    private Order entry;

    /** The exit order */
    // 退出订单
    private Order exit;

    /** The type of the entry order */
    // 进入订单类型
    private OrderType startingType;

    /** The cost model for transactions of the asset */
    // 资产交易的成本模型
    private CostModel transactionCostModel;

    /** The cost model for holding the asset */
    // 持有资产的成本模型
    private CostModel holdingCostModel;

    /**
     * Constructor.
     */
    public Trade() {
        this(OrderType.BUY);
    }

    /**
     * Constructor.
     * 
     * @param startingType the starting {@link OrderType order type} of the trade
     *                     (i.e. type of the entry order)
     */
    public Trade(OrderType startingType) {
        this(startingType, new ZeroCostModel(), new ZeroCostModel());
    }

    /**
     * Constructor.
     * 
     * @param startingType         the starting {@link OrderType order type} of the trade (i.e. type of the entry order)
     * @param transactionCostModel the cost model for transactions of the asset
     * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
     */
    public Trade(OrderType startingType, CostModel transactionCostModel, CostModel holdingCostModel) {
        if (startingType == null) {
            throw new IllegalArgumentException("Starting type must not be null");
        }
        this.startingType = startingType;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
    }

    /**
     * Constructor.
     * 
     * @param entry the entry {@link Order order}
     * @param exit  the exit {@link Order order}
     */
    public Trade(Order entry, Order exit) {
        this(entry, exit, entry.getCostModel(), new ZeroCostModel());
    }

    /**
     * Constructor.
     * 
     * @param entry                the entry {@link Order order}
     * @param exit                 the exit {@link Order order}
     * @param transactionCostModel the cost model for transactions of the asset
     * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
     */
    public Trade(Order entry, Order exit, CostModel transactionCostModel, CostModel holdingCostModel) {

        if (entry.getType().equals(exit.getType())) {
            throw new IllegalArgumentException("Both orders must have different types");
        }

        if (!(entry.getCostModel().equals(transactionCostModel))
                || !(exit.getCostModel().equals(transactionCostModel))) {
            throw new IllegalArgumentException("Orders and the trade must incorporate the same trading cost model");
        }

        this.startingType = entry.getType();
        this.entry = entry;
        this.exit = exit;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
    }

    /**
     * @return the entry {@link Order order} of the trade
     */
    public Order getEntry() {
        return entry;
    }

    /**
     * @return the exit {@link Order order} of the trade
     */
    public Order getExit() {
        return exit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Trade) {
            Trade t = (Trade) obj;
            return (entry == null ? t.getEntry() == null : entry.equals(t.getEntry()))
                    && (exit == null ? t.getExit() == null : exit.equals(t.getExit()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry, exit);
    }

    /**
     * Operates the trade at the index-th position
     * 
     * @param index the bar index
     * @return the order
     */
    public Order operate(int index) {
        return operate(index, NaN, NaN);
    }

    /**
     * Operates the trade at the index-th position
     * 
     * @param index  the bar index
     * @param price  the price
     * @param amount the amount
     * @return the order
     */
    public Order operate(int index, Num price, Num amount) {
        Order order = null;
        if (isNew()) {
            order = new Order(index, startingType, price, amount, transactionCostModel);
            entry = order;
        } else if (isOpened()) {
            if (index < entry.getIndex()) {
                throw new IllegalStateException("The index i is less than the entryOrder index");
            }
            order = new Order(index, startingType.complementType(), price, amount, transactionCostModel);
            exit = order;
        }
        return order;
    }

    /**
     * 交易是否被关闭（进入订单和退出订单都不为空）
     * @return true if the trade is closed, false otherwise
     * 如果交易关闭，则为true，否则为false
     */
    public boolean isClosed() {
        return (entry != null) && (exit != null);
    }

    /**
     * 交易是否被打开（进入订单不为空，退出订单为空）
     * @return true if the trade is opened, false otherwise
     * 如果交易被打开，则为true，否则为false
     */
    public boolean isOpened() {
        return (entry != null) && (exit == null);
    }

    /**
     * 交易是否是新的（进入订单为空且退出订单也为空）
     * @return true if the trade is new, false otherwise
     * 如果交易是新的，则为true，否则为false
     */
    public boolean isNew() {
        return (entry == null) && (exit == null);
    }

    @Override
    public String toString() {
        return "Entry: " + entry + " exit: " + exit;
    }

    /**
     * Calculate the profit of the trade if it is closed
     * 
     * @return the profit or loss of the trade
     */
    public Num getProfit() {
        Num profit;
        if (isOpened()) {
            profit = entry.getNetPrice().numOf(0);
        } else {
            profit = calculateGrossProfit(exit.getPricePerAsset()).minus(getTradeCost());
        }
        return profit;
    }

    /**
     * Calculate the profit of the trade. If it is open, calculates the profit until
     * the final bar.
     * 
     * @param finalIndex the index of the final bar to be considered (if trade is
     *                   open)
     * @param finalPrice the price of the final bar to be considered (if trade is
     *                   open)
     * @return the profit or loss of the trade
     */
    public Num getProfit(int finalIndex, Num finalPrice) {
        Num grossProfit = calculateGrossProfit(finalPrice);
        // add trading costs
        return grossProfit.minus(getTradeCost(finalIndex));
    }

    /**
     * Calculate the gross (w/o trading costs) profit of the trade.
     * 
     * @param finalPrice the price of the final bar to be considered (if trade is
     *                   open)
     * @return the profit or loss of the trade
     */
    private Num calculateGrossProfit(Num finalPrice) {
        Num grossProfit;
        if (isOpened()) {
            grossProfit = entry.getAmount().multipliedBy(finalPrice).minus(entry.getValue());
        } else {
            grossProfit = exit.getValue().minus(entry.getValue());
        }

        // Profits of long position are losses of short
        if (entry.getType().equals(OrderType.SELL)) {
            grossProfit = grossProfit.multipliedBy(entry.getNetPrice().numOf(-1));
        }
        return grossProfit;
    }

    /**
     * Calculates the total cost of the trade
     * 
     * @param finalIndex the index of the final bar to be considered (if trade is
     *                   open)
     * @return the cost of the trade
     */
    public Num getTradeCost(int finalIndex) {
        Num transactionCost = transactionCostModel.calculate(this, finalIndex);
        Num borrowingCost = getHoldingCost(finalIndex);
        return transactionCost.plus(borrowingCost);
    }

    /**
     * Calculates the total cost of the closed trade
     * 
     * @return the cost of the trade
     */
    public Num getTradeCost() {
        Num transactionCost = transactionCostModel.calculate(this);
        Num borrowingCost = getHoldingCost();
        return transactionCost.plus(borrowingCost);
    }

    /**
     * Calculates the holding cost of the closed trade
     * 
     * @return the cost of the trade
     */
    public Num getHoldingCost() {
        return holdingCostModel.calculate(this);
    }

    /**
     * Calculates the holding cost of the trade
     * 
     * @param finalIndex the index of the final bar to be considered (if trade is
     *                   open)
     * @return the cost of the trade
     */
    public Num getHoldingCost(int finalIndex) {
        return holdingCostModel.calculate(this, finalIndex);
    }
}
