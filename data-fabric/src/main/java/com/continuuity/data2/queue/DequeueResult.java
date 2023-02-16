/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data2.queue;


import com.google.common.collect.Iterators;

import java.util.Iterator;


/**
 * Represents result of an dequeue. The iterable gives dequeued data entries in the order of dequeue.
 */
public interface DequeueResult extends Iterable<byte[]> {

  /**
   * Returns {@code true} if there is no data in the queue.
   */
  boolean isEmpty();

  /**
   * Reclaim all dequeue entries represented by this result. The effect is to put entries represented by this
   * result back to the dequeued set of the queue consumer. Note that call to this method is transactional
   * and requires a new transaction on the {@link Queue2Consumer} instance who provides the instance of this
   * {@link DequeueResult}.
   *
   * E.g.
   * <pre>
   *   startTransaction();
   *   DequeueResult result;
   *   try {
   *     result = consumer.dequeue();
   *     commitTransaction();
   *   } catch (Exception e) {
   *     rollbackTransaction();
   *
   *     // Skip the result.
   *     startTransaction();
   *     result.skip;
   *     commitTransaction();
   *   }
   *
   * </pre>
   */
  void reclaim();

  /**
   * Returns number of entries in this result.
   */
  int size();

  /**
   * An (immutable) empty dequeue result.
   */
  static final DequeueResult EMPTY_RESULT = new DequeueResult() {
    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public void reclaim() {
      // No-op
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Iterator<byte[]> iterator() {
      return Iterators.emptyIterator();
    }
  };
}
