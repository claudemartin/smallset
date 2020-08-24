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
  private SmallSet value; // TODO : rename

  public ByteSet(final SmallSet set) {
    this.value = set;
  }

  public boolean remove(byte element) {
    return value != (value = value.remove(element));
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Byte)
      return remove((byte) o);
    return false;
  }

  public boolean add(byte e) {
    return value != (value = value.add(e));
  }

  @Override
  public boolean add(Byte e) {
    return add((byte) e);
  }

  @Override
  public void clear() {
    value = SmallSet.empty();
  }

  public SmallSet toSmallSet() {
    return this.value;
  }

  public boolean contains(final byte v) {
    return this.value.contains(v);
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof Byte)
      return this.value.contains((byte) o);
    return false;
  }

  @Override
  public boolean isEmpty() {
    return this.value.isEmpty();
  }

  @Override
  public void forEach(final Consumer<? super Byte> action) {
    this.value.forEach((ByteConsumer) action::accept);
  }
  
  @Override
  public ByteIterator iterator() {
    return new ByteIterator() {
      private SmallSet  _set       = ByteSet.this.value;
      private byte      _lastValue = -1;

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
        ByteSet.this.value = ByteSet.this.value.remove(_lastValue);
        _lastValue = -1;
      }
    };

  }

  @Override
  public int size() {
    return this.value.size();
  }

  /**
   * Returns the hash code value for this set.
   * 
   * @see Set#hashCode()
   */
  @Override
  public int hashCode() {
    return this.value.hashCode();
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
        SmallSet other = SmallSet.empty();
        if (this.size() != set.size())
          return false;
        for (Byte b : set)
          other = other.add(b);
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
    return this.value.min();
  }

  public OptionalByte last() {
    return this.value.max();
  }

  public OptionalByte lower(final Byte e) {
    return value.lower(e);
  }

  public OptionalByte floor(final Byte e) {
    return value.floor(e);
  }

  public OptionalByte ceiling(final Byte e) {
    return value.ceiling(e);
  }

  public OptionalByte higher(final Byte e) {
    return value.higher(e);
  }

  public OptionalByte pollFirst() {
    if (value.isEmpty())
      return OptionalByte.empty();
    final byte first = (byte) Integer.numberOfTrailingZeros(this.value.value);
    value = value.remove(first);
    return OptionalByte.of(first);
  }

  public OptionalByte pollLast() {
    if (value.isEmpty())
      return OptionalByte.empty();
    final byte last = (byte) (31 - Integer.numberOfLeadingZeros(this.value.value));
    value = value.remove(last);
    return OptionalByte.of(last);
  }

  @Override
  public Byte[] toArray() {
    final int size = this.value.size();
    final Byte[] result = new Byte[size];
    int i = 0;
    for (Byte n : this.value) {
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
