package ch.claude_martin.smallset;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ByteConsumerTest {

  final static byte           FIVE     = 5;

  final AtomicReference<Byte> ref      = new AtomicReference<Byte>();
  final ByteConsumer          consumer = ref::set;

  @BeforeEach
  public void before() {
    ref.set((byte) -1);
  }

  @Test
  void testAcceptAsByte() {
    consumer.acceptAsByte(FIVE);
    assertEquals(ref.get(), FIVE);
  }

  @Test
  void testAccept() {
    consumer.accept(FIVE);
    assertEquals(ref.get(), FIVE);
  }

  @Test
  void testAndThen() {
    ByteConsumer noop = b -> {};
    var c = noop.andThen(consumer);
    c.acceptAsByte(FIVE);
    assertEquals(ref.get(), FIVE);
  }

  @Test
  void testAndThenInt() {
    ByteConsumer noop = b -> {};
    var c = noop.andThenInt(i -> ref.set((byte) i));
    c.acceptAsByte(FIVE);
    assertEquals(ref.get(), FIVE);
  }

}
