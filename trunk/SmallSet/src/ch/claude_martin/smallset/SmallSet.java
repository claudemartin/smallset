package ch.claude_martin.smallset;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 *   for (int itr = set, value = 0; itr != 0; itr &gt;&gt;&gt;= 1) {
 *     if ((itr &amp; 1) != 0)
 *       <i>process</i>(value);
 *     value++;
 *   }
 * </pre></code>
 * 
 * <p>
 * In most cases the type int is used for the set (32 bits as a bit field) and byte is the type of
 * the elements. Since there is no <i>OptionalByte</i> in java some methods return
 * {@link OptionalInt} instead.
 * 
 * @author Claude Martin
 *
 */
public final class SmallSet {

  private SmallSet() {
  }

  private static int checkRange(final int i) {
    if (i > 31)
      throw new IllegalArgumentException("out of range: i>31");
    if (i < 0)
      throw new IllegalArgumentException("out of range: i<0");
    return i;
  }

  private static byte checkRange(final byte i) {
    if (i > 31)
      throw new IllegalArgumentException("out of range: i>31");
    if (i < 0)
      throw new IllegalArgumentException("out of range: i<0");
    return i;
  }

  private static byte numberToByte(final Number n) {
    if (null == n)
      throw new NullPointerException("given number is null");
    final double d = n.doubleValue();

    if (Double.isNaN(d))
      throw new IllegalArgumentException("not a number");
    if (Double.isInfinite(d))
      throw new IllegalArgumentException("infinite");
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
   * Creates a set from byte elements.
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
   * Creates a set from int elements.
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
  public static int of(final EnumSet<?> enumset) {
    requireNonNull(enumset, "enumset");
    int set = 0;
    for (final Enum<?> e : enumset)
      set |= (1 << checkRange(e.ordinal()));
    return set;
  }

  /**
   * Creates set of a BitSet.
   */
  public static int of(final BitSet bitset) {
    requireNonNull(bitset, "bitset");
    int result = 0;
    for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
      result |= (1 << checkRange(i));
    }
    return result;
  }

  /** Set with just one single value. */
  public static int singleton(final int val) {
    return 1 << checkRange(val);
  }

  /** Set with just one single value. */
  public static int singleton(final byte val) {
    return 1 << checkRange(val);
  }

  /** Set with just one single element. */
  public static int singleton(final Enum<?> element) {
    return 1 << checkRange(requireNonNull(element, "element").ordinal());
  }

  /** Set with just one single value. */
  public static int singleton(final Number n) {
    return 1 << numberToByte(requireNonNull(n, "n"));
  }

  /** Empty set. */
  public static int empty() {
    return 0;
  }

  /**
   * Tests if an element is in the given set.
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
   * Tests if an element is in the given set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public static boolean contains(final int set, final Enum<?> element) {
    return (set & (1 << checkRange(requireNonNull(element, "element").ordinal()))) != 0;
  }

  /**
   * Checks if the given set contains all elements.
   * 
   * @param set
   *          A set
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public static boolean containsAll(final int set, final Collection<? extends Number> elements) {
    final int mask = of(requireNonNull(elements, "elements"));
    return (set & mask) == mask;
  }

  /**
   * Checks if the given set contains all elements.
   * 
   * @param set
   *          A set
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public static boolean containsAll(final int set, final EnumSet<?> elements) {
    final int mask = of(requireNonNull(elements, "elements"));
    return (set & mask) == mask;
  }

  /**
   * Checks if the given set contains all elements.
   * 
   * @param set
   *          A set
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public static boolean containsAll(final int set, final byte... elements) {
    final int mask = of(requireNonNull(elements, "elements"));
    return (set & mask) == mask;
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
   * Adds an enum element to the set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>set ∪ {element.ordinal()}</code>
   */
  public static int add(final int set, final Enum<?> element) {
    return set | (1 << checkRange(requireNonNull(element, "element").ordinal()));
  }

  /**
   * The powerset, which is the set of all subsets.
   * <p>
   * Note: Complexity is <code>O(2<sup>n</sup>)</code>. For a set with 32 elements this would be
   * rather large (2<sup>32</sup> = 4294967296).
   * <p>
   * This is not thread safe and has to be processed sequentially.
   * 
   * @return The powerset of this set.
   * */
  public static IntStream powerset(final int set) {
    final int setSize = size(set);
    if (setSize == 0)
      return IntStream.of(empty());
    if (setSize == 1)
      return IntStream.of(empty(), singleton(log(set)));

    final long powersetSize = 1 << setSize;
    final OfInt itr = new OfInt() {
      private long i = 0;
      private final byte[] array = toArray(set);

      @Override
      public int nextInt() {
        if (!hasNext())
          throw new NoSuchElementException();
        try {
          int result = 0;
          for (int x = 0; x < Integer.SIZE; x++)
            if (((this.i & (1L << x)) != 0))
              result |= 1 << this.array[x];
          return result;
        } finally {
          this.i++;
        }
      }

      @Override
      public boolean hasNext() {
        return this.i < powersetSize;
      }
    };

    final int characteristics = Spliterator.NONNULL | Spliterator.SIZED | Spliterator.DISTINCT;
    return StreamSupport.intStream(//
        Spliterators.spliterator(itr, powersetSize, characteristics), false);
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

  /**
   * Removes an element from a set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>set \ {element.ordinal()}</code>
   */
  public static int remove(final int set, final Enum<?> element) {
    return set & ~(1 << checkRange(requireNonNull(element, "element").ordinal()));
  }

  /**
   * Replaces each element of this set with the result of applying the operator to that element.
   * Errors or runtime exceptions thrown by the operator are relayed to the caller.
   *
   * <p>
   * This is basically the same as collect(stream(set).map(operator))
   * 
   * @param operator
   *          the operator to apply to each element
   * @throws NullPointerException
   *           if the specified operator is null or if the operator result is a null value
   * @throws IllegalArgumentException
   *           if the operator returns an invalid value
   */
  public static int replaceAll(int set, IntUnaryOperator operator) {
    Objects.requireNonNull(operator);
    if (set == 0)
      return 0;
    int result = 0;
    byte value = (byte) Integer.numberOfTrailingZeros(set);
    set >>>= value;
    while (set != 0) {
      if ((set & 1) != 0)
        result = add(result, (byte) checkRange(operator.applyAsInt(value)));
      set >>>= 1;
      value++;
    }
    return result;
  }

  /** Union of two sets. */
  public static int union(final int a, final int b) {
    return a | b;
  }

  /** Intersection of two sets. */
  public static int intersect(final int a, final int b) {
    return a & b;
  }

  /** Elements of b removed from a: a \ b = intersect(a , complement(b)). */
  public static int minus(final int a, final int b) {
    return a & ~b;
  }

  /** Complement of a set. The domain is [0,1,..,31]. */
  public static int complement(final int set) {
    return ~set;
  }

  /** Complement of a set. The domain is [min,..,max], both inclusive. */
  public static int complement(final int set, final int min, final int max) {
    if (checkRange(min) > checkRange(max))
      throw new IllegalArgumentException("max>min");
    return ~set & ofRangeClosed(min, max);
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

  /** An iterator of the given set that returns integer. */
  public static OfInt intIterator(int set) {
    return new OfInt() {
      private ByteIterator itr = iterator(set);

      @Override
      public boolean hasNext() {
        return this.itr.hasNext();
      }

      @Override
      public int nextInt() throws NoSuchElementException {
        return this.itr.nextByte();
      }
    };
  }

  private static final int CHARACTERISTICS = Spliterator.SIZED | Spliterator.SUBSIZED
      | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED;

  public static Spliterator.OfInt spliterator(final int set) {
    return spliterator(set, size(set));
  }

  private static Spliterator.OfInt spliterator(final int set, final int size) {
    return Spliterators.spliterator(intIterator(set), size(set), CHARACTERISTICS);
  }

  /**
   * Creates an {@link IntStream} for the given set.
   * 
   * @see #byteStream(int)
   */
  public static IntStream stream(final int set) {
    final int size = size(set);
    if (size == 0)
      return IntStream.empty();
    if (size == 1)
      return IntStream.of(log(set));
    return StreamSupport.intStream(() -> spliterator(set, size), CHARACTERISTICS, false);
  }

  /**
   * Creates an {@link Stream} of {@link Byte Bytes} (boxed) for the given set.
   * 
   * @see #stream(int)
   */
  public static Stream<Byte> byteStream(final int set) {
    final int size = size(set);
    if (size == 0)
      return Stream.empty();
    if (size == 1)
      return Stream.of(Byte.valueOf((byte) log(set)));
    return StreamSupport.stream(
        () -> Spliterators.spliterator(iterator(set), size, CHARACTERISTICS), //
        CHARACTERISTICS, false);
  }

  /**
   * Mutable integer that is used internally by #collect(IntStream).
   */
  static final class MutableInt {
    int value = empty();
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
  public static int collect(final IntStream stream) throws IllegalArgumentException {
    requireNonNull(stream, "stream");
    return stream.collect(//
        MutableInt::new,//
        (set, b) -> set.value = SmallSet.add(set.value, (byte) checkRange(b)), //
        (a, b) -> a.value |= b.value).value;
  }

  /**
   * Fast implementation of {@link Stream#collect} to collect numbers from a stream into a set.
   * <p>
   * This is a terminal operation.
   * 
   * @param stream
   *          A Stream, e.g. one created by {@link SmallSet#byteStream(int)}
   * @return An integer representing a set of the values from the stream
   * @throws IllegalArgumentException
   *           if any of the values is out of range
   */
  public static int collect(final Stream<? extends Number> stream) throws IllegalArgumentException {
    requireNonNull(stream, "stream");
    return stream.collect(//
        MutableInt::new,//
        (set, n) -> set.value = SmallSet.add(set.value, numberToByte(n)), //
        (a, b) -> a.value |= b.value).value;
  }

  /** Returns the number of elements in this set (its cardinality). */
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
    if (checkRange(a) >= z)
      throw new IllegalArgumentException("z<=a");
    checkRange(z - 1);
    return lessThan(z - a) << a;
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
    return lessOrEqual(z - a) << a;
  }

  /**
   * From 0 (inclusive) to n (inclusive). The returned values has n+1 bits set to 1.
   * <p>
   * Example: lessOrEqual(5) = 0b00111111 = 63
   */
  private static int lessOrEqual(final int n) {
    assert n >= 0 : "n<0";
    assert n < Integer.SIZE : "n>31";
    return lessThan(n + 1);
  }

  /**
   * From 0 (inclusive) to n (exclusive). The returned values has n bits set to 1.
   * <p>
   * Example: lessThan(5) = 0b00011111 = 31
   * */
  private static int lessThan(final int n) {
    assert n > 0 : "n<=0";
    assert n <= Integer.SIZE : "n>32";
    return 0xffffffff >>> (Integer.SIZE - n);
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

  /** Creates a new {@link TreeSet} of the given set. */
  public static TreeSet<Byte> toSet(int set) {
    final TreeSet<Byte> result = new TreeSet<>();
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result.add(value);
      set >>>= 1;
    }
    return result;
  }

  /** Given set as {@code byte[]}. */
  public static byte[] toArray(int set) {
    final int size = size(set);
    final byte[] result = new byte[size];
    int i = 0;
    for (byte value = 0; set != 0; value++) {
      if ((set & 1) != 0)
        result[i++] = value;
      set >>>= 1;
    }
    return result;
  }

  /**
   * Remove smallest value and consume it.
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
  public static int next(final int set, final ByteConsumer consumer) throws NoSuchElementException {
    requireNonNull(consumer, "consumer");
    if (set == 0)
      throw new NoSuchElementException("empty set");
    final byte next = (byte) Integer.numberOfTrailingZeros(set);
    consumer.accept(next);
    return set & ~(1 << next);
  }

  /** Creates a new {@link BitSet} of the given set. */
  public static BitSet toBitSet(int set) {
    return BitSet.valueOf(new long[] { set & 0xFFFFFFFFL });
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

  /**
   * Returns any of the elements.
   * 
   * @throws IllegalArgumentException
   *           if set is empty (0)
   */
  public static byte random(final int set, final Random rng) {
    requireNonNull(rng, "rng");
    if (set == 0)
      throw new IllegalArgumentException("set must not be empty.");
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
    if (size == 0)
      return OptionalInt.empty();
    if (size == 1)
      return OptionalInt.of(log(set));

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
    final int size = size(set);
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

  /**
   * Returns an {@code OptionalInt} describing the minimum element of the given set, or an empty
   * optional if the set is empty. This is equivalent to <code>reduce(set, Integer::min)</code>.
   */
  public static OptionalInt min(int set) {
    int result = Integer.numberOfTrailingZeros(set);
    if (result == Integer.SIZE)
      return OptionalInt.empty();
    return OptionalInt.of(result);
  }

  /**
   * Returns an {@code OptionalInt} describing the maximum element of the given set, or an empty
   * optional if the set is empty. This is equivalent to <code>reduce(set, Integer::max)</code>.
   */
  public static OptionalInt max(int set) {
    int result = 31 - Integer.numberOfLeadingZeros(set);
    if (result == -1)
      return OptionalInt.empty();
    return OptionalInt.of(result);
  }

  /**
   * Binary logarithm: returns n for a given 2<sup>n</sup>.
   * 
   * This can be used to get a value from a singleton set.
   */
  static int log(int i) {
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
