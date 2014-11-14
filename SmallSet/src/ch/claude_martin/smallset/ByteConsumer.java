package ch.claude_martin.smallset;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Represents an operation that accepts a single {@code byte}-valued argument and returns no result.
 * This is the primitive type specialization of {@link Consumer} for {@code byte}. Unlike most other
 * functional interfaces, {@code ByteConsumer} is expected to operate via side-effects.
 *
 * @see Consumer
 * @see IntConsumer
 */
@FunctionalInterface
public interface ByteConsumer {

  /**
   * Performs this operation on the given argument.
   *
   * @param value
   *          the input argument
   */
  void accept(final byte value);

  /**
   * Returns a composed {@code ByteConsumer} that performs, in sequence, this operation followed by
   * the {@code after} operation. If performing either operation throws an exception, it is relayed
   * to the caller of the composed operation. If performing this operation throws an exception, the
   * {@code after} operation will not be performed.
   *
   * @param after
   *          the operation to perform after this operation
   * @return a composed {@code ByteConsumer} that performs in sequence this operation followed by
   *         the {@code after} operation
   * @throws NullPointerException
   *           if {@code after} is null
   */
  default ByteConsumer andThen(final IntConsumer after) {
    Objects.requireNonNull(after);
    return (final byte b) -> {
      accept(b);
      after.accept(b);
    };
  }
}