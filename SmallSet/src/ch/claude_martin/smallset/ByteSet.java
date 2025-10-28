package ch.claude_martin.smallset;

import java.util.NavigableSet;

/** A mutable, navigable view of a {@link SmallSet}. Basic operations are done with the given {@link SmallSet} value.
 * This implements {@link NavigableSet}. This implementation is not thread-safe.
 *
 * @see SmallSet#toSet()
 *
 * @author Claude Martin */
public interface ByteSet extends NavigableSet<Byte>, Cloneable {

  /** Removes the given byte value. */
  public boolean remove(byte element);

  /** {@inheritDoc}
   *
   * Be careful that you don't call this with an {@link Integer} by mistake, as this set can't contain an object of that
   * type. */
  @Override
  public boolean remove(Object o);

  /** Adds the given byte value. */
  public boolean add(byte element) throws IllegalArgumentException;

  /** This set as a {@link SmallSet}. */
  public SmallSet toSmallSet();

  /** */
  public boolean contains(final byte v);

  /** {@inheritDoc}
   *
   * Be careful that you don't call this with an {@link Integer} by mistake, as this set can't contain an object of that
   * type. */
  @Override
  public boolean contains(Object o);

  public OptionalByte firstAsOptionalByte();

  public OptionalByte lastAsOptionalByte();

  public OptionalByte lowerAsOptionalByte(final Byte e);

  public OptionalByte floorAsOptionalByte(final Byte e);

  public OptionalByte ceilingAsOptionalByte(final Byte e);

  public OptionalByte higherAsOptionalByte(final Byte e);

  public OptionalByte pollFirstAsOptionalByte();

  public OptionalByte pollLastAsOptionalByte();

  public boolean addAll(byte... c);

  @Override
  public ByteIterator iterator();

  @Override
  public ByteIterator descendingIterator();
  
  @Override 
  public ByteSet reversed();

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
