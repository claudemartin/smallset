package ch.claude_martin.smallset;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ByteConsumerTest {

  final static byte           FIVE     = 5;

  final AtomicReference<Byte> ref      = new AtomicReference<>();
  final ByteConsumer          consumer = this.ref::set;

  @BeforeEach
  public void before() {
    this.ref.set((byte) -1);
  }

  @Test
  void testAcceptAsByte() {
    this.consumer.acceptAsByte(ByteConsumerTest.FIVE);
    assertEquals(this.ref.get(), ByteConsumerTest.FIVE);
  }

  @Test
  void testAccept() {
    this.consumer.accept(ByteConsumerTest.FIVE);
    assertEquals(this.ref.get(), ByteConsumerTest.FIVE);
  }

  @Test
  void testAndThen() {
    ByteConsumer noop = b -> {
    };
    var c = noop.andThen(this.consumer);
    c.acceptAsByte(ByteConsumerTest.FIVE);
    assertEquals(this.ref.get(), ByteConsumerTest.FIVE);
  }

  @Test
  void testAndThenInt() {
    ByteConsumer noop = b -> {
    };
    var c = noop.andThenInt(i -> this.ref.set((byte) i));
    c.acceptAsByte(ByteConsumerTest.FIVE);
    assertEquals(this.ref.get(), ByteConsumerTest.FIVE);
  }

}
