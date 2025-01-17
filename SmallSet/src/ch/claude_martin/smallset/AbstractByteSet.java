package ch.claude_martin.smallset;

import java.util.*;

abstract class AbstractByteSet extends AbstractCollection<Byte> implements ByteSet {

  @Override
  public String toString() {
    return this.toSmallSet().toString();
  }

  @Override
  public int size() {
    return toSmallSet().size();
  }

  @Override
  public int hashCode() {
    int h = 0;
    int value = this.toSmallSet().value;
    for (byte n; value != 0; value &= ~(1 << n))
      h += Byte.hashCode(n = (byte) Integer.numberOfTrailingZeros(value));
    return h;
  }

  /** Compares the specified object with this set for equality. The two sets can only be equal if both contain the same
   * Byte objects.
   * 
   * @see Set#equals(Object) */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Set<?> other) {
      if (this.size() != other.size())
        return false;
      for (Object e : other) {
        if (e instanceof byte b && this.contains(b))
          continue;
        else
          return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public Comparator<? super Byte> comparator() {
    return Byte::compare;
  }

  @Override
  public Byte first() {
    if (this.isEmpty())
      return null;
    return (byte) Integer.numberOfTrailingZeros(this.toSmallSet().value);
  }

  @Override
  public Byte last() {
    if (this.isEmpty())
      return null;
    return (byte) (31 - Integer.numberOfLeadingZeros(this.toSmallSet().value));
  }

  @Override
  public Byte lower(Byte e) {
    return toSmallSet().lower(e).orNull();
  }

  @Override
  public Byte floor(Byte e) {
    return toSmallSet().floor(e).orNull();
  }

  @Override
  public Byte ceiling(Byte e) {
    return toSmallSet().ceiling(e).orNull();
  }

  @Override
  public Byte higher(Byte e) {
    return toSmallSet().higher(e).orNull();
  }

  public OptionalByte firstAsOptionalByte() {
    return this.toSmallSet().min();
  }

  public OptionalByte lastAsOptionalByte() {
    return this.toSmallSet().max();
  }

  public OptionalByte lowerAsOptionalByte(final Byte e) {
    return this.toSmallSet().lower(e);
  }

  public OptionalByte floorAsOptionalByte(final Byte e) {
    return this.toSmallSet().floor(e);
  }

  public OptionalByte ceilingAsOptionalByte(final Byte e) {
    return this.toSmallSet().ceiling(e);
  }

  public OptionalByte higherAsOptionalByte(final Byte e) {
    return this.toSmallSet().higher(e);
  }

  public OptionalByte pollFirstAsOptionalByte() {
    return OptionalByte.ofNullable(pollFirst());
  }

  public OptionalByte pollLastAsOptionalByte() {
    return OptionalByte.ofNullable(pollLast());
  }

  @Override
  public Byte[] toArray() {
    final SmallSet set = this.toSmallSet();
    final int size = set.size();
    final Byte[] result = new Byte[size];
    int i = 0;
    for (Byte n : set) {
      result[i++] = n;
    }
    return result;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return super.toArray(a);
  }

  @Override
  public boolean contains(byte b) {
    return toSmallSet().contains(b);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    Objects.requireNonNull(c, "c");
    for (Object e : c)
      if (!this.contains(e))
        return false;
    return true;
  }

  @Override
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

  public ByteSet subSet(Byte fromElement, boolean fromInclusive, Byte toElement, boolean toInclusive) {
    final byte a = (byte) (fromInclusive ? fromElement : fromElement + 1);
    final byte b = (byte) (toInclusive ? toElement + 1 : toElement);
    return this.abstractSubSet(a, b);
  }

  @Override
  public ByteSet subSet(Byte fromElement, Byte toElement) {
    if (fromElement > toElement)
      throw new IllegalArgumentException("fromElement > toElement");
    if (fromElement < 0)
      throw new IllegalArgumentException("fromElement < 0");
    if (toElement > Integer.SIZE)
      throw new IllegalArgumentException("toElement > " + Integer.SIZE);
    return this.abstractSubSet(fromElement, toElement);
  }

  protected abstract ByteSet abstractSubSet(byte fromElement, byte toElement);

  @Override
  public ByteSet headSet(Byte toElement, boolean inclusive) {
    return this.subSet((byte) 0, true, toElement, inclusive);
  }

  @Override
  public ByteSet tailSet(Byte fromElement, boolean inclusive) {
    return this.subSet(fromElement, inclusive, (byte) 32, false);
  }

  @Override
  public ByteSet headSet(Byte toElement) {
    return this.subSet((byte) 0, true, toElement, false);
  }

  @Override
  public ByteSet tailSet(Byte fromElement) {
    return this.subSet(fromElement, true, (byte) 32, false);
  }

  @Override
  public abstract ByteSet clone();
}
