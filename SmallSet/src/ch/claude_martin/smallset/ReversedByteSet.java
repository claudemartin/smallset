package ch.claude_martin.smallset;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/// Wraps some original ByteSet reversing it's order. Because it's possible that an already reversed set gets reversed
/// again, this could result in a set with natural order.
final class ReversedByteSet extends AbstractByteSet {
  private final ByteSet original;

  private ReversedByteSet(final ByteSet original) {
    super();
    this.original = original;
  }

  static ByteSet of(final ByteSet original) {
    Objects.requireNonNull(original);
    if (original instanceof final ReversedByteSet reversed && original.comparator() == AbstractByteSet.REVERSED) {
      return reversed.original;
    }
    return new ReversedByteSet(original);
  }

  @Override
  public SmallSet toSmallSet() {
    return this.original.toSmallSet();
  }

  @Override
  public ByteSet clone() {
    return new ReversedByteSet(this.original.clone());
  }

  @Override
  public int size() {
    return this.original.size();
  }

  @Override
  public boolean equals(final Object o) {
    // order doesn't matter.
    return this.original.equals(o);
  }

  @Override
  public Range range() {
    return ((AbstractByteSet) this.original).range().reverse();
  }

  @Override
  public Comparator<? super Byte> comparator() {
    final var originalComparator = this.original.comparator();
    if (originalComparator == AbstractByteSet.NATURAL_ORDER) {
      return AbstractByteSet.REVERSED;
    }
    if (originalComparator == AbstractByteSet.REVERSED) {
      return AbstractByteSet.NATURAL_ORDER;
    }
    throw new IllegalArgumentException("Unexpected value: " + originalComparator);
  }

  @Override
  public boolean add(final Byte b) {
    Objects.requireNonNull(b);
    return this.original.add(b);
  }

  @Override
  public boolean add(final byte b) throws IllegalArgumentException {
    return this.original.add(b);
  }

  @Override
  public boolean remove(final Object o) {
    Objects.requireNonNull(o);
    if (o instanceof final byte b) {
      return this.original.remove(b);
    } else {
      return false;
    }
  }

  @Override
  public boolean remove(final byte b) {
    return this.original.remove(b);
  }

  @Override
  public ByteIterator iterator() {
    if (this.isNaturalOrder()) {
      return ReversedByteSet.this.toSmallSet().iterator();
    }
    return new ByteIterator() {
      private SmallSet remaining    = ReversedByteSet.this.toSmallSet();
      private byte     lastReturned = -1;

      @Override
      public boolean hasNext() {
        return !this.remaining.isEmpty();
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        final byte next = (this.remaining.max()).orElseThrow(NoSuchElementException::new);
        this.remaining = this.remaining.remove(next);
        return this.lastReturned = next;
      }

      @Override
      public void remove() {
        if (this.lastReturned == -1) {
          throw new IllegalStateException();
        }
        ReversedByteSet.this.remove(this.lastReturned);
        this.lastReturned = -1;
      }
    };
  }

  @Override
  public Byte first() {
    if (this.isEmpty()) {
      throw new NoSuchElementException();
    }
    return this.original.last();
  }

  @Override
  public OptionalByte firstAsOptionalByte() {
    if (this.isEmpty()) {
      return OptionalByte.empty();
    }
    return this.original.lastAsOptionalByte();
  }

  @Override
  public Byte last() {
    if (this.isEmpty()) {
      throw new NoSuchElementException();
    }
    return this.original.first();
  }

  @Override
  public OptionalByte lastAsOptionalByte() {
    if (this.isEmpty()) {
      return OptionalByte.empty();
    }
    return this.original.firstAsOptionalByte();
  }

  @Override
  public ByteSet descendingSet() {
    return ReversedByteSet.of(this);
  }

  @Override
  public ByteIterator descendingIterator() {
    return this.original.iterator();
  }

  @Override
  public String debug() {
    return ((AbstractByteSet) this.original).debug() + " reversed() => " + this.toString();
  }
}
