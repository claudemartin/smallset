package ch.claude_martin.smallset;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * An Iterator specialized for byte values.
 * 
 * @see java.util.PrimitiveIterator.OfInt
 */
public interface ByteIterator extends PrimitiveIterator<Byte, ByteConsumer> {

  /**
   * Returns the next {@code byte} element in the iteration.
   *
   * @return the next {@code byte} element in the iteration
   */
  byte nextByte();

  @Override
  boolean hasNext();

  /**
   * Performs the given action for each remaining element until all elements have been processed or
   * the action throws an exception. Actions are performed in the order of iteration, if that order
   * is specified. Exceptions thrown by the action are relayed to the caller.
   *
   * @param action
   *          The action to be performed for each element
   * @throws NullPointerException
   *           if the specified action is null
   */
  @Override
  default void forEachRemaining(final ByteConsumer action) {
    Objects.requireNonNull(action);
    while (hasNext())
      action.accept(nextByte());
  }

  /**
   * {@inheritDoc}
   * 
   * @implSpec The default implementation boxes the result of calling {@link #nextByte()}, and
   *           returns that boxed result.
   */
  @Override
  default Byte next() {
    return nextByte();
  }

  /**
   * {@inheritDoc}
   * 
   * @implSpec If the action is an instance of {@code ByteConsumer} then it is cast to
   *           {@code ByteConsumer} and passed to {@link #forEachRemaining}; otherwise the action is
   *           adapted to an instance of {@code ByteConsumer}, by boxing the argument of
   *           {@code ByteConsumer}, and then passed to {@link #forEachRemaining}.
   */
  @Override
  default void forEachRemaining(final Consumer<? super Byte> action) {
    if (action instanceof ByteConsumer)
      forEachRemaining((ByteConsumer) action);
    else {
      // The method reference action::accept is never null
      Objects.requireNonNull(action);
      forEachRemaining((ByteConsumer) action::accept);
    }
  }

}