package ch.claude_martin.smallset;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

abstract sealed class AbstractByteSet extends AbstractCollection<Byte> implements ByteSet
    permits BasicByteSet, SubByteSet, ReversedByteSet {

  static value record Range(byte low, boolean lowInclusive, byte high, boolean highInclusive) {

    public Range(final Byte low, final Byte high) {
      this(low, true, high, false);
    }

    SmallSet toSmallSet() {
      final boolean naturalOrder = (this.low <= this.high);
      if (naturalOrder) {
        return SmallSet.ofRange(this.low, this.lowInclusive, this.high, this.highInclusive);
      } else {
        return SmallSet.ofRange(this.high, this.highInclusive, this.low, this.lowInclusive);
      }
    }

    private byte natHigh() {
      return (this.low > this.high) ? this.low : this.high;
    }

    private boolean natHighInclusive() {
      return (this.low > this.high) ? this.lowInclusive : this.highInclusive;
    }

    private byte natLow() {
      return (this.low <= this.high) ? this.low : this.high;
    }

    private boolean natLowInclusive() {
      return (this.low <= this.high) ? this.lowInclusive : this.highInclusive;
    }

    boolean containsAll(final Range range) {
      if (this.low == this.high && (!this.lowInclusive || !this.highInclusive)) {
        return false;
      }
      final byte low = this.natLow();
      final byte rangeLow = range.natLow();

      if (low > rangeLow
          || low == rangeLow && range.natLowInclusive() && !this.natLowInclusive()) {
        return false;
      }

      final byte high = this.natHigh();
      final byte rangeHigh = range.natHigh();

      return high > rangeHigh
          || high == rangeHigh && (!range.natHighInclusive() || this.natHighInclusive());
    }

    public boolean isEmpty() {
      // While a range like new Range(1,false,2,false) is practically empty,
      // it is not treated as such.
      return this.low == this.high && (!this.lowInclusive || !this.highInclusive);
    }

    boolean tooLow(final byte key) {
      final int c = Byte.compare(key, this.natLow());
      return c < 0 || (c == 0 && !this.natLowInclusive()) ? true : false;
    }

    boolean tooHigh(final byte key) {
      final int c = Byte.compare(key, this.natHigh());
      return c > 0 || (c == 0 && !this.natHighInclusive());
    }

    boolean inRange(final byte key) {
      return !this.tooLow(key) && !this.tooHigh(key);
    }

    boolean inClosedRange(final byte key) {
      return (Byte.compare(key, this.natLow()) >= 0)
          && (Byte.compare(this.natHigh(), key) >= 0);
    }

    boolean inRange(final byte key, final boolean inclusive) {
      return inclusive ? this.inRange(key) : this.inClosedRange(key);
    }

    Range reverse() {
      return new Range(this.high, this.highInclusive, this.low, this.lowInclusive);
    }
  }

  static final Comparator<? super Byte> NATURAL_ORDER = null;
  static final Comparator<? super Byte> REVERSED      = Comparator.<Byte> naturalOrder().reversed();

  @Override
  public abstract int size();

  @Override
  public abstract Comparator<? super Byte> comparator();

  protected boolean isNaturalOrder() {
    return this.comparator() == AbstractByteSet.NATURAL_ORDER;
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

  /// Compares the specified object with this set for equality. The two sets can only be equal if both contain the same
  /// Byte objects.
  ///
  /// @see Set#equals(Object) *
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof final Set<?> other) {
      if (other.size() != this.size()) {
        return false;
      }
      try {
        return this.containsAll(other);
      } catch (ClassCastException | NullPointerException unused) {
        return false;
      }
    }
    return false;
  }

  abstract Range range();

  @Override
  public abstract Byte first() throws NoSuchElementException;

  @Override
  public abstract Byte last() throws NoSuchElementException;

  @Override
  public abstract OptionalByte firstAsOptionalByte();

  @Override
  public abstract OptionalByte lastAsOptionalByte();

  @Override
  public Byte pollFirst() {
    final var first = this.firstAsOptionalByte();
    if (first.isPresent()) {
      final byte asByte = first.getAsByte();
      this.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public OptionalByte pollFirstAsOptionalByte() {
    return OptionalByte.ofNullable(this.pollFirst());
  }

  @Override
  public Byte pollLast() {
    final var first = this.lastAsOptionalByte();
    if (first.isPresent()) {
      final byte asByte = first.getAsByte();
      this.remove(asByte);
      return asByte;
    }
    return null;
  }

  @Override
  public OptionalByte pollLastAsOptionalByte() {
    return OptionalByte.ofNullable(this.pollLast());
  }

  @Override
  public Byte lower(final Byte e) {
    return this.lowerAsOptionalByte(e).orNull();
  }

  @Override
  public Byte higher(final Byte e) {
    return this.higherAsOptionalByte(e).orNull();
  }

  @Override
  public Byte floor(final Byte e) {
    return this.floorAsOptionalByte(e).orNull();
  }

  @Override
  public Byte ceiling(final Byte e) {
    return this.ceilingAsOptionalByte(e).orNull();
  }

  @Override
  public OptionalByte lowerAsOptionalByte(final Byte e) {
    if (this.isNaturalOrder()) {
      return this.toSmallSet().lower(e);
    }
    return this.toSmallSet().higher(e);
  }

  @Override
  public OptionalByte higherAsOptionalByte(final Byte e) {
    if (this.isNaturalOrder()) {
      return this.toSmallSet().higher(e);
    }
    return this.toSmallSet().lower(e);
  }

  @Override
  public OptionalByte floorAsOptionalByte(final Byte e) {
    if (this.isNaturalOrder()) {
      return this.toSmallSet().floor(e);
    }
    return this.toSmallSet().ceiling(e);
  }

  @Override
  public OptionalByte ceilingAsOptionalByte(final Byte e) {
    if (this.isNaturalOrder()) {
      return this.toSmallSet().ceiling(e);
    }
    return this.toSmallSet().floor(e);
  }

  @Override
  public Byte[] toArray() {
    final int size = this.size();
    final Byte[] result = new Byte[size];
    int i = 0;
    for (final Byte n : this) {
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
  public abstract ByteIterator iterator();

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
  public Spliterator<Byte> spliterator() {
    // Technically, a reversed set is still sorted, but using a specific spliterator is not worth it.
    return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
        | Spliterator.DISTINCT | (this.isNaturalOrder() ? Spliterator.SORTED : 0));
  }

  @Override
  public ByteSet subSet(final Byte fromElement, final boolean fromInclusive, final Byte toElement,
      final boolean toInclusive) {
    return SubByteSet.of(this, new Range(fromElement, fromInclusive, toElement, toInclusive));

  }

  @Override
  public ByteSet subSet(final Byte fromElement, final Byte toElement) {
    // In reversed order, fromElement must be greater than toElement.
    return SubByteSet.of(this, new Range(fromElement, toElement));
  }

  @Override
  public ByteSet headSet(final Byte toElement, final boolean inclusive) {
    final Range range = this.range();
    if (range.isEmpty()) {
      throw new IllegalArgumentException("toElement out of range");
    }
    return this.subSet(range.low(), range.lowInclusive(), toElement, inclusive);
  }

  @Override
  public ByteSet headSet(final Byte toElement) {
    return this.headSet(toElement, false);
  }

  @Override
  public ByteSet tailSet(final Byte fromElement, final boolean inclusive) {
    final Range range = this.range();
    if (range.isEmpty()) {
      throw new IllegalArgumentException("fromElement out of range");
    }
    return this.subSet(fromElement, inclusive, range.high(), range.highInclusive());
  }

  @Override
  public ByteSet tailSet(final Byte fromElement) {
    return this.tailSet(fromElement, true);
  }

  @Override
  public abstract ByteSet clone();

  public abstract String debug();
}
