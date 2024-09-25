package ch.claude_martin.smallset;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import ch.claude_martin.smallset.SmallSet.S;

/**
 * A container object which may or may not contain a {@code byte} value.
 */
public value class OptionalByte implements Serializable {
  private static final OptionalByte EMPTY = new OptionalByte();

  private final boolean             isPresent;
  private final byte                value;

  /**
   * Construct an empty instance.
   */
  private OptionalByte() {
    this.isPresent = false;
    this.value = 0;
  }

  public static OptionalByte empty() {
    return EMPTY;
  }

  /**
   * Construct an instance with the value present.
   *
   * @param value
   *          the byte value to be present
   */
  private OptionalByte(byte value) {
    this.isPresent = true;
    this.value = value;
  }

  public static OptionalByte of(byte value) {
    return new OptionalByte(value);
  }

  public static OptionalByte of(int value) {
    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
      throw new IllegalArgumentException("value out of range: " + value);
    return new OptionalByte((byte) value);
  }
  
  public static OptionalByte of(OptionalInt value) {
    if (value.isEmpty())
      return EMPTY;
    return of(value.getAsInt());
  }

  public static OptionalByte ofNullable(Number value) {
    if (value == null)
      return empty();
    return of(value.intValue());
  }

  public byte getAsByte() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public boolean isPresent() {
    return isPresent;
  }
  
  public boolean isEmpty() {
    return !isPresent;
  }

  public void ifPresent(ByteConsumer consumer) {
    if (isPresent)
      consumer.accept(value);
  }
  
  public void ifPresentOrElse(IntConsumer action, Runnable emptyAction) {
    if (isPresent) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public int orElse(byte other) {
    return isPresent ? value : other;
  }

  public int orElseGet(Supplier<? extends Byte> other) {
    return isPresent ? value : other.get();
  }

  public <X extends Throwable> int orElseThrow(Supplier<X> exceptionSupplier) throws X {
    if (isPresent) {
      return value;
    } else {
      throw exceptionSupplier.get();
    }
  }
  
  public int orElseThrow() {
    if (!isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public IntStream stream() {
    return isPresent ? IntStream.of(value) : IntStream.empty();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj instanceof OptionalByte other)
      return (isPresent && other.isPresent) ? value == other.value : isPresent == other.isPresent;
      
    return false;
  }

  @Override
  public int hashCode() {
    return isPresent ? value : Integer.MIN_VALUE;
  }

  @Override
  public String toString() {
    return isPresent ? String.format("OptionalByte[%s]", value) : "OptionalByte.empty";
  }

  public <U> Optional<U> mapToObj(Function<Byte, ? extends U> mapper) {
    if (!isPresent)
      return Optional.empty();
    return Optional.ofNullable(mapper.apply(this.value));
  }

  public OptionalInt mapToInt(ToIntFunction<Byte> mapper) {
    if (!isPresent)
      return OptionalInt.empty();
    return OptionalInt.of(mapper.applyAsInt(this.value));
  }

  public OptionalByte map(UnaryOperator<Byte> mapper) {
    if (!isPresent)
      return OptionalByte.empty();
    return OptionalByte.of(mapper.apply(this.value));
  }
  
  record S(boolean isPresent, byte value) implements Serializable {
    Object readResolve() throws ObjectStreamException {
      return isPresent ? new OptionalByte(value) : empty();
    }
  }
  
  Object writeReplace() {
    return new S(isPresent, value);
  }
}
