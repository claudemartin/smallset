package ch.claude_martin.smallset;

import static ch.claude_martin.smallset.SmallSet.add;
import static ch.claude_martin.smallset.SmallSet.collect;
import static ch.claude_martin.smallset.SmallSet.complement;
import static ch.claude_martin.smallset.SmallSet.contains;
import static ch.claude_martin.smallset.SmallSet.empty;
import static ch.claude_martin.smallset.SmallSet.forEach;
import static ch.claude_martin.smallset.SmallSet.intersect;
import static ch.claude_martin.smallset.SmallSet.isEmpty;
import static ch.claude_martin.smallset.SmallSet.iterate;
import static ch.claude_martin.smallset.SmallSet.iterator;
import static ch.claude_martin.smallset.SmallSet.log;
import static ch.claude_martin.smallset.SmallSet.minus;
import static ch.claude_martin.smallset.SmallSet.next;
import static ch.claude_martin.smallset.SmallSet.of;
import static ch.claude_martin.smallset.SmallSet.ofRange;
import static ch.claude_martin.smallset.SmallSet.ofRangeClosed;
import static ch.claude_martin.smallset.SmallSet.random;
import static ch.claude_martin.smallset.SmallSet.reduce;
import static ch.claude_martin.smallset.SmallSet.remove;
import static ch.claude_martin.smallset.SmallSet.singleton;
import static ch.claude_martin.smallset.SmallSet.size;
import static ch.claude_martin.smallset.SmallSet.stream;
import static ch.claude_martin.smallset.SmallSet.sum;
import static ch.claude_martin.smallset.SmallSet.toArray;
import static ch.claude_martin.smallset.SmallSet.toEnumSet;
import static ch.claude_martin.smallset.SmallSet.toSet;
import static ch.claude_martin.smallset.SmallSet.union;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.A;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.B;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.C;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.D;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.E;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.F;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.G;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.H;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.I;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.J;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.K;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.L;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.M;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.N;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.O;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.P;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.Q;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.R;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.S;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.T;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.U;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.V;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.W;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.X;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.Y;
import static ch.claude_martin.smallset.SmallSetTest.Alphabet.Z;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.Test;

@SuppressWarnings("static-method")
public class SmallSetTest {

  private static final List<Byte> BAD_VALUES = asList((byte) -1, (byte) 32);

  @Test
  public final void testOf() {
    int set = of(1, 2, 3, 31);
    assertEquals(set, of(3, 2, 31, 2, 2, 1));
    assertEquals(set, of((byte) 1, (byte) 2, (byte) 3, (byte) 31));

    set = of(new HashSet<>(Arrays.<Number> asList(1, 2f, 3.0d, (short) 4, 5L)));
    assertEquals(of(1, 2, 3, 4, 5), set);

    for (byte i : BAD_VALUES) {
      try {
        of(i);
        fail("" + i);
      } catch (Exception e) {
        // expected
      }
    }
    
    try {
      of((List<Number>)null);
    } catch (NullPointerException e) {
      // expected
    }
    
    try {
      of(asList(Integer.valueOf(12), (Integer) null));
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public final void testSingleton() {
    assertEquals(of(2), singleton(2));
    for (byte i : BAD_VALUES) {
      try {
        singleton(i);
        fail("" + i);
      } catch (IllegalArgumentException e) {
        // expected
      }
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
    final int range = ofRange((byte) 5, (byte) 8);
    assertEquals(set, range);

    assertEquals(-1, ofRange((byte) 0, (byte) 32));

    try {
      ofRange(0, 0);
      fail("0,0");
    } catch (Exception e) {
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

    assertEquals(of(0), ofRangeClosed(0, 0));

    for (Byte b : BAD_VALUES) {
      try {
        ofRange(b, (byte) 32);
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

    toSet(singleton(31));

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

    toArray(singleton(31));

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
    { // sequential [ x | 10 divides x ]:
      final int by10 = collect(stream(complement(empty())).filter(x -> x % 10 == 0));
      assertEquals(of(asList(0, 10, 20, 30)), by10);
    }

    { // parallel [ x | x is even ]:
      int expected = empty();
      for (byte i = 0; i < 32; i += 2)
        expected = add(expected, i);

      for (int i = 0; i < 10; i++) {
        final int actual = collect(stream(complement(empty())).parallel().filter(x -> x % 2 == 0));
        assertEquals(expected, actual);
      }
    }

    for (final Byte bad : BAD_VALUES) {
      try {
        collect(IntStream.of(bad));
      } catch (IllegalArgumentException e) {
        // Expected
      }
    }
  }

  @Test
  public void testLog() throws Exception {
    try {
      assertEquals(0, log(0));
    } catch (IllegalArgumentException e) {
      // expected!
    }

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
  public void testReduce() throws Exception {
    assertEquals(OptionalInt.empty(), reduce(empty(), Integer::sum));
    assertEquals(OptionalInt.empty(), reduce(singleton(5), Integer::sum));
    assertEquals(OptionalInt.of(36), reduce(of(5, 31), Integer::sum));

    assertEquals(0, reduce(empty(), 0, Integer::sum));
    assertEquals(5, reduce(singleton(5), 0, Integer::sum));
    assertEquals(36, reduce(of(5, 31), 0, Integer::sum));

    assertEquals(sum(complement(empty())), reduce(complement(empty()), 0, Integer::sum));
    assertEquals(sum(complement(empty())), reduce(complement(empty()), Integer::sum).getAsInt());

  }

}
