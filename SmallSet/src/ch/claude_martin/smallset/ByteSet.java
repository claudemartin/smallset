package ch.claude_martin.smallset;

import java.io.Serializable;
import java.util.NavigableSet;

/// A mutable, navigable view of a {@link SmallSet}. Basic operations are done with the given {@link SmallSet} value.
/// This implements {@link NavigableSet}. This implementation is not thread-safe. This data structure can contains the
/// values 0 (inclusive) to 31 (exclusive), but this set might be a subset with a smaller range.
///
/// {@snippet file = "Snippets.java" region = "classBasicByteSet"}
///
/// @see SmallSet#toSet()
///
/// @author Claude Martin
///
public interface ByteSet extends NavigableSet<Byte>, Cloneable, Serializable {

  /// Removes the given byte value. This does nothing and returns false if the given value is out of range.
  public boolean remove(byte element);

  /// {@inheritDoc}
  ///
  /// Be careful that you don't call this with an {@link Integer} by mistake, as this set can't contain an object of
  /// that type.
  @Override
  public boolean remove(Object o);

  /// Adds the given byte value.
  public boolean add(byte element) throws IllegalArgumentException;

  /// This set as a {@link SmallSet}.
  public SmallSet toSmallSet();

  /// True, if the given byte value is in this set.
  public boolean contains(final byte v);

  /// {@inheritDoc}
  ///
  /// Be careful that you don't call this with an {@link Integer} by mistake, as this set can't contain an object of
  /// that type.
  @Override
  public boolean contains(Object o);

  /// The allowed range if this set. Only elements of this range can be added to this set. Note that the returned
  /// {@link SmallSet} is always in natural order, while this set might be reversed.
  public SmallSet range();

  public OptionalByte firstAsOptionalByte();

  public OptionalByte lastAsOptionalByte();

  public OptionalByte lowerAsOptionalByte(final Byte e);

  public OptionalByte floorAsOptionalByte(final Byte e);

  public OptionalByte ceilingAsOptionalByte(final Byte e);

  public OptionalByte higherAsOptionalByte(final Byte e);

  public OptionalByte pollFirstAsOptionalByte();

  public OptionalByte pollLastAsOptionalByte();

  /// Adds multiple values.
  ///
  /// @see #addAll(java.util.Collection)
  public boolean addAll(byte... c);

  @Override
  public ByteIterator iterator();

  @Override
  public ByteIterator descendingIterator();

  @Override
  public default ByteSet reversed() {
    return this.descendingSet();
  }

  @Override
  ByteSet descendingSet();

  public ByteSet clone();

  @Override
  public ByteSet subSet(Byte fromElement, boolean fromInclusive, Byte toElement, boolean toInclusive);

  @Override
  public ByteSet subSet(Byte fromElement, Byte toElement);

  @Override
  public ByteSet headSet(Byte toElement, boolean inclusive);

  @Override
  public ByteSet tailSet(Byte fromElement, boolean inclusive);

  @Override
  public ByteSet headSet(Byte toElement);

  @Override
  public ByteSet tailSet(Byte fromElement);

}
