package ch.claude_martin.smallset;

import static java.util.Objects.requireNonNull;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.random.RandomGenerator;
import java.util.stream.*;

/**
 * Value class for sets of small integers (in the range of 0 to 31).
 * 
 * <p>
 * Iteration is possible with {@link #iterator()}. 
 * {@link #forEach(ByteConsumer)} does the same but doesn't require an iterator 
 * object.
 * 
 * <p>
 * Sets can be compared but this is done on the bit field (int) and is only useful 
 * when used in a data structure based on sorting, such as a tree.
 * 
 * @author Claude Martin
 *
 */
public value class SmallSet implements Iterable<Byte>, Comparable<SmallSet>, Serializable {
  private static final long serialVersionUID = 1L;

  final static SmallSet EMPTY = new SmallSet(0);
  
  final int value; // Visible to other classes in this package (ByteSet etc.)

  /** Creates a set from a bitset value. Not to be confused with {@link #singleton(int)} 
   * or {@link #of(byte...)}. */
  SmallSet(int value) { 
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
   * Returns the int value that is equivalent to this set. 
   * @see #fromInt(int)
   */
  public int toInt() {
    return this.value;
  }
  
  /** 
   * Returns the set that is equivalent to the given int value. 
   * @see #toInt()
   */
  public static SmallSet fromInt(int value) {
    return new SmallSet(value);
  }

  /** Empty set. 
   * @see #empty()*/
  public static SmallSet of() {
    return EMPTY;
  }

  /**
   * Creates a SmallSet from a collection or iterable.
   * 
   * @param values
   *          Sequence of numbers
   */
  public static SmallSet of(final Iterable<? extends Number> values) {
    requireNonNull(values, "values");
    if (values instanceof ByteSet bs)
      return bs.toSmallSet();
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

  /** Empty set. This will be the "zero instance" once "Null-Restricted Value Class Types" are available. */
  public static SmallSet empty() {
    return EMPTY;
  }

  /**
   * Tests if an element is in the set.
   * 
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public boolean contains(final byte element) {
    return (this.value & (1 << checkRange(element))) != 0;
  }

  /**
   * Tests if an element is in the set.
   * 
   * @param element
   *          An element
   * @return <code>element ∈ set</code>
   */
  public boolean contains(final int element) {
    return (this.value & (1 << checkRange(element))) != 0;
  }

  /**
   * Tests if an enum element's ordinal value is in the set.
   * 
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
    return (this.value & elements.value) == elements.value;
  }

  /**
   * Compares this SmallSet to the other.
   */
  public int compareTo(SmallSet other) {
    return this.value - other.value;
}

  /**
   * Adds an element to the set.
   * 
   * @param element
   *          An element
   * @return <code>set ∪ {element}</code>
   */
  public SmallSet add(final byte element) {
    return new SmallSet(this.value  | (1 << checkRange(element)));
  }

  /**
   * Adds an element to the set.
   * 
   * @param element
   *          An element
   * @return <code>set ∪ {element}</code>
   */
  public SmallSet add(final int element) {
    return new SmallSet(this.value  | (1 << checkRange(element)));
  }

  /**
   * Adds an enum element to the set.
   * 
   * @param element
   *          An element
   * @return <code>set ∪ {element.ordinal()}</code>
   */
  public SmallSet add(final Enum<?> element) {
    return new SmallSet(this.value | (1 << checkRange(requireNonNull(element, "element").ordinal())));
  }

  /** Returns the value of this small set. 
   * Note that it is not compatible with {@link Set#hashCode()}. 
   * 
   * @implNote We could just use super.hashCode() but that's not stable.
   * Just using this.value is easy and gives us the same value even if we restart the JVM.
   * */
  public int hashCode() {
    return this.value;  
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
  public Stream<SmallSet> powerset() {
    return powersetAsInts().mapToObj(SmallSet::new);
  }

  /** There is no SmallSetStream yet, so instead we have this. */
  public IntStream powersetAsInts() {
    final int setSize = this.size();
    if (setSize == 0)
      return IntStream.of(empty().value);
    if (setSize == 1)
      return IntStream.of(empty().value, this.value);

    final long powersetSize = 1L << this.size();
    final var itr = new PrimitiveIterator.OfInt() {
      private long         i     = 0;
      private final byte[] array = SmallSet.this.toArray();

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
   * @param element
   *          An element
   * @return <code>set \ {element}</code>
   */
  public SmallSet remove(final byte element) {
    return this.removeTrustedByte(checkRange(element));
  }

  /**
   * Removes an element from a set.
   * 
   * @param element
   *          An element
   * @return <code>set \ {element}</code>
   */
  public SmallSet remove(final int element) {
    return this.removeTrustedByte((byte) checkRange(element));
  }

  /** {@link #remove(byte)}, but without check of range. */
  private SmallSet removeTrustedByte(final byte element) {
    return new SmallSet(this.value & ~(1 << element));
  }

  /**
   * Removes an element from a set.
   * 
   * @param element
   *          An element
   * @return <code>set \ {element.ordinal()}</code>
   */
  public SmallSet remove(final Enum<?> element) {
    return this.removeTrustedByte((byte) checkRange(requireNonNull(element, "element").ordinal()));
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
    var copy = this;
    while (!copy.isEmpty()) {
      var next = next(copy.value);
      copy = copy.removeTrustedByte(next);
      result = result.add((byte) checkRange(operator.applyAsInt(next)));
    }
    return result;
  }

  /** Union of two sets. */
  public SmallSet union(final SmallSet other) {
    return new SmallSet(this.value | other.value);
  }

  /** Intersection of two sets. */
  public SmallSet intersect(final SmallSet other) {
    return new SmallSet(this.value & other.value);
  }

  /** Elements of b removed from a: a \ b = a.intersect(b.complement()). */
  public SmallSet minus(final SmallSet other) {
    return new SmallSet(this.value & ~other.value);
  }

  /** Complement of a set. The domain is [0,1,..,31]. */
  public SmallSet complement() {
    return new SmallSet(~this.value);
  }

  /** Complement of a set. The domain is [min,..,max], both inclusive. */
  public SmallSet complement(final int min, final int max) {
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
    var copy = this;
    while (!copy.isEmpty())
      copy = copy.next(action);
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
   * @see #intStream()
   */
  public Stream<Byte> stream() {
    final int size = this.size();
    return switch (size) {
      case 0 -> Stream.empty();
      case 1 -> Stream.of(Byte.valueOf((byte) log(this.value)));
      default -> StreamSupport.stream(() -> Spliterators.spliterator(this.iterator(), size, CHARACTERISTICS), //
            CHARACTERISTICS, false);
    };
  }
  
  public Spliterator.OfInt intSpliterator() {
    return this.intSpliterator(this.size());
  }

  private Spliterator.OfInt intSpliterator(final int size) {
    return Spliterators.spliterator(this.intIterator(), size, CHARACTERISTICS);
  }

  /**
   * Creates an {@link IntStream} of the values of the set.
   * 
   * @see #byteStream(int)
   */
  public IntStream intStream() {
    final int size = this.size();
    return switch (size) {
      case 0 -> IntStream.empty();
      case 1 -> IntStream.of(log(this.value));
      default ->  StreamSupport.intStream(() -> this.intSpliterator(size), CHARACTERISTICS, false);
    };
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
   * @return A set representing a set of the values from the stream
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
   *          A Stream, e.g. one created by {@link SmallSet#stream()}
   * @return A set representing a set of the values from the stream
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

  /** Returns the number of elements in this set (its cardinality). 
   * @implNote This calls {@link Integer#bitCount(int)}.  */
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
   * @param from
   *          First element (inclusive)
   * @param toExclusive
   *          Last element (exclusive)
   * @return <code>SmallSet.of(a, ... , z-1)</code>
   */
  public static SmallSet ofRange(final int from, final int toExclusive) {
    if (checkRange(from) == toExclusive) return empty();
    if (from > toExclusive)
      throw new IllegalArgumentException("from > toExclusive");
    checkRange(toExclusive - 1);
    return new SmallSet(lessThan(toExclusive - from) << from);
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
   * Example: lessOrEqual(5) = ( 0, 1, 2, 3, 4, 5 ) = 0b00111111 = 63
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
   * Example: lessThan(5) = ( 0, 1, 2, 3, 4 ) = 0b00011111 = 31
   * */
  private static int lessThan(final int n) {
    assert n > 0 : "n<=0";
    assert n <= Integer.SIZE : "n>32";
    return 0xffffffff >>> (Integer.SIZE - n);
  }

  /** String representation of the set. Equal to {@link #toString(CharSequence, CharSequence, CharSequence) toString(",", "(", ")")}. */
  public String toString() {
    if (this.value == 0)
      return "()";
    final StringBuilder sb = new StringBuilder("(");
    this.forEach(b -> sb.append(b).append(','));
    sb.setCharAt(sb.length()-1, ')');
    return sb.toString();
  }
  
  /** String representation of the set, separated by the specified delimiter, with the specified prefix and suffix, in natural order. */
  public String toString(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
    if (this.value == 0)
      return "()";
    return this.stream().map(String::valueOf).collect(Collectors.joining(delimiter, prefix, suffix));
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
    var copy = this;
    while (!copy.isEmpty()) {
      copy = copy.removeTrustedByte(result[i++] = SmallSet.next(copy.value));
    }
    return result;
  }

  /**
   * Remove smallest value and consume it. The given consumer is not called if this set is empty.
   * 
   * <p>
   * This can be used like this: <code><pre>
   * SmallSet set = of(.....);
   * while (!set.isEmpty())
   *   set = set.next(b -&gt; <i>process</i>(b));
   * </pre></code>
   * 
   * However, it's easier to just use {@link #forEach(ByteConsumer)} instead.
   * 
   * @see #iterator()
   * @see #forEach(ByteConsumer)
   */
  public SmallSet next(final ByteConsumer consumer) {
    requireNonNull(consumer, "consumer");
    if (this.isEmpty())
      return this;
    final byte next = next(this.value);
    assert next >= 0 && next < 32;
    consumer.acceptAsByte(next);
    return new SmallSet(this.value & ~(1 << next));
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
    if (this.isEmpty()) return result;
    final E[] constants = type.getEnumConstants();
    var copy = this;
    while (!copy.isEmpty()) {
      final byte n;
      result.add(constants[n = SmallSet.next(copy.value)]);
      copy = copy.removeTrustedByte(n);
    }
    return result;
  }

  /**
   * Returns any of the elements.
   * 
   * @throws NoSuchElementException
   *           if this set is empty
   * @see #random(RandomGenerator, ByteConsumer)
   */
  public byte random(final RandomGenerator rng) throws NoSuchElementException {
    requireNonNull(rng, "rng");
    if (this.isEmpty())
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
   * Consumes any of the elements. The given consumer is not called if this set is empty.
   * 
   * @see #random(RandomGenerator)
   */
  public void random(final RandomGenerator rng, ByteConsumer consumer) {
    requireNonNull(rng, "rng");
    requireNonNull(consumer, "consumer");
    if (this.isEmpty())
      return;
    consumer.acceptAsByte(this.random(rng));
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
    return switch (this.size()) {
      case 0 -> identity;
      case 1 -> op.applyAsInt(identity, log(this.value));
      default -> {
        int result = identity;
        var copy = this;
        while (!copy.isEmpty()) {
          final var next = SmallSet.next(copy.value);
          result = op.applyAsInt(result, next);
          copy = copy.removeTrustedByte(next);
        }
        yield result;
      }
    };
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
    return switch (this.size()) {
      case 0 -> OptionalInt.empty();
      case 1 -> OptionalInt.of((byte) log(this.value));
      default -> {
        var set = this.value;
        int result = Integer.numberOfTrailingZeros(set);
        int value = result + 1;
        set >>>= value;
        while (set != 0) {
          if ((set & 1) != 0)
            result = op.applyAsInt(result, value);
          value++;
          set >>>= 1;
        }
        yield OptionalInt.of(result);
      }
    };
  }

  /**
   * The sum of all values. This returns 0 for an empty set. <br>
   * This is equivalent to but faster than: {@code stream(set).sum()}
   */
  public int sum() {
    final int size = this.size();
    return switch (size) {
      case 0 -> 0;
      // singleton: it's just the binary logarithm of set.
      case 1 -> log(this.value); 
      // two values -> check leading/trailing zeroes:
      case 2 -> Integer.numberOfTrailingZeros(this.value) + (31 - Integer.numberOfLeadingZeros(this.value));
      case 32 -> 496;
      default -> {
        if (size > 16) // then the complement has fewer values to count:
          yield 496 - this.complement().sum();
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
        yield result;
      }    
    };
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
   * Returns the greatest element in this set strictly less than the given
   * element, or {@code empty} if there is no such element.
   * 
   * @param e
   * @return
   */
  public OptionalByte lower(final int e) {
    return lower((byte) checkRange(e));
  }

  /**
   * Returns the greatest element in this set less than or equal to the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte floor(final byte e) {
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
   * Returns the greatest element in this set less than or equal to the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte floor(final int e) {
    return floor((byte) checkRange(e));
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
   * Returns the least element in this set greater than or equal to the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte ceiling(final int e) {
    return ceiling((byte) checkRange(e));
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
   * Returns the least element in this set strictly greater than the given
   * element, or {@code empty} if there is no such element.
   */
  public OptionalByte higher(final int e) {
    return higher((byte) checkRange(e));
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
  
  record S(int i) implements Serializable {
    Object readResolve() throws ObjectStreamException {
      return new SmallSet(i);
    }
  }
  
  Object writeReplace() {
    return new S(value);
  }
}
