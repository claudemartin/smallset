package ch.claude_martin.smallset;


import java.io.*;
import java.util.stream.Collectors;

// This is just a simple demo of what my code does. Some of this will change with the next preview of Valhalla.

public class Demo {
  public static void main(String[] args) {
    IO.println("Here we create sets and use them like values.");
    IO.println("But Java can actually use them like primitive integers.");
    final var set1 = SmallSet.of(1, 2, 3);
    IO.println(set1);

    IO.println(set1.compareTo(set1));

    final var set2 = SmallSet.of(7, 6, 5);
    IO.println(set2);
    IO.println(set1.compareTo(set2));
    IO.println(set2.compareTo(set1));

    IO.println();
    IO.println(new int[] { set1.value, set2.value }); // [I
    IO.println(new SmallSet[] { set1, set2 }); // [L
    // IO.println(new SmallSet![] { set1, set2 }); // see
    // https://openjdk.org/jeps/8316779
    IO.println(new Object[] { set1, set2 }); // [L
    // Older versions used to have [Q for arrays of value types
    // See: '4.3. Descriptors' in the latest JLS.
    // https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.3

    IO.println();
    IO.println("Filtered power set:");
    final var stream = set2.union(set1).powerset();
    IO.println(stream.filter(s -> s.size() == 2).map(String::valueOf).collect(Collectors.joining(" ")));

    IO.println();
    IO.println("You can still use it as a referenced object:");

    final Object o = set1; // Now it's in the heap memory
    printObject(o, set1, set2); // must be passed as a reference

    IO.println();
    IO.println(java.util.List.of(set1, set2));
    IO.println(java.util.Set.of(set1, set2));
    IO.println(java.util.Map.of(set1, set2));

    // It's serialzeable and the copy is equal to the original:
    try (final var bos = new ByteArrayOutputStream()) {
      try (final var oos = new ObjectOutputStream(bos)) {
        oos.writeObject(set1);
      }
      try (final var bis = new ByteArrayInputStream(bos.toByteArray()); final var oin = new ObjectInputStream(bis)) {
        final var copy = (SmallSet) oin.readObject();
        IO.println(set1 == copy); // true
      }
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }


    Sudoku.demo();
  }

  private static void printObject(Object o, Object set1, Object set2) {
    // This method must treat the set as an object (i.e. "o" is a reference)
    IO.println(o); // Uses custom toString implementation
    IO.println(o.hashCode()); // based on the 'value' field
    IO.println(System.identityHashCode(o)); // Same for any copy of the same value
    IO.println("bla".equals(o)); // false
    IO.println(o.equals(set2)); // false
    IO.println(o.equals(null)); // false
    IO.println(o.equals(o)); // true
    IO.println(o == set1); // true
    IO.println(set1 == o); // true
    IO.println(o == set2); // false
    IO.println(o == null); // false
    IO.println(o == o); // true
  }
}
