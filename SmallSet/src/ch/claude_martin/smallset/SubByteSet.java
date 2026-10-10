package ch.claude_martin.smallset;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/// Wraps some original ByteSet applying a subrange.
final class SubByteSet extends AbstractByteSet {
  private final ByteSet original;

  private final Range   range;

  private SubByteSet(final ByteSet original, final Range range) {
    super();
    this.original = original;
    this.range = range;
  }

  static ByteSet of(final ByteSet original, final Range range) {
    Objects.requireNonNull(original);
    Objects.requireNonNull(range);
    final Range originalRange = ((AbstractByteSet) original).range();
    if (!originalRange.containsAll(range)) {
      throw new IllegalArgumentException("The given range lies outside the bounds of the existing range");
    }
    if (range.equals(originalRange)) {
      return original;
    }
    return new SubByteSet(original, range);
  }

  @Override
  public int size() {
    return this.range.isEmpty() ? 0 : this.toSmallSet().size();
  }

  @Override
  public Range range() {
    return this.range;
  }

  private boolean inRange(final Object o) {
    if (o instanceof final byte b && b >= 0 && b < Integer.SIZE) {
      return this.range.inRange(b);
    }
    return false;
  }

  @Override
  public SmallSet toSmallSet() {
    return this.original.toSmallSet().intersect(this.range.toSmallSet());
  }

  @Override
  public ByteSet clone() {
    return new SubByteSet(this.original.clone(), this.range);
  }

  @Override
  public Comparator<? super Byte> comparator() {
    return this.original.comparator();
  }

  @Override
  public boolean add(final Byte b) {
    Objects.requireNonNull(b);
    if (this.inRange(b)) {
      return this.original.add(b);
    }
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean add(final byte b) throws IllegalArgumentException {
    if (this.inRange(b)) {
      return this.original.add(b);
    }
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean remove(final Object o) {
    Objects.requireNonNull(o);
    if (o instanceof final byte b && this.inRange(b)) {
      return this.original.remove(b);
    } else {
      return false;
    }
  }

  @Override
  public boolean remove(final byte b) {
    if (this.inRange(b)) {
      return this.original.remove(b);
    }
    return false;
  }

  @Override
  public ByteIterator iterator() {
    final ByteIterator itr = this.original.iterator();
    return new ByteIterator() {
      private byte lastReturned = -1;
      private int  remaining    = SubByteSet.this.size();

      @Override
      public boolean hasNext() {
        return this.remaining > 0;
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this.remaining == 0) {
          throw new NoSuchElementException();
        }
        byte next;
        do {
          next = itr.nextByte();
        } while (this.lastReturned < 0 && !SubByteSet.this.inRange(next));
        this.remaining--;
        return this.lastReturned = next;
      }

      @Override
      public void remove() {
        if (this.lastReturned == -1) {
          throw new IllegalStateException();
        }
        SubByteSet.this.remove(this.lastReturned);
        this.lastReturned = -1;
      }
    };
  }

  @Override
  public Byte first() {
    if (this.isEmpty()) {
      throw new NoSuchElementException();
    }
    return this.iterator().next();
  }

  @Override
  public OptionalByte firstAsOptionalByte() {
    if (this.isEmpty()) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(this.iterator().next());
  }

  @Override
  public Byte last() {
    if (this.isEmpty()) {
      throw new NoSuchElementException();
    }
    return this.descendingIterator().next();
  }

  @Override
  public OptionalByte lastAsOptionalByte() {
    if (this.isEmpty()) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(this.descendingIterator().next());
  }

  @Override
  public ByteSet descendingSet() {
    return ReversedByteSet.of(this);
  }

  @Override
  public ByteIterator descendingIterator() {
    return this.descendingSet().iterator();
  }

  @Override
  public String debug() {
    return ((AbstractByteSet) this.original).debug() + " subSet(" + this.range + ") => " + this.toString();
  }
}
