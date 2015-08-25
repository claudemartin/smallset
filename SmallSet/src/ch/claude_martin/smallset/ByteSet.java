package ch.claude_martin.smallset;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A mutable, navigable view of a bit set. Basic operations are done with the
 * given integer value. This does not implement {@link NavigableSet} but has
 * many methods similar to those from that interface. This implementation is not
 * thread-safe.
 * 
 * <p>
 * Note that Java 10 might introduce value types. In that case this should be
 * converted to sucha value type that conists only of one integer (value).
 * 
 * @see SmallSet#toSet(int)
 * 
 * @author Claude Martin
 */
public final class ByteSet extends AbstractCollection<Byte> implements Set<Byte> {
  private int value;

  public ByteSet(final int set) {
    this.value = set;
  }

  public boolean remove(byte element) {
    return value != (value = SmallSet.remove(value, element));
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Byte)
      return remove((byte) o);
    return false;
  }

  public boolean add(byte e) {
    return value != (value = SmallSet.add(value, e));
  }

  @Override
  public boolean add(Byte e) {
    return add((byte) e);
  }

  @Override
  public void clear() {
    value = 0;
  }

  public int toSmallSet() {
    return value;
  }

  public boolean contains(final byte v) {
    return SmallSet.contains(this.value, v);
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof Byte)
      return SmallSet.contains(this.value, (byte) o);
    return false;
  }

  @Override
  public boolean isEmpty() {
    return this.value == 0;
  }

  @Override
  public void forEach(final Consumer<? super Byte> action) {
    SmallSet.forEach(this.value, (ByteConsumer) action::accept);
  }
  
  @Override
  public ByteIterator iterator() {
    return new ByteIterator() {
      private int  _set       = ByteSet.this.value;
      private byte _lastValue = -1;

      @Override
      public boolean hasNext() {
        return this._set != 0;
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this._set == 0)
          throw new NoSuchElementException();
        byte next = SmallSet.next(this._set);
        this._set = SmallSet.remove(this._set, next);
        return _lastValue = next;
      }

      @Override
      public void remove() {
        if (_lastValue == -1)
          throw new IllegalStateException();
        ByteSet.this.value = SmallSet.remove(value, _lastValue);
        _lastValue = -1;
      }
    };

  }

  @Override
  public int size() {
    return SmallSet.size(this.value);
  }

  /**
   * Returns the hash code value for this set.
   * 
   * @see Set#hashCode()
   */
  @Override
  public int hashCode() {
    return SmallSet.hashCode(value);
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
      return this.value == ((ByteSet) o).value;
    if (o instanceof Set) {
      try {
        final Set<Byte> set = ((Set<Byte>) o);
        int other = SmallSet.empty();
        if (this.size() != set.size())
          return false;
        for (Byte b : set)
          other = SmallSet.add(other, b);
        return other == this.value;
      } catch (ClassCastException | IllegalArgumentException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public ByteSet clone() {
    return new ByteSet(value);
  }

  public Comparator<? super Byte> comparator() {
    return Byte::compare;
  }

  public OptionalByte first() {
    return SmallSet.min(this.value);
  }

  public OptionalByte last() {
    return SmallSet.max(this.value);
  }

  public OptionalByte lower(final Byte e) {
    return SmallSet.lower(value, e);
  }

  public OptionalByte floor(final Byte e) {
    return SmallSet.floor(value, e);
  }

  public OptionalByte ceiling(final Byte e) {
    return SmallSet.ceiling(value, e);
  }

  public OptionalByte higher(final Byte e) {
    return SmallSet.higher(value, e);
  }

  public OptionalByte pollFirst() {
    if (value == 0)
      return OptionalByte.empty();
    final byte first = SmallSet.next(value);
    value = SmallSet.remove(value, first);
    return OptionalByte.of(first);
  }

  public OptionalByte pollLast() {
    if (value == 0)
      return OptionalByte.empty();
    final byte last = (byte) (31 - Integer.numberOfLeadingZeros(value));
    value = SmallSet.remove(value, last);
    return OptionalByte.of(last);
  }

  @Override
  public Byte[] toArray() {
    int set = value;
    final int size = SmallSet.size(set);
    final Byte[] result = new Byte[size];
    int i = 0;
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result[i++] = value;
      set >>>= 1;
    }
    return result;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return super.toArray(a); // Implemented in AbstractCollection.
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object e : c)
      if (!contains(e))
        return false;
    return true;
  }

  public boolean addAll(byte... c) {
    boolean modified = false;
    for (byte e : c)
      if (add(e))
        modified = true;
    return modified;
  }

  @Override
  public boolean addAll(Collection<? extends Byte> c) {
    boolean modified = false;
    for (Byte e : c)
      if (add(e))
        modified = true;
    return modified;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
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
    Objects.requireNonNull(c);
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
