package ch.claude_martin.smallset;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/// A container object which may or may not contain a {@code byte} value.
///
/// In a future release of Java this might be replaced by the type {@code byte?} (nullable primitive byte).
public value class OptionalByte implements Serializable {
  private static final long         serialVersionUID = 1L;
  private static final OptionalByte EMPTY            = new OptionalByte();

  private final boolean             isPresent;
  private final byte                value;

  /// Construct an empty instance.
  private OptionalByte() {
    this.isPresent = false;
    this.value = 0;
  }

  /// Returns an empty {@code OptionalByte} instance. No value is present for this {@code OptionalByte}.
  public static OptionalByte empty() {
    return OptionalByte.EMPTY;
  }

  /// Construct an instance with the value present.
  ///
  /// @param value
  ///          the byte value to be present
  private OptionalByte(final byte value) {
    this.isPresent = true;
    this.value = value;
  }

  /// Returns an {@code OptionalByte} describing the given value.
  public static OptionalByte of(final byte value) {
    return new OptionalByte(value);
  }

  /// Returns an {@code OptionalByte} describing the given value as a byte.
  /// 
  /// @throws IllegalArgumentException
  ///           if the given value is out of range.
  public static OptionalByte of(final int value) {
    if (value instanceof byte byteValue) {
      return new OptionalByte(byteValue);
    }
    throw new IllegalArgumentException("value out of range: " + value);
  }

  /// Returns an {@code OptionalByte} describing the given optional value as a byte. This returns {@link #empty() an
  /// empty OptionalByte} if the given value is empty.
  /// 
  /// @throws IllegalArgumentException
  ///           if the given value is out of range.
  public static OptionalByte of(final OptionalInt value) {
    if (value.isEmpty()) {
      return OptionalByte.EMPTY;
    }
    return OptionalByte.of(value.getAsInt());
  }

  /// Returns an {@code OptionalByte} describing the given value as a byte. This returns {@link #empty() an empty
  /// OptionalByte} if the given value is null.
  /// 
  /// @throws IllegalArgumentException
  ///           if the given value is out of range.
  public static OptionalByte ofNullable(final Number value) {
    if (value == null) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(value.intValue());
  }

  /// If a value is present, returns the value, otherwise throws {@code NoSuchElementException}.
  /// 
  /// @apiNote The preferred alternative to this method is {@link #orElseThrow()}.
  /// @return the value described by this {@code OptionalByte}
  /// @throws NoSuchElementException
  ///           if no value is present
  public byte getAsByte() {
    if (!this.isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return this.value;
  }

  /// If a value is present, returns {@code true}, otherwise {@code false}.
  ///
  /// @return {@code true} if a value is present, otherwise {@code false}
  public boolean isPresent() {
    return this.isPresent;
  }

  /// If a value is not present, returns {@code true}, otherwise {@code false}.
  /// 
  /// @return {@code true} if a value is not present, otherwise {@code false}
  public boolean isEmpty() {
    return !this.isPresent;
  }

  /// If a value is present, performs the given action with the value, otherwise does nothing.
  ///
  /// @param action
  ///          the action to be performed, if a value is present
  /// @throws NullPointerException
  ///           if value is present and the given action is {@code null}
  public void ifPresent(final ByteConsumer action) {
    if (this.isPresent) {
      action.accept(this.value);
    }
  }

  /// If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
  /// 
  /// @param action
  ///          the action to be performed, if a value is present
  /// @param emptyAction
  ///          the empty-based action to be performed, if no value is present
  /// @throws NullPointerException
  ///           if a value is present and the given action is {@code null}, or no value is present and the given
  ///           empty-based action is {@code null}.
  public void ifPresentOrElse(final IntConsumer action, final Runnable emptyAction) {
    if (this.isPresent) {
      action.accept(this.value);
    } else {
      emptyAction.run();
    }
  }

  /// If a value is present, returns the value, otherwise returns {@code 0}.
  /// 
  /// @return the value, if present, otherwise {@code 0}
  public byte or0() {
    return this.isPresent ? this.value : 0;
  }

  /// If a value is present, returns the value, otherwise returns {@code null}.
  /// 
  /// @return the value, if present, otherwise {@code null}
  public Byte orNull() {
    return this.isPresent ? this.value : null;
  }

  /// If a value is present, returns the value, otherwise returns {@code other}.
  /// 
  /// @param other
  ///          the value to be returned, if no value is present
  /// @return the value, if present, otherwise {@code other}
  public byte orElse(final byte other) {
    return this.isPresent ? this.value : other;
  }

  /// If a value is present, returns the value, otherwise returns the result produced by the supplying function.
  ///
  /// @param supplier
  ///          the supplying function that produces a value to be returned
  /// @return the value, if present, otherwise the result produced by the supplying function
  /// @throws NullPointerException
  ///           if no value is present and the supplying function is {@code null}
  public byte orElseGet(final Supplier<? extends Byte> supplier) {
    return this.isPresent ? this.value : supplier.get();
  }

  /// If a value is present, returns the value, otherwise throws an exception produced by the exception supplying
  /// function.
  /// 
  /// @apiNote A method reference to the exception constructor with an empty argument list can be used as the supplier.
  ///          For example, {@code IllegalStateException::new}
  /// 
  /// @param <X>
  ///          Type of the exception to be thrown
  /// @param exceptionSupplier
  ///          the supplying function that produces an exception to be thrown
  /// @return the value, if present
  /// @throws X
  ///           if no value is present
  /// @throws NullPointerException
  ///           if no value is present and the exception supplying function is {@code null}
  public <X extends Throwable> byte orElseThrow(final Supplier<X> exceptionSupplier) throws X {
    if (this.isPresent) {
      return this.value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  /// If a value is present, returns the value, otherwise throws {@code NoSuchElementException}.
  /// 
  /// @return the value described by this {@code OptionalInt}
  /// @throws NoSuchElementException
  ///           if no value is present
  public byte orElseThrow() {
    if (!this.isPresent) {
      throw new NoSuchElementException("No value present");
    }
    return this.value;
  }

  /// If a value is present, returns a sequential {@link IntStream} containing only that value, otherwise returns an
  /// empty {@code IntStream}.
  ///
  /// @apiNote This method can be used to transform a {@code Stream} of optional bytes to an {@code IntStream} of present
  ///          values:
  /// ```
  ///     Stream<OptionalByte> os = ...
  ///     IntStream s = os.flatMapToInt(OptionalByte::stream)
  /// ```
  ///
  /// @return the optional value as an {@code IntStream}
  public IntStream stream() {
    return this.isPresent ? IntStream.of(this.value) : IntStream.empty();
  }

  /// Indicates whether some other object is "equal to" this {@code OptionalByte}. The other object is considered equal
  /// if:
  /// <ul>
  /// <li>it is also an {@code OptionalByte} and;
  /// <li>both instances have no value present or;
  /// <li>the present values are "equal to" each other via {@code ==}.
  /// </ul>
  ///
  /// @param obj
  ///          an object to be tested for equality
  /// @return {@code true} if the other object is "equal to" this object otherwise {@code false}
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof final OptionalByte other) {
      return this.isPresent ? other.isPresent && this.value == other.value : !other.isPresent;
    }

    return false;
  }

  /// Returns the hash code of the value, if present, otherwise {@link Integer#MIN_VALUE} if no value is present.
  /// 
  /// @return hash code value of the present value or {@link Integer#MIN_VALUE} if no value is present
  @Override
  public int hashCode() {
    return this.isPresent ? this.value : Integer.MIN_VALUE;
  }

  /// Returns a non-empty string representation of this {@code OptionalByte} suitable for debugging. The exact
  /// presentation format is unspecified and may vary between implementations and versions.
  ///
  /// If a value is present the result includes its string representation in the result. Empty and present {@code
  /// OptionalByte}s are unambiguously differentiable.
  ///
  /// @return the string representation of this instance
  @Override
  public String toString() {
    return this.isPresent ? String.format("OptionalByte[%s]", this.value) : "OptionalByte.empty";
  }

  /// Returns an {@link OptionalInt} representing the same value as this, cast to integer. If no value is present, the
  /// returned {@link OptionalInt} is also empty. If this If a value is present, the returned {@link OptionalInt}
  /// contains that value as an integer.
  public OptionalInt toOptionalInt() {
    return this.isEmpty() ? OptionalInt.empty() : OptionalInt.of(this.value);
  }

  /// Returns an {@link Optional} representing the same value as this, mapped using the given function. If no value is
  /// present, the returned {@link Optional} is also empty. If a value is present, the returned {@link Optional} contains
  /// that value mapped using the given function.
  /// 
  /// @throws NullPointerException
  ///           if no value is present and the mapping function is {@code null}
  public <U> Optional<U> mapToObj(final Function<Byte, ? extends U> mapper) {
    if (!this.isPresent) {
      return Optional.empty();
    }
    return Optional.ofNullable(mapper.apply(this.value));
  }

  /// Returns an {@link OptionalInt} representing the value as this, mapped using the given function. If no value is
  /// present, the returned {@link OptionalInt} is also empty. If a value is present, the returned {@link OptionalInt}
  /// contains that value mapped by the given function.
  /// 
  /// @throws NullPointerException
  ///           if no value is present and the mapping function is {@code null}
  public OptionalInt mapToInt(final ToIntFunction<Byte> mapper) {
    if (!this.isPresent) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(mapper.applyAsInt(this.value));
  }

  /// Returns an {@link OptionalByte} representing the value as this, mapped using the given operator. If no value is
  /// present, the returned {@link OptionalByte} is also empty. If a value is present, the returned {@link OptionalInt}
  /// contains that value mapped by the given operator.
  /// 
  /// @throws NullPointerException
  ///           if no value is present and the mapping operator is {@code null}
  public OptionalByte map(final UnaryOperator<Byte> mapper) {
    if (!this.isPresent) {
      return OptionalByte.empty();
    }
    return OptionalByte.of(mapper.apply(this.value));
  }

  /// Returns an {@link OptionalByte} representing the value as this, filtered using the given predicate. If no value is
  /// present, or if the given predicate returns {@code false} for the value, the returned {@link OptionalByte} is also
  /// empty. Otherwise, returns this OptionalByte .
  /// 
  /// @throws NullPointerException
  ///           if no value is present and the predicate is {@code null}
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
  private void readObject(final java.io.ObjectInputStream s)
      throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }

  @java.io.Serial
  private void readObjectNoData()
      throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }
}
