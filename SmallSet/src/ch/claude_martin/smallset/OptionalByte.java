package ch.claude_martin.smallset;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

/// A container object which may or may not contain a {@code byte} value.
/// 
/// In a future release of Java this might be replaced by the type {@code byte?} (nullable primitive byte). 
public value class OptionalByte implements Serializable {
  private static final OptionalByte EMPTY = new OptionalByte();

  private final boolean             isPresent;
  private final byte                value;

  /// Construct an empty instance.
  private OptionalByte() {
    this.isPresent = false;
    this.value = 0;
  }

  public static OptionalByte empty() {
    return OptionalByte.EMPTY;
  }

  /// Construct an instance with the value present.
  /// 
  /// @param value the byte value to be present
  private OptionalByte(final byte value) {
    this.isPresent = true;
    this.value = value;
  }

  public static OptionalByte of(final byte value) {
    return new OptionalByte(value);
  }

  public static OptionalByte of(final int value) {
    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
      throw new IllegalArgumentException("value out of range: " + value);
    }
    return new OptionalByte((byte) value);
  }

  public static OptionalByte of(final OptionalInt value) {
    if (value.isEmpty()) {
      return OptionalByte.EMPTY;
    }
    return OptionalByte.of(value.getAsInt());
  }

  public static OptionalByte ofNullable(final Number value) {
    if (value == null) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(value.intValue());
  }

  public byte getAsByte() {
    if (!this.isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return this.value;
  }

  public boolean isPresent() {
    return this.isPresent;
  }

  public boolean isEmpty() {
    return !this.isPresent;
  }

  public void ifPresent(final ByteConsumer consumer) {
    if (this.isPresent) {
      consumer.accept(this.value);
    }
  }

  public void ifPresentOrElse(final IntConsumer action, final Runnable emptyAction) {
    if (this.isPresent) {
      action.accept(this.value);
    } else {
      emptyAction.run();
    }
  }

  public byte or0() {
    return this.isPresent ? this.value : 0;
  }

  public Byte orNull() {
    return this.isPresent ? this.value : null;
  }

  public byte orElse(final byte other) {
    return this.isPresent ? this.value : other;
  }

  public byte orElseGet(final Supplier<? extends Byte> other) {
    return this.isPresent ? this.value : other.get();
  }

  public <X extends Throwable> byte orElseThrow(final Supplier<X> exceptionSupplier) throws X {
    if (this.isPresent) {
      return this.value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  public byte orElseThrow() {
    if (!this.isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return this.value;
  }

  public IntStream stream() {
    return this.isPresent ? IntStream.of(this.value) : IntStream.empty();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof final OptionalByte other) {
      return (this.isPresent && other.isPresent) ? this.value == other.value : this.isPresent == other.isPresent;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return this.isPresent ? this.value : Integer.MIN_VALUE;
  }

  @Override
  public String toString() {
    return this.isPresent ? String.format("OptionalByte[%s]", this.value) : "OptionalByte.empty";
  }

  public <U> Optional<U> mapToObj(final Function<Byte, ? extends U> mapper) {
    if (!this.isPresent) {
      return Optional.empty();
    }
    return Optional.ofNullable(mapper.apply(this.value));
  }

  public OptionalInt mapToInt(final ToIntFunction<Byte> mapper) {
    if (!this.isPresent) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(mapper.applyAsInt(this.value));
  }

  public OptionalByte map(final UnaryOperator<Byte> mapper) {
    if (!this.isPresent) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(mapper.apply(this.value));
  }
  
  public OptionalByte filter(final Predicate<? super Byte> predicate) {
    Objects.requireNonNull(predicate);
    if (this.isEmpty()) {
        return this;
    } else {
        return predicate.test(this.value) ? this : OptionalByte.empty();
    }
  }

  record Proxy(boolean isPresent, byte value) implements Serializable {
    Object readResolve() throws ObjectStreamException {
      return this.isPresent ? new OptionalByte(this.value) : OptionalByte.empty();
    }
  }

  @java.io.Serial
  Object writeReplace() {
    return new Proxy(this.isPresent, this.value);
  }
  
  @java.io.Serial
  private void readObject(java.io.ObjectInputStream s)
      throws java.io.InvalidObjectException {
      throw new java.io.InvalidObjectException("Proxy required");
  }

  @java.io.Serial
  private void readObjectNoData()
      throws java.io.InvalidObjectException {
      throw new java.io.InvalidObjectException("Proxy required");
  }
}
