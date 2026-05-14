package ch.claude_martin.smallset;

import static ch.claude_martin.smallset.SmallSet.of;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.random.RandomGenerator;
import java.util.stream.*;

import org.junit.jupiter.api.*;

/// Snippets used in javadoc. See {@link Demo} for more examples. All methods are {@link #testAllSnippets()
/// tested automatically}.
class Snippets {

  void classSmallSet() {
    // @start region = classSmallSet
    SmallSet a = SmallSet.of(0, 1, 2, 3);
    SmallSet b = SmallSet.ofRange(7, 10);
    SmallSet odd = a.union(b).add(31).filter(e -> e % 2 == 1);
    IO.println(odd); // (', 1,3,7,9,31)
    // @end region = classSmallSet
    assertEquals(SmallSet.of(1, 3, 7, 9, 31), odd);
  }

  void collector() {
    // @start region = collector
    Stream<? extends Number> ints = Stream.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    SmallSet set = ints.collect(SmallSet.collector());
    // @end region = collector
    assertEquals(SmallSet.of(1, 2, 3), set);
  }

  void toInt() {
    // @start region = toInt
    SmallSet set = SmallSet.of(1, 2, 3);
    int i = set.toInt();
    IO.println(i); // 14
    // @end region = toInt
    // 2^1+2^2+2^3 = 2+4+8 = 14
    assertEquals(14, i);
  }

  void fromInt() {
    // @start region = fromInt
    SmallSet set = SmallSet.fromInt(14);
    IO.println(set); // (1,2,3)
    // @end region = fromInt
    // 2^1+2^2+2^3 = 2+4+8 = 14
    assertEquals(SmallSet.of(1, 2, 3), set);
  }

  void of() {
    {
      // @start region = ofNothing
      SmallSet empty = SmallSet.of();
      // @end region = ofNothing
    }
    {
      // @start region = ofIterable
      List<? extends Number> list = List.of(1, 2, 3);// @replace substring = '1, 2, 3' replacement = '...'
      SmallSet set = SmallSet.of(list);
      // @end region = ofIterable
      assertEquals(SmallSet.of(1, 2, 3), set);
    }
    {
      // @start region = ofBytes
      byte four = 4, six = 6;
      SmallSet set = SmallSet.of(four, six);
      // @end region = ofBytes
      assertEquals(SmallSet.of(4, 6), set);
    }
    {
      // @start region = ofInts
      SmallSet set = SmallSet.of(4, 6);
      // @end region = ofInts
      assertArrayEquals(new byte[] { 4, 6 }, set.toArray());
    }
    {
      // @start region = ofEnums
      SmallSet set1 = SmallSet.of(Month.MAY, Month.JULY);
      SmallSet set2 = SmallSet.of(Month.MAY.ordinal(), Month.JULY.ordinal());
      assert set1 == set2;
      // @end region = ofEnums
      assertEquals(EnumSet.of(Month.MAY, Month.JULY), set1.toEnumSet(Month.class));
      assertEquals(set1, set2);
    }
    {
      // @start region = ofEnumSet
      EnumSet<Month> months = EnumSet.of(Month.MAY, Month.JULY);
      SmallSet set = SmallSet.of(months);
      // @end region = ofEnumSet
      assertEquals(SmallSet.of(Month.MAY, Month.JULY), set);
    }
    {
      // @start region = ofBitSet
      BitSet bits = new BitSet();
      bits.set(4);
      bits.set(6);
      SmallSet set = SmallSet.of(bits);
      // @end region = ofBitSet
      assertEquals(SmallSet.of(4, 6), set);
    }
  }

  void singleton() {
    {
      // @start region = singletonByte
      byte value = 16;
      SmallSet set = SmallSet.singleton(value);
      // @end region = singletonByte
      assertEquals(SmallSet.of(16), set);
    }
    {
      // @start region = singletonInt
      SmallSet set = SmallSet.singleton(16);
      // @end region = singletonInt
      assertEquals(SmallSet.of(16), set);
    }
    {
      // @start region = singletonEnum
      SmallSet set = SmallSet.singleton(Month.AUGUST);
      // @end region = singletonEnum
      assertEquals(SmallSet.of(Month.AUGUST), set);
    }
    {
      // @start region = singletonEnum
      SmallSet set = SmallSet.singleton(Month.AUGUST);
      // @end region = singletonEnum
      assertEquals(SmallSet.of(Month.AUGUST), set);
    }
    {
      // @start region = singletonNumber
      SmallSet set = SmallSet.singleton(16.0);
      // @end region = singletonNumber
      assertEquals(SmallSet.of(16), set);
    }
  }

  void empty() {
    // @start region = empty
    SmallSet set = SmallSet.empty();
    // @end region = empty
    assertEquals(SmallSet.of(), set);
  }

  void contains() {
    {
      // @start region = containsInt
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      boolean found = set.contains(3);
      // @end region = containsInt
      assertTrue(found);
    }
    {
      // @start region = containsByte
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      byte value = 3;
      boolean found = set.contains(value);
      // @end region = containsByte
      assertTrue(found);
    }
    {
      // @start region = containsEnum
      SmallSet set = SmallSet.of(Month.APRIL, Month.SEPTEMBER);
      boolean found = set.contains(Month.JUNE); // false
      // @end region = containsEnum
      assertFalse(found);
    }
  }

  private SmallSet getAccessModes() {
    return SmallSet.empty();
  }

  void containsAll() {
    {
      // @start region = containsAllNumbers
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      boolean found = set.containsAll(List.of(1, 2, 3)); // @replace substring = '1, 2, 3' replacement = '...'
      // @end region = containsAllNumbers
      assertTrue(found);
    }
    {
      // @start region = containsAllEnums
      SmallSet accessModes = getAccessModes();
      boolean ok = accessModes.containsAll(EnumSet.of(AccessMode.READ, AccessMode.WRITE));
      // @end region = containsAllEnums
      assertFalse(ok);
    }
    {
      // @start region = containsAllBytes
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      byte four = 4, six = 6;
      boolean ok = set.containsAll(four, six);
      // @end region = containsAllBytes
      assertFalse(ok);
    }
    {
      // @start region = containsAllSmallSet
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      boolean ok = set.containsAll(SmallSet.of(4, 6));
      // @end region = containsAllSmallSet
      assertFalse(ok);
    }
  }

  void compareTo() {
    // @start region = compareTo
    SmallSet a = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    SmallSet b = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    int comparison = a.compareTo(b);
    switch (comparison) {
      case 0 -> IO.println("a = b");
      case -1 -> IO.println("a < b");
      case 1 -> IO.println("a > b");
    }
    // @end region = compareTo
    assertEquals(0, comparison);
  }

  void add() {
    {
      // @start region = addByte
      byte four = 4, six = 6, max = 31;
      SmallSet set = SmallSet.of(four);
      SmallSet more = set.add(six).add(max);
      IO.println(more); // (4,6,31)
      // @end region = addByte
      assertEquals("(4,6,31)", more.toString());
    }
    {
      // @start region = addInt
      SmallSet a = SmallSet.of(0);
      SmallSet b = a.add(17);
      SmallSet c = b.add(5).add(31);
      IO.println(c); // (0,5,17,31)
      // @end region = addInt
      assertEquals("(0,5,17,31)", c.toString());
    }
    {
      // @start region = addEnum
      SmallSet a = SmallSet.of(Month.APRIL);
      SmallSet b = a.add(Month.JUNE);
      SmallSet c = b.add(Month.MAY).add(Month.APRIL);
      IO.println(c); // (3,4,5)
      // @end region = addEnum
      assertEquals("(3,4,5)", c.toString());
    }
  }

  void powerset() {
    LongAdder adder = new LongAdder();
    // @start region = powerset
    SmallSet set = SmallSet.of(1, 2, 3);
    Stream<SmallSet> power = set.powerset();
    power.forEach(subset -> {
      adder.increment(); // @replace substring = 'adder.increment();' replacement = '...'
    });
    // @end region = powerset
    assertEquals(adder.sum(), 1 << set.size()); // 8
  }

  void remove() {
    {
      // @start region = removeByte
      byte two = 2, four = 4;
      SmallSet a = SmallSet.of(1, 2, 3);
      SmallSet b = a.remove(two).remove(four);
      IO.println(b); // (1,3)
      // @end region = removeByte
      assertEquals("(1,3)", b.toString());
    }
    {
      // @start region = removeInt
      SmallSet a = SmallSet.of(1, 2, 3);
      SmallSet b = a.remove(2).remove(4);
      IO.println(b); // (1,3)
      // @end region = removeInt
      assertEquals("(1,3)", b.toString());
    }
    {
      // @start region = removeEnum
      SmallSet a = SmallSet.of(Month.JANUARY, Month.FEBRUARY, Month.MARCH);
      SmallSet b = a.remove(Month.FEBRUARY).remove(Month.APRIL);
      IO.println(b); // (0,2)
      // @end region = removeEnum
      assertEquals("(0,2)", b.toString());
    }
  }

  void union() {
    // @start region = union
    SmallSet a = SmallSet.of(1, 2, 3);
    SmallSet b = SmallSet.of(14, 15, 16);
    SmallSet union = a.union(b);
    IO.println(b); // (1,2,3,14,15,16)
    // @end region = union
    assertEquals("(1,2,3,14,15,16)", union.toString());
  }

  void intersect() {
    // @start region = intersect
    SmallSet a = SmallSet.of(1, 2, 3);
    SmallSet b = SmallSet.of(3, 4, 5);
    SmallSet intersection = a.intersect(b);
    IO.println(b); // (3)
    // @end region = intersect
    assertEquals("(3)", intersection.toString());

  }

  void minus() {
    // @start region = minus
    SmallSet a = SmallSet.of(1, 2, 3);
    SmallSet b = SmallSet.of(3, 4, 5);
    SmallSet diff = a.minus(b);
    IO.println(b); // (1,2)
    // @end region = minus
    assertEquals("(1,2)", diff.toString());
  }

  void complement() {
    {
      // @start region = complement
      SmallSet all = SmallSet.empty().complement();
      SmallSet not17 = all.remove(17); // (0-16,18-31)
      SmallSet just17 = not17.complement(); // (17)
      // @end region = complement
      assertEquals("(17)", just17.toString());
    }
    {
      // @start region = complementDomain
      byte min = 6, max = 11;
      SmallSet all = SmallSet.empty().complement(min, max);
      SmallSet not8 = all.remove(8); // (6,7,9,10,11)
      SmallSet just8 = not8.complement(min, max); // (8)
      // @end region = complementDomain
      assertEquals("(8)", just8.toString());
    }
  }

  void forEach() {
    List<Byte> data = new ArrayList<>();
    // @start region = forEach
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    set.forEach(element -> {
      data.add(element); // @replace substring = 'data.add(element);' replacement = '...'
    });
    // @end region = forEach
    assertEquals(data, set.stream().toList());
  }

  void filter() {
    // @start region = filter
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    SmallSet even = set.filter(e -> e % 2 == 0);
    // @end region = filter
    assertEquals(SmallSet.of(2), even);
  }

  void map() {
    // @start region = map
    SmallSet a = SmallSet.of(1, 2, 3);
    SmallSet b = a.map(i -> ++i);
    IO.println(b); // (2,3,4)
    // @end region = map
    assertEquals("(2,3,4)", b.toString());
  }

  void mapToObj() {
    {
      // @start region = mapToObj
      SmallSet set = SmallSet.of(1, 2, 3);
      List<String> strings = set.mapToObj(Objects::toString); // [ "1", "2", "3"]
      // @end region = mapToObj
      assertEquals(List.of("1", "2", "3"), strings);
    }
    {
      // @start region = mapToObj2
      SmallSet set = SmallSet.of(1, 2, 3);
      Set<String> strings = set.mapToObj(Objects::toString, TreeSet::new); // [ "1", "2", "3"]
      // @end region = mapToObj2
      assertEquals(Set.of("1", "2", "3"), strings);
    }
  }

  void stream() {
    // @start region = stream
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    set.stream().forEach(IO::println); // as bytes // @highlight regex = '(stream|bytes)'
    set.intStream().forEach(IO::println); // as integers // @highlight regex = '(intStream|integers)'
    // @end region = stream
  }

  void collect() {
    // @start region = collect
    IntStream ints = IntStream.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    SmallSet set = SmallSet.collect(ints);
    // @end region = collect
    assertEquals("(1,2,3)", set.toString());
  }

  void size() {
    // @start region = size
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    int size = set.size();
    // @end region = size
    assertEquals(3, size);
  }

  void ofRange() {
    // @start region = ofRange
    SmallSet set = SmallSet.ofRange(1, 4);
    IO.println(set); // (1,2,3)
    // @end region = ofRange
    assertEquals("(1,2,3)", set.toString());
  }

  void ofRangeClosed() {
    // @start region = ofRangeClosed
    SmallSet set = SmallSet.ofRangeClosed(1, 3);
    IO.println(set); // (1,2,3)
    // @end region = ofRangeClosed
    assertEquals("(1,2,3)", set.toString());
  }

  void _toString() {
    {
      // @start region = toString
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      String string = set.toString(" → ", "⟪ ", " ⟫");
      IO.println(string); // "⟪ 1 → 2 → 3 ⟫"
      // @end region = toString
      assertEquals("⟪ 1 → 2 → 3 ⟫", string);
    }
    {
      // @start region = toStringCollector
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      String string = set.toString(Collectors.joining("|"));
      IO.println(string); // "1|2|3"
      // @end region = toStringCollector
      assertEquals("1|2|3", string);
    }
  }

  void toSet() {
    // @start region = toSet
    Set<Byte> bytes = SmallSet.of(1, 2, 3).toSet(); // @replace substring = '1, 2, 3' replacement = '...'
    IO.println(bytes); // [1, 2, 3]
    // @end region = toSet
    assertEquals(Set.of((byte) 1, (byte) 2, (byte) 3), bytes);
  }

  void toArray() {
    {
      // @start region = toArray
      SmallSet set = SmallSet.of(14, 5, 2);
      byte[] bytes = set.toArray();
      IO.println(Arrays.toString(bytes)); // [2, 5, 14]
      // @end region = toArray
      assertArrayEquals(new byte[] { 2, 5, 14 }, bytes);
    }
    {
      // @start region = toArray2
      SmallSet set = SmallSet.of(14, 5, 2);
      byte[] destination = new byte[256];
      set.toArray(destination);
      IO.println(Arrays.toString(destination)); // [2, 5, 14, -1, ...
      // @end region = toArray2
      byte[] expected = new byte[256];
      Arrays.fill(expected, (byte) -1);
      expected[0] = 2;
      expected[1] = 5;
      expected[2] = 14;
      assertArrayEquals(expected, destination);
    }
    {
      // @start region = toArray3
      SmallSet set = SmallSet.of(5, 8, 13);
      String[] strings = set.toArray(
          n -> String.valueOf((char) ('\u2460' + --n)),
          String[]::new);
      IO.println(Arrays.toString(strings)); // ["⑤", "⑧", "⑬"]
      // @end region = toArray3
      assertArrayEquals(new String[] { "⑤", "⑧", "⑬" }, strings);
    }
  }

  void next() {
    List<Byte> bytes = new ArrayList<>();
    // @start region = next
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    while (!set.isEmpty()) {
      set = set.next(b -> bytes.add(b)); // @replace substring = 'bytes.add' replacement = "process"
    }
    // Same as: set.forEach(b -> process(b));
    // @end region = next
    assertEquals(List.<Byte> of((byte) 1, (byte) 2, (byte) 3), bytes);
  }

  void toBitSet() {
    List<Byte> bytes = new ArrayList<>();
    // @start region = toBitSet
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    BitSet bits = set.toBitSet();
    bits.set(5);
    set = SmallSet.of(bits); // (1,2,3,5)
    // @end region = toBitSet
    assertEquals("{1, 2, 3, 5}", bits.toString());
    assertEquals("(1,2,3,5)", set.toString());
  }

  void toEnumSet() {
    List<Byte> bytes = new ArrayList<>();
    // @start region = toEnumSet
    SmallSet set = SmallSet.of(Month.APRIL, Month.JULY);
    EnumSet<Month> months = set.toEnumSet(Month.class);
    IO.println(months); // [APRIL, JULY]
    // @end region = toEnumSet
    assertEquals("[APRIL, JULY]", months.toString());
  }

  void random() {
    RandomGenerator fakeRng = new Random() {
      @Override
      public int nextInt(int bound) {
        return 0;
      }
    };
    {
      byte value = -1;
      // @start region = random
      RandomGenerator rng = fakeRng; // @replace substring = '1, 2, 3' replacement = 'new SecureRandom()'
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      if (!set.isEmpty()) {
        value = set.random(rng); // @replace substring = 'value' replacement = 'byte value'
        IO.println(value); // print a random value
      }
      // @end region = random
      assertEquals((byte) 1, value);
    }
    {
      AtomicInteger i = new AtomicInteger(-1);
      // @start region = random2
      RandomGenerator rng = fakeRng; // @replace substring = '1, 2, 3' replacement = 'new SecureRandom()'
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      set.random(rng, i::set); // prints a random element // @replace substring = 'i::set' replacement = 'IO::println'
      // @end region = random2
      assertEquals(1, i.get());
    }
  }

  void reduce() {
    {
      // @start region = reduce1
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      // same as set.sum():
      int sum = set.reduce(0, Integer::sum);
      IO.println(sum);
      // @end region = reduce1
      assertEquals(sum, set.sum());
    }
    {
      // @start region = reduce2
      SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
      // same as set.sum():
      int sum = set.reduce(Integer::sum).orElse(0);
      IO.println(sum);
      // @end region = reduce2
      assertEquals(sum, set.sum());
    }
  }

  void sum() {
    // @start region = sum
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    int sum = set.sum();
    IO.println(sum);
    // @end region = sum
    assertEquals(set.intStream().sum(), sum);
  }

  void min() {
    // @start region = min
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    OptionalByte min = set.min();
    IO.println(min.orElseThrow());
    // @end region = min
    assertEquals(set.intStream().min(), min.mapToInt(i -> i));
  }

  void max() {
    // @start region = max
    SmallSet set = SmallSet.of(1, 2, 3); // @replace substring = '1, 2, 3' replacement = '...'
    OptionalByte max = set.max();
    IO.println(max.orElseThrow());
    // @end region = max
    assertEquals(set.intStream().max(), max.mapToInt(i -> i));
  }

  void singleElement() {
    // @start region = singleElement
    List<SmallSet> sets = List.of(SmallSet.of(1, 2, 3), SmallSet.of(4), SmallSet.empty());
    SmallSet singletons = SmallSet.collect(sets.stream()
        .flatMapToInt(set -> set.singleElement().stream())); // @highlight substring = 'singleElement()'
    IO.println("All integers that are in some singleton: ");
    IO.println(singletons); // (4)
    // @end region = singleElement
    assertEquals(SmallSet.of(4), singletons);
  }

  void lower() {
    {
      // @start region = lowerByte
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      byte nine = 9;
      OptionalByte lower9 = set.lower(nine);
      IO.println(lower9); // OptionalByte[5]
      // @end region = lowerByte
      assertEquals(OptionalByte.of(5), lower9);
    }
    {
      // @start region = lowerInt
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      OptionalByte lower9 = set.lower(9);
      IO.println(lower9); // OptionalByte[5]
      // @end region = lowerInt
      assertEquals(OptionalByte.of(5), lower9);
    }
  }

  void floor() {
    {
      // @start region = floorByte
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      byte nine = 9;
      OptionalByte floor9 = set.floor(nine);
      IO.println(floor9); // OptionalByte[5]
      // @end region = floorByte
      assertEquals(OptionalByte.of(5), floor9);
    }
    {
      // @start region = floorInt
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      OptionalByte floor9 = set.floor(9);
      IO.println(floor9); // OptionalByte[5]
      // @end region = floorInt
      assertEquals(OptionalByte.of(5), floor9);
    }
  }

  void ceiling() {
    {
      // @start region = ceilingByte
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      byte nine = 9;
      OptionalByte ceiling9 = set.ceiling(nine);
      IO.println(ceiling9); // OptionalByte[23]
      // @end region = ceilingByte
      assertEquals(OptionalByte.of(23), ceiling9);
    }
    {
      // @start region = ceilingInt
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      OptionalByte ceiling9 = set.ceiling(9);
      IO.println(ceiling9); // OptionalByte[23]
      // @end region = ceilingInt
      assertEquals(OptionalByte.of(23), ceiling9);
    }
  }

  void higher() {
    {
      // @start region = higherByte
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      byte nine = 9;
      OptionalByte higher9 = set.higher(nine);
      IO.println(higher9); // OptionalByte[23]
      // @end region = higherByte
      assertEquals(OptionalByte.of(23), higher9);
    }
    {
      // @start region = higherInt
      SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
      OptionalByte higher9 = set.higher(9);
      IO.println(higher9); // OptionalByte[23]
      // @end region = higherInt
      assertEquals(OptionalByte.of(23), higher9);
    }
  }

  void classBasicByteSet() {
    // @start region = classSmallSet
    SmallSet set = SmallSet.of(1, 5, 23); // @replace substring = '1, 2, 3' replacement = '...'
    ByteSet mutableSet = set.toSet();
    mutableSet.add((byte) 7);
    set = mutableSet.toSmallSet();
    // @end region = classSmallSet
  }

  //////////////////////////////////////////////////////////

  @TestFactory
  List<DynamicTest> testAllSnippets() throws Exception {
    List<DynamicTest> result = new ArrayList<>();
    // Just to make sure they actually work.
    final var object = new Snippets();
    for (Method m : Snippets.class.getDeclaredMethods()) {
      if (!m.getName().startsWith("test") && !m.isSynthetic()) {
        // dynamicTest​(String displayName, Executable executable)
        result.add(DynamicTest.dynamicTest(m.getName().replaceFirst("^_", ""), () -> {
          try {
            m.invoke(object);
          } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof org.opentest4j.AssertionFailedError afe) {
              throw afe;
            }
            fail("Failed while running " + m.getName(), e);
          }
        }));
      }
    }
    return result;
  }

  static class IO {
    static void println(Object set) {
      // so that the unit tests don't actually print anything
    }
  }
}
