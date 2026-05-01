package ch.claude_martin.smallset;

import java.util.NoSuchElementException;
import java.util.Objects;

/// Wraps some original ByteSet applying a subrange or inversion of order. It's never both.
final class ModifiedByteSet extends AbstractByteSet {
  private final ByteSet  original;
  private final boolean  descending;
  private final SmallSet range;

  private ModifiedByteSet(final ByteSet original, final boolean descending, final SmallSet range) {
    super();
    this.original = original;
    this.descending = descending;
    this.range = range;
  }

  @Override
  public SmallSet toSmallSet() {
    if (this.descending) {
      return this.original.toSmallSet();
    }
    return this.original.toSmallSet().intersect(this.range);
  }

  public static ModifiedByteSet reversed(final ByteSet original) {
    return new ModifiedByteSet(original, true, SmallSet.empty().complement());
  }

  public static ModifiedByteSet ranged(final ByteSet original, final SmallSet range) {
    return new ModifiedByteSet(original, false, range);
  }

  @Override
  public ByteSet clone() {
    return new ModifiedByteSet(this.original.clone(), this.descending, this.range);
  }

  @Override
  public boolean add(final Byte b) {
    Objects.requireNonNull(b);
    if (this.range.contains(b)) {
      return this.original.add(b);
    }
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean add(final byte b) throws IllegalArgumentException {
    if (this.range.contains(b)) {
      return this.original.add(b);
    }
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean remove(final Object o) {
    Objects.requireNonNull(o);
    if (o instanceof final byte b && this.range.contains(b)) {
      return this.original.remove(b);
    } else {
      return false;
    }
  }

  @Override
  public boolean remove(final byte b) {
    if (this.range.contains(b)) {
      return this.original.remove(b);
    }
    return false;
  }

  @Override
  public ByteIterator iterator() {
    return new ByteIterator() {
      private SmallSet remaining = ModifiedByteSet.this.toSmallSet();
      private byte     lastRet   = -1;

      @Override
      public boolean hasNext() {
        return !this.remaining.isEmpty();
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        final byte next = (ModifiedByteSet.this.descending ? this.remaining.max() : this.remaining.min())
            .orElseThrow(NoSuchElementException::new);
        this.remaining = this.remaining.remove(next);
        return this.lastRet = next;
      }

      @Override
      public void remove() {
        if (this.lastRet == -1) {
          throw new IllegalStateException();
        }
        ModifiedByteSet.this.remove(this.lastRet);
        this.lastRet = -1;
      }
    };
  }

  @Override
  public Byte pollFirst() {
    final var first = this.toSmallSet().min();
    if (first.isPresent()) {
      final byte asByte = first.getAsByte();
      this.original.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public Byte pollLast() {
    final var first = this.toSmallSet().max();
    if (first.isPresent()) {
      final byte asByte = first.getAsByte();
      this.original.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public ByteSet descendingSet() {
    if (this.descending) {
      return this.original;
    }
    return ModifiedByteSet.reversed(this);
  }

  @Override
  public ByteSet reversed() {
    return descendingSet();
  }

  @Override
  public ByteIterator descendingIterator() {
    return descendingSet().iterator();
  }

  @Override
  public ByteSet abstractSubSet(final byte a, final byte b) {
    final var newRange = SmallSet.ofRange(a, b);
    if (newRange.containsAll(this.range)) {
      return this;
    }
    return ModifiedByteSet.ranged(this, newRange);
  }
}
