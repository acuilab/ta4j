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
import org.ta4j.core.num.Num;

import java.io.Serializable;
import java.util.List;

import static org.ta4j.core.num.NaN.NaN;

/**
 * A history/record of a trading session.
 *
 * Holds the full trading record when running a {@link Strategy strategy}. It is
 * used to:
 * <ul>
 * <li>check to satisfaction of some trading rules (when running a strategy)
 * <li>analyze the performance of a trading strategy
 * </ul>
 * 
 * 交易会话历史/记录。执行策略时保留完整的交易记录。它用于：
 *  检查是否满足某些交易规则（在执行策略时）
 *  分析交易策略的表现
 */
public interface TradingRecord extends Serializable {

    /**
     * 获得当前交易
     * @return the current trade
     */
    Trade getCurrentTrade();

    /**
     * Operates an order in the trading record.
     * 操作交易记录中的订单
     * 
     * @param index the index to operate the order
     */
    default void operate(int index) {
        operate(index, NaN, NaN);
    }

    /**
     * Operates an order in the trading record.
     * 操作交易记录中的订单
     * 
     * @param index  the index to operate the order 操作订单的索引号
     * @param price  the price of the order	    订单价格
     * @param amount the amount to be ordered	    订单数量
     */
    void operate(int index, Num price, Num amount);

    /**
     * Operates an entry order in the trading record.
     * 操作交易记录中的进入订单。
     * 
     * @param index the index to operate the entry
     * @return true if the entry has been operated, false otherwise
     */
    default boolean enter(int index) {
        return enter(index, NaN, NaN);
    }

    /**
     * Operates an entry order in the trading record.
     * 操作交易记录中的进入订单。
     * 
     * @param index  the index to operate the entry 操作进入订单的索引号
     * @param price  the price of the order	    订单价格
     * @param amount the amount to be ordered	    订单数量
     * @return true if the entry has been operated, false otherwise
     *	    如果该进入订单已被操作，则为true，否则为false
     */
    boolean enter(int index, Num price, Num amount);

    /**
     * Operates an exit order in the trading record.
     * 操作交易记录中的退出订单。
     * 
     * @param index the index to operate the exit   操作退出订单的索引号
     * @return true if the exit has been operated, false otherwise
     */
    default boolean exit(int index) {
        return exit(index, NaN, NaN);
    }

    /**
     * Operates an exit order in the trading record.
     * 操作交易记录中的退出订单。
     * 
     * @param index  the index to operate the exit  操作退出订单的索引号
     * @param price  the price of the order	    订单价格
     * @param amount the amount to be ordered	    订单数量
     * @return true if the exit has been operated, false otherwise
     *	    如果该退出订单已被操作，则为true，否则为false
     */
    boolean exit(int index, Num price, Num amount);

    /**
     * 如果没有交易开放，则为true，否则为false
     * @return true if no trade is open, false otherwise
     */
    default boolean isClosed() {
        return !getCurrentTrade().isOpened();
    }

    /**
     * 获得记录的交易
     * @return the recorded trades
     */
    List<Trade> getTrades();

    /**
     * 获得记录的交易数量
     * @return the number of recorded trades
     */
    default int getTradeCount() {
        return getTrades().size();
    }

    /**
     * 获得记录的最后一个交易
     * @return the last trade recorded
     */
    default Trade getLastTrade() {
        List<Trade> trades = getTrades();
        if (!trades.isEmpty()) {
            return trades.get(trades.size() - 1);
        }
        return null;
    }

    /**
     * 获得最后的订单
     * @return the last order recorded
     */
    Order getLastOrder();

    /**
     * 获得指定类型的最后订单
     * @param orderType the type of the order to get the last of
     * @return the last order (of the provided type) recorded
     */
    Order getLastOrder(OrderType orderType);

    /**
     * 获得最后的进入订单
     * @return the last entry order recorded
     */
    Order getLastEntry();

    /**
     * 获得最后的退出订单
     * @return the last exit order recorded
     */
    Order getLastExit();
}
