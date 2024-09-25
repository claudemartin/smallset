package ch.claude_martin.smallset;

import static ch.claude_martin.smallset.SmallSet.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class OptionalByteTest {
  private static final OptionalByte EMPTY    = OptionalByte.empty();
  private final static byte         FIVE     = 5;
  private static final OptionalByte OPT_FIVE = OptionalByte.of(FIVE);

  @Test
  void testHashCode() {
    assertEquals(FIVE, OPT_FIVE.hashCode());
    assertEquals(Integer.MIN_VALUE, EMPTY.hashCode());
  }

  @Test
  void testOf() {
    assertEquals(OptionalByte.of((byte) 5), OPT_FIVE);
    assertEquals(OptionalByte.of(5), OPT_FIVE);
    assertEquals(OptionalByte.of(OptionalInt.of(5)), OPT_FIVE);
  }

  @Test
  void testGetAsByte() {
    assertEquals(OptionalByte.of(5).getAsByte(), OPT_FIVE.getAsByte());
    assertThrows(NoSuchElementException.class, () -> OptionalByte.empty().getAsByte());
  }

  @Test
  void testIsPresent() {
    assertTrue(OPT_FIVE.isPresent());
    assertFalse(EMPTY.isPresent());
  }

  @Test
  void testIsEmpty() {
    assertFalse(OPT_FIVE.isEmpty());
    assertTrue(EMPTY.isEmpty());
  }

  @Test
  void testIfPresent() {
    var b = new AtomicBoolean(false);
    OPT_FIVE.ifPresent(x -> b.set(true));
    assertTrue(b.get());
    EMPTY.ifPresent(x -> fail());
    assertTrue(b.get());
  }

  @Test
  void testIfPresentOrElse() {
    var b = new AtomicBoolean(false);
    OPT_FIVE.ifPresentOrElse(x -> b.set(true), () -> fail());
    assertTrue(b.get());

    b.set(false);
    EMPTY.ifPresentOrElse(x -> fail(), () -> b.set(true));
    assertTrue(b.get());
  }

  @Test
  void testOrElse() {
    assertEquals(FIVE, OPT_FIVE.orElse((byte) 9));
    assertEquals(FIVE, EMPTY.orElse(FIVE));
  }

  @Test
  void testOrElseGet() {
    assertEquals(FIVE, OPT_FIVE.orElseGet(() -> (byte) 9));
    assertEquals(FIVE, EMPTY.orElseGet(() -> FIVE));
  }

  @Test
  void testOrElseThrowSupplierOfX() {
    assertEquals(FIVE, OPT_FIVE.orElseThrow(() -> new NoSuchElementException()));
    assertThrows(NoSuchElementException.class, () -> EMPTY.orElseThrow(() -> new NoSuchElementException()));
  }

  @Test
  void testOrElseThrow() {
    assertEquals(FIVE, OPT_FIVE.orElseThrow());
    assertThrows(NoSuchElementException.class, () -> EMPTY.orElseThrow());
  }

  @Test
  void testStream() {
    List<Integer> list = OPT_FIVE.stream().mapToObj(i -> i).collect(Collectors.toList());
    assertEquals(List.of(5), list);

    List<Integer> list2 = EMPTY.stream().mapToObj(i -> i).collect(Collectors.toList());
    assertEquals(List.of(), list2);
  }

  @Test
  void testEquals() {
    assertEquals(EMPTY, EMPTY);
    assertEquals(OPT_FIVE, OPT_FIVE);
    assertNotEquals(EMPTY, OPT_FIVE);
  }

  @Test
  void testToString() {
    assertEquals("OptionalByte.empty", EMPTY.toString());
    assertEquals("OptionalByte[5]", OPT_FIVE.toString());
  }

  @Test
  void testMapToObj() {
    assertEquals(Optional.of(FIVE), OPT_FIVE.mapToObj(b -> b));
    assertEquals(Optional.of("X"), OPT_FIVE.mapToObj(b -> "X"));
    assertEquals(Optional.empty(), EMPTY.mapToObj(b -> "X"));
  }

  @Test
  void testMapToInt() {
    assertEquals(OptionalInt.of(FIVE), OPT_FIVE.mapToInt(b -> b));
    assertEquals(OptionalInt.of(7), OPT_FIVE.mapToInt(b -> 7));
    assertEquals(OptionalInt.empty(), EMPTY.mapToInt(b -> 7));
  }

  @Test
  void testMap() {
    assertEquals(OptionalByte.of(FIVE), OPT_FIVE.map(b -> b));
    assertEquals(OptionalByte.of(7), OPT_FIVE.map(b -> 7));
    assertEquals(OptionalByte.empty(), EMPTY.map(b -> 7));
  }
  
  @Test
  public void testSerializable() throws Exception {
    // This would fail if there wasn't a writeReplace() method.
    try (final var bos = new ByteArrayOutputStream(); final var out = new ObjectOutputStream(bos)) {
      final var original = OptionalByte.of(42);
      out.writeObject(original);
      out.writeObject(OptionalByte.empty());
      try (final var bis = new ByteArrayInputStream(bos.toByteArray()); final var in = new ObjectInputStream(bis)) {
        final var copy = (OptionalByte) in.readObject();
        assertEquals(original, copy);
        assertEquals((OptionalByte) in.readObject(), OptionalByte.empty());
      }
    }
  }
}
