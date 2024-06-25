package ch.claude_martin.smallset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unlikely-arg-type")
class ByteSetTest {
  final byte     FIVE = 5;
  final byte     OOR  = 42;
  // "U" for "universal set" because it's equal to the domain.
  final SmallSet U    = SmallSet.empty().complement();

  @Test
  void testAdd() {
    var set = new ByteSet(SmallSet.empty());
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
    var set = new ByteSet(U);
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
    var set = new ByteSet(U);
    set.clear();
    assertTrue(set.isEmpty());
    assertEquals(0, set.size());
  }

  @Test
  void testIsEmpty() {
    var set = new ByteSet(SmallSet.empty());
    assertTrue(set.isEmpty());
    set.add(FIVE);
    assertFalse(set.isEmpty());
  }

  @Test
  void testContains() {
    var set = new ByteSet(SmallSet.empty());
    for (byte i = -3; i < 40; i++) {
      var result = set.contains(i);
      assertFalse(result);
    }
    set.add(FIVE);
    assertTrue(set.contains(FIVE));
  }

  @Test
  void testForEach() {
    var set = new ByteSet(SmallSet.empty());
    AtomicInteger i = new AtomicInteger();
    set.forEach(b -> i.incrementAndGet());
    assertEquals(0, i.get());

    set.add(FIVE);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(1, i.get());

    i.set(0);
    set = new ByteSet(U);
    set.forEach(b -> i.incrementAndGet());
    assertEquals(32, i.get());
  }

  @Test
  void testIterator() {
    var set = new ByteSet(SmallSet.empty());
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
    set = new ByteSet(U);
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
    var set = SmallSet.empty().toSet();
    assertEquals(Set.of(), set);

    for (byte b = 0; b < 32; b++) {
      set.add(b);
      assertEquals(Set.copyOf(set), set);
    }

    for (byte b = 0; b < 32; b += 2) {
      set.remove(b);
      assertEquals(Set.copyOf(set), set);
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
  void testPollFirst() {
    var set = SmallSet.ofRange(FIVE, 8).toSet();
    assertEquals(OptionalByte.of(FIVE), set.pollFirst());
    assertEquals(SmallSet.ofRange(FIVE + 1, 8).toSet(), set);
  }

  @Test
  void testPollLast() {
    var set = SmallSet.ofRangeClosed(2, FIVE).toSet();
    assertEquals(OptionalByte.of(FIVE), set.pollLast());
    assertEquals(SmallSet.ofRange(2, FIVE).toSet(), set);
  }

  @Test
  void testSubSet() {
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
    assertThrows(IllegalArgumentException.class, () -> subSet.remove((byte) 12));
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
  
}
