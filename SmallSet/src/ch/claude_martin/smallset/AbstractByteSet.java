package ch.claude_martin.smallset;

import java.util.*;

abstract class AbstractByteSet extends AbstractCollection<Byte> implements ByteSet {

  @Override
  public String toString() {
    return this.toSmallSet().toString();
  }

  @Override
  public int size() {
    return this.toSmallSet().size();
  }

  @Override
  public int hashCode() {
    int h = 0;
    int value = this.toSmallSet().value;
    for (byte n; value != 0; value &= ~(1 << n)) {
      h += Byte.hashCode(n = (byte) Integer.numberOfTrailingZeros(value));
    }
    return h;
  }

  /** Compares the specified object with this set for equality. The two sets can only be equal if both contain the same
   * Byte objects.
   *
   * @see Set#equals(Object) */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof final Set<?> other) {
      if (this.size() != other.size()) {
        return false;
      }
      for (final Object e : other) {
        if (e instanceof final byte b && this.contains(b)) {
          continue;
        } else {
          return false;
        }
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
    if (this.isEmpty()) {
      return null;
    }
    return (byte) Integer.numberOfTrailingZeros(this.toSmallSet().value);
  }

  @Override
  public Byte last() {
    if (this.isEmpty()) {
      return null;
    }
    return (byte) (31 - Integer.numberOfLeadingZeros(this.toSmallSet().value));
  }

  @Override
  public Byte lower(final Byte e) {
    return this.toSmallSet().lower(e).orNull();
  }

  @Override
  public Byte floor(final Byte e) {
    return this.toSmallSet().floor(e).orNull();
  }

  @Override
  public Byte ceiling(final Byte e) {
    return this.toSmallSet().ceiling(e).orNull();
  }

  @Override
  public Byte higher(final Byte e) {
    return this.toSmallSet().higher(e).orNull();
  }

  @Override
  public OptionalByte firstAsOptionalByte() {
    return this.toSmallSet().min();
  }

  @Override
  public OptionalByte lastAsOptionalByte() {
    return this.toSmallSet().max();
  }

  @Override
  public OptionalByte lowerAsOptionalByte(final Byte e) {
    return this.toSmallSet().lower(e);
  }

  @Override
  public OptionalByte floorAsOptionalByte(final Byte e) {
    return this.toSmallSet().floor(e);
  }

  @Override
  public OptionalByte ceilingAsOptionalByte(final Byte e) {
    return this.toSmallSet().ceiling(e);
  }

  @Override
  public OptionalByte higherAsOptionalByte(final Byte e) {
    return this.toSmallSet().higher(e);
  }

  @Override
  public OptionalByte pollFirstAsOptionalByte() {
    return OptionalByte.ofNullable(this.pollFirst());
  }

  @Override
  public OptionalByte pollLastAsOptionalByte() {
    return OptionalByte.ofNullable(this.pollLast());
  }

  @Override
  public Byte[] toArray() {
    final SmallSet set = this.toSmallSet();
    final int size = set.size();
    final Byte[] result = new Byte[size];
    int i = 0;
    for (final Byte n : set) {
      result[i++] = n;
    }
    return result;
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return super.toArray(a);
  }

  @Override
  public boolean contains(final byte b) {
    return this.toSmallSet().contains(b);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    Objects.requireNonNull(c, "c");
    for (final Object e : c) {
      if (!this.contains(e)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(final byte... c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    for (final byte e : c) {
      if (this.add(e)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean addAll(final Collection<? extends Byte> c) {
    Objects.requireNonNull(c, "c");
    boolean modified = false;
    for (final Byte e : c) {
      if (this.add(e)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
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
  public boolean removeAll(final Collection<?> c) {
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

  @Override
  public ByteSet subSet(final Byte fromElement, final boolean fromInclusive, final Byte toElement,
      final boolean toInclusive) {
    final byte a = (byte) (fromInclusive ? fromElement : fromElement + 1);
    final byte b = (byte) (toInclusive ? toElement + 1 : toElement);
    return this.abstractSubSet(a, b);
  }

  @Override
  public ByteSet subSet(final Byte fromElement, final Byte toElement) {
    if (fromElement > toElement) {
      throw new IllegalArgumentException("fromElement > toElement");
    }
    if (fromElement < 0) {
      throw new IllegalArgumentException("fromElement < 0");
    }
    if (toElement > Integer.SIZE) {
      throw new IllegalArgumentException("toElement > " + Integer.SIZE);
    }
    return this.abstractSubSet(fromElement, toElement);
  }

  protected abstract ByteSet abstractSubSet(byte fromElement, byte toElement);

  @Override
  public ByteSet headSet(final Byte toElement, final boolean inclusive) {
    return this.subSet((byte) 0, true, toElement, inclusive);
  }

  @Override
  public ByteSet tailSet(final Byte fromElement, final boolean inclusive) {
    return this.subSet(fromElement, inclusive, (byte) 32, false);
  }

  @Override
  public ByteSet headSet(final Byte toElement) {
    return this.subSet((byte) 0, true, toElement, false);
  }

  @Override
  public ByteSet tailSet(final Byte fromElement) {
    return this.subSet(fromElement, true, (byte) 32, false);
  }

  @Override
  public abstract ByteSet clone();
}
