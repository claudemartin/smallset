package ch.claude_martin.smallset;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A mutable, navigable view of a bit set. Basic operations are done with the
 * given {@link SmallSet} value. This does not (yet) implement
 * {@link NavigableSet} but has many methods similar to those from that
 * interface. This implementation is not thread-safe.
 * 
 * @see SmallSet#toSet(int)
 * 
 * @author Claude Martin
 */
public final class ByteSet extends AbstractCollection<Byte> implements Set<Byte> {
  private SmallSet set;

  /** Creates an empty set. */
  public ByteSet() {
  }

  /** Creates a set containing the same values as the given set. */
  public ByteSet(final SmallSet set) {
    this.set = set;
  }

  private static boolean outOfRange(byte b) {
    return b < 0 || b > 31;
  }

  /**
   * Removes the given byte. This does nothing and returns false if the given
   * value is below 0 or greater than 31.
   */
  public boolean remove(byte element) {
    if (outOfRange(element))
      return false;
    return this.set != (this.set = this.set.remove(element));
  }

  /**
   * Removes the given element. Note that this will only remove Bytes, but not
   * instances of {@link Integer} or other {@link Number}s. This does nothing
   * and returns false, if the given value is below 0 or greater than 31.
   * {@inheritDoc}
   */
  @Override
  public boolean remove(Object o) {
    if (o instanceof Byte b && !outOfRange(b))
      return remove((byte) b);
    return false;
  }

  public boolean add(byte e) throws IllegalArgumentException {
    return this.set != (this.set = this.set.add(e));
  }

  @Override
  public boolean add(Byte e) throws IllegalArgumentException {
    return add((byte) e);
  }

  @Override
  public void clear() {
    this.set = SmallSet.empty();
  }

  public SmallSet toSmallSet() {
    return this.set;
  }

  public boolean contains(final byte v) {
    if (outOfRange(v))
      return false;
    return this.set.contains(v);
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof Byte b && !outOfRange(b))
      return this.set.contains(b);
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
      private SmallSet _set       = ByteSet.this.set;
      private byte     _lastValue = -1;

      @Override
      public boolean hasNext() {
        return !this._set.isEmpty();
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this._set.isEmpty())
          throw new NoSuchElementException();
        byte next = (byte) Integer.numberOfTrailingZeros(this._set.value);
        this._set = this._set.remove(next);
        return _lastValue = next;
      }

      @Override
      public void remove() {
        if (_lastValue == -1)
          throw new IllegalStateException();
        ByteSet.this.set = ByteSet.this.set.remove(_lastValue);
        _lastValue = -1;
      }
    };

  }

  @Override
  public int size() {
    return this.set.size();
  }

  /**
   * Returns the hash code value for this set that is compatible with
   * {@link Set#hashCode()}
   * 
   * @see Set#hashCode()
   */
  @Override
  public int hashCode() {
    int h = 0;
    int value = this.set.value;
    for (byte n; value != 0; value &= ~(1 << n))
      h += Byte.hashCode(n = (byte) Integer.numberOfTrailingZeros(value));
    return h;
  }

  /**
   * Compares the specified object with this set for equality. The two sets can
   * only be equal if both contain the same Byte objects.
   * 
   * @see Set#equals(Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof ByteSet bs)
      return this.set == bs.set;
    if (o instanceof Set<?> other) {
      if (this.size() != other.size())
        return false;
      for (Object e : other) {
        if (e instanceof Byte b && this.set.contains(b))
          continue;
        else
          return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public ByteSet clone() {
    return new ByteSet(this.set);
  }

  public Comparator<? super Byte> comparator() {
    return Byte::compare;
  }

  public OptionalByte first() {
    return this.set.min();
  }

  public OptionalByte last() {
    return this.set.max();
  }

  public OptionalByte lower(final Byte e) {
    return this.set.lower(e);
  }

  public OptionalByte floor(final Byte e) {
    return this.set.floor(e);
  }

  public OptionalByte ceiling(final Byte e) {
    return this.set.ceiling(e);
  }

  public OptionalByte higher(final Byte e) {
    return this.set.higher(e);
  }

  public OptionalByte pollFirst() {
    if (this.set.isEmpty())
      return OptionalByte.empty();
    final byte first = (byte) Integer.numberOfTrailingZeros(this.set.value);
    this.set = this.set.remove(first);
    return OptionalByte.of(first);
  }

  public OptionalByte pollLast() {
    if (this.set.isEmpty())
      return OptionalByte.empty();
    final byte last = (byte) (31 - Integer.numberOfLeadingZeros(this.set.value));
    this.set = this.set.remove(last);
    return OptionalByte.of(last);
  }

  /**
   * Returns a view of the portion of this set whose elements range from
   * {@code fromElement}, inclusive, to {@code toElement}, exclusive.
   * 
   * @param fromElement
   *          low endpoint (inclusive) of the returned set
   * @param toElement
   *          high endpoint (exclusive) of the returned set
   */
  public Set<Byte> subSet(byte fromElement, byte toElement) {
    if (fromElement == 0 && toElement == Integer.SIZE)
      return this;
    if (fromElement > toElement)
      throw new IllegalArgumentException("fromElement > toElement");
    if (fromElement < 0)
      throw new IllegalArgumentException("fromElement < 0");
    if (toElement > Integer.SIZE)
      throw new IllegalArgumentException("toElement > " + Integer.SIZE);
    return new AbstractSet<Byte>() {
      private final SmallSet range = SmallSet.ofRange(fromElement, toElement);

      private SmallSet getSmallSet() {
        return ByteSet.this.set.intersect(range);
      }

      @Override
      public int size() {
        return getSmallSet().size();
      }

      @Override
      public boolean add(Byte b) {
        Objects.requireNonNull(b);
        if (range.contains(b))
          return ByteSet.this.add(b);
        throw new IllegalArgumentException("Can't add " + b + ". Value is out of range.");
      }

      @Override
      public boolean remove(Object o) {
        Objects.requireNonNull(o);
        if (o instanceof Byte b && range.contains(b))
          return ByteSet.this.remove(b);
        throw new IllegalArgumentException("Can't remove " + o + ". Value is out of range or not a byte.");
      }

      @Override
      public Iterator<Byte> iterator() {
        final var itr = getSmallSet().iterator();
        return new Iterator<Byte>() {
          Byte lastReturned = null;

          @Override
          public Byte next() {
            return lastReturned = itr.next();
          }

          @Override
          public boolean hasNext() {
            return itr.hasNext();
          }

          @Override
          public void remove() {
            if (lastReturned == null)
              throw new IllegalStateException("This iterator has no element to remove.");
            ByteSet.this.remove(lastReturned);
            lastReturned = null;
          }
        };
      }
    };
  }

  /**
   * Returns a view of the portion of this set whose elements are strictly less
   * than {@code toElement}.
   * 
   * @param toElement
   *          toElement high endpoint (exclusive) of the returned set
   */
  public Set<Byte> headSet(byte toElement) {
    return subSet((byte) 0, toElement);
  }

  /**
   * Returns a view of the portion of this set whose elements are greater than
   * or equal to {@code fromElement}.
   * 
   * @param fromElement
   *          fromElement low endpoint (inclusive) of the returned set
   */
  public Set<Byte> tailSet(byte fromElement) {
    return subSet((byte) fromElement, (byte) Integer.SIZE);
  }

  @Override
  public Byte[] toArray() {
    final int size = this.set.size();
    final Byte[] result = new Byte[size];
    int i = 0;
    for (Byte n : this.set) {
      result[i++] = n;
    }
    return result;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return super.toArray(a); // Implemented in AbstractCollection.
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    Objects.requireNonNull(c, "c");
    for (Object e : c)
      if (!this.contains(e))
        return false;
    return true;
  }

  public boolean addAll(byte... c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    for (byte e : c)
      if (this.add(e))
        modified = true;
    return modified;
  }

  @Override
  public boolean addAll(Collection<? extends Byte> c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    for (Byte e : c)
      if (this.add(e))
        modified = true;
    return modified;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    final ByteIterator it = this.iterator();
    while (it.hasNext()) {
      if (!c.contains(it.next())) {
        it.remove();
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    final ByteIterator it = this.iterator();
    while (it.hasNext()) {
      if (c.contains(it.next())) {
        it.remove();
        modified = true;
      }
    }
    return modified;
  }

}
