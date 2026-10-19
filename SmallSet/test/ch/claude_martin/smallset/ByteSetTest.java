package ch.claude_martin.smallset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import ch.claude_martin.smallset.AbstractByteSet.Range;

import org.junit.jupiter.api.BeforeAll;

import static ch.claude_martin.smallset.SmallSet.collect;
import static ch.claude_martin.smallset.SmallSet.collector;
import static ch.claude_martin.smallset.SmallSet.empty;
import static ch.claude_martin.smallset.SmallSet.of;
import static ch.claude_martin.smallset.SmallSet.ofRange;
import static ch.claude_martin.smallset.SmallSet.ofRangeClosed;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unlikely-arg-type")
class ByteSetTest {
  final static byte     FIVE = 5;
  final static byte     OOR  = 42;
  // "U" for "universal set" because it's equal to the domain.
  final static SmallSet U    = SmallSet.empty().complement();

  @BeforeAll
  public static void before() {
    // warm up
    U.forEach(i -> {
      var set = U.remove(i).toSet();
      set.removeIf(e -> e % 3 == 0);
      set.add(set.ceiling(FIVE));
      set.subSet((byte) (FIVE + (i % 7)), (byte) (FIVE + 10 + (i % 4))).reversed().ceiling(i);
      set.stream().mapToInt(b -> b).sum();
    });
  }

  @Test
  void testAdd() {
    {
      final var set = BasicByteSet.of(SmallSet.empty());
      for (byte i = 0; i < 3; i++) {
        var result = set.add(ByteSetTest.FIVE);
        assertEquals(i == 0, result);
        assertEquals(Set.of(ByteSetTest.FIVE), set);
        assertEquals(set.reversed(), set);
        assertEquals(set.subSet(i, (byte) 15), set);
      }

      assertThrows(IllegalArgumentException.class, () -> set.add(OOR));
      assertThrows(IllegalArgumentException.class, () -> set.reversed().add(OOR));
      assertThrows(IllegalArgumentException.class, () -> set.add((byte) -1));
      assertThrows(IllegalArgumentException.class, () -> set.reversed().add((byte) -1));

      for (byte i = 0; i < 32; i++) {
        var result = set.add(i);
        assertEquals(i != ByteSetTest.FIVE, result);
      }
      assertEquals(set, this.U.toSet());
    }
    {
      final var set = BasicByteSet.of(SmallSet.empty()).subSet((byte) 10, (byte) 20).reversed();
      assertThrows(IllegalArgumentException.class, () -> set.add(FIVE));

      set.add((byte) 15);
      for (byte i = 10; i < 20; i++) {
        var result = set.add(i);
        assertEquals(i != (byte) 15, result);
      }
    }
  }

  @Test
  void testRemove() {
    final var set = BasicByteSet.of(this.U);
    assertFalse(set.remove(Integer.valueOf(7)));
    assertFalse(set.remove("5"));
    assertFalse(set.reversed().remove("5"));
    assertFalse(set.headSet((byte) 21).remove("5"));

    for (int i = 0; i < 3; i++) {
      var result = set.remove(ByteSetTest.FIVE);
      assertEquals(i == 0, result);
      assertFalse(set.contains(ByteSetTest.FIVE));
      assertFalse(set.reversed().contains(ByteSetTest.FIVE));
      assertFalse(set.tailSet((byte) i).contains(ByteSetTest.FIVE));
      assertEquals(31, set.reversed().size());
      assertEquals(15, set.headSet((byte) 16, false).size());
      assertEquals(SmallSet.singleton(ByteSetTest.FIVE).complement().toSet(), set);
    }
    for (byte i = 0; i < 32; i++) {
      var result = set.remove(i);
      assertEquals(i != ByteSetTest.FIVE, result);
      assertFalse(set.reversed().remove(i));
      assertFalse(set.headSet(ByteSetTest.FIVE, true).remove(i));
    }
    assertEquals(set, Set.of());
    set.add(ByteSetTest.FIVE);
    for (byte i = -3; i < 40; i++) {
      var result = set.remove(i);
      assertEquals(i == ByteSetTest.FIVE, result);
    }
    set.add(ByteSetTest.FIVE);
    for (byte i = -3; i < 40; i++) {
      var result = set.reversed().subSet((byte) 5, true, (byte) 5, true).remove(i);
      assertEquals(i == ByteSetTest.FIVE, result);
    }
    set.add(ByteSetTest.FIVE);
    for (byte i = -3; i < 40; i++) {
      var result = set.subSet((byte) Math.clamp(i, 0, 5), true, (byte) 17, true).reversed().remove(i);
      assertEquals(i == ByteSetTest.FIVE, result);
    }

    for (ByteSet set2 : List.of(set, set.reversed(), set.headSet((byte) 21), set.reversed().reversed(),
        set.reversed().subSet((byte) 12, (byte) 2))) {
      assertTrue(set.isEmpty());
      assertFalse(set.remove(Byte.valueOf(ByteSetTest.FIVE)));
      assertFalse(set.remove(ByteSetTest.OOR));
      assertFalse(set.remove((byte) -1));
      assertFalse(set.remove((byte) -400));
    }
  }

  @Test
  void testClear() {
    var set = BasicByteSet.of(this.U);
    set.clear();
    assertTrue(set.isEmpty());
    assertEquals(0, set.size());
  }

  @Test
  void testIsEmpty() {
    var set = BasicByteSet.of(SmallSet.empty());
    assertTrue(set.isEmpty());
    set.add(ByteSetTest.FIVE);
    assertFalse(set.isEmpty());
  }

  @Test
  void testContains() {
    var set = BasicByteSet.of(SmallSet.empty());
    for (byte i = -3; i < 40; i++) {
      var result = set.contains(i);
      assertFalse(result);
    }
    set.add(ByteSetTest.FIVE);
    assertTrue(set.contains(ByteSetTest.FIVE));
  }

  @Test
  void testForEach() {
    var set = BasicByteSet.of(SmallSet.empty());
    AtomicInteger i = new AtomicInteger();
    set.forEach(b -> i.incrementAndGet());
    assertEquals(0, i.get());

    set.add(ByteSetTest.FIVE);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(1, i.get());

    i.set(0);
    set = BasicByteSet.of(this.U);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(32, i.get());
  }

  @Test
  void testIterator() {
    var set = BasicByteSet.of(SmallSet.empty());
    AtomicInteger i = new AtomicInteger();
    for (Byte b : set) {
      i.incrementAndGet();
    }
    assertEquals(0, i.get());

    set.add(ByteSetTest.FIVE);
    for (Byte b : set) {
      i.set(b.intValue());
    }
    assertEquals(5, i.get());

    assertEquals(ByteSetTest.FIVE, set.iterator().nextByte());

    i.set(0);
    set = BasicByteSet.of(this.U);
    for (Byte b : set) {
      i.getAndIncrement();
    }
    assertEquals(32, i.get());

    i.set(0);
    var itr = set.iterator();
    for (byte b = 0; b <= ByteSetTest.FIVE; b++) {
      assertEquals(b, itr.nextByte());
    }
    itr.remove();
    assertEquals(SmallSet.singleton(ByteSetTest.FIVE).complement().toSet(), set);
  }

  @Test
  void testSize() {
    var set = SmallSet.empty().toSet();
    assertEquals(0, set.size());
    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertEquals(1 + b, set.size());
    }
  }

  @Test
  void testHashCode() {
    var set = SmallSet.empty().toSet();
    assertEquals(Set.of().hashCode(), set.hashCode());

    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertEquals(Set.copyOf(set).hashCode(), set.hashCode());
    }

    for (byte b = 0; b < 32; b += 2) {
      set.remove(b);
      assertEquals(Set.copyOf(set).hashCode(), set.hashCode());
    }
  }

  @Test
  void testEquals() {
    assertEquals(this.U.toSet(), this.U.toSet());
    assertEquals(this.U, this.U.toSet().toSmallSet());

    final var set = SmallSet.empty().toSet();
    assertEquals(Set.of(), set);
    assertEquals(set, Set.of());
    assertEquals(set, set);

    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertEquals(Set.copyOf(set), set);
      assertEquals(set, Set.copyOf(set));
      assertEquals(set, set);
    }

    assertEquals(this.U.toSet(), set);
    assertEquals(set, this.U.toSet());

    for (byte b = 0; b < 32; b += 2) {
      set.remove(b);
      assertEquals(Set.copyOf(set), set);
      assertEquals(set, Set.copyOf(set));
      assertEquals(set, set);
    }

    {
      final var filtered = this.U.filter(b -> b % 5 == 0);
      assertEquals(filtered.toSet(), filtered.toSet());
      assertEquals(filtered.toSet(), filtered.toSet().reversed());
      assertEquals(filtered.toSet(), filtered.toSet().reversed().reversed());
      assertEquals(filtered.toSet(), filtered.toSet().tailSet((byte) 0));
      assertEquals(filtered.toSet(), filtered.toSet().reversed().tailSet((byte) 31));
      assertEquals(filtered.toSet(), filtered.toSet().headSet((byte) 31));
      assertEquals(filtered.toSet(), filtered.toSet().headSet((byte) 31).reversed());
    }
  }

  @Test
  void testClone() {
    var set = SmallSet.singleton(ByteSetTest.FIVE).toSet();
    var clone = set.clone();
    assertNotSame(clone, set);
    assertEquals(clone, set);
    clone.remove(ByteSetTest.FIVE);
    assertNotEquals(clone, set);
    assertEquals(SmallSet.singleton(ByteSetTest.FIVE).toSet(), set);
    assertTrue(clone.isEmpty());
  }

  @Test
  void testPollFirstAsOptionalByte() {
    var set = SmallSet.ofRange(ByteSetTest.FIVE, 8).toSet();
    assertEquals(OptionalByte.of(ByteSetTest.FIVE), set.pollFirstAsOptionalByte());
    assertEquals(SmallSet.ofRange(ByteSetTest.FIVE + 1, 8).toSet(), set);
  }

  @Test
  void testPollLastAsOptionalByte() {
    var set = SmallSet.ofRangeClosed(2, ByteSetTest.FIVE).toSet();
    assertEquals(OptionalByte.of(ByteSetTest.FIVE), set.pollLastAsOptionalByte());
    assertEquals(SmallSet.ofRange(2, ByteSetTest.FIVE).toSet(), set);
  }

  @Test
  void testToArray() {
    var set = SmallSet.singleton(ByteSetTest.FIVE).toSet();
    assertArrayEquals(new Byte[] { ByteSetTest.FIVE }, set.toArray());
    set = this.U.toSet();
    assertEquals(32, set.toArray().length);
    assertArrayEquals(this.U.stream().toArray(), set.toArray());
  }

  @Test
  void testContainsAll() {
    var set = SmallSet.singleton(ByteSetTest.FIVE).toSet();
    assertTrue(set.containsAll(set));
    assertTrue(set.containsAll(Set.of()));
    assertFalse(set.containsAll(Set.of("5")));

    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertTrue(set.containsAll(set));
      assertTrue(set.containsAll(Set.of()));
      assertFalse(set.containsAll(Set.of("5")));
      assertTrue(set.containsAll(List.of(ByteSetTest.FIVE)));
    }
  }

  @Test
  void testAddAll() {
    var set = SmallSet.empty().toSet();
    assertTrue(set.addAll(ByteSetTest.FIVE, ByteSetTest.FIVE, ByteSetTest.FIVE));
    assertFalse(set.addAll(ByteSetTest.FIVE, ByteSetTest.FIVE));
    assertTrue(set.addAll(List.of((byte) 8, (byte) 1)));
    assertEquals(Set.of((byte) 1, ByteSetTest.FIVE, (byte) 8), set);
  }

  @Test
  void testRetainAll() {
    var set = this.U.toSet();

    set.retainAll(List.of());
    assertTrue(set.isEmpty());

    set = this.U.toSet();
    set.retainAll(List.of(ByteSetTest.FIVE));

    assertEquals(Set.of(ByteSetTest.FIVE), set);
  }

  @Test
  void testRemoveAll() {
    var set = this.U.toSet();

    set.removeAll(List.of());
    assertEquals(this.U, set.toSmallSet());
    set.removeAll(List.of(ByteSetTest.FIVE));
    assertFalse(set.contains(ByteSetTest.FIVE));
    assertEquals(31, set.size());
    set.removeAll(this.U.toSet());
    assertTrue(set.isEmpty());
  }

  @Test
  void testRange() {
    for (boolean a : Set.of(true, false)) {
      for (boolean b : Set.of(true, false)) {
        Range range = new Range(FIVE, a, FIVE, b);
        assertEquals(a && b, range.inRange(FIVE), range + ".inRange(FIVE)");
        assertEquals(a == b, range.inRange(range), range + ".inRange(" + range + ")");
        assertEquals(a && b, range.inRange(new Range(FIVE, true, FIVE, true)),
            range + ".inRange(new Range(FIVE, true, FIVE, true))");
      }
    }

    {
      Range range = new Range(FIVE, false, (byte) 21, false);
      assertFalse(range.inRange(FIVE), range + ".inRange(FIVE)");
      for (byte i = 6; i < 21; i++) {
        assertTrue(range.inRange(i), range + ".inRange(" + i + ")");
      }
      assertFalse(range.reverse().inRange(FIVE), range.reverse() + ".inRange(FIVE)");
    }
    {
      Range range = new Range(FIVE, false, FIVE, true);
      for (byte i = 0; i < 32; i++) {
        assertFalse(range.inRange(i), range + ".inRange(" + i + ")");
      }
      assertFalse(range.inRange(range.reverse()), range + ".inRange(" + range.reverse() + ")");
      assertFalse(range.reverse().inRange(range), range.reverse() + ".inRange(" + range + ")");
    }
    {
      Range range = new Range(FIVE, false, (byte) 6, false);
      for (byte i = 0; i < 32; i++) {
        assertFalse(range.inRange(i), range + ".inRange(" + i + ")");
      }
      assertTrue(range.inRange(range), range + ".inRange(" + range + ")");
      assertTrue(range.inRange(range.reverse()), range + ".inRange(" + range.reverse() + ")");
      assertTrue(range.reverse().inRange(range), range.reverse() + ".inRange(" + range + ")");
    }
  }

  @Test
  void testSubSet() {
    {
      ByteSet set = empty().toSet().subSet((byte) 5, false, (byte) 7, false).reversed();
      assertThrows(IllegalArgumentException.class, () -> set.subSet((byte) 5, false, (byte) 5, true));
    }
    // see below for a randomised version with more operations.
    for (byte first : Set.of((byte) 0, (byte) 7, (byte) 13)) {
      for (byte last : Set.of((byte) 13, (byte) 17, (byte) 31)) {
        for (boolean fullRange : Set.of(true, false)) {
          final NavigableSet<Byte> set = (fullRange ? SmallSet.ofRangeClosed(first, last) : SmallSet.of(first, last))
              .toSet();
          final NavigableSet<Byte> reference = new TreeSet<>();
          reference.addAll(set);

          for (boolean reversed : Set.of(true, false)) {
            for (byte fromElement : Set.of((byte) 5, (byte) 6, (byte) 12)) {
              for (boolean fromInclusive : Set.of(true, false)) {
                for (byte toElement : Set.of((byte) 5, (byte) 6, (byte) 16)) {
                  for (boolean toInclusive : Set.of(true, false)) {
                    Object result;
                    try {
                      result = (reversed ? set.reversed() : set)
                          .subSet(fromElement, fromInclusive, toElement, toInclusive);
                    } catch (Throwable t) {
                      result = t;
                    }

                    Object resultReference;
                    try {
                      resultReference = (reversed ? reference.reversed() : reference)
                          .subSet(fromElement, fromInclusive, toElement, toInclusive);
                    } catch (Throwable t) {
                      resultReference = t;
                    }

                    try {
                      if (resultReference instanceof Throwable) {
                        // The messages are different.
                        assertEquals(resultReference.getClass(), result.getClass(),
                            "Creating sub sets didn't throw the same type of exception.");
                      } else {
                        assertEquals(resultReference, result, "Creating sub sets didn't return an equal result.");

                        Object result2;
                        try {
                          result2 = ((Set<Byte>) result).add((byte) 14);
                        } catch (Throwable t) {
                          result2 = t;
                        }

                        Object resultReference2;
                        try {
                          resultReference2 = ((Set<Byte>) resultReference).add((byte) 14);
                        } catch (Throwable t) {
                          resultReference2 = t;
                        }

                        if (resultReference2 instanceof Throwable) {
                          // The messages are different.
                          assertEquals(resultReference2.getClass(), result2.getClass(),
                              "Adding 14 didn't throw the same type of exception.");
                        } else {
                          assertEquals(resultReference2, result2, "Adding 14 didn't work the same on both.");
                          assertEquals(resultReference, result, "Adding 14 didn't result in the same sets.");
                        }
                      }
                    } catch (AssertionError e) {
                      e.addSuppressed(new RuntimeException("(5, 7, 21)" + (reversed ? ".reversed()" : "")
                          + ".subSet(" + fromElement + ", " + fromInclusive + ", " + toElement + ", " + toInclusive
                          + ") = " + resultReference));
                      throw e;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  void testNavigableSet0() {
    {
      var original = this.U.toSet();
      assertEquals(Set.of(), original.subSet((byte) 5, false, (byte) 5, false));
      assertEquals(Set.of((byte) 5), original.subSet((byte) 5, true, (byte) 5, true));
    }
  }

  @Test
  void testNavigableSet1() {
    {
      var original = this.U.toSet();
      this.subSetTest(original, original.subSet((byte) 5, (byte) 7));
      original = this.U.toSet();
      this.subSetTest(original, original.subSet((byte) 5, true, (byte) 6, true));
      original = this.U.toSet();
      this.subSetTest(original.reversed(), original.subSet((byte) 5, true, (byte) 6, true).reversed());
    }
    {
      var reversed = this.U.toSet().reversed();
      List<Byte> expected = IntStream.iterate(31, i -> i >= 0, i -> i - 1).mapToObj(i -> (byte) i).toList();
      assertEquals(expected, reversed.stream().toList());
    }
    {
      List<Byte> reversed = new ArrayList<>();
      SmallSet.of(5, 7, 9).toSet().descendingIterator().forEachRemaining(reversed::add);
      List<Byte> expected = List.of((byte) 9, (byte) 7, (byte) 5);
      assertEquals(expected, reversed.stream().toList());
    }
    {
      var set = this.U.toSet();
      var reversed = set.reversed();
      assertEquals(set, reversed);
      var reversedTwice = reversed.reversed();
      assertSame(set, reversedTwice);
      assertEquals(set, reversedTwice);
      assertEquals((byte) 0, set.first());
      assertEquals((byte) 31, set.last());
    }
    {
      var set = this.U.toSet();
      Set<Byte> subSet = set.subSet(ByteSetTest.FIVE, (byte) 12);
      assertEquals(SmallSet.ofRange(ByteSetTest.FIVE, (byte) 12).toSet(), subSet);

      assertTrue(subSet.remove(ByteSetTest.FIVE));
      assertFalse(subSet.contains(ByteSetTest.FIVE));
      assertFalse(set.contains(ByteSetTest.FIVE));

      var itr = subSet.iterator();

      assertEquals((byte) 6, itr.next());
      assertEquals((byte) 7, itr.next());
      itr.remove();
      assertFalse(subSet.contains((byte) 7));
      assertFalse(set.contains((byte) 7));

      assertThrows(IllegalArgumentException.class, () -> subSet.add(OOR));
      assertThrows(IllegalArgumentException.class, () -> subSet.add((byte) 12));
      assertFalse(subSet.remove((byte) 12));
    }
    {
      var set = this.U.toSet();
      ByteSet subSet = set.subSet(ByteSetTest.FIVE, (byte) 12);
      assertEquals(ByteSetTest.FIVE, subSet.pollFirst());
      assertEquals(this.U.toSet().subSet((byte) 6, (byte) 12), subSet);
      assertEquals(this.U.remove(ByteSetTest.FIVE).toSet(), set);
      assertEquals((byte) 11, subSet.pollLast());
      assertEquals(this.U.toSet().subSet((byte) 6, (byte) 11), subSet);
      assertEquals(this.U.remove(ByteSetTest.FIVE).remove((byte) 11).toSet(), set);
      assertThrows(IllegalArgumentException.class, () -> subSet.subSet((byte) 2, (byte) 16));
      assertThrows(IllegalArgumentException.class, () -> subSet.subSet((byte) 0, (byte) 32));
    }
    {
      var set = this.U.toSet();
      ByteSet subSet = set.subSet(ByteSetTest.FIVE, (byte) 12);
      var itr = subSet.iterator();
      assertEquals(ByteSetTest.FIVE, itr.next());
      itr.remove();
      assertEquals((byte) 6, subSet.first());
      assertEquals(this.U.remove(5).toSet(), set);
    }
    {
      var set = this.U.toSet();
      ByteSet subSet = set.subSet(ByteSetTest.FIVE, (byte) 12).descendingSet();
      var itr = subSet.descendingIterator();
      assertEquals(ByteSetTest.FIVE, itr.next()); // natural order
      itr.remove();
      assertEquals((byte) 6, subSet.last());// reversed order
      assertEquals((byte) 11, subSet.first());// reversed order
      assertEquals(this.U.remove(5).toSet(), set);
    }
    {
      var set = this.U.toSet();
      ByteSet subSet = set.subSet(ByteSetTest.FIVE, (byte) 12);
      var itr = subSet.descendingIterator();
      assertEquals((byte) 11, itr.next());
      itr.remove();
      assertEquals((byte) 10, subSet.last());
      assertEquals(this.U.remove(11).toSet(), set);
    }
    {
      var set = this.U.toSet();
      ByteSet subSet = set.subSet(ByteSetTest.FIVE, (byte) 12).descendingSet();
      var itr = subSet.iterator();
      assertEquals((byte) 11, itr.next());
      itr.remove();
      assertEquals((byte) 10, subSet.first());
      assertEquals(this.U.remove(11).toSet(), set);
    }
  }

  void subSetTest(ByteSet original, ByteSet subSetFrom5To7) {
    assertEquals(this.U.toSet(), original);
    assertEquals(SmallSet.ofRange(5, 7).toSet(), subSetFrom5To7);
    assertTrue(subSetFrom5To7.remove((byte) 6));
    assertFalse(subSetFrom5To7.remove((byte) 6));
    assertEquals(this.U.remove((byte) 6).toSet(), original);
  }

  @Test
  void testNavigableSet2() {
    final var random = new java.util.Random(0x5EED);
    for (int trial = 0; trial < 1000; trial++) {
      var set = SmallSet.empty();
      for (byte b = 0; b < 32; b++) {
        if (random.nextBoolean()) {
          set = set.add(b);
        }
      }
      final var actual = set.toSet();
      final var expected = new java.util.TreeSet<Byte>();
      expected.addAll(actual);

      ByteSet a = actual;
      NavigableSet<Byte> e = expected;

      for (int step = 0; step < 200; step++) {
        try {
          try {
            final var range = a.range();
            boolean aFailed = false, eFailed = false;
            switch (random.nextInt(14)) {
              case 0 -> {
                try {
                  a = a.reversed();
                } catch (IllegalArgumentException ex) {
                  aFailed = true;
                }
                try {
                  e = e.reversed();
                } catch (IllegalArgumentException ex) {
                  eFailed = true;
                }
              }
              case 1 -> {
                byte x = (byte) random.nextInt(32);
                byte y = (byte) random.nextInt(32);
                if (a.comparator() == null ? x <= y
                    : a.comparator().compare(x, y) <= 0) {
                  boolean xi = random.nextBoolean();
                  boolean yi = random.nextBoolean();
                  try {
                    a = a.subSet(x, xi, y, yi);
                  } catch (IllegalArgumentException ex) {
                    aFailed = true;
                  }
                  try {
                    e = e.subSet(x, xi, y, yi);
                  } catch (IllegalArgumentException ex) {
                    eFailed = true;
                  }
                }
              }
              case 2 -> {
                if (range.size() > 3) {
                  byte x = range.random(random);
                  boolean inclusive = random.nextBoolean();
                  try {
                    a = a.headSet(x, inclusive);
                  } catch (IllegalArgumentException ex) {
                    aFailed = true;
                  }
                  try {
                    e = e.headSet(x, inclusive);
                  } catch (IllegalArgumentException ex) {
                    eFailed = true;
                  }
                }
              }
              case 3 -> {
                if (range.size() > 3) {
                  byte x = range.random(random);
                  boolean inclusive = random.nextBoolean();
                  try {
                    a = a.tailSet(x, inclusive);
                  } catch (IllegalArgumentException ex) {
                    aFailed = true;
                  }
                  try {
                    e = e.tailSet(x, inclusive);
                  } catch (IllegalArgumentException ex) {
                    eFailed = true;
                  }
                }
              }
              case 4 -> {
                if (!range.isEmpty()) {
                  byte x = range.random(random);
                  assertEquals(e.add(x), a.add(x));
                }
              }
              case 5 -> {
                if (range.size() >= 2) {
                  byte x = range.random(random);
                  byte y = range.remove(x).random(random);
                  assertEquals(e.addAll(Set.of(x, y)), a.addAll(Set.of(x, y)));
                }
              }
              case 6 -> {
                byte x = this.U.random(random);
                boolean ra = false, re = false;
                try {
                  ra = a.add(x);
                } catch (IllegalArgumentException ex) {
                  aFailed = true;
                }
                try {
                  re = e.add(x);
                } catch (IllegalArgumentException ex) {
                  eFailed = true;
                }
                assertEquals(ra, re);
              }
              case 7 -> {
                byte x = (byte) random.nextInt(32);
                assertEquals(e.remove(x), a.remove(x));
              }
              case 8 -> {
                assertEquals(e.pollFirst(), a.pollFirst());
              }
              case 9 -> {
                assertEquals(e.pollLast(), a.pollLast());
              }
              case 10 -> {
                byte x = (byte) random.nextInt(32);
                assertEquals(e.lower(x), a.lower(x));
                assertEquals(e.floor(x), a.floor(x));
              }
              case 11 -> {
                byte x = (byte) random.nextInt(32);
                assertEquals(e.ceiling(x), a.ceiling(x));
                assertEquals(e.higher(x), a.higher(x));
              }
              case 12 -> {
                assertEquals(e.first(), a.first());
              }
              case 13 -> {
                assertEquals(e.last(), a.last());
              }
            }
            assertEquals(aFailed, eFailed, () -> "operation failed for only one set.");
          } catch (java.util.NoSuchElementException ex) {
            try {
              assertTrue(e.isEmpty());
              assertTrue(a.isEmpty());
            } catch (AssertionError ae) {
              ae.addSuppressed(ex);
              throw ae;
            }
          }

          assertEquals(new java.util.ArrayList<>(e),
              new java.util.ArrayList<>(a));
          assertEquals(e.size(), a.size());
          assertEquals(e.isEmpty(), a.isEmpty());
        } catch (AssertionError ex) {
          ex.addSuppressed(new RuntimeException("trial=" + trial + ", step=" + step));
          throw ex;
        }
      }
    }
  }

  @Test
  public void testSerializable() throws Exception {
    try (final var bos = new ByteArrayOutputStream(); final var out = new ObjectOutputStream(bos)) {
      out.writeObject(empty().toSet());
      var original = of(7, 12, 31).toSet().reversed().headSet((byte) 9).reversed().subSet((byte) 10, (byte) 21);
      assertEquals(Set.of((byte) 12), original);
      out.writeObject(original);
      try (final var bis = new ByteArrayInputStream(bos.toByteArray()); final var in = new ObjectInputStream(bis)) {
        assertEquals((ByteSet) in.readObject(), empty().toSet());
        var copy = (ByteSet) in.readObject();
        assertEquals(original, copy);

        final var originalFieldOnSubSet = SubByteSet.class.getDeclaredField("original");
        originalFieldOnSubSet.setAccessible(true);
        final var originalFieldOnReversedSet = ReversedByteSet.class.getDeclaredField("original");
        originalFieldOnReversedSet.setAccessible(true);

        do {
          final var field = switch (original) {
            case SubByteSet _ -> originalFieldOnSubSet;
            case ReversedByteSet _ -> originalFieldOnReversedSet;
            default -> null;
          };
          if (field == null)
            break;
          original = (ByteSet) field.get(original);
          copy = (ByteSet) field.get(copy);

          assertEquals(original, copy);
          assertEquals(original.range(), copy.range());
        } while (true);
      }
    }
  }
}
