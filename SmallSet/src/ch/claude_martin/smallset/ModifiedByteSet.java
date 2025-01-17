package ch.claude_martin.smallset;

import java.util.*;

/** Wraps some original ByteSet applying a subrange or inversion of order. It's never both. */
final class ModifiedByteSet extends AbstractByteSet {
  private final ByteSet  original;
  private final boolean  descending;
  private final SmallSet range;

  private ModifiedByteSet(ByteSet original, boolean descending, SmallSet range) {
    super();
    this.original = original;
    this.descending = descending;
    this.range = range;
  }

  @Override
  public SmallSet toSmallSet() {
    if (descending)
      return this.original.toSmallSet();
    return this.original.toSmallSet().intersect(range);
  }

  public static ModifiedByteSet reversed(ByteSet original) {
    return new ModifiedByteSet(original, true, SmallSet.empty().complement());
  }

  public static ModifiedByteSet ranged(ByteSet original, SmallSet range) {
    return new ModifiedByteSet(original, false, range);
  }

  @Override
  public ByteSet clone() {
    return new ModifiedByteSet(this.original.clone(), this.descending, this.range);
  }

  @Override
  public boolean add(Byte b) {
    Objects.requireNonNull(b);
    if (range.contains(b))
      return this.original.add(b);
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean add(byte b) throws IllegalArgumentException {
    if (range.contains(b))
      return this.original.add(b);
    throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
  }

  @Override
  public boolean remove(Object o) {
    Objects.requireNonNull(o);
    if (o instanceof Byte b && range.contains(b)) {
      return this.original.remove(b);
    } else {
      return false;
    }
  }

  @Override
  public boolean remove(byte b) {
    if (range.contains(b))
      return this.original.remove(b);
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
        final byte next = (descending ? remaining.max() : remaining.min())
            .orElseThrow(() -> new NoSuchElementException());
        this.remaining = this.remaining.remove(next);
        return lastRet = next;
      }

      @Override
      public void remove() {
        if (lastRet == -1)
          throw new IllegalStateException();
        ModifiedByteSet.this.remove(lastRet);
        lastRet = -1;
      }
    };
  }

  @Override
  public Byte pollFirst() {
    var first = this.toSmallSet().min();
    if (first.isPresent()) {
      byte asByte = first.getAsByte();
      original.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public Byte pollLast() {
    var first = this.toSmallSet().max();
    if (first.isPresent()) {
      byte asByte = first.getAsByte();
      original.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public ByteSet descendingSet() {
    if (this.descending)
      return original;
    return reversed(this);
  }

  @Override
  public ByteIterator descendingIterator() {
    return (!descending ? reversed(this) : this.original).iterator();
  }

  @Override
  public ByteSet abstractSubSet(byte a, byte b) {
    final var newRange = SmallSet.ofRange(a, b);
    if (newRange.containsAll(this.range))
      return this;
    return ModifiedByteSet.ranged(this, newRange);
  }
}
