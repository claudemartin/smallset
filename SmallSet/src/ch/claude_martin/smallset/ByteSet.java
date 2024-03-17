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
 * given {@link SmallSet} value. This does not (yet) implement {@link NavigableSet} but
 * has many methods similar to those from that interface. This implementation is
 * not thread-safe.
 * 
 * @see SmallSet#toSet(int)
 * 
 * @author Claude Martin
 */
public final class ByteSet extends AbstractCollection<Byte> implements Set<Byte> {
  private SmallSet set;

  public ByteSet(final SmallSet set) {
    this.set = set;
  }

  public boolean remove(byte element) {
    return this.set != (this.set = this.set.remove(element));
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Byte)
      return remove((byte) o);
    return false;
  }

  public boolean add(byte e) {
    return this.set != (this.set = this.set.add(e));
  }

  @Override
  public boolean add(Byte e) {
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
    return this.set.contains(v);
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof Byte)
      return this.set.contains((byte) o);
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
   * Compares the specified object with this set for equality.
   * 
   * @see Set#equals(Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    if (o instanceof ByteSet)
      return this.set == ((ByteSet) o).set;
    if (o instanceof Set) {
      try {
        final Set<Byte> _set = ((Set<Byte>) o);
        SmallSet other = SmallSet.empty();
        if (this.size() != _set.size())
          return false;
        for (Byte b : _set)
          other = other.add(b);
        return other == this.set;
      } catch (ClassCastException | IllegalArgumentException e) {
        return false;
      }
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
   *                      low endpoint (inclusive) of the returned set
   * @param toElement
   *                      high endpoint (exclusive) of the returned set
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
      public Iterator<Byte> iterator() {
        return getSmallSet().iterator();
      }
    };
  }

  /**
   * Returns a view of the portion of this set whose elements are strictly less
   * than {@code toElement}.
   * 
   * @param toElement
   *                    toElement high endpoint (exclusive) of the returned set
   */
  public Set<Byte> headSet(byte toElement) {
    return subSet((byte) 0, toElement);
  }

  /**
   * Returns a view of the portion of this set whose elements are greater than or
   * equal to {@code fromElement}.
   * 
   * @param fromElement fromElement low endpoint (inclusive) of the returned set
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
