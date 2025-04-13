package ch.claude_martin.smallset;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

final class BasicByteSet extends AbstractByteSet {
  private SmallSet set;

  /** Creates an empty set. */
  public BasicByteSet() {
  }

  /** Creates a set containing the same values as the given set. */
  public BasicByteSet(final SmallSet set) {
    this.set = set;
  }

  private static boolean outOfRange(final byte b) {
    return b < 0 || b > 31;
  }

  /** Removes the given byte. This does nothing and returns false if the given value is below 0 or greater than 31. */
  @Override
  public boolean remove(final byte element) {
    if (BasicByteSet.outOfRange(element)) {
      return false;
    }
    return this.set != (this.set = this.set.remove(element));
  }

  /** Removes the given element. Note that this will only remove Bytes, but not instances of {@link Integer} or other
   * {@link Number}s. This does nothing and returns false, if the given value is below 0 or greater than 31.
   * {@inheritDoc} */
  @Override
  public boolean remove(final Object o) {
    if (o instanceof final Byte b && !BasicByteSet.outOfRange(b)) {
      return this.remove((byte) b);
    }
    return false;
  }

  @Override
  public boolean add(final byte e) throws IllegalArgumentException {
    return this.set != (this.set = this.set.add(e));
  }

  @Override
  public boolean add(final Byte e) throws IllegalArgumentException {
    return this.add((byte) e);
  }

  @Override
  public void clear() {
    this.set = SmallSet.empty();
  }

  @Override
  public SmallSet toSmallSet() {
    return this.set;
  }

  @Override
  public boolean contains(final byte v) {
    if (BasicByteSet.outOfRange(v)) {
      return false;
    }
    return this.set.contains(v);
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof final Byte b && !BasicByteSet.outOfRange(b)) {
      return this.set.contains(b);
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return this.set.isEmpty();
  }

  @Override
  public void forEach(final Consumer<? super Byte> action) {
    this.set.forEach((ByteConsumer) action::accept);
  }

  @Override
  public ByteIterator iterator() {
    return new ByteIterator() {
      private SmallSet remaining = BasicByteSet.this.set;
      private byte     lastRet   = -1;

      @Override
      public boolean hasNext() {
        return !this.remaining.isEmpty();
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this.remaining.isEmpty()) {
          throw new NoSuchElementException();
        }
        final byte next = (byte) Integer.numberOfTrailingZeros(this.remaining.value);
        this.remaining = this.remaining.remove(next);
        return this.lastRet = next;
      }

      @Override
      public void remove() {
        if (this.lastRet == -1) {
          throw new IllegalStateException();
        }
        BasicByteSet.this.set = BasicByteSet.this.set.remove(this.lastRet);
        this.lastRet = -1;
      }
    };

  }

  @Override
  public int size() {
    return this.set.size();
  }

  /** Returns the hash code value for this set that is compatible with {@link Set#hashCode()}
   *
   * @see Set#hashCode() */
  @Override
  public int hashCode() {
    int h = 0;
    int value = this.set.value;
    for (byte n; value != 0; value &= ~(1 << n)) {
      h += Byte.hashCode(n = (byte) Integer.numberOfTrailingZeros(value));
    }
    return h;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof final BasicByteSet bs) {
      return this.set == bs.set;
    }
    return super.equals(o);
  }

  @Override
  public BasicByteSet clone() {
    return new BasicByteSet(this.set);
  }

  @Override
  public ByteSet descendingSet() {
    return ModifiedByteSet.reversed(this);
  }

  @Override
  public ByteSet reversed() {
    return ModifiedByteSet.reversed(this);
  }

  @Override
  public ByteIterator descendingIterator() {
    return this.descendingSet().iterator();
  }

  @Override
  public ByteSet abstractSubSet(final byte fromElement, final byte toElement) {
    if (fromElement == 0 && toElement == Integer.SIZE) {
      return this;
    }
    return ModifiedByteSet.ranged(this, SmallSet.ofRange(fromElement, toElement));
  }

  @Override
  public Byte pollFirst() {
    if (this.set.isEmpty()) {
      return null;
    }
    final byte first = (byte) Integer.numberOfTrailingZeros(this.set.value);
    this.set = this.set.remove(first);
    return first;
  }

  @Override
  public Byte pollLast() {
    if (this.set.isEmpty()) {
      return null;
    }
    final byte last = (byte) (31 - Integer.numberOfLeadingZeros(this.set.value));
    this.set = this.set.remove(last);
    return last;
  }
}
