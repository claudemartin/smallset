package ch.claude_martin.smallset;

import static java.lang.Math.getExponent;
import static java.util.Objects.requireNonNull;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Random;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;

/**
 * Utility methods for sets of small integers (bytes in the range of 0 to 31), represented as bit
 * fields (int).
 * <p>
 * It is recommended to import all methods with static imports so they can be used on primitive int
 * values.
 * 
 * <p>
 * Iteration is possible with {@link #iterate(int)} or {@link #iterator(int)}, but it can be done
 * faster. The following code doesn't need to create any new objects and therefore has no overhead.
 * {@link #forEach(int, ByteConsumer)} does the same but has to execute a lambda expression.
 * 
 * <code><pre>
 *   final int set = SmallSet.of(<i>???</i>)
 *   <i>...</i>
 *   // iterate and process all values in set:
 *   for (int itr = set, value = 0; itr != 0; itr >>>= 1) {
 *     if ((itr &amp; 1) != 0)
 *       <i>process</i>(value);
 *     value++;
 *   }
 * </pre></code>
 * 
 * @author Claude Martin
 *
 */
public final class SmallSet {

  private SmallSet() {
  }

  private static int checkRange(int i) {
    if (i > 31)
      throw new IllegalArgumentException("out of range: i>31");
    if (i < 0)
      throw new IllegalArgumentException("out of range: i<0");
    return i;
  }

  private static byte checkRange(byte i) {
    if (i > 31)
      throw new IllegalArgumentException("out of range: i>31");
    if (i < 0)
      throw new IllegalArgumentException("out of range: i<0");
    return i;
  }

  private static byte numberToByte(final Number n) {
    final double d = n.doubleValue();
    if (d > 31d)
      throw new IllegalArgumentException("out of range: i>31");
    if (d < 0d)
      throw new IllegalArgumentException("out of range: i<0");
    return n.byteValue();
  }

  /**
   * Creates a SmallSet from a collection or iterable.
   * 
   * @param values
   *          Sequence of numbers
   */
  public static int of(final Iterable<? extends Number> values) {
    requireNonNull(values, "values");
    int set = 0;
    for (final Number n : values) {
      requireNonNull(n, "values must not contain null");
      set |= (1 << numberToByte(n));
    }
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
    for (final byte b : i)
      set |= (1 << checkRange(b));
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
    requireNonNull(enums, "enums");
    int set = 0;
    for (final Enum<?> e : enums) {
      requireNonNull(enums, "enums must not contain null");
      set |= (1 << checkRange(e.ordinal()));
    }
    return set;
  }

  /**
   * Creates set of enum values, using the ordinal of each element.
   */
  public static int of(EnumSet<?> enumset) {
    requireNonNull(enumset, "enumset");
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
  public static void forEach(int set, final ByteConsumer consumer) {
    requireNonNull(consumer, "consumer");
    if (set == 0)
      return;
    byte value = (byte) Integer.numberOfTrailingZeros(set);
    set >>>= value;
    while (set != 0) {
      if ((set & 1) != 0)
        consumer.accept(value);
      set >>>= 1;
      value++;
    }
  }

  /**
   * Creates an iterator for a given set.
   * 
   * @see #iterate(int)
   */
  public static ByteIterator iterator(final int set) {
    return new ByteIterator() {
      private byte next = (byte) Integer.numberOfTrailingZeros(set);
      private int _set = set >>> this.next;

      @Override
      public boolean hasNext() {
        return this._set != 0;
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this._set == 0)
          throw new NoSuchElementException();
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

  /**
   * Fast implementation of {@link IntStream#collect} to collect values from a stream into a set.
   * <p>
   * This is a terminal operation.
   * 
   * @param stream
   *          An IntStream, e.g. one created by {@link SmallSet#stream(int)}
   * @return An integer representing a set of the values from the stream
   * @throws IllegalArgumentException
   *           if any of the values is out of range
   */
  public static int collect(IntStream stream) throws IllegalArgumentException {
    requireNonNull(stream, "stream");
    final class MutableInt {
      int value = 0;
    }
    return stream.collect(//
        MutableInt::new,//
        (set, b) -> set.value = SmallSet.add(set.value, (byte) checkRange(b)), //
        (a, b) -> a.value |= b.value).value;
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
  public static String toString(int set) {
    if (set == 0)
      return "()";
    final StringJoiner sj = new StringJoiner(",", "(", ")");

    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        sj.add(Byte.toString(value));
      set >>>= 1;
    }

    return sj.toString();
  }

  /** {@link TreeSet} of the given set. */
  public static TreeSet<Byte> toSet(int set) {
    final TreeSet<Byte> result = new TreeSet<>();
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result.add(value);
      set >>>= 1;
    }
    return result;
  }

  public static byte[] toArray(int set) {
    final int size = size(set);
    byte[] result = new byte[size];
    int i = 0;
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result[i++] = value;
      set >>>= 1;
    }
    return result;
  }

  /**
   * Remove smalles value and consume it.
   * 
   * <p>
   * This can be used like this: <code><pre>
   * int set = of(expected);
   * while (set != 0)
   *   set = next(set, b -&gt; actual.add(b));
   * </pre></code>
   * 
   * @throw NoSuchElementException when the set is empty
   * @see #iterate(int)
   */
  public static int next(final int set, ByteConsumer consumer) throws NoSuchElementException {
    requireNonNull(consumer, "consumer");
    if (set == 0)
      throw new NoSuchElementException("empty set");
    byte next = (byte) Integer.numberOfTrailingZeros(set);
    consumer.accept(next);
    return set & ~(1 << next);
  }

  /** {@link BitSet} of the given set. */
  public static BitSet toBitSet(int set) {
    final BitSet result = new BitSet(32);
    for (int value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result.set(value);
      set >>>= 1;
    }
    return result;
  }

  /** {@link EnumSet} of the given set. */
  public static <E extends Enum<E>> EnumSet<E> toEnumSet(int set, final Class<E> type) {
    requireNonNull(type, "type");
    final EnumSet<E> result = EnumSet.noneOf(type);
    final E[] constants = type.getEnumConstants();
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result.add(constants[value]);
      set >>>= 1;
    }
    return result;
  }

  /** Return any of the bytes. */
  public static byte random(final int set, final Random rng) {
    requireNonNull(rng, "rng");
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

  /**
   * Performs a reduction on the elements of this stream, using the provided identity value and an
   * associative accumulation function, and returns the reduced value.
   * 
   * @see #reduce(int, IntBinaryOperator)
   * @see #sum(int)
   * */
  public static int reduce(int set, final int identity, final IntBinaryOperator op) {
    requireNonNull(op, "op");
    final int size = size(set);
    if (size == 0)
      return identity;
    if (size == 1)
      return op.applyAsInt(identity, log(set));
    int value = Integer.numberOfTrailingZeros(set);
    if (size == 2)
      return op.applyAsInt(value, 31 - Integer.numberOfLeadingZeros(set));
    int result = identity;
    set >>>= value;
    while (set != 0) {
      if ((set & 1) != 0)
        result = op.applyAsInt(result, value);
      set >>>= 1;
      value++;
    }
    return result;
  }

  /**
   * Performs a reduction on the elements of this stream, using the provided associative
   * accumulation function, and returns the reduced value or empty.
   * 
   * @see #reduce(int, int, IntBinaryOperator)
   * @see #sum(int)
   * */
  public static OptionalInt reduce(int set, final IntBinaryOperator op) {
    requireNonNull(op, "op");
    final int size = size(set);
    if (size <= 1)
      return OptionalInt.empty();

    int result = Integer.numberOfTrailingZeros(set);
    int value = result + 1;
    set >>>= value;
    while (set != 0) {
      if ((set & 1) != 0)
        result = op.applyAsInt(result, value);
      value++;
      set >>>= 1;
    }
    return OptionalInt.of(result);
  }

  /**
   * The sum of all values. This returns 0 for an empty set. <br>
   * This is equivalent to but faster than: {@code stream(set).sum()}
   */
  public static int sum(int set) {
    // Both empty set and (0) return zero:
    int size = size(set);
    if (size == 0) {
      return 0;
    } else if (size == 1) {
      // singleton: it's just the binary logarithm of set.
      return log(set);
    } else if (size == 2) {
      // two values -> check leading/trailing zeroes:
      return Integer.numberOfTrailingZeros(set) + (31 - Integer.numberOfLeadingZeros(set));
    } else {
      if (size > 16) // then the complement has fewer values to count:
        return 496 - sum(complement(set));
      // now we actually count the values:
      int result = 0;
      int value = Integer.numberOfTrailingZeros(set);
      set >>>= value;
      while (set != 0) {
        if ((set & 1) != 0)
          result += value;
        value++;
        set >>>= 1;
      }
      return result;
    }
  }

  /** Binary logarithm: returns n for a given 2<sup>n</sup>. */
  static int log(int i) {
    if (i == 0)
      throw new IllegalArgumentException("log(0) = -Infinity");
    int result = 0;
    if ((i & 0xFFFF_0000) != 0) {
      i >>>= 16;
      result += 16;
    }
    if ((i >= 0x100)) {
      i >>>= 8;
      result += 8;
    }
    if ((i >= 0x10)) {
      i >>>= 4;
      result += 4;
    }
    if ((i >= 0x4)) {
      i >>>= 2;
      result += 2;
    }
    result += (i >>> 1);
    return result;
  }

}
