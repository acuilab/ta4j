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

import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of a {@link TradingRecord}.
 * 交易记录的基础实现
 *
 */
public class BaseTradingRecord implements TradingRecord {

    private static final long serialVersionUID = -4436851731855891220L;

    /**
     * The recorded orders
     * 记录的订单
     */
    private List<Order> orders = new ArrayList<>();

    /**
     * The recorded BUY orders
     * 记录的买单
     */
    private List<Order> buyOrders = new ArrayList<>();

    /**
     * The recorded SELL orders
     * 记录的卖单
     */
    private List<Order> sellOrders = new ArrayList<>();

    /**
     * The recorded entry orders
     * 记录的进入单
     */
    private List<Order> entryOrders = new ArrayList<>();

    /**
     * The recorded exit orders
     * 记录的退出单
     */
    private List<Order> exitOrders = new ArrayList<>();

    /**
     * The recorded trades
     * 记录的交易
     */
    private List<Trade> trades = new ArrayList<>();

    /**
     * The entry type (BUY or SELL) in the trading session
     * 在交易会话中进入单的类型
     */
    private Order.OrderType startingType;

    /**
     * The current non-closed trade (there's always one)
     * 当前非关闭的交易（总有一个新的【进入订单和退出订单都为空】或打开的【进入订单不为空，退出订单为空】交易）
     */
    private Trade currentTrade;

    /**
     * Trading cost models
     * 交易成本模型
     * 
     */
    private CostModel transactionCostModel; // 交易成本
    private CostModel holdingCostModel;	    // 持仓成本

    /**
     * Constructor.
     */
    public BaseTradingRecord() {
        this(Order.OrderType.BUY);
    }

    /**
     * Constructor.
     * @param orderType	    进入订单类型
     */
    public BaseTradingRecord(Order.OrderType orderType) {
        this(orderType, new ZeroCostModel(), new ZeroCostModel());
    }

    /**
     * Constructor.
     *
     * @param entryOrderType       the {@link Order.OrderType order type} of entries in the trading session
     *	    进入订单类型
     * @param transactionCostModel the cost model for transactions of the asset
     *	    交易成本模型
     * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
     *	    持仓成本模型
     */
    public BaseTradingRecord(Order.OrderType entryOrderType, CostModel transactionCostModel, CostModel holdingCostModel) {
        if (entryOrderType == null) {
            throw new IllegalArgumentException("Starting type must not be null");
        }
        this.startingType = entryOrderType;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
        currentTrade = new Trade(entryOrderType, transactionCostModel, holdingCostModel);
    }

    /**
     * Constructor.
     *
     * @param orders the orders to be recorded (cannot be empty)
     */
    public BaseTradingRecord(Order... orders) {
        this(new ZeroCostModel(), new ZeroCostModel(), orders);
    }

    /**
     * Constructor.
     *
     * @param transactionCostModel the cost model for transactions of the asset
     * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
     * @param orders               the orders to be recorded (cannot be empty)
     */
    public BaseTradingRecord(CostModel transactionCostModel, CostModel holdingCostModel, Order... orders) {
        this(orders[0].getType(), transactionCostModel, holdingCostModel);
        for (Order o : orders) {
	    // 新订单是否要成为一个进入订单
            boolean newOrderWillBeAnEntry = currentTrade.isNew();
            if (newOrderWillBeAnEntry && o.getType() != startingType) {
                // Special case for entry/exit types reversal	进入/退出类型反转的特殊情况
                // E.g.: BUY, SELL,
                // BUY, SELL,
                // SELL, BUY,
                // BUY, SELL
                currentTrade = new Trade(o.getType(), transactionCostModel, holdingCostModel);
            }
            Order newOrder = currentTrade.operate(o.getIndex(), o.getPricePerAsset(), o.getAmount());
            recordOrder(newOrder, newOrderWillBeAnEntry);
        }
    }

    /**
     * 获得当前交易
     * @return 
     */
    @Override
    public Trade getCurrentTrade() {
        return currentTrade;
    }

    @Override
    public void operate(int index, Num price, Num amount) {
        if (currentTrade.isClosed()) {
            // Current trade closed, should not occur
	    // 当前交易已关闭，不应发生
            throw new IllegalStateException("Current trade should not be closed");
        }
        boolean newOrderWillBeAnEntry = currentTrade.isNew();
        Order newOrder = currentTrade.operate(index, price, amount);
        recordOrder(newOrder, newOrderWillBeAnEntry);
    }

    @Override
    public boolean enter(int index, Num price, Num amount) {
        if (currentTrade.isNew()) {
            operate(index, price, amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean exit(int index, Num price, Num amount) {
        if (currentTrade.isOpened()) {
            operate(index, price, amount);
            return true;
        }
        return false;
    }

    @Override
    public List<Trade> getTrades() {
        return trades;
    }

    @Override
    public Order getLastOrder() {
        if (!orders.isEmpty()) {
            return orders.get(orders.size() - 1);
        }
        return null;
    }

    @Override
    public Order getLastOrder(Order.OrderType orderType) {
        if (Order.OrderType.BUY.equals(orderType) && !buyOrders.isEmpty()) {
            return buyOrders.get(buyOrders.size() - 1);
        } else if (Order.OrderType.SELL.equals(orderType) && !sellOrders.isEmpty()) {
            return sellOrders.get(sellOrders.size() - 1);
        }
        return null;
    }

    @Override
    public Order getLastEntry() {
        if (!entryOrders.isEmpty()) {
            return entryOrders.get(entryOrders.size() - 1);
        }
        return null;
    }

    @Override
    public Order getLastExit() {
        if (!exitOrders.isEmpty()) {
            return exitOrders.get(exitOrders.size() - 1);
        }
        return null;
    }

    /**
     * Records an order and the corresponding trade (if closed).
     * 记录订单和相应的交易（如果订单已关闭）。
     *
     * @param order   the order to be recorded
     * @param isEntry true if the order is an entry, false otherwise (exit)
     *	    如果订单是进入订单，则为true，否则为false（退出订单）
     */
    private void recordOrder(Order order, boolean isEntry) {
        if (order == null) {
            throw new IllegalArgumentException("Order should not be null");
        }

        // Storing the new order in entries/exits lists
	// 在进入/退出列表中保存新订单
        if (isEntry) {
            entryOrders.add(order);
        } else {
            exitOrders.add(order);
        }

        // Storing the new order in orders list
	// 在订单列表中保存新订单
        orders.add(order);
        if (Order.OrderType.BUY.equals(order.getType())) {
            // Storing the new order in buy orders list
	    // 在买单列表中保存新订单
            buyOrders.add(order);
        } else if (Order.OrderType.SELL.equals(order.getType())) {
            // Storing the new order in sell orders list
	    // 在卖单列表中保存新订单
            sellOrders.add(order);
        }

        // Storing the trade if closed
	// 保存已关闭订单
        if (currentTrade.isClosed()) {
            trades.add(currentTrade);
            currentTrade = new Trade(startingType, transactionCostModel, holdingCostModel);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseTradingRecord:\n");
        for (Order order : orders) {
            sb.append(order.toString()).append("\n");
        }
        return sb.toString();
    }
}
