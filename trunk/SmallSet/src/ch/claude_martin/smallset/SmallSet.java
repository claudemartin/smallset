package ch.claude_martin.smallset;

import static java.util.Objects.requireNonNull;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;

/**
 * Utility methods for sets of small integers (bytes in the range of 0 to 31), represented as bit
 * fields (int).
 * <p>
 * It is recommended to import all methods with static imports so they can be used on primitive int
 * values.
 * 
 * @author Claude Martin
 *
 */
public final class SmallSet {

  private SmallSet() {
  }

  private static int checkRange(int i) {
    if (i > 31)
      throw new IllegalArgumentException("i>31");
    if (i < 0)
      throw new IllegalArgumentException("i<0");
    return i;
  }

  /**
   * Creates a SmallSet from a collection or iterable.
   * 
   * @param values
   *          Sequence of numbers
   */
  public static int of(final Iterable<? extends Number> values) {
    int set = 0;
    for (final Number n : values)
      set |= (1 << checkRange(n.byteValue()));
    return set;
  }

  /**
   * Creates a SmallSet from bytes.
   * 
   * @param i
   *          Sequence of bytes.
   */
  public static int of(final byte... i) {
    int set = 0;
    for (final int integer : i)
      set |= (1 << checkRange(integer));
    return set;
  }

  /**
   * Creates a SmallSet from integers.
   * 
   * @param i
   *          Sequence of integers.
   */
  public static int of(final int... i) {
    int set = 0;
    for (final int integer : i)
      set |= (1 << checkRange(integer));
    return set;
  }

  /**
   * Creates set of enum values.
   */
  @SafeVarargs
  public static <E extends Enum<E>> int of(final E... enums) {
    int set = 0;
    for (final Enum<?> e : enums)
      set |= (1 << checkRange(e.ordinal()));
    return set;
  }

  /**
   * Creates set of enum values, using the ordinal of each element.
   */
  public static int of(EnumSet<?> enumset) {
    int set = 0;
    for (final Enum<?> e : enumset)
      set |= (1 << checkRange(e.ordinal()));
    return set;
  }

  /** Set with just one single value. */
  public static int singleton(final int val) {
    return 1 << checkRange(val);
  }

  /** Set with just one single value. */
  public static int singleton(final byte val) {
    return 1 << checkRange(val);
  }

  /** Empty set. */
  public static int empty() {
    return 0;
  }

  /**
   * Tests if element is in the given set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public static boolean contains(final int set, final byte element) {
    return (set & (1 << checkRange(element))) != 0;
  }

  /**
   * Adds an element to the set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>set ∪ {element}</code>
   */
  public static int add(final int set, final byte element) {
    return set | (1 << checkRange(element));
  }

  /**
   * Removes an element from a set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>set \ {element}</code>
   */
  public static int remove(final int set, final byte element) {
    return set & ~(1 << checkRange(element));
  }

  /** Union of two sets. */
  public static int union(final int a, final int b) {
    return a | b;
  }

  /** Intersection of two sets. */
  public static int intersect(final int a, final int b) {
    return a & b;
  }

  /** Bytes of b removed from a: a \ b = intersect(a , complement(b)). */
  public static int minus(final int a, final int b) {
    return a & ~b;
  }

  /** Complement of a set. The domain is [0,1,..,31]. */
  public static int complement(final int a) {
    return ~a;
  }

  /**
   * Creates an {@link Iterable} that can be used in an extended for-loop (<code>foreach</code>).
   * 
   * @see #iterator(int)
   */
  public static Iterable<Byte> iterate(final int set) {
    return new Iterable<Byte>() {
      @Override
      public Iterator<Byte> iterator() {
        return SmallSet.iterator(set);
      }
    };
  }

  /**
   * Performs the given action for each byte until all bytes have been processed or the action
   * throws an exception. Actions are performed in the order of iteration (ascending). Exceptions
   * thrown by the action are relayed to the caller.
   */
  public static void forEach(final int set, final ByteConsumer consumer) {
    requireNonNull(consumer);
    final ByteIterator itr = iterator(set);
    while (itr.hasNext())
      consumer.accept(itr.nextByte());
  }

  /**
   * Creates an iterator for a given set.
   * 
   * @see #iterate(int)
   */
  public static ByteIterator iterator(final int set) {
    return new ByteIterator() {
      private int _set = set;
      private byte next = 0;
      {
        while (this._set != 0 && (this._set & 1) == 0) {
          this._set >>>= 1;
          this.next++;
        }
      }

      @Override
      public boolean hasNext() {
        return this._set != 0;
      }

      @Override
      public byte nextByte() {
        final byte result = this.next;
        do {
          this._set >>>= 1;
          this.next++;
        } while (this._set != 0 && (this._set & 1) == 0);
        return result;
      }
    };
  }

  /**
   * Creates an {@link IntStream} for the given set. Note that the stream isn't lazy, but contains
   * no more than 32 integers.
   */
  public static IntStream stream(final int set) {
    final ByteIterator itr = iterator(set);
    final Builder builder = IntStream.builder();
    while (itr.hasNext())
      builder.add(itr.nextByte());
    return builder.build();
  }

  /** Returns the number of bytes in this set (its cardinality). */
  public static int size(final int set) {
    return Integer.bitCount(set);
  }

  /** True, if empty. */
  public static boolean isEmpty(final int set) {
    return set == 0;
  }

  /**
   * Range of bytes.
   * 
   * @param a
   *          First element (inclusive)
   * @param z
   *          Last element (exclusive)
   * @return <code>SmallSet.of(j, ... , k-1)</code>
   */
  public static int ofRange(final int a, final int z) {
    if (checkRange(a) > z)
      throw new IllegalArgumentException("z<a");
    checkRange(z - 1);
    int i = 0;
    for (int x = a; x < z; x++)
      i += 1 << x;
    return i;
  }

  /**
   * Closed Range of integers.
   * 
   * @param a
   *          First element (inclusive)
   * @param z
   *          Last element (inclusive)
   * @return <code>SmallSet.of(j, ... , k)</code>
   */
  public static int ofRangeClosed(final int a, final int z) {
    if (checkRange(a) > checkRange(z))
      throw new IllegalArgumentException("z<a");
    int i = 0;
    for (int x = a; x <= checkRange(z); x++)
      i += 1 << x;
    return i;
  }

  /** String representation of the given set. */
  public static String toString(final int set) {
    if (set == 0)
      return "()";
    final StringJoiner sj = new StringJoiner(",", "(", ")");
    for (final ByteIterator itr = iterator(set); itr.hasNext();)
      sj.add(Integer.toString(itr.nextByte()));
    return sj.toString();
  }

  /** {@link TreeSet} of the given set. */
  public static TreeSet<Byte> toSet(final int set) {
    final TreeSet<Byte> result = new TreeSet<>();
    for (final ByteIterator itr = iterator(set); itr.hasNext();)
      result.add(itr.nextByte());
    return result;
  }

  /** {@link BitSet} of the given set. */
  public static BitSet toBitSet(final int set) {
    final BitSet result = new BitSet(32);
    for (final ByteIterator itr = iterator(set); itr.hasNext();)
      result.set(itr.next());
    return result;
  }

  /** {@link EnumSet} of the given set. */
  public static <E extends Enum<E>> EnumSet<E> toEnumSet(final int set, final Class<E> type) {
    requireNonNull(type);
    final EnumSet<E> result = EnumSet.noneOf(type);
    final E[] constants = type.getEnumConstants();
    for (final ByteIterator itr = iterator(set); itr.hasNext();)
      result.add(constants[itr.nextByte()]);
    return result;
  }

  /** Return any of the bytes. */
  public static byte random(final int set, final Random rng) {
    requireNonNull(rng, "nrg");
    if (set == 0)
      throw new IllegalStateException("SmallSet is empty!");
    int r = rng.nextInt(size(set));
    int c = set;
    byte result = -1;
    while (true) {
      result++;
      if ((c & 1) != 0) {
        if (r == 0)
          return result;
        r--;
      }
      c >>>= 1;
    }
  }
}
