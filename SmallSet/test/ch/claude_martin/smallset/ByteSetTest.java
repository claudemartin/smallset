package ch.claude_martin.smallset;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
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
  final static byte FIVE = 5;
  final static byte OOR  = 42;
  // "U" for "universal set" because it's equal to the domain.
  final static SmallSet    U    = SmallSet.empty().complement();

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
    final StringBuilder sb = new StringBuilder();
    final AtomicInteger trial = new AtomicInteger(0);
    final AtomicInteger step = new AtomicInteger(0);
    final Supplier<String> getMessage = () -> {
      sb.append("/* trial: ").append(trial.get()).append(" */\n");
      sb.append("/* step: ").append(step.get()).append(" */\n");
      return sb.toString();
    };
    while (trial.getAndIncrement() < 1000) {
      var set = SmallSet.empty();
      for (byte b = 0; b < 32; b++) {
        if (random.nextBoolean()) {
          set = set.add(b);
        }
      }
      final var actual = set.toSet();
      final var expected = new java.util.TreeSet<Byte>();
      expected.addAll(actual);

      sb.setLength(0);
      // sb.append(actual.toSmallSet().toString(",", "\nvar set = SmallSet.of(", ").toSet();\n"));

      NavigableSet<Byte> a = actual;
      NavigableSet<Byte> e = expected;

      while (step.getAndIncrement() < 200) {
        try {
          final var range = ((AbstractByteSet) a).range().toSmallSet();
          boolean aFailed = false, eFailed = false;
          switch (random.nextInt(14)) {
            case 0 -> {
              // sb.append("set = set.reversed();\n");
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
                // sb.append("set = set.subSet((byte)")
                // .append(x).append(",")
                // .append(xi).append(",(byte)")
                // .append(y).append(",")
                // .append(yi).append(");\n");
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
                // sb.append("set = set.headSet((byte)")
                // .append(x).append(",")
                // .append(inclusive).append(");\n");
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
                // sb.append("set = set.tailSet((byte)")
                // .append(x).append(",")
                // .append(inclusive).append(");\n");
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
                // sb.append("set.add((byte)").append(x).append(");\n");
                assertEquals(e.add(x), a.add(x), getMessage);
              }
            }
            case 5 -> {
              if (range.size() >= 2) {
                byte x = range.random(random);
                byte y = range.remove(x).random(random);
                // sb.append("set.addAll(Set.of((byte)").append(x).append(",(byte)").append(y).append("));\n");
                assertEquals(e.addAll(Set.of(x, y)), a.addAll(Set.of(x, y)), getMessage);
              }
            }
            case 6 -> {
              byte x = this.U.random(random);
              // sb.append("set.add((byte)").append(x).append(");\n");
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
              assertEquals(ra, re, getMessage);
            }
            case 7 -> {
              byte x = (byte) random.nextInt(32);
              // sb.append("set.remove((byte)").append(x).append(");\n");
              assertEquals(e.remove(x), a.remove(x), getMessage);
            }
            case 8 -> {
              // sb.append("set.pollFirst();\n");
              assertEquals(e.pollFirst(), a.pollFirst(), getMessage);
            }
            case 9 -> {
              sb.append("set.pollLast();\n");
              assertEquals(e.pollLast(), a.pollLast(), getMessage);
            }
            case 10 -> {
              byte x = (byte) random.nextInt(32);
              // sb.append("set.lower((byte)").append(x).append(");\n");
              // sb.append("set.floor((byte)").append(x).append(");\n");
              assertEquals(e.lower(x), a.lower(x), getMessage);
              assertEquals(e.floor(x), a.floor(x), getMessage);
            }
            case 11 -> {
              byte x = (byte) random.nextInt(32);
              // sb.append("set.ceiling((byte)").append(x).append(");\n");
              // sb.append("set.higher((byte)").append(x).append(");\n");
              assertEquals(e.ceiling(x), a.ceiling(x), getMessage);
              assertEquals(e.higher(x), a.higher(x), getMessage);
            }
            case 12 -> {
              // sb.append("set.first();\n");
              assertEquals(e.first(), a.first(), getMessage);
            }
            case 13 -> {
              // sb.append("set.last();\n");
              assertEquals(e.last(), a.last(), getMessage);
            }
          }
          assertEquals(aFailed, eFailed, () -> "operation failed for only one set.\n" + getMessage.get());
        } catch (java.util.NoSuchElementException ex) {
          try {
            assertTrue(e.isEmpty(), getMessage);
            assertTrue(a.isEmpty(), getMessage);
          } catch (AssertionError ae) {
            ae.addSuppressed(ex);
            throw ae;
          }
        }

        try {
          assertEquals(new java.util.ArrayList<>(e),
              new java.util.ArrayList<>(a),
              getMessage);
          assertEquals(e.size(), a.size(), getMessage);
          assertEquals(e.isEmpty(), a.isEmpty(), getMessage);
        } catch (IllegalArgumentException ex) {
          try {
            fail(getMessage);
          } catch (AssertionError ae) {
            ae.addSuppressed(ex);
            throw ae;
          }
        }

      }
    }
  }

}
