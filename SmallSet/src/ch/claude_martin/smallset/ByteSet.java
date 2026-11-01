package ch.claude_martin.smallset;

import java.io.Serializable;
import java.util.NavigableSet;

/// A mutable, navigable view of a {@link SmallSet}. Basic operations are done with the given {@link SmallSet} value.
/// This implements {@link NavigableSet}. This implementation is not thread-safe. This data structure can contains the
/// values 0 (inclusive) to 31 (exclusive), but this set might be a subset with a smaller range.
///
/// {@snippet file = "Snippets.java" region = "interfaceByteSet"}
///
/// @see SmallSet#toSet()
///
/// @author Claude Martin
///
public interface ByteSet extends NavigableSet<Byte>, Cloneable, Serializable {

  /// Removes the given byte value. This does nothing and returns false if the given value is out of range,
  /// or if the given element is not a byte. Make sure you cast integers to byte, if you want to remove its byte value.
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
  /// 
  /// {@snippet file = "Snippets.java" region = "range"}
  public SmallSet range();

  /// @see #first()
  public OptionalByte firstAsOptionalByte();

  /// @see #last()
  public OptionalByte lastAsOptionalByte();

  /// @see #lower(Object)
  public OptionalByte lowerAsOptionalByte(final Byte e);

  /// @see #floor(Object)
  public OptionalByte floorAsOptionalByte(final Byte e);

  /// @see #ceiling(Object)
  public OptionalByte ceilingAsOptionalByte(final Byte e);

  /// @see #higher(Object)
  public OptionalByte higherAsOptionalByte(final Byte e);

  /// @see #pollFirst()
  public OptionalByte pollFirstAsOptionalByte();

  /// @see #pollLast()
  public OptionalByte pollLastAsOptionalByte();

  /// Adds multiple values.
  ///
  /// @see #addAll(java.util.Collection)
  public boolean addAll(byte... c);

  @Override
  public ByteIterator iterator();

  @Override
  public default ByteIterator descendingIterator()  {
    return this.descendingSet().iterator();
  }

  @Override
  public default ByteSet reversed() {
    return this.descendingSet();
  }

  @Override
  ByteSet descendingSet();

  public ByteSet clone();

  @Override
  public default ByteSet subSet(Byte fromElement, boolean fromInclusive, Byte toElement, boolean toInclusive) {
    return this.subSet((byte) fromElement, fromInclusive, (byte) toElement, toInclusive);
  }
  
  public ByteSet subSet(byte fromElement, boolean fromInclusive, byte toElement, boolean toInclusive);

  @Override
  public default ByteSet subSet(Byte fromElement, Byte toElement) {
    return this.subSet((byte) fromElement, (byte) toElement);
  }
  
  /// @see #subSet(Byte, Byte)
  public default ByteSet subSet(byte fromElement, byte toElement)  {
    return this.subSet(fromElement, true, toElement, false);
  }

  @Override
  public default ByteSet headSet(Byte toElement, boolean inclusive)  {
    return this.headSet((byte) toElement, inclusive);
  }

  /// @see #headSet(Byte, boolean)
  public ByteSet headSet(byte toElement, boolean inclusive);
  
  @Override
  public default ByteSet headSet(Byte toElement)  {
    return this.headSet((byte) toElement, false);
  };
  
  /// @see #headSet(Byte)
  public default ByteSet headSet(byte toElement) {
    return this.headSet((byte) toElement, false);
  };
  
  @Override
  public default ByteSet tailSet(Byte fromElement, boolean inclusive)  {
    return this.tailSet((byte) fromElement, inclusive);
  }
  
  /// @see #tailSet(Byte, boolean)
  public ByteSet tailSet(byte fromElement, boolean inclusive);

  @Override
  public default ByteSet tailSet(Byte fromElement) {
    return this.tailSet((byte) fromElement, true);
  }
  
  /// @see #tailSet(Byte)
  public default ByteSet tailSet(byte fromElement) {
    return this.tailSet(fromElement, true);
  };

}
