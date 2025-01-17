package ch.claude_martin.smallset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unlikely-arg-type")
class ByteSetTest {
  final byte     FIVE = 5;
  final byte     OOR  = 42;
  // "U" for "universal set" because it's equal to the domain.
  final SmallSet U    = SmallSet.empty().complement();

  @Test
  void testAdd() {
    var set = new BasicByteSet(SmallSet.empty());
    for (int i = 0; i < 3; i++) {
      var result = set.add(FIVE);
      assertEquals(i == 0, result);
      assertEquals(Set.of(FIVE), set);
    }

    assertThrows(IllegalArgumentException.class, () -> set.add(OOR));

    for (byte i = 0; i < 32; i++) {
      var result = set.add(i);
      assertEquals(i != FIVE, result);
    }
  }

  @Test
  void testRemove() {
    var set = new BasicByteSet(U);
    assertFalse(set.remove(Integer.valueOf(7)));
    assertFalse(set.remove("5"));

    for (int i = 0; i < 3; i++) {
      var result = set.remove(FIVE);
      assertEquals(i == 0, result);
      assertFalse(set.contains(FIVE));
      assertEquals(31, set.size());
      assertEquals(SmallSet.singleton(FIVE).complement().toSet(), set);
    }
    for (byte i = 0; i < 32; i++) {
      var result = set.remove(i);
      assertEquals(i != FIVE, result);
    }
    set.add(FIVE);
    for (byte i = -3; i < 40; i++) {
      var result = set.remove(i);
      assertEquals(i == FIVE, result);
    }

    assertTrue(set.isEmpty());
    assertFalse(set.remove(Byte.valueOf(FIVE)));
    assertFalse(set.remove(OOR));
  }

  @Test
  void testClear() {
    var set = new BasicByteSet(U);
    set.clear();
    assertTrue(set.isEmpty());
    assertEquals(0, set.size());
  }

  @Test
  void testIsEmpty() {
    var set = new BasicByteSet(SmallSet.empty());
    assertTrue(set.isEmpty());
    set.add(FIVE);
    assertFalse(set.isEmpty());
  }

  @Test
  void testContains() {
    var set = new BasicByteSet(SmallSet.empty());
    for (byte i = -3; i < 40; i++) {
      var result = set.contains(i);
      assertFalse(result);
    }
    set.add(FIVE);
    assertTrue(set.contains(FIVE));
  }

  @Test
  void testForEach() {
    var set = new BasicByteSet(SmallSet.empty());
    AtomicInteger i = new AtomicInteger();
    set.forEach(b -> i.incrementAndGet());
    assertEquals(0, i.get());

    set.add(FIVE);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(1, i.get());

    i.set(0);
    set = new BasicByteSet(U);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(32, i.get());
  }

  @Test
  void testIterator() {
    var set = new BasicByteSet(SmallSet.empty());
    AtomicInteger i = new AtomicInteger();
    for (Byte b : set)
      i.incrementAndGet();
    assertEquals(0, i.get());

    set.add(FIVE);
    for (Byte b : set)
      i.set(b.intValue());
    assertEquals(5, i.get());

    assertEquals(FIVE, set.iterator().nextByte());

    i.set(0);
    set = new BasicByteSet(U);
    for (Byte b : set)
      i.getAndIncrement();
    assertEquals(32, i.get());

    i.set(0);
    var itr = set.iterator();
    for (byte b = 0; b <= FIVE; b++) {
      assertEquals(b, itr.nextByte());
    }
    itr.remove();
    assertEquals(SmallSet.singleton(FIVE).complement().toSet(), set);
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

    assertEquals(U.toSet(), set);
    assertEquals(set, U.toSet());

    for (byte b = 0; b < 32; b += 2) {
      set.remove(b);
      assertEquals(Set.copyOf(set), set);
      assertEquals(set, Set.copyOf(set));
      assertEquals(set, set);
    }
  }

  @Test
  void testClone() {
    var set = SmallSet.singleton(FIVE).toSet();
    var clone = set.clone();
    assertNotSame(clone, set);
    assertEquals(clone, set);
    clone.remove(FIVE);
    assertNotEquals(clone, set);
    assertEquals(SmallSet.singleton(FIVE).toSet(), set);
    assertTrue(clone.isEmpty());
  }

  @Test
  void testPollFirstAsOptionalByte() {
    var set = SmallSet.ofRange(FIVE, 8).toSet();
    assertEquals(OptionalByte.of(FIVE), set.pollFirstAsOptionalByte());
    assertEquals(SmallSet.ofRange(FIVE + 1, 8).toSet(), set);
  }

  @Test
  void testPollLastAsOptionalByte() {
    var set = SmallSet.ofRangeClosed(2, FIVE).toSet();
    assertEquals(OptionalByte.of(FIVE), set.pollLastAsOptionalByte());
    assertEquals(SmallSet.ofRange(2, FIVE).toSet(), set);
  }

  @Test
  void testToArray() {
    var set = SmallSet.singleton(FIVE).toSet();
    assertArrayEquals(new Byte[] { FIVE }, set.toArray());
    set = U.toSet();
    assertEquals(32, set.toArray().length);
    assertArrayEquals(U.stream().toArray(), set.toArray());
  }

  @Test
  void testContainsAll() {
    var set = SmallSet.singleton(FIVE).toSet();
    assertTrue(set.containsAll(set));
    assertTrue(set.containsAll(Set.of()));
    assertFalse(set.containsAll(Set.of("5")));

    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertTrue(set.containsAll(set));
      assertTrue(set.containsAll(Set.of()));
      assertFalse(set.containsAll(Set.of("5")));
      assertTrue(set.containsAll(List.of(FIVE)));
    }
  }

  @Test
  void testAddAll() {
    var set = SmallSet.empty().toSet();
    assertTrue(set.addAll(FIVE, FIVE, FIVE));
    assertFalse(set.addAll(FIVE, FIVE));
    assertTrue(set.addAll(List.of((byte) 8, (byte) 1)));
    assertEquals(Set.of((byte) 1, FIVE, (byte) 8), set);
  }

  @Test
  void testRetainAll() {
    var set = U.toSet();

    set.retainAll(List.of());
    assertTrue(set.isEmpty());

    set = U.toSet();
    set.retainAll(List.of(FIVE));

    assertEquals(Set.of(FIVE), set);
  }

  @Test
  void testRemoveAll() {
    var set = U.toSet();

    set.removeAll(List.of());
    assertEquals(U, set.toSmallSet());
    set.removeAll(List.of(FIVE));
    assertFalse(set.contains(FIVE));
    assertEquals(31, set.size());
    set.removeAll(U.toSet());
    assertTrue(set.isEmpty());
  }

  @Test
  void testNavigableSet() {
    {
      var original = U.toSet();
      subSetTest(original, original.subSet((byte) 5, (byte) 7));
      original = U.toSet();
      subSetTest(original, original.subSet((byte) 5, true, (byte) 6, true));
    }
    {
      var reversed = U.toSet().reversed();
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
      var set = U.toSet();
      var reversed = set.reversed();
      assertEquals(set, reversed);
      var reversedTwice = reversed.reversed();
      assertSame(set, reversedTwice);
      assertEquals((byte) 0, set.first());
      assertEquals((byte) 31, set.last());
    }
    {
      var set = U.toSet();
      Set<Byte> subSet = set.subSet(FIVE, (byte) 12);
      assertEquals(SmallSet.ofRange(FIVE, (byte) 12).toSet(), subSet);

      assertTrue(subSet.remove(FIVE));
      assertFalse(subSet.contains(FIVE));
      assertFalse(set.contains(FIVE));

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
      var set = U.toSet();
      ByteSet subSet = set.subSet(FIVE, (byte) 12);
      assertEquals(FIVE, subSet.pollFirst());
      assertEquals(U.toSet().subSet((byte) 6, (byte) 12), subSet);
      assertEquals(U.remove(FIVE).toSet(), set);
      assertEquals((byte) 11, subSet.pollLast());
      assertEquals(U.toSet().subSet((byte) 6, (byte) 11), subSet);
      assertEquals(U.remove(FIVE).remove((byte) 11).toSet(), set);
      assertSame(subSet.subSet((byte) 2, (byte) 16), subSet);
      assertSame(set.subSet((byte) 0, (byte) 32), set);
    }
    {
      var set = U.toSet();
      ByteSet subSet = set.subSet(FIVE, (byte) 12);
      var itr = subSet.iterator();
      assertEquals(FIVE, itr.next());
      itr.remove();
      assertEquals((byte) 6, subSet.first());
      assertEquals(U.remove(5).toSet(), set);
    }
    {
      var set = U.toSet();
      ByteSet subSet = set.subSet(FIVE, (byte) 12).descendingSet();
      var itr = subSet.descendingIterator();
      assertEquals(FIVE, itr.next());
      itr.remove();
      assertEquals((byte) 6, subSet.first());
      assertEquals(U.remove(5).toSet(), set);
    }
    {
      var set = U.toSet();
      ByteSet subSet = set.subSet(FIVE, (byte) 12);
      var itr = subSet.descendingIterator();
      assertEquals((byte) 11, itr.next());
      itr.remove();
      assertEquals((byte) 10, subSet.last());
      assertEquals(U.remove(11).toSet(), set);
    }
    {
      var set = U.toSet();
      ByteSet subSet = set.subSet(FIVE, (byte) 12).descendingSet();
      var itr = subSet.iterator();
      assertEquals((byte) 11, itr.next());
      itr.remove();
      assertEquals((byte) 10, subSet.last());
      assertEquals(U.remove(11).toSet(), set);
    }
  }

  void subSetTest(ByteSet original, ByteSet subSetFrom5To7) {
    assertEquals(U.toSet(), original);
    assertEquals(SmallSet.ofRange(5, 7).toSet(), subSetFrom5To7);
    assertTrue(subSetFrom5To7.remove((byte) 6));
    assertFalse(subSetFrom5To7.remove((byte) 6));
    assertEquals(U.remove((byte) 6).toSet(), original);
  }

}
