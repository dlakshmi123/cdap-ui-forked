/*
 * Copyright (c) 2013, Continuuity Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are not permitted
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.continuuity.examples.ticker;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.dataset.KeyValueTable;
import com.continuuity.api.data.dataset.SimpleTimeseriesTable;
import com.continuuity.api.data.stream.Stream;
import com.continuuity.examples.ticker.data.MultiIndexedTable;
import com.continuuity.examples.ticker.order.OrderDataSaver;
import com.continuuity.examples.ticker.query.Orders;
import com.continuuity.examples.ticker.query.Summary;
import com.continuuity.examples.ticker.query.Timeseries;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * The Ticker App consists of 2 flows
 */
public class TickerApp implements Application {
  /**
   * Configures the {@link com.continuuity.api.Application} by returning an
   * {@link com.continuuity.api.ApplicationSpecification}.
   *
   * @return An instance of {@code ApplicationSpecification}.
   */
  @Override
  public ApplicationSpecification configure() {
    Set<byte[]> doNotIndexFields = Sets.newTreeSet(Bytes.BYTES_COMPARATOR);
    doNotIndexFields.add(OrderDataSaver.PAYLOAD_COL);

    return ApplicationSpecification.Builder.with()
      .setName("Ticker")
      .setDescription("Application for storing and querying stock tick data")
      .withStreams()
        .add(new Stream("tickers"))
        .add(new Stream("orders"))
      .withDataSets()
        .add(new SimpleTimeseriesTable("tickTimeseries", 60 * 60))
        .add(new KeyValueTable("tickerSet"))
        .add(new MultiIndexedTable("orderIndex", OrderDataSaver.TIMESTAMP_COL, doNotIndexFields))
      .withFlows()
        .add(new TickStreamFlow())
        .add(new OrderStreamFlow())
      .withProcedures()
        .add(new Timeseries())
        .add(new Orders())
        .add(new Summary())
      .noMapReduce()
      .noWorkflow()
      .build();
  }
}

