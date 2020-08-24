package ch.claude_martin.smallset;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for sets of small integers (bytes in the range of 0 to 31),
 * represented as bit fields (int).
 * <p>
 * It is recommended to import all methods with static imports so they can be
 * used on primitive int values.
 * 
 * <p>
 * Iteration is possible with {@link #iterator()}. 
 * {@link #forEach(ByteConsumer)} does the same but doesnt require an iterator 
 * object.
 * 
 * <p>
 * In many cases the type int is used for the set (32 bits as a bit field) and
 * byte is the type of the elements. Make sure you do not confuse an element with 
 * an integer field set.
 * 
 * @author Claude Martin
 *
 */
public inline class SmallSet implements Iterable<Byte>, Comparable<SmallSet?> {

  final int value; // Visible to other classes in this package (ByteSet etc.)

  /** Creates a set from a bitset value. Not to be confused with {@link #singleton(int)} 
   * or {@link #of(byte...)}. */
  public SmallSet(int value) { //TODO make private
    // All integer values are legal. We can't check anything here.
    this.value = value;
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
  public static SmallSet of(final Iterable<? extends Number> values) {
    requireNonNull(values, "values");
    if (values instanceof ByteSet)
      return ((ByteSet) values).toSmallSet();
    int set = 0;
    for (final Number n : values) {
      requireNonNull(n, "values must not contain null");
      set |= (1 << numberToByte(n));
    }
    return new SmallSet(set);
  }

  /**
   * Creates a set from byte elements.
   * 
   * @param i
   *          Sequence of bytes.
   */
  public static SmallSet of(final byte... i) {
    int set = 0;
    for (final byte b : i)
      set |= (1 << checkRange(b));
    return new SmallSet(set);
  }

  /**
   * Creates a set from int elements.
   * 
   * @param i
   *          Sequence of integers.
   */
  public static SmallSet of(final int... i) {
    int set = 0;
    for (final int integer : i)
      set |= (1 << checkRange(integer));
    return new SmallSet(set);
  }

  /**
   * Creates set of enum values.
   */
  @SafeVarargs
  public static <E extends Enum<E>> SmallSet of(final E... enums) {
    requireNonNull(enums, "enums");
    int set = 0;
    for (final Enum<?> e : enums) {
      requireNonNull(enums, "enums must not contain null");
      set |= (1 << checkRange(e.ordinal()));
    }
    return new SmallSet(set);
  }

  /**
   * Creates set of enum values, using the ordinal of each element.
   */
  public static SmallSet of(final EnumSet<?> enumset) {
    requireNonNull(enumset, "enumset");
    int set = 0;
    for (final Enum<?> e : enumset)
      set |= (1 << checkRange(e.ordinal()));
    return new SmallSet(set);
  }

  /**
   * Creates set of a BitSet.
   */
  public static SmallSet of(final BitSet bitset) {
    requireNonNull(bitset, "bitset");
    int result = 0;
    for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
      result |= (1 << checkRange(i));
    }
    return new SmallSet(result);
  }

  /** Set with just one single value. */
  public static SmallSet singleton(final int val) {
    return new SmallSet(1 << checkRange(val));
  }

  /** Set with just one single value. */
  public static SmallSet singleton(final byte val) {
    return new SmallSet(1 << checkRange(val));
  }

  /** Set with just one single element. */
  public static SmallSet singleton(final Enum<?> element) {
    return new SmallSet( 1 << checkRange(requireNonNull(element, "element").ordinal()));
  }

  /** Set with just one single value. */
  public static SmallSet singleton(final Number n) {
    return new SmallSet(1 << numberToByte(requireNonNull(n, "n")));
  }

  /** Empty set. */
  public static SmallSet empty() {
    return new SmallSet(0);
  }

  /**
   * Tests if an element is in the set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public boolean contains(final byte element) {
    return (this.value & (1 << checkRange(element))) != 0;
  }

  /**
   * Tests if an enum element's ordinal value is in the set.
   * 
   * @param set
   *          A set
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public boolean contains(final Enum<?> element) {
    return (this.value & (1 << checkRange(requireNonNull(element, "element").ordinal()))) != 0;
  }

  /**
   * Checks if the set contains all elements.
   * 
   * @param set
   *          A set
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public boolean containsAll(final Collection<? extends Number> elements) {
    final int mask = of(requireNonNull(elements, "elements")).value;
    return (this.value & mask) == mask;
  }

  /**
   * Checks if the set contains all elements.
   * 
   * @param set
   *          A set
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public boolean containsAll(final EnumSet<?> elements) {
    final int mask = of(requireNonNull(elements, "elements")).value;
    return (this.value & mask) == mask;
  }

  /**
   * Checks if the set contains all elements.
   * 
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public  boolean containsAll(final byte... elements) {
    final int mask = of(requireNonNull(elements, "elements")).value;
    return (this.value & mask) == mask;
  }

    /**
   * Checks if the set contains all elements.
   * 
   * @param elements
   *          Elements to be checked for containment in given set
   */
  public  boolean containsAll(final SmallSet elements) {
    final int mask = requireNonNull(elements, "elements").value;
    return (this.value & mask) == mask;
  }

  /**
   * Compares this SmallSet to the other.
   */
  // Since existing API can accept "null", we use the indirect projection
  public int compareTo(SmallSet? other) {
      if (other == null) {
          return -1;
      }
      return this.value - other.value;
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
  public SmallSet add(final byte element) {
    return new SmallSet(this.value  | (1 << checkRange(element)));
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
  public SmallSet add(final Enum<?> element) {
    return new SmallSet(this.value | (1 << checkRange(requireNonNull(element, "element").ordinal())));
  }

  /** Calculates a hash code that is compatible with {@link Set#hashCode()}. */
  public int hashCode() {
    int h = 0;
    int set = this.value;
    for (byte n; set != 0; set &= ~(1 << n))
      h += Byte.hashCode(n = next(set));
    return h;
  }

  /**
   * The powerset, which is the set of all subsets.
   * <p>
   * Note: Complexity is <code>O(2<sup>n</sup>)</code>. For a set with 32
   * elements this would be rather large (2<sup>32</sup> = 4294967296).
   * <p>
   * This is not thread safe and has to be processed sequentially.
   * 
   * @return The powerset of this set.
   * */
  public Stream<SmallSet?> powerset() {
    final int setSize = this.size();
    if (setSize == 0)
      return Stream.of(empty());
    if (setSize == 1)
      return Stream.of(empty(), this);

    final long powersetSize = 1 << setSize;
    final var itr = new Iterator<SmallSet?>() {
      private long         i     = 0;
      private final byte[] array = SmallSet.this.toArray();

      @Override
      public SmallSet next() {
        if (!hasNext())
          throw new NoSuchElementException();
        try {
          int result = 0;
          for (int x = 0; x < Integer.SIZE; x++)
            if (((this.i & (1L << x)) != 0))
              result |= 1 << this.array[x];
          return new SmallSet(result);
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
    return StreamSupport.stream(//
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
  public SmallSet remove(final byte element) {
    return rem(this, checkRange(element));
  }

  /** {@link #remove(byte)}, but without check of range. */
  static SmallSet rem(final SmallSet set, final byte element) {
    return new SmallSet(set.value & ~(1 << element));
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
  public SmallSet remove(final Enum<?> element) {
    return this.remove((byte) checkRange(requireNonNull(element, "element").ordinal()));
  }

  /**
   * Replaces each element of this set with the result of applying the operator
   * to that element. Errors or runtime exceptions thrown by the operator are
   * relayed to the caller.
   *
   * @param operator
   *          the operator to apply to each element
   * @throws NullPointerException
   *           if the specified operator is null or if the operator result is a
   *           null value
   * @throws IllegalArgumentException
   *           if the operator returns an invalid value
   */
  public SmallSet replaceAll(IntUnaryOperator operator) {
    Objects.requireNonNull(operator);
    if (this.isEmpty())
      return this;
    SmallSet result = empty();
    for (byte n : this)
      result = result.add((byte) checkRange(operator.applyAsInt(n)));
    return result;
  }

  /** Union of two sets. */
  public  SmallSet union(final SmallSet other) {
    return new SmallSet(this.value | other.value);
  }

  /** Intersection of two sets. */
  public  SmallSet intersect(final SmallSet other) {
    return new SmallSet(this.value & other.value);
  }

  /** Elements of b removed from a: a \ b = a.intersect(b.complement()). */
  public  SmallSet minus(final SmallSet other) {
    return new SmallSet(this.value & ~other.value);
  }

  /** Complement of a set. The domain is [0,1,..,31]. */
  public SmallSet complement() {
    return new SmallSet(~this.value);
  }

  /** Complement of a set. The domain is [min,..,max], both inclusive. */
  public  SmallSet complement(final int min, final int max) {
    if (checkRange(min) > checkRange(max))
      throw new IllegalArgumentException("max>min");
    return new SmallSet(~this.value & ofRangeClosed(min, max).value);
  }

  /**
   * Performs the given action for each byte until all bytes have been processed
   * or the action throws an exception. Actions are performed in the order of
   * iteration (ascending). Exceptions thrown by the action are relayed to the
   * caller.
   */
  public void forEach(final ByteConsumer action) {
    requireNonNull(action, "action");
    for (byte n : this)
      action.accept(n);
  }

  /**
   * Creates an iterator for the set.
   */
  public ByteIterator iterator() {
    return new ByteIterator() {
      private int _set = SmallSet.this.value;

      @Override
      public boolean hasNext() {
        return this._set != 0;
      }

      @Override
      public byte nextByte() throws NoSuchElementException {
        if (this._set == 0)
          throw new NoSuchElementException();
        byte next = SmallSet.next(this._set);
        this._set &= ~(1 << next);
        return next;
      }
    };
  }

  /** An iterator of the set that yields integers. */
  public OfInt intIterator() {
    return new OfInt() {
      private ByteIterator itr = SmallSet.this.iterator();

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
  

  private static final int CHARACTERISTICS = Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED
                                               | Spliterator.DISTINCT | Spliterator.SORTED;

  @Override
  public Spliterator<Byte> spliterator() {
    return Spliterators.spliterator(this.iterator(), this.size(), CHARACTERISTICS);
  }
  
  /**
   * Creates an {@link Stream} of {@link Byte Bytes} (boxed) for the set.
   * 
   * @see #stream(int)
   */
  public Stream<Byte> stream() {
    final int size = this.size();
    if (size == 0)
      return Stream.empty();
    if (size == 1)
      return Stream.of(Byte.valueOf((byte) log(this.value)));
    return StreamSupport.stream(() -> Spliterators.spliterator(this.iterator(), size, CHARACTERISTICS), //
        CHARACTERISTICS, false);
  }
  
  public Spliterator.OfInt intSpliterator() {
    return this.intSpliterator(this.size());
  }

  private Spliterator.OfInt intSpliterator(final int size) {
    return Spliterators.spliterator(this.intIterator(), size, CHARACTERISTICS);
  }

  /**
   * Creates an {@link IntStream} for the set.
   * 
   * @see #byteStream(int)
   */
  public IntStream intStream() {
    final int size = this.size();
    if (size == 0)
      return IntStream.empty();
    if (size == 1)
      return IntStream.of(log(this.value));
    return StreamSupport.intStream(() -> this.intSpliterator(size), CHARACTERISTICS, false);
  }

  /**
   * Mutable integer that is used internally by #collect(IntStream).
   */
  static final class MutableInt {
    int value = 0;
  }

  /**
   * Fast implementation of {@link IntStream#collect} to collect values from a
   * stream into a set.
   * <p>
   * This is a terminal operation.
   * 
   * @param stream
   *          An IntStream, e.g. one created by {@link SmallSet#stream(int)}
   * @return An integer representing a set of the values from the stream
   * @throws IllegalArgumentException
   *           if any of the values is out of range
   */
  public static SmallSet collect(final IntStream stream) throws IllegalArgumentException {
    requireNonNull(stream, "stream");
    return new SmallSet(stream.collect(//
        MutableInt::new,//
        (set, b) -> set.value |= (1 << checkRange(b)), //
        (a, b) -> a.value |= b.value).value);
  }

  /**
   * Fast implementation of {@link Stream#collect} to collect numbers from a
   * stream into a set.
   * <p>
   * This is a terminal operation.
   * 
   * @param stream
   *          A Stream, e.g. one created by {@link SmallSet#byteStream(int)}
   * @return An integer representing a set of the values from the stream
   * @throws IllegalArgumentException
   *           if any of the values is out of range
   */
  public static SmallSet collect(final Stream<? extends Number> stream) throws IllegalArgumentException {
    requireNonNull(stream, "stream");
    return new SmallSet(stream.collect(//
        MutableInt::new,//
        (set, n) -> set.value |= (1 << numberToByte(n)), //
        (a, b) -> a.value |= b.value).value);
  }

  /** Returns the number of elements in this set (its cardinality). */
  public int size() {
    return Integer.bitCount(this.value);
  }

  /** True, if empty. */
  public boolean isEmpty() {
    return this.value == 0;
  }

  /**
   * Range of bytes.
   * 
   * @param a
   *          First element (inclusive)
   * @param z
   *          Last element (exclusive)
   * @return <code>SmallSet.of(a, ... , z-1)</code>
   */
  public static SmallSet ofRange(final int a, final int z) {
    if (checkRange(a) >= z)
      throw new IllegalArgumentException("z<=a");
    checkRange(z - 1);
    return new SmallSet(lessThan(z - a) << a);
  }

  /**
   * Closed Range of integers.
   * 
   * @param a
   *          First element (inclusive)
   * @param z
   *          Last element (inclusive)
   * @return <code>SmallSet.of(a, ... , z)</code>
   */
  public static SmallSet ofRangeClosed(final int a, final int z) {
    if (checkRange(a) > checkRange(z))
      throw new IllegalArgumentException("z<a");
    return new SmallSet(lessOrEqual(z - a) << a);
  }

  /**
   * From 0 (inclusive) to n (inclusive). The returned values has n+1 bits set
   * to 1.
   * <p>
   * Example: lessOrEqual(5) = 0b00111111 = 63
   */
  private static int lessOrEqual(final int n) {
    assert n >= 0 : "n<0";
    assert n < Integer.SIZE : "n>31";
    return lessThan(n + 1);
  }

  /**
   * From 0 (inclusive) to n (exclusive). The returned values has n bits set to
   * 1.
   * <p>
   * Example: lessThan(5) = 0b00011111 = 31
   * */
  private static int lessThan(final int n) {
    assert n > 0 : "n<=0";
    assert n <= Integer.SIZE : "n>32";
    return 0xffffffff >>> (Integer.SIZE - n);
  }

  /** String representation of the set. */
  public String toString() {
    if (this.value == 0)
      return "()";
    final StringJoiner sj = new StringJoiner(",", "(", ")");
    for (byte n : this)
      sj.add(Byte.toString(n));
    return sj.toString();
  }

  /** Creates a mutable {@link ByteSet} of the set. */
  public ByteSet toSet() {
    return new ByteSet(this);
  }

  /** Given set as {@code byte[]}. */
  public byte[] toArray() {
    final int size = this.size();
    final byte[] result = new byte[size];
    int i = 0;
    for (byte b : this) {
      result[i++] = b;
    }
    return result;
  }

  /**
   * Remove smallest value and consume it.
   * 
   * <p>
   * This can be used like this: <code><pre>
   * int set = of(.....);
   * while (set != 0)
   *   set = next(set, b -&gt; <i>process</i>(b));
   * </pre></code>
   * 
   * However, it's easier to just use {@link #forEach(ByteConsumer)} instead.
   * 
   * @throw NoSuchElementException when the set is empty
   * @see #iterator()
   * @see #forEach(ByteConsumer)
   */
  public int next(final ByteConsumer consumer) throws NoSuchElementException {
    requireNonNull(consumer, "consumer");
    if (this.isEmpty())
      throw new NoSuchElementException("empty set");
    final byte next = next(this.value);
    consumer.accept(next);
    return this.value & ~(1 << next);
  }

  /**
   * Number of trailing zeroes. This is
   * {@link Integer#numberOfTrailingZeros(int)} cast to {@code byte}, which is
   * the {@link #min() minimum element} in the set or 32.
   * 
   * @return First element, or 32 if set is empty.
   * 
   * @see #next(ByteConsumer)
   * @see #higher(byte)
   * @see #min()
   */
  private static byte next(final int set) {
    return (byte) Integer.numberOfTrailingZeros(set);
  }
  
  /** Creates a new {@link BitSet} of the set. */
  public BitSet toBitSet() {
    return BitSet.valueOf(new long[] { this.value & 0xFFFFFFFFL });
  }

  /** {@link EnumSet} of the set. */
  public <E extends Enum<E>> EnumSet<E> toEnumSet(final Class<E> type) {
    requireNonNull(type, "type");
    final EnumSet<E> result = EnumSet.noneOf(type);
    final E[] constants = type.getEnumConstants();
    for (byte n : this)
      result.add(constants[n]);
    return result;
  }

  /**
   * Returns any of the elements.
   * 
   * @throws NoSuchElementException
   *           if this set is empty
   */
  public byte random(final Random rng) {
    requireNonNull(rng, "rng");
    if (this.value == 0)
      throw new NoSuchElementException("set is empty.");
    int r = rng.nextInt(this.size());
    int c = this.value;
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
   * Performs a reduction on the elements of this set, using the provided
   * identity value and an associative accumulation function, and returns the
   * reduced value.
   * 
   * @see #reduce(IntBinaryOperator)
   * @see #sum()
   * */
  public int reduce(final int identity, final IntBinaryOperator op) {
    requireNonNull(op, "op");
    final int size = this.size();
    if (size == 0)
      return identity;
    if (size == 1)
      return op.applyAsInt(identity, log(this.value));
    int result = identity;
    for (byte b : this) {
        result = op.applyAsInt(result, b);
    }
    return result;
  }

  /**
   * Performs a reduction on the elements of this stream, using the provided
   * associative accumulation function, and returns the reduced value or empty.
   * 
   * @see #reduce(IntBinaryOperator)
   * @see #sum()
   * */
  public OptionalInt reduce( final IntBinaryOperator op) {
    requireNonNull(op, "op");
    final int size = this.size();
    if (size == 0)
      return OptionalInt.empty();
    int set = this.value;
    if (size == 1)
      return OptionalInt.of((byte) log(set));

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
  public int sum() {
    final int size = this.size();
    if (size == 0) {
      return 0;
    } else if (size == 1) {
      // singleton: it's just the binary logarithm of set.
      return log(this.value);
    } else if (size == 2) {
      // two values -> check leading/trailing zeroes:
      return Integer.numberOfTrailingZeros(this.value) + (31 - Integer.numberOfLeadingZeros(this.value));
    } else {
      if (size > 16) // then the complement has fewer values to count:
        return 496 - this.complement().sum();
      int result = 0;
      int set = this.value;
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
   * Returns an {@code OptionalByte} describing the minimum element of the given
   * set, or an empty optional if the set is empty. This is equivalent to
   * <code>reduce(Integer::min)</code>.
   * 
   * @see #next()
   */
  public OptionalByte min() {
    int result = Integer.numberOfTrailingZeros(this.value);
    if (result == Integer.SIZE)
      return OptionalByte.empty();
    return OptionalByte.of((byte) result);
  }

  /**
   * Returns an {@code OptionalByte} describing the maximum element of the given
   * set, or an empty optional if the set is empty. This is equivalent to
   * <code>reduce(Integer::max)</code>.
   */
  public OptionalByte max() {
    int result = 31 - Integer.numberOfLeadingZeros(this.value);
    if (result == -1)
      return OptionalByte.empty();
    return OptionalByte.of((byte) result);
  }

  /**
   * Returns the greatest element in this set strictly less than the given
   * element, or {@code empty} if there is no such element.
   * 
   * @param e
   * @return
   */
  public OptionalByte lower(final byte e) {
    checkRange(e);
    int set = this.value;
    if (e == 0 || set == 0)
      return OptionalByte.empty();
    byte result = -1;
    byte n;
    while (set != 0) {
      n = next(set);
      if (n >= e)
        break;
      result = n;
      set &= ~(1 << n); // remove n
    }
    return result == -1 || result == 32 ? OptionalByte.empty() : OptionalByte.of(result);
  }

  /**
   * Returns the greatest element in this set less than or equal to the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte floor( final byte e) {
    checkRange(e);
    int set = this.value;
    if (set == 0)
      return OptionalByte.empty();
    byte result = -1;
    byte n;
    while (set != 0) {
      n = next(set);
      if (n > e)
        break;
      result = n;
      set &= ~(1 << n); // remove n
    }
    return result == -1 || result == 32 ? OptionalByte.empty() : OptionalByte.of(result);
  }

  /**
   * Returns the least element in this set greater than or equal to the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte ceiling(final byte e) {
    checkRange(e);
    int set = this.value;
    if (set == 0)
      return OptionalByte.empty();
    byte result = -1;
    byte n;
    while (set != 0) {
      n = next(set);
      if (n >= e) {
        result = n;
        break;
      }
      set &= ~(1 << n); // remove n
    }
    return result == -1 || result == 32 ? OptionalByte.empty() : OptionalByte.of(result);
  }

  /**
   * Returns the least element in this set strictly greater than the given
   * element, or {@code empty} if there is no such element.
   */
  public  OptionalByte higher(final byte e) {
    checkRange(e);
    int set = this.value;
    if (e == 31 || set == 0)
      return OptionalByte.empty();
    byte result = -1;
    byte n;
    while (set != 0) {
      n = next(set);
      if (n > e) {
        result = n;
        break;
      }
      set &= ~(1 << n); // remove n
    }
    return result == -1 || result == 32 ? OptionalByte.empty() : OptionalByte.of(result);
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

  /**
   * Compares the specified object with this set for equality.  Returns
   * {@code true} if the given object is also a SmallSet, the two sets have
   * the same size, and every member of the given set is contained in
   * this set. 
   *
   * @param o object to be compared for equality with this set
   * @return {@code true} if the specified object is equal to this set
   */
  @Override
  public boolean equals(Object o) {
      if (o == this)
          return true;
      if (!(o instanceof SmallSet))
          return false;
      var c = (SmallSet) o;
      if (c.size() != size())
          return false;
      try {
          return containsAll(c);
      } catch (ClassCastException | NullPointerException unused) {
          return false;
      }
  }


}
