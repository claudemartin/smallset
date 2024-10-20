package ch.claude_martin.smallset;

import static java.lang.System.out;

import java.io.*;
import java.util.stream.Collectors;

// This is just a simple demo of what my code does. Some of this will change with the next preview of Valhalla.

public class Demo {
  public static void main(String[] args) {
    out.println("Here we create sets and use them like values.");
    out.println("But Java can actually use them like primitive integers.");
    final var set1 = SmallSet.of(1, 2, 3);
    out.println(set1);

    out.println(set1.compareTo(set1));

    final var set2 = SmallSet.of(7, 6, 5);
    out.println(set2);
    out.println(set1.compareTo(set2));
    out.println(set2.compareTo(set1));

    out.println();
    out.println(new int[] { set1.value, set2.value }); // [I
    out.println(new SmallSet[] { set1, set2 }); // [L
    // out.println(new SmallSet![] { set1, set2 }); // see https://openjdk.org/jeps/8316779
    out.println(new Object[] { set1, set2 }); // [L
    // Older versions used to have [Q for arrays of value types
    // See: '4.3. Descriptors' in the latest JLS.
    // https://docs.oracle.com/javase/specs/jvms/se23/html/jvms-4.html#jvms-4.3

    out.println();
    out.println("Filtered power set:");
    final var stream = set2.union(set1).powerset();
    out.println(stream
        .filter(s -> s.size() == 2)
        .map(String::valueOf)
        .collect(Collectors.joining(" ")));

    out.println();
    out.println("You can still use it as a referenced object:");

    final Object o = set1; // Now it's in the heap memory
    printObject(o, set1, set2); // must be passed as a reference

    out.println();
    out.println(java.util.List.of(set1, set2));
    out.println(java.util.Set.of(set1, set2));
    out.println(java.util.Map.of(set1, set2));

    // It's serialzeable and the copy is equal to the original:
    try (final var bos = new ByteArrayOutputStream()) {
      try (final var oos = new ObjectOutputStream(bos)) {
        oos.writeObject(set1);
      }
      try (final var bis = new ByteArrayInputStream(bos.toByteArray()); final var oin = new ObjectInputStream(bis)) {
        final var copy = (SmallSet) oin.readObject();
        out.println(set1 == copy); // true
      }
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
  }

  private static void printObject(Object o, Object set1, Object set2) {
    // This method must treat the set as an object (i.e. "o" is a reference)
    out.println(o); // Uses custom toString implementation
    out.println(o.hashCode()); // based on the 'value' field
    out.println(System.identityHashCode(o)); // Same for any copy of the same value
    out.println("bla".equals(o)); // false
    out.println(o.equals(set2)); // false
    out.println(o.equals(null)); // false
    out.println(o.equals(o)); // true
    out.println(o == set1); // true
    out.println(set1 == o); // true
    out.println(o == set2); // false
    out.println(o == null); // false
    out.println(o == o); // true
  }
}
