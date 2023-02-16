package com.continuuity.data.operation.ttqueue;

import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * Result from a {@link QueueDequeue} operation.
 */
public class DequeueResult {

  private final DequeueStatus status;
  private final QueueEntryPointer[] pointers;
  private final QueueEntry[] entries;

  public DequeueResult(final DequeueStatus status) {
    this(status, (QueueEntryPointer[]) null, null);
  }

  public DequeueResult(final DequeueStatus status,
                       final QueueEntryPointer pointer,
                       final QueueEntry entry) {
    this(status, new QueueEntryPointer[] { pointer }, new QueueEntry[] { entry });
  }

  public DequeueResult(final DequeueStatus status,
                       final QueueEntryPointer[] pointers,
                       final QueueEntry[] entries) {
    this.status = status;
    this.pointers = pointers;
    this.entries = entries;
  }

  public DequeueResult(DequeueStatus status,
                       QueueEntryPointer pointer,
                       QueueEntry entry,
                       QueueState queueState) {
    this(status, new QueueEntryPointer[] { pointer }, new QueueEntry[] { entry }, queueState);
  }

  public DequeueResult(DequeueStatus status,
                       QueueEntryPointer[] pointers,
                       QueueEntry[] entries,
                       @SuppressWarnings("unused") QueueState queueState) {
    this.status = status;
    this.pointers = pointers;
    this.entries = entries;
    // ignore the queue state, it is not returned any more - TODO remove these two ctors
  }

  public boolean isSuccess() {
    return this.status == DequeueStatus.SUCCESS;
  }

  public boolean isEmpty() {
    return this.status == DequeueStatus.EMPTY;
  }

  public DequeueStatus getStatus() {
    return this.status;
  }

  public QueueEntryPointer[] getEntryPointers() {
    return this.pointers;
  }

  public QueueEntryPointer getEntryPointer() {
    return this.pointers[0];
  }

  public QueueEntry[] getEntries() {
    return this.entries;
  }

  public QueueEntry getEntry() {
    return this.entries[0];
  }

  /**
   * Defines DequeueStatus.
   */
  public static enum DequeueStatus {
    SUCCESS, EMPTY
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("status", this.status)
        .add("entryPointers", Arrays.toString(this.pointers))
        .add("entries", Arrays.toString(this.entries))
        .toString();
  }
}
