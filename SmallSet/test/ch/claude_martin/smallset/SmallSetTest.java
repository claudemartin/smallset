package ch.claude_martin.smallset;

import static ch.claude_martin.smallset.SmallSet.*;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.PrimitiveIterator.OfInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.random.RandomGenerator;
import java.util.stream.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SmallSetTest {

  /** @see #testCollect() */
  @BeforeAll
  public static void before() {
    // This leads to optimization, which reduces the overall time of the test run.
    collect(empty().complement().stream().parallel().filter(x -> x % 2 == 0));
  }

  private static final List<Byte> BAD_VALUES = List.of((byte) -1, (byte) 32);

  @SafeVarargs
  private static <T> T[] array(T... elements) {
    return elements;
  }

  @Test
  public final void testOf() {
    SmallSet set = of(1, 2, 3, 31);
    assertEquals(set, of(3, 2, 31, 2, 2, 1));
    assertEquals(set, of((byte) 1, (byte) 2, (byte) 3, (byte) 31));

    set = of(Set.of(1, 2f, 3.0d, (short) 4, 5L, BigInteger.TEN));
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

    for (Double d : array((Double) null, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
      try {
        of(List.of(d));
        fail("of([" + d + "])");
      } catch (NullPointerException | IllegalArgumentException e) {
        // expected
      }
    }
    {
      final Random rng = new Random();
      for (int i = 0; i < 100; i++) {
        set = new SmallSet(rng.nextInt());
        final BitSet bs = set.toBitSet();
        assertEquals(set, of(bs));
      }
    }

    SmallSet empty = of();
    assertSame(empty, empty());
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
    final SmallSet set = of(1, 2, 3);
    assertTrue(set.contains((byte) 1));
    assertTrue(set.contains((byte) 2));
    assertTrue(set.contains((byte) 3));
    assertFalse(set.contains((byte) 0));
    assertFalse(set.contains((byte) 4));

    for (byte i : BAD_VALUES) {
      assertThrows(IllegalArgumentException.class, () -> empty().contains(i), Byte.toString(i));
    }
    {
      EnumSet<Alphabet> bert = EnumSet.of(B, E, R, T);
      for (Enum<Alphabet> e : bert)
        assertEquals(bert.contains(e), of(bert).contains(e));
      assertFalse(bert.contains(X));
    }
    assertThrows(NullPointerException.class, () -> set.contains(null));
  }

  @Test
  public void testContainsAll() throws Exception {

    final SmallSet oneTo5 = of(1, 2, 3, 4, 5);
    assertTrue(oneTo5.containsAll(oneTo5.toSet()));
    assertTrue(oneTo5.containsAll((byte) 3, (byte) 5));
    assertFalse(oneTo5.containsAll((byte) 3, (byte) 30));

    final EnumSet<Alphabet> alphabet = EnumSet.allOf(Alphabet.class);
    final SmallSet az = of(alphabet);
    assertTrue(az.containsAll(alphabet));
    assertTrue(az.containsAll(alphabet.stream().map(Enum::ordinal).collect(Collectors.toList())));

    for (Alphabet a1 : alphabet) {
      assertTrue(az.containsAll(EnumSet.of(a1)));
      assertTrue(az.containsAll(List.of(a1.ordinal())));
      for (Alphabet a2 : alphabet) {
        assertTrue(az.containsAll(EnumSet.of(a1, a2)));
        assertTrue(az.containsAll(List.of(a1.ordinal(), a2.ordinal())));
      }
      assertFalse(empty().containsAll(EnumSet.of(a1)));
      assertFalse(empty().containsAll(List.of(a1.ordinal())));
    }

    try {
      of(1, 2, 3).containsAll((EnumSet<?>) null);
      fail("contains( , null)");
    } catch (NullPointerException e) {
      // excepted
    }
    try {
      of(1, 2, 3).containsAll((List<Integer>) null);
      fail("contains( , null)");
    } catch (NullPointerException e) {
      // excepted
    }
  }

  @Test
  public final void testAdd() {
    SmallSet set = of(1, 2, 3);
    set = set.add((byte) 1);
    assertEquals(of(1, 2, 3), set);
    set = set.add((byte) 5);
    assertEquals(of(1, 2, 3, 5), set);
    set = set.add((byte) 8);
    assertEquals(of(1, 2, 3, 5, 8), set);

    for (Byte b : BAD_VALUES) {
      assertThrows(IllegalArgumentException.class, () -> empty().add(b));
    }

    {
      final EnumSet<Alphabet> abc = EnumSet.allOf(Alphabet.class);
      set = empty();

      for (Enum<Alphabet> e : abc) {
        final SmallSet tmp = set;
        set = set.add(e);
        assertEquals(tmp.union(singleton(e)), set);
      }
      assertEquals(of(abc), set);
      assertThrows(NullPointerException.class, () -> empty().add((Enum<?>) null));
    }

  }

  @Test
  public final void testRemove() {
    SmallSet set = of(1, 2, 3);
    set = set.remove((byte) 12);
    assertEquals(of(1, 2, 3), set);
    set = set.remove((byte) 2);
    assertEquals(of(1, 3), set);
    set = set.remove((byte) 2);
    assertEquals(of(1, 3), set);
    set = set.remove((byte) 1);
    assertEquals(of(3), set);
    set = set.remove((byte) 0);
    assertEquals(of(3), set);

    for (Byte b : BAD_VALUES) {
      try {
        set.remove(b);
        fail("" + b);
      } catch (Exception e) {
        // expected
      }
    }

    {
      final EnumSet<Alphabet> abc = EnumSet.allOf(Alphabet.class);
      set = of(abc);

      for (Enum<Alphabet> e : abc) {
        final SmallSet tmp = set;
        set = set.remove(e);
        assertEquals(tmp.minus(singleton(e)), set);
      }
      assertEquals(empty(), set);
      try {
        set.add((Enum<?>) null);
      } catch (NullPointerException e) {
        // expected
      }
    }

  }

  @Test
  public final void testIntIterator() {
    final SmallSet set = of(1, 2, 3, 5, 7, 11, 13, 17, 31);
    for (Function<OfInt, Integer> f : List.<Function<OfInt, Integer>> of(//
        OfInt::nextInt, OfInt::next)) {
      SmallSet out = empty();
      for (OfInt itr = set.intIterator(); itr.hasNext();) {
        final Integer integer = f.apply(itr);
        final int i = integer;
        final byte b = (byte) i;
        assertTrue(set.contains(b));
        out = out.add(b);
      }
      assertEquals(set, out);
    }
    {
      final BitSet bs = new BitSet();
      final OfInt itr = set.intIterator();
      itr.forEachRemaining((IntConsumer) (i -> bs.set(i)));
      assertEquals(set, of(bs));
    }
  }

  @Test
  public final void testIterator() {
    final List<Byte> bytes = List.of(new Byte[] { 1, 5, 10 });
    final Set<Byte> set = new TreeSet<>();
    final ByteIterator itr = of(bytes).iterator();
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
    assertEquals(0, empty().size());
    assertEquals(1, singleton(0).size());
    assertEquals(1, singleton(5).size());
    assertEquals(4, of(5, 23, 4, 7).size());
    assertEquals(32, empty().complement().size());
  }

  @Test
  public final void testIsEmpty() {
    assertTrue(empty().isEmpty());
    assertFalse(of(12).isEmpty());
    assertFalse(of(1, 2, 3).isEmpty());
    assertFalse(empty().complement().isEmpty());
  }

  @Test
  public void testOfRange() throws Exception {
    final SmallSet set = of(5, 6, 7);
    final SmallSet range = ofRange(5, 8);
    assertEquals(set, range);

    assertEquals(new SmallSet(-1), ofRange(0, 32));

    for (byte a = 0; a < 32; a++)
      for (byte z = (byte) (a + 1); z <= 32; z++) {
        SmallSet actual = ofRange(a, z);
        SmallSet expected = empty();
        for (byte b = a; b < z; b++)
          expected = expected.add(b);
        String msg = "[" + a + ".." + z + "[";
        assertEquals(expected, actual, msg);
      }

    assertEquals(SmallSet.empty(), ofRange(0, 0));
    assertEquals(SmallSet.empty(), ofRange(5, 5));

    assertThrows(IllegalArgumentException.class, () -> ofRange(5, 2), "ofRange(5, 2)");
    assertThrows(IllegalArgumentException.class, () -> ofRange(-1, 32), "ofRange(-1, 32)");
    assertThrows(IllegalArgumentException.class, () -> ofRange(0, 33), "ofRange(0, 33)");
  }

  @Test
  public void testOfRangeClosed() throws Exception {
    final SmallSet set = of(5, 6, 7);
    final SmallSet range = ofRangeClosed(5, 7);
    assertEquals(set, range);

    assertEquals(new SmallSet(-1), ofRangeClosed(0, 31));

    for (byte a = 0; a < 32; a++)
      for (byte z = a; z < 32; z++) {
        SmallSet actual = ofRangeClosed(a, z);
        SmallSet expected = empty();
        for (byte b = a; b <= z; b++)
          expected = expected.add(b);
        String msg = "[" + a + ".." + z + "]";
        assertEquals(expected, actual, msg);
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
    assertEquals("()", empty().toString());
    assertEquals("(5)", of(5).toString());
    assertEquals("(5,31)", of(5, 31).toString());
    assertEquals("(1,2,3,5,9,17,31)", of(1, 2, 3, 5, 9, 17, 31).toString());
  }

  @Test
  public final void testToSet() {
    final Byte _0 = 0;
    final Byte _1 = 1;
    final Byte _5 = 5;
    assertEquals(Collections.EMPTY_SET, empty().toSet());
    assertEquals(new HashSet<>(List.of(_1, _5)), of(1, 5).toSet());
    assertEquals(Set.of(_0, _1, _5), of(0, 1, 5).toSet());
    assertEquals(Set.of(_0, _5).hashCode(), of(0, 5).toSet().hashCode());
    for (byte i = 0; i < 32; i++) {
      assertEquals(new TreeSet<>(List.of(i)), singleton(i).toSet());
      assertEquals(Set.of(i).hashCode(), singleton(i).toSet().hashCode());
    }
    for (byte i = 0; i < 32; i++) {
      for (byte j = 0; j < 32; j++) {
        if (i == j)
          continue;
        final var set = SmallSet.empty().toSet();
        assertEquals(Set.of(), set);
        assertEquals(Set.of().hashCode(), set.hashCode());
        set.add(i);
        assertEquals(Set.of(i), set);
        assertEquals(of(i).toSet(), set);
        assertEquals(Set.of(i).hashCode(), set.hashCode());
        set.add(j);
        assertEquals(Set.of(i, j), set);
        assertEquals(of(i, j).toSet(), set);
        assertEquals(Set.of(i, j).hashCode(), set.hashCode());
        assertEquals(of(i, j).toSet(), of(i, j).toSet().subSet((byte) 0, (byte) 32));
        assertEquals(empty().toSet(), of(i, j).toSet().subSet((byte) 8, (byte) 8));
        assertEquals(new TreeSet<>(Set.of(i, j)).subSet((byte) 7, (byte) 15),
            of(i, j).toSet().subSet((byte) 7, (byte) 15));
      }
    }
  }

  @Test
  public final void testToArray() {
    assertArrayEquals(new byte[0], empty().toArray());
    assertArrayEquals(new byte[] { 1, 5 }, of(1, 5).toArray());
    assertArrayEquals(new byte[] { 0, 1, 5 }, of(0, 1, 5).toArray());
    assertArrayEquals(new byte[] { 7, 31 }, of(7, 31).toArray());
    for (byte i = 0; i < 32; i++)
      assertArrayEquals(new byte[] { i }, singleton(i).toArray());
  }

  @Test
  public void testRandom() throws Exception {
    // Not random at all, so we should always easily find each value
    final var rng = new RandomGenerator() {
      char next = 0;

      @Override
      public long nextLong() {
        return next++;
      }

      @Override
      public int nextInt(int bound) {
        return next++ % bound;
      }

    };
    final SmallSet set = of(1, 3, 5, 7, 11, 13);
    for (int i = 0; i < 100; i++)
      assertTrue(set.contains(set.random(rng)));

    for (byte v : set)
      assertTrue(findRandom(rng, set, v));

    final var fullSet = empty().complement();
    for (byte v = 0; v < Integer.SIZE; ++v)
      assertTrue(findRandom(rng, fullSet, v));

    for (var r : List.of(rng, new Random(), new SecureRandom())) {
      assertEquals(6, singleton(6).random(r));
      assertThrows(NoSuchElementException.class, () -> empty().random(r));
      assertThrows(NullPointerException.class, () -> of(5).random(null));
    }

    final var i = new AtomicInteger(0);
    empty().random(rng, b -> i.incrementAndGet());
    assertEquals(0, i.get());

    set.random(rng, b -> i.incrementAndGet());
    assertEquals(1, i.get());
  }

  private boolean findRandom(RandomGenerator rng, SmallSet set, byte value) {
    for (int i = 0; i < set.size() * 10; i++)
      if (set.random(rng) == value)
        return true;
    throw new AssertionError(set + ".random(rng) never returned the value " + value);
  }

  /* Enum with 32 elements. */
  public static enum Alphabet {
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, Ä, Ö, Ü, Ë, Ï, ẞ;
  }

  @Test
  public void testEnum() throws Exception {
    SmallSet set1 = of(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, Ä, Ö, Ü, Ë, Ï, ẞ);
    SmallSet set2 = of(EnumSet.allOf(Alphabet.class));
    assertEquals(set1, set2);
    assertEquals(set1.toEnumSet(Alphabet.class), EnumSet.allOf(Alphabet.class));
    assertEquals(set1.toEnumSet(Alphabet.class), set2.toEnumSet(Alphabet.class));
    assertEquals(empty().toEnumSet(Alphabet.class), EnumSet.noneOf(Alphabet.class));

    EnumSet<Alphabet> enumSet = set1.toEnumSet(Alphabet.class);
    assertEquals(EnumSet.allOf(Alphabet.class), enumSet);
    for (Alphabet a : Alphabet.values()) {
      assertEquals(EnumSet.of(a), singleton(a).toEnumSet(Alphabet.class));
      for (Alphabet b : Alphabet.values())
        assertEquals(EnumSet.of(a, b), of(a, b).toEnumSet(Alphabet.class));
    }
  }

  @Test
  public void testUnion() throws Exception {
    final SmallSet union = of(1, 2, 3).union(of(3, 4, 5));
    assertEquals(ofRangeClosed(1, 5), union);
  }

  @Test
  public void testIntersect() throws Exception {
    final SmallSet intersetion = of(1, 2, 3).intersect(of(3, 4, 5));
    assertEquals(singleton(3), intersetion);
  }

  @Test
  public void testComplement() throws Exception {
    SmallSet complement = of(1, 2, 3).complement();
    assertEquals(of(0).union(ofRange(4, 32)), complement);
    complement = of(0, 31).complement();
    assertEquals(ofRange(1, 31), complement);

    complement = of(0, 31).complement(0, 31);
    assertEquals(ofRange(1, 31), complement);

    complement = of(0, 1, 2).complement(0, 5);
    assertEquals(of(3, 4, 5), complement);

    for (int i = 0; i < 32; i++) {
      complement = empty().complement(0, i);
      assertEquals(ofRangeClosed(0, i), complement);
    }
  }

  @Test
  public void testMinus() throws Exception {
    final SmallSet minus = of(1, 2, 3, 4).minus(of(2, 3));
    assertEquals(of(1, 4), minus);
    assertEquals(of(1, 4), of(1, 4).minus(empty()));
    assertEquals(empty(), empty().minus(of(8, 9)));
  }

  @Test
  public void testForEach() throws Exception {
    final List<Byte> list = new ArrayList<>();
    of(1, 2, 3).forEach(list::add);
    assertEquals(of(1, 2, 3).stream().toList(), list);

    list.clear();
    empty().complement().forEach(list::add);
    assertEquals(empty().complement().stream().toList(), list);

    list.clear();
    empty().forEach(list::add);
    assertEquals(empty().stream().toList(), list);

    empty().forEach(n -> fail("iteration on empty set"));
  }

  @Test
  public void testStream() throws Exception {
    for (int i = 0; i < 32; i++) {
      assertEquals((i * (i + 1)) / 2, ofRangeClosed(0, i).stream().mapToInt(n -> (int) n).sum());
      assertEquals((i * (i + 1)) / 2, ofRangeClosed(0, i).intStream().sum());
    }

    TreeSet<Integer> expected = new TreeSet<>(List.of(0, 5, 10, 15, 20, 25, 30));
    {
      var stream = empty().complement().stream();
      final TreeSet<Byte> by5 = stream.filter(x -> x % 5 == 0)//
          .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
      assertEquals(expected.stream().map(i -> i.byteValue()).collect(Collectors.toSet()), by5);
    }
    {
      var stream = empty().complement().intStream();
      final TreeSet<Integer> by5 = stream.filter(x -> x % 5 == 0)//
          .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
      assertEquals(expected, by5);
    }
  }

  @Test
  public void testCollect() throws Exception {
    final SmallSet all = empty().complement(); // = -1
    { // sequential [ x | 10 divides x ]:
      final SmallSet by10 = collect(all.stream().filter(x -> x % 10 == 0));
      assertEquals(of(List.of(0, 10, 20, 30)), by10);
    }
    { // sequential [ x | 10 divides x ]:
      final SmallSet by10 = collect(all.intStream().filter(x -> x % 10 == 0));
      assertEquals(of(List.of(0, 10, 20, 30)), by10);
    }
    { // sequential [ x | 10 divides x ]:
      final SmallSet by10 = collect(all.intStream().filter(x -> x % 10 == 0).mapToObj(Double::valueOf));
      assertEquals(of(List.of(0, 10, 20, 30)), by10);
    }

    { // parallel [ x | x is even ]:
      SmallSet expected = empty();
      for (byte i = 0; i < 32; i += 2)
        expected = expected.add(i);
      for (int i = 0; i < 32; i++) {
        // The first run is very slow, then it gets optimized.
        // before() does that first run so this will be faster.
        SmallSet actual = collect(all.intStream().parallel().filter(x -> x % 2 == 0));
        assertEquals(expected, actual);
        // Now with a Stream of Numbers (Bytes):
        actual = collect(all.stream().parallel().filter(x -> x % 2 == 0).map(b -> b.intValue()));
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
    assertEquals(0, empty().sum());
    assertEquals(0, of(0).sum());
    assertEquals(1, of(0, 1).sum());

    assertEquals(17, of(5, 12).sum());
    assertEquals(44, of(11, 30, 3).sum());
    assertEquals(IntStream.range(0, 32).sum(), empty().complement().sum());

    for (int i = 0; i < 32; i++) {
      assertEquals(i, singleton(i).sum());
      for (int j = 0; j < 32; j++) {
        if (j == i)
          continue;
        SmallSet set2 = of(i, j);
        assertEquals(i + j, set2.sum(), set2.toString());
        for (int k = 0; k < 32; k++) {
          if (k == i || k == j)
            continue;
          SmallSet set3 = of(i, j, k);
          assertEquals(i + j + k, set3.sum(), set3.toString());
          for (int l = 0; l < 32; l++) {
            if (l == i || l == j || l == k)
              continue;
            SmallSet set4 = of(i, j, k, l);
            assertEquals(i + j + k + l, set4.sum(), set4.toString());
          }
        }
      }
    }

    for (int i = 0; i < 32; i++) {
      assertEquals(i * (i + 1) / 2, ofRangeClosed(0, i).sum(), "(0,..," + i + ")");
    }
  }

  @Test
  public void testMinMax() throws Exception {
    assertEquals(OptionalByte.empty(), empty().max());
    assertEquals(OptionalByte.empty(), empty().min());

    for (int i = 0; i < 32; i++) {
      assertEquals(OptionalByte.of(i), singleton(i).max());
      assertEquals(OptionalByte.of(i), singleton(i).min());
    }

    assertEquals(OptionalByte.of(0), empty().complement().min());
    assertEquals(OptionalByte.of(31), empty().complement().max());

    {
      final Random rng = new Random(System.nanoTime());
      SmallSet set = empty();
      while (true) {
        final IntSummaryStatistics stats = set.intStream().summaryStatistics();
        assertEquals(stats.getMax(), (int) set.max().mapToObj(Integer::valueOf).orElse(Integer.MIN_VALUE));
        assertEquals(stats.getMin(), (int) set.min().mapToObj(Integer::valueOf).orElse(Integer.MAX_VALUE));
        final SmallSet complement = set.complement();
        if (complement.isEmpty())
          break;
        set = set.add(complement.random(rng));
      }
    }
  }

  @Test
  public void testNavigation() throws Exception {
    final SmallSet set = of(1, 2, 3, 31);

    // FLOOR:
    assertEquals(OptionalByte.of(31), set.floor((byte) 31));
    assertEquals(OptionalByte.of(3), set.floor((byte) 30));
    assertEquals(OptionalByte.of(3), set.floor((byte) 3));
    assertEquals(OptionalByte.of(1), set.floor((byte) 1));
    assertEquals(OptionalByte.empty(), set.floor((byte) 0));
    assertEquals(OptionalByte.empty(), empty().floor((byte) 0));

    // CEILING:
    assertEquals(OptionalByte.of(31), set.ceiling((byte) 31));
    assertEquals(OptionalByte.of(31), set.ceiling((byte) 30));
    assertEquals(OptionalByte.of(3), set.ceiling((byte) 3));
    assertEquals(OptionalByte.of(1), set.ceiling((byte) 1));
    assertEquals(OptionalByte.empty(), empty().ceiling((byte) 0));

    // HIGHER:
    assertEquals(OptionalByte.empty(), set.higher((byte) 31));
    assertEquals(OptionalByte.of(31), set.higher((byte) 30));
    assertEquals(OptionalByte.of(31), set.higher((byte) 3));
    assertEquals(OptionalByte.of(2), set.higher((byte) 1));
    assertEquals(OptionalByte.of(1), set.higher((byte) 0));
    assertEquals(OptionalByte.empty(), empty().higher((byte) 0));

    // LOWER:
    assertEquals(OptionalByte.of(3), set.lower((byte) 31));
    assertEquals(OptionalByte.of(3), set.lower((byte) 30));
    assertEquals(OptionalByte.of(2), set.lower((byte) 3));
    assertEquals(OptionalByte.empty(), set.lower((byte) 1));
    assertEquals(OptionalByte.empty(), set.lower((byte) 0));
    assertEquals(OptionalByte.empty(), empty().lower((byte) 5));
  }

  @Test
  public void testReduce() throws Exception {
    assertEquals(OptionalInt.empty(), empty().reduce(Integer::sum));
    assertEquals(OptionalInt.of(5), singleton(5).reduce(Integer::sum));
    assertEquals(OptionalInt.of(36), of(5, 31).reduce(Integer::sum));

    assertEquals(0, empty().reduce(0, Integer::sum));
    assertEquals(5, singleton(5).reduce(0, Integer::sum));
    assertEquals(36, of(5, 31).reduce(0, Integer::sum));

    assertEquals(empty().complement().sum(), empty().complement().reduce(0, Integer::sum));
    assertEquals(empty().complement().sum(), empty().complement().reduce(Integer::sum).getAsInt());
  }

  @Test
  public void testReplaceAll() throws Exception {

    assertEquals(empty(), empty().replaceAll(i -> i * 5));

    SmallSet set = of(6, 14, 30);
    set = set.replaceAll(i -> i / 2);
    assertEquals(of(3, 7, 15), set, set.toString());

    set = ofRange(0, 32);
    set = set.replaceAll(i -> i);
    assertEquals(ofRange(0, 32), set, set.toString());

    set = ofRange(0, 32);
    set = set.replaceAll(i -> -(i - 31));
    assertEquals(ofRange(0, 32), set, set.toString());

    set = ofRange(0, 6);
    for (int j = 0; j < 32 - 6; j++) {
      set = set.replaceAll(i -> i + 1);
      assertEquals(ofRange(j + 1, j + 7), set, set.toString());
    }

    for (final Byte b : BAD_VALUES) {
      assertThrows(IllegalArgumentException.class, () -> of(15).replaceAll(i -> b));
    }
  }

  @Test
  public void testToBitSet() throws Exception {
    final Random rng = new Random();
    final BitSet bitset = new BitSet();
    SmallSet set = empty();
    // Test empty:
    assertEquals(bitset, set.toBitSet());
    // Test nonempty:
    for (int i = 0; i < 64; i++) {
      final byte value = (byte) rng.nextInt(32);
      set = set.add(value);
      bitset.set(value);
      assertEquals(bitset, set.toBitSet());
    }
    // Test all:
    bitset.set(0, 32, true);
    set = empty().complement();
    assertEquals(bitset, set.toBitSet());
  }

  @Test
  public void testPowerset() throws Exception {
    final var toSet = Collectors.<SmallSet> toSet();
    // Very large result, but this should be lazy:
    empty().complement().powerset().peek(x -> {
    });

    SmallSet set = empty();
    Stream<SmallSet> ps = set.powerset();
    assertEquals(Set.of(empty()), ps.collect(toSet));
    Set<SmallSet> collected;

    for (int i : List.of(0, 5, 31)) {
      set = singleton(i);
      ps = set.powerset();
      collected = ps.collect(toSet);
      assertEquals(Set.of(empty(), singleton(i)), collected);
    }

    set = of(7, 12, 31);
    ps = set.powerset();
    collected = ps.collect(toSet);

    assertEquals(1 << set.size(), collected.size());
    assertTrue(collected.contains(empty()));
    assertTrue(collected.contains(singleton(7)));
    assertTrue(collected.contains(singleton(12)));
    assertTrue(collected.contains(singleton(31)));
    assertTrue(collected.contains(of(7, 12)));
    assertTrue(collected.contains(of(12, 31)));
    assertTrue(collected.contains(of(7, 31)));
    assertTrue(collected.contains(of(7, 12, 31)));

    set = of(0, 3, 5, 7, 11, 13, 31);
    ps = set.powerset();
    assertEquals(1 << set.size(), ps.distinct().count());
  }

  @Test
  public void testSerializable() throws Exception {
    {
      final var set = of(7, 12, 31);
      final var copy = copyOfRef(set);
      assertEquals(set, copy);
    }
    {
      final var set = of(0, 7, 13);
      final var copy = copyOfPrimitiveSmallSetInRecord(set);
      assertEquals(set, copy);
    }
  }

  private SmallSet copyOfRef(SmallSet original) throws Exception {
    final var bos = new ByteArrayOutputStream();
    final var out = new ObjectOutputStream(bos);
    out.writeObject(original);
    final var bis = new ByteArrayInputStream(bos.toByteArray());
    final var in = new ObjectInputStream(bis);
    final SmallSet copy = (SmallSet) in.readObject();
    return copy;
  }

  static record SmallSetHolder(SmallSet set) implements Serializable {
  }

  /**
   * This fails at the moment. The preview based on Java 20 can't compute the
   * correct offsets.
   */
  private SmallSet copyOfPrimitiveSmallSetInRecord(SmallSet original) throws Exception {
    final var bos = new ByteArrayOutputStream();
    final var out = new ObjectOutputStream(bos);
    final var holder = new SmallSetHolder(original);
    out.writeObject(holder);
    final var bis = new ByteArrayInputStream(bos.toByteArray());
    final var in = new ObjectInputStream(bis);
    final SmallSetHolder copy = (SmallSetHolder) in.readObject();
    return copy.set;
  }
}
