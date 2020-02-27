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
package org.ta4j.core.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cached {@link Indicator indicator}.
 * 缓存的指标。缓存指标的构造函数，避免重新计算相同索引的指标
 *
 * Caches the constructor of the indicator. Avoid to calculate the same index of the indicator twice.
 * 
 * 一些指标需要递归调用和/或前一个报价的值才能计算其最后一个值。 
 * 因此，已经为ta4j提供的所有指标实施了缓存机制。 该系统避免两次计算相同的值。 
 * 因此，如果已经计算出一个值，则下次请求该值时将从缓存中检索该值。 最后一个柱的值将不会被缓存。 
 * 这样，您可以通过向其添加价格/交易来修改TimeSeries的最后一个柱，并使用指标重新计算结果。
 * @param <T>
 */
public abstract class CachedIndicator<T> extends AbstractIndicator<T> {

    private static final long serialVersionUID = 7505855220893125595L;

    /**
     * List of cached results
     * 缓存结果列表
     */
    private final List<T> results = new ArrayList<>();

    /**
     * Should always be the index of the last result in the results list. I.E. the last calculated result.
     * 应该始终是结果列表中最后一个结果的索引。 即 最后的计算结果。
     */
    protected int highestResultIndex = -1;

    /**
     * Constructor.
     *
     * @param series the related bar series
     */
    public CachedIndicator(BarSeries series) {
        super(series);
    }

    /**
     * Constructor.
     *
     * @param indicator a related indicator (with a bar series)
     */
    public CachedIndicator(Indicator<?> indicator) {
        this(indicator.getBarSeries());
    }

    /**
     * 获得指标值
     * @param index 柱序号
     * @return 
     */
    @Override
    public T getValue(int index) {
        BarSeries series = getBarSeries();
        if (series == null) {
            // Series is null; the indicator doesn't need cache.    柱序列为空；指标不需要缓存
            // (e.g. simple computation of the value)		    （例如，简单地计算值）
            // --> Calculating the value
            return calculate(index);
        }

        // Series is not null
	// 柱序列不为空
        final int removedBarsCount = series.getRemovedBarsCount();
        final int maximumResultCount = series.getMaximumBarCount();

        T result;
        if (index < removedBarsCount) {
            // Result already removed from cache
            log.trace("{}: result from bar {} already removed from cache, use {}-th instead",
                    getClass().getSimpleName(), index, removedBarsCount);
            increaseLengthTo(removedBarsCount, maximumResultCount);
            highestResultIndex = removedBarsCount;
            result = results.get(0);
            if (result == null) {
                // It should be "result = calculate(removedBarsCount);".
                // We use "result = calculate(0);" as a workaround
                // to fix issue #120 (https://github.com/mdeverdelhan/ta4j/issues/120).
                result = calculate(0);
                results.set(0, result);
            }
        } else {
            if (index == series.getEndIndex()) {
                // Don't cache result if last bar
                result = calculate(index);
            } else {
                increaseLengthTo(index, maximumResultCount);
                if (index > highestResultIndex) {
                    // Result not calculated yet
                    highestResultIndex = index;
                    result = calculate(index);
                    results.set(results.size() - 1, result);
                } else {
                    // Result covered by current cache
                    int resultInnerIndex = results.size() - 1 - (highestResultIndex - index);
                    result = results.get(resultInnerIndex);
                    if (result == null) {
                        result = calculate(index);
                        results.set(resultInnerIndex, result);
                    }
                }
            }

        }
        return result;
    }

    /**
     * @param index the bar index
     * @return the value of the indicator
     */
    protected abstract T calculate(int index);

    /**
     * Increases the size of cached results buffer.
     *
     * @param index     the index to increase length to
     * @param maxLength the maximum length of the results buffer
     */
    private void increaseLengthTo(int index, int maxLength) {
        if (highestResultIndex > -1) {
            int newResultsCount = Math.min(index - highestResultIndex, maxLength);
            if (newResultsCount == maxLength) {
                results.clear();
                results.addAll(Collections.nCopies(maxLength, null));
            } else if (newResultsCount > 0) {
                results.addAll(Collections.nCopies(newResultsCount, null));
                removeExceedingResults(maxLength);
            }
        } else {
            // First use of cache
            assert results.isEmpty() : "Cache results list should be empty";
            results.addAll(Collections.nCopies(Math.min(index + 1, maxLength), null));
        }
    }

    /**
     * Removes the N first results which exceed the maximum bar count. (i.e. keeps
     * only the last maximumResultCount results)
     *
     * @param maximumResultCount the number of results to keep
     */
    private void removeExceedingResults(int maximumResultCount) {
        int resultCount = results.size();
        if (resultCount > maximumResultCount) {
            // Removing old results
            final int nbResultsToRemove = resultCount - maximumResultCount;
            for (int i = 0; i < nbResultsToRemove; i++) {
                results.remove(0);
            }
        }
    }
}
