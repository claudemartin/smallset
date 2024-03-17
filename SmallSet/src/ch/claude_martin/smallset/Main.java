package ch.claude_martin.smallset;

import java.util.List;

// This only exists because Valhalla is early alpha and I don't use an IDE or JUNit for tests.

public class Main {
    public static void main(String[] args) {
        System.out.println("Here we create sets and use them like values.");
        System.out.println("But Java can actually use them like primitive integers.");
        final var set1 = SmallSet.of(1,2,3);
        System.out.println(set1);
        
        System.out.println(set1.compareTo(set1));

        final var set2 = SmallSet.of(7,6,5);
        System.out.println(set2);
        System.out.println(set1.compareTo(set2));
        System.out.println(set2.compareTo(set1));

        System.out.println();
        System.out.println("Note that you see [Q here:");
        System.out.println(new SmallSet[]{set1, set2});
        System.out.println("and [L here:");
        System.out.println(new Object[]{set1, set2});
        System.out.println("'Q' seems to be for inline classes while 'L' is the FieldType term for 'reference'.");
        // See: '4.3. Descriptors' in the lastest JLS. 
        // https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.3
        
        System.out.println();
        System.out.println("Filtered power set:");
        final var stream = set2.remove(2).union(set1).powerset();
        System.out.println(stream.filter(s->s.size()==2).map(String::valueOf).collect(java.util.stream.Collectors.joining(" ")));
        System.out.println();

        System.out.println();
        System.out.println("You can still use it as a referenced object:");
        final SmallSet.ref o = set1; // Now it's in the heap memory
        System.out.println(o); // custom toString implementation
        // Automatically generated code for hashCode and equals:
        System.out.println(o.hashCode()); // based on the 'value' field
        System.out.println("bla".equals(o)); // false
        System.out.println(o.equals(set2)); // false
        System.out.println(o.equals(null)); // false
        System.out.println(o.equals(o)); // true
        System.out.println(o == set1); // true
        System.out.println(set1 == o); // true
        System.out.println(o == set2); // false
        System.out.println(o == null); // false
        System.out.println(o == o); // true
    }


}