package ch.claude_martin.smallset;

import static ch.claude_martin.smallset.SmallSet.*;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.PrimitiveIterator.OfInt;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("static-method")
public class SmallSetTest {

  /** @see #testCollect() */
  @BeforeClass
  public static void before() {
    // This leads to optimization, which reduces the overall time of the test
    // run.
    if (Runtime.getRuntime().availableProcessors() > 1) {
      ForkJoinPool.commonPool().execute(() -> {
        collect(stream(complement(empty())).parallel().filter(x -> x % 2 == 0));
      });
    }
  }

  private static final List<Byte> BAD_VALUES = asList((byte) -1, (byte) 32);

  @Test
  public final void testOf() {
    int set = of(1, 2, 3, 31);
    assertEquals(set, of(3, 2, 31, 2, 2, 1));
    assertEquals(set, of((byte) 1, (byte) 2, (byte) 3, (byte) 31));

    set = of(new HashSet<>(Arrays.<Number> asList(1, 2f, 3.0d, (short) 4, 5L, BigInteger.TEN)));
    assertEquals(of(1, 2, 3, 4, 5, 10), set);

    for (byte i : BAD_VALUES) {
      try {
        of(i);
        fail("" + i);
      } catch (Exception e) {
        // expected
      }
    }

    try {
      of((List<Number>) null);
      fail("of(null)");
    } catch (NullPointerException e) {
      // expected
    }

    for (Double d : asList((Double) null, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
      try {
        of(asList(d));
        fail("of([" + d + "])");
      } catch (NullPointerException | IllegalArgumentException e) {
        // expected
      }
    }
    {
      final Random rng = new Random();
      for (int i = 0; i < 100; i++) {
        set = rng.nextInt();
        final BitSet bs = toBitSet(set);
        assertEquals(set, of(bs));
      }
    }
  }

  @Test
  public final void testSingleton() {
    for (byte i = 0; i < 32; i++) {
      assertEquals(of(i), singleton(i));
      assertEquals(of(i), singleton(BigDecimal.valueOf(i)));
      assertEquals(of(i), singleton(Double.valueOf(i)));
      assertEquals(of(i), singleton(Long.valueOf(i)));
      assertEquals(of(i), singleton(Integer.valueOf(i)));
      assertEquals(of(i), singleton(Short.valueOf(i)));
      assertEquals(of(i), singleton(Byte.valueOf(i)));
    }

    for (byte i : BAD_VALUES) {
      try {
        singleton(i);
        fail("" + i);
      } catch (IllegalArgumentException e) {
        // expected
      }
      try {
        singleton(BigDecimal.valueOf(i));
        fail("BigDecimal of " + i);
      } catch (IllegalArgumentException e) {
        // expected
      }
    }

    try {
      singleton(Double.NaN);
      fail("NaN");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      singleton(Double.POSITIVE_INFINITY);
      fail("POSITIVE_INFINITY");
    } catch (IllegalArgumentException e) {
      // expected
    }

  }

  @Test
  public final void testEmpty() {
    assertEquals(of(new byte[0]), empty());
  }

  @Test
  public final void testContains() {
    final int set = of(1, 2, 3);
    assertTrue(contains(set, (byte) 1));
    assertTrue(contains(set, (byte) 2));
    assertTrue(contains(set, (byte) 3));
    assertFalse(contains(set, (byte) 0));
    assertFalse(contains(set, (byte) 4));

    for (byte i : BAD_VALUES) {
      try {
        contains(0, i);
        fail("" + i);
      } catch (Exception e) {
        // expected
      }
    }
    {
      EnumSet<Alphabet> bert = EnumSet.of(B, E, R, T);
      for (Enum<Alphabet> e : bert)
        assertEquals(bert.contains(e), contains(of(bert), e));

      try {
        contains(of(1, 2, 3), null);
        fail("contains( , null)");
      } catch (NullPointerException e) {
        // excepted
      }
    }

    try {
      contains(set, null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testContainsAll() throws Exception {

    final int oneTo5 = of(1, 2, 3, 4, 5);
    assertTrue(containsAll(oneTo5, toSet(oneTo5)));
    assertTrue(containsAll(oneTo5, (byte) 3, (byte) 5));
    assertFalse(containsAll(oneTo5, (byte) 3, (byte) 30));

    final EnumSet<Alphabet> alphabet = EnumSet.allOf(Alphabet.class);
    final int az = of(alphabet);
    assertTrue(containsAll(az, alphabet));
    assertTrue(containsAll(az, alphabet.stream().map(Enum::ordinal).collect(Collectors.toList())));

    for (Alphabet a1 : alphabet) {
      assertTrue(containsAll(az, EnumSet.of(a1)));
      assertTrue(containsAll(az, Arrays.asList(a1.ordinal())));
      for (Alphabet a2 : alphabet) {
        assertTrue(containsAll(az, EnumSet.of(a1, a2)));
        assertTrue(containsAll(az, Arrays.asList(a1.ordinal(), a2.ordinal())));
      }
      assertFalse(containsAll(empty(), EnumSet.of(a1)));
      assertFalse(containsAll(empty(), Arrays.asList(a1.ordinal())));
    }

    try {
      containsAll(of(1, 2, 3), (EnumSet<?>) null);
      fail("contains( , null)");
    } catch (NullPointerException e) {
      // excepted
    }
    try {
      containsAll(of(1, 2, 3), (List<Integer>) null);
      fail("contains( , null)");
    } catch (NullPointerException e) {
      // excepted
    }
  }

  @Test
  public final void testAdd() {
    int set = of(1, 2, 3);
    set = add(set, (byte) 1);
    assertEquals(of(1, 2, 3), set);
    set = add(set, (byte) 5);
    assertEquals(of(1, 2, 3, 5), set);
    set = add(set, (byte) 8);
    assertEquals(of(1, 2, 3, 5, 8), set);

    for (Byte b : BAD_VALUES) {
      try {
        add(set, b);
        fail("" + b);
      } catch (Exception e) {
        // expected
      }
    }

    {
      final EnumSet<Alphabet> abc = EnumSet.allOf(Alphabet.class);
      set = empty();

      for (Enum<Alphabet> e : abc) {
        final int tmp = set;
        set = add(set, e);
        assertEquals(union(tmp, singleton(e)), set);
      }
      assertEquals(of(abc), set);
      try {
        add(set, (Enum<?>) null);
      } catch (NullPointerException e) {
        // expected
      }
    }

  }

  @Test
  public final void testRemove() {
    int set = of(1, 2, 3);
    set = remove(set, (byte) 12);
    assertEquals(of(1, 2, 3), set);
    set = remove(set, (byte) 2);
    assertEquals(of(1, 3), set);
    set = remove(set, (byte) 2);
    assertEquals(of(1, 3), set);
    set = remove(set, (byte) 1);
    assertEquals(of(3), set);
    set = remove(set, (byte) 0);
    assertEquals(of(3), set);

    for (Byte b : BAD_VALUES) {
      try {
        remove(set, b);
        fail("" + b);
      } catch (Exception e) {
        // expected
      }
    }

    {
      final EnumSet<Alphabet> abc = EnumSet.allOf(Alphabet.class);
      set = of(abc);

      for (Enum<Alphabet> e : abc) {
        final int tmp = set;
        set = remove(set, e);
        assertEquals(minus(tmp, singleton(e)), set);
      }
      assertEquals(empty(), set);
      try {
        add(set, (Enum<?>) null);
      } catch (NullPointerException e) {
        // expected
      }
    }

  }

  @Test
  public final void testIntIterator() {
    final int set = of(1, 2, 3, 5, 7, 11, 13, 17, 31);
    for (Function<OfInt, Integer> f : Arrays.<Function<OfInt, Integer>> asList(//
        OfInt::nextInt, OfInt::next)) {
      int out = 0;
      for (OfInt itr = intIterator(set); itr.hasNext();) {
        final Integer integer = f.apply(itr);
        final int i = integer;
        final byte b = (byte) i;
        assertTrue(contains(set, b));
        out = add(out, b);
      }
      assertEquals(set, out);
    }
    {
      final BitSet bs = new BitSet();
      final OfInt itr = intIterator(set);
      itr.forEachRemaining((IntConsumer) (i -> bs.set(i)));
      assertEquals(set, of(bs));
    }
  }

  @Test
  public final void testIterator() {
    final List<Byte> bytes = asList(new Byte[] { 1, 5, 10 });
    final Set<Byte> set = new TreeSet<>();
    final ByteIterator itr = iterator(of(bytes));
    while (itr.hasNext())
      set.add(itr.next());
    assertFalse(itr.hasNext());
    try {
      itr.next();
      fail("unexpected 'next'");
    } catch (NoSuchElementException e) {
      // expected!
    }
    assertFalse(itr.hasNext());
    assertEquals(set, new TreeSet<>(bytes));
  }

  @Test
  public final void testSize() {
    assertEquals(0, size(empty()));
    assertEquals(1, size(singleton(0)));
    assertEquals(1, size(singleton(5)));
    assertEquals(4, size(of(5, 23, 4, 7)));
    assertEquals(32, size(complement(empty())));
  }

  @Test
  public final void testIsEmpty() {
    assertTrue(isEmpty(empty()));
    assertFalse(isEmpty(of(12)));
    assertFalse(isEmpty(of(1, 2, 3)));
  }

  @Test
  public void testOfRange() throws Exception {
    final int set = of(5, 6, 7);
    final int range = ofRange(5, 8);
    assertEquals(set, range);

    assertEquals(-1, ofRange(0, 32));

    for (byte a = 0; a < 32; a++)
      for (byte z = (byte) (a + 1); z <= 32; z++) {
        int actual = ofRange(a, z);
        int expected = 0;
        for (byte b = a; b < z; b++)
          expected = add(expected, b);
        String msg = "[" + a + ".." + z + "[";
        assertEquals(msg, expected, actual);
      }

    try {
      ofRange(0, 0);
      fail("0,0");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      ofRange(5, 5);
      fail("5,5");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      ofRange(5, 2);
      fail("5,2");
    } catch (IllegalArgumentException e) {
      // expected
    }

    for (Byte b : BAD_VALUES) {
      try {
        ofRange(b, 32);
        fail("" + b);
      } catch (Exception e) {
        // expected
      }
    }
  }

  @Test
  public void testOfRangeClosed() throws Exception {
    final int set = of(5, 6, 7);
    final int range = ofRangeClosed(5, 7);
    assertEquals(set, range);

    assertEquals(-1, ofRangeClosed(0, 31));

    for (byte a = 0; a < 32; a++)
      for (byte z = a; z < 32; z++) {
        int actual = ofRangeClosed(a, z);
        int expected = 0;
        for (byte b = a; b <= z; b++)
          expected = add(expected, b);
        String msg = "[" + a + ".." + z + "]";
        assertEquals(msg, expected, actual);
      }

    for (int i = 0; i < 32; i++)
      assertEquals(of(i), ofRangeClosed(i, i));

    try {
      ofRangeClosed(5, 2);
      fail("5,2");
    } catch (IllegalArgumentException e) {
      // expected
    }

    for (Byte b : BAD_VALUES) {
      try {
        ofRangeClosed(b, (byte) 32);
        fail("" + b);
      } catch (Exception e) {
        // expected
      }
    }

  }

  @Test
  public final void testToString() {
    assertEquals("()", SmallSet.toString(empty()));
    assertEquals("(5)", SmallSet.toString(of(5)));
    assertEquals("(5,31)", SmallSet.toString(of(5, 31)));
    SmallSet.toString(-1);
  }

  @Test
  public final void testToSet() {
    final Byte _0 = 0;
    final Byte _1 = 1;
    final Byte _5 = 5;
    assertEquals(Collections.EMPTY_SET, toSet(empty()));
    assertEquals(new TreeSet<>(asList(_1, _5)), toSet(of(1, 5)));
    assertEquals(new TreeSet<>(asList(_0, _1, _5)), toSet(of(0, 1, 5)));
    for (byte i = 0; i < 32; i++)
      assertEquals(new TreeSet<>(asList(i)), toSet(singleton(i)));
  }

  @Test
  public final void testToArray() {
    assertArrayEquals(new byte[0], toArray(empty()));
    assertArrayEquals(new byte[] { 1, 5 }, toArray(of(1, 5)));
    assertArrayEquals(new byte[] { 0, 1, 5 }, toArray(of(0, 1, 5)));
    for (byte i = 0; i < 32; i++)
      assertArrayEquals(new byte[] { i }, toArray(singleton(i)));
  }

  @Test
  public void testRandom() throws Exception {
    final Random rng = new Random();
    final int set = of(1, 3, 5);
    for (int i = 0; i < 100; i++)
      assertTrue(contains(set, random(set, rng)));

    assertEquals(0, random(singleton(0), rng));

    try {
      random(empty(), rng);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      random(of(5), null);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  public static enum Alphabet {
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
  }

  @Test
  public void testEnum() throws Exception {
    int set1 = of(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z);
    int set2 = of(EnumSet.allOf(Alphabet.class));
    assertEquals(set1, set2);

    EnumSet<Alphabet> enumSet = toEnumSet(set1, Alphabet.class);
    assertEquals(EnumSet.allOf(Alphabet.class), enumSet);
    for (Alphabet a : Alphabet.values()) {
      assertEquals(EnumSet.of(a), toEnumSet(singleton(a), Alphabet.class));
      for (Alphabet b : Alphabet.values())
        assertEquals(EnumSet.of(a, b), toEnumSet(of(a, b), Alphabet.class));
    }
  }

  @Test
  public void testUnion() throws Exception {
    int union = union(of(1, 2, 3), of(3, 4, 5));
    assertEquals(ofRangeClosed(1, 5), union);
  }

  @Test
  public void testIntersect() throws Exception {
    int intersetion = intersect(of(1, 2, 3), of(3, 4, 5));
    assertEquals(singleton(3), intersetion);
  }

  @Test
  public void testComplement() throws Exception {
    int complement = complement(of(1, 2, 3));
    assertEquals(union(of(0), ofRange(4, 32)), complement);
    complement = complement(of(0, 31));
    assertEquals(ofRange(1, 31), complement);

    complement = complement(of(0, 31), 0, 31);
    assertEquals(ofRange(1, 31), complement);

    complement = complement(of(0, 1, 2), 0, 5);
    assertEquals(of(3, 4, 5), complement);

    for (int i = 0; i < 32; i++) {
      complement = complement(empty(), 0, i);
      assertEquals(ofRangeClosed(0, i), complement);
    }
  }

  @Test
  public void testMinus() throws Exception {
    int minus = minus(of(1, 2, 3, 4), of(2, 3));
    assertEquals(of(1, 4), minus);
  }

  @Test
  public void testIterate() throws Exception {
    for (Byte b : iterate(of(0, 1, 3, 31))) {
      assertTrue(b >= 0);
    }
  }

  @Test
  public void testNext() throws Exception {
    final Byte[] bytes = new Byte[] { 4, 5, 12, 15, 18, 23, 25, 27, 29, 31 };
    final Set<Byte> expected = new TreeSet<>(Arrays.<Byte> asList(bytes));
    final Set<Byte> actual = new TreeSet<>();
    int set = of(expected);
    while (set != 0)
      set = next(set, b -> actual.add(b));
    assertEquals(expected, actual);

    try {
      next(0, b -> fail("not next: " + b));
      fail("next(0, ...)");
    } catch (NoSuchElementException e) {
      // expected
    }
  }

  @Test
  public void testForEach() throws Exception {
    Set<Byte> set = new TreeSet<>();
    forEach(of(1, 2, 3), set::add);
    assertEquals(toSet(of(1, 2, 3)), set);

    set.clear();
    forEach(complement(empty()), set::add);
    assertEquals(toSet(complement(empty())), set);

    set.clear();
    forEach(empty(), set::add);
    assertEquals(toSet(empty()), set);
  }

  @Test
  public void testStream() throws Exception {
    for (int i = 0; i < 32; i++)
      assertEquals((i * (i + 1)) / 2, stream(ofRangeClosed(0, i)).sum());

    {
      final TreeSet<Integer> by5 = stream(complement(empty())).filter(x -> x % 5 == 0)//
          .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
      assertEquals(new TreeSet<>(asList(0, 5, 10, 15, 20, 25, 30)), by5);
    }
  }

  @Test
  public void testCollect() throws Exception {
    final int all = complement(empty()); // = -1
    { // sequential [ x | 10 divides x ]:
      final int by10 = collect(stream(all).filter(x -> x % 10 == 0));
      assertEquals(of(asList(0, 10, 20, 30)), by10);
    }

    { // parallel [ x | x is even ]:
      int expected = empty();
      for (byte i = 0; i < 32; i += 2)
        expected = add(expected, i);
      for (int i = 0; i < 32; i++) {
        // The first run is very slow, then it gets optimized.
        // before() does that first run so this will be faster.
        int actual = collect(stream(all).parallel().filter(x -> x % 2 == 0));
        assertEquals(expected, actual);
        // Now with a Stream of Numbers (Bytes -> Integer):
        actual = collect(byteStream(all).parallel().filter(x -> x % 2 == 0).map(b -> b.intValue()));
        assertEquals(expected, actual);
      }
    }

    for (final Byte bad : BAD_VALUES) {
      try {
        collect(IntStream.of(bad));
        fail("" + bad);
      } catch (IllegalArgumentException e) {
        // Expected
      }
      try {
        collect(Stream.of(bad));
        fail("" + bad);
      } catch (IllegalArgumentException e) {
        // Expected
      }
    }
  }

  @Test
  public void testLog() throws Exception {
    assertEquals(0, log(/* 2^0 = */1));
    assertEquals(4, log(2 * 2 * 2 * 2));

    for (int i = 1; i < 32; i++)
      assertEquals(i, log(1 << i));

  }

  @Test
  public void testSum() throws Exception {
    assertEquals(0, sum(empty()));
    assertEquals(0, sum(of(0)));
    assertEquals(1, sum(of(0, 1)));

    assertEquals(17, sum(of(5, 12)));
    assertEquals(44, sum(of(11, 30, 3)));

    for (int i = 0; i < 32; i++) {
      assertEquals(i, sum(singleton(i)));
      for (int j = 0; j < 32; j++) {
        if (j == i)
          continue;
        int set2 = of(i, j);
        assertEquals(SmallSet.toString(set2), i + j, sum(set2));
        for (int k = 0; k < 32; k++) {
          if (k == i || k == j)
            continue;
          int set3 = of(i, j, k);
          assertEquals(SmallSet.toString(set3), i + j + k, sum(set3));
        }
      }
    }

    for (int i = 0; i < 32; i++) {
      assertEquals("(0,..," + i + ")", i * (i + 1) / 2, sum(ofRangeClosed(0, i)));
    }
  }

  @Test
  public void testMinMax() throws Exception {
    assertEquals(OptionalByte.empty(), max(empty()));
    assertEquals(OptionalByte.empty(), min(empty()));

    for (int i = 0; i < 32; i++) {
      assertEquals(OptionalByte.of(i), max(singleton(i)));
      assertEquals(OptionalByte.of(i), min(singleton(i)));
    }

    assertEquals(OptionalByte.of(0), min(complement(empty())));
    assertEquals(OptionalByte.of(31), max(complement(empty())));

    {
      final Random rng = new Random(System.nanoTime());
      int set = empty();
      while (true) {
        final IntSummaryStatistics stats = stream(set).summaryStatistics();
        assertEquals(stats.getMax(), (int) max(set).mapToObj(Integer::valueOf).orElse(Integer.MIN_VALUE));
        assertEquals(stats.getMin(), (int) min(set).mapToObj(Integer::valueOf).orElse(Integer.MAX_VALUE));
        final int complement = complement(set);
        if (complement == 0)
          break;
        set = add(set, random(complement, rng));
      }
    }
  }

  @Test
  public void testNavigation() throws Exception {
    final int set = of(1, 2, 3, 31);

    // FLOOR:
    assertEquals(OptionalByte.of(31), SmallSet.floor(set, (byte) 31));
    assertEquals(OptionalByte.of(3), SmallSet.floor(set, (byte) 30));
    assertEquals(OptionalByte.of(3), SmallSet.floor(set, (byte) 3));
    assertEquals(OptionalByte.of(1), SmallSet.floor(set, (byte) 1));
    assertEquals(OptionalByte.empty(), SmallSet.floor(set, (byte) 0));
    assertEquals(OptionalByte.empty(), SmallSet.floor(empty(), (byte) 0));

    // CEILING:
    assertEquals(OptionalByte.of(31), SmallSet.ceiling(set, (byte) 31));
    assertEquals(OptionalByte.of(31), SmallSet.ceiling(set, (byte) 30));
    assertEquals(OptionalByte.of(3), SmallSet.ceiling(set, (byte) 3));
    assertEquals(OptionalByte.of(1), SmallSet.ceiling(set, (byte) 1));
    assertEquals(OptionalByte.empty(), SmallSet.ceiling(empty(), (byte) 0));

    // HIGHER:
    assertEquals(OptionalByte.empty(), SmallSet.higher(set, (byte) 31));
    assertEquals(OptionalByte.of(31), SmallSet.higher(set, (byte) 30));
    assertEquals(OptionalByte.of(31), SmallSet.higher(set, (byte) 3));
    assertEquals(OptionalByte.of(2), SmallSet.higher(set, (byte) 1));
    assertEquals(OptionalByte.of(1), SmallSet.higher(set, (byte) 0));
    assertEquals(OptionalByte.empty(), SmallSet.higher(empty(), (byte) 0));

    // LOWER:
    assertEquals(OptionalByte.of(3), SmallSet.lower(set, (byte) 31));
    assertEquals(OptionalByte.of(3), SmallSet.lower(set, (byte) 30));
    assertEquals(OptionalByte.of(2), SmallSet.lower(set, (byte) 3));
    assertEquals(OptionalByte.empty(), SmallSet.lower(set, (byte) 1));
    assertEquals(OptionalByte.empty(), SmallSet.lower(set, (byte) 0));
    assertEquals(OptionalByte.empty(), SmallSet.lower(empty(), (byte) 5));
  }

  @Test
  public void testReduce() throws Exception {
    assertEquals(OptionalInt.empty(), reduce(empty(), Integer::sum));
    assertEquals(OptionalInt.of(5), reduce(singleton(5), Integer::sum));
    assertEquals(OptionalInt.of(36), reduce(of(5, 31), Integer::sum));

    assertEquals(0, reduce(empty(), 0, Integer::sum));
    assertEquals(5, reduce(singleton(5), 0, Integer::sum));
    assertEquals(36, reduce(of(5, 31), 0, Integer::sum));

    assertEquals(sum(complement(empty())), reduce(complement(empty()), 0, Integer::sum));
    assertEquals(sum(complement(empty())), reduce(complement(empty()), Integer::sum).getAsInt());
  }

  @Test
  public void testReplaceAll() throws Exception {

    assertEquals(empty(), replaceAll(empty(), i -> i * 5));

    int set = of(6, 14, 30);
    set = replaceAll(set, i -> i / 2);
    assertEquals(SmallSet.toString(set), of(3, 7, 15), set);

    set = ofRange(0, 32);
    set = replaceAll(set, i -> i);
    assertEquals(SmallSet.toString(set), ofRange(0, 32), set);

    set = ofRange(0, 32);
    set = replaceAll(set, i -> -(i - 31));
    assertEquals(SmallSet.toString(set), ofRange(0, 32), set);

    set = ofRange(0, 6);
    for (int j = 0; j < 32 - 6; j++) {
      set = replaceAll(set, i -> i + 1);
      assertEquals(SmallSet.toString(set), ofRange(j + 1, j + 7), set);
    }

    for (Byte b : BAD_VALUES) {
      try {
        replaceAll(of(15), i -> b);
        fail("bad: " + b);
      } catch (IllegalArgumentException e) {
        // expected
      }
    }

  }

  @Test
  public void testToBitSet() throws Exception {
    final Random rng = new Random();
    final BitSet bitset = new BitSet();
    int set = empty();
    // Test empty:
    assertEquals(bitset, toBitSet(set));
    // Test nonempty:
    for (int i = 0; i < 64; i++) {
      final byte value = (byte) rng.nextInt(32);
      set = add(set, value);
      bitset.set(value);
      assertEquals(bitset, toBitSet(set));
    }
    // Test all:
    bitset.set(0, 32, true);
    set = -1;
    assertEquals(bitset, toBitSet(set));
  }

  @Test
  public void testPowerset() throws Exception {
    final Collector<Integer, ?, Set<Integer>> toSet = Collectors.toSet();
    // Very large result, but this should be lazy:
    powerset(complement(empty()));

    int set = empty();
    IntStream ps = powerset(set);
    assertEquals(new HashSet<>(asList(empty())), ps.boxed().collect(toSet));
    Set<Integer> collected;

    for (int i : asList(0, 5, 31)) {
      set = singleton(i);
      ps = powerset(set);
      collected = ps.boxed().collect(toSet);
      assertEquals(new HashSet<>(asList(empty(), singleton(i))), collected);
    }

    set = of(7, 12, 31);
    ps = powerset(set);
    collected = ps.boxed().collect(toSet);

    assertEquals(1 << size(set), collected.size());
    assertTrue(collected.contains(empty()));
    assertTrue(collected.contains(singleton(7)));
    assertTrue(collected.contains(singleton(12)));
    assertTrue(collected.contains(singleton(31)));
    assertTrue(collected.contains(of(7, 12)));
    assertTrue(collected.contains(of(12, 31)));
    assertTrue(collected.contains(of(7, 31)));
    assertTrue(collected.contains(of(7, 12, 31)));

    set = of(0, 3, 5, 7, 11, 13, 31);
    ps = powerset(set);
    assertEquals(1 << size(set), ps.distinct().count());

  }
}
