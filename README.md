<h2>Abstract</h2>
Methods to use an int as a bit set of small values. Uses 32 bits for a bit set and offers high performance for algorithms. Designed for Java 8 and compatible with common interfaces and classes.

<h2>Download</h2>
https://github.com/claudemartin/smallset/releases/tag/0.1

<h2>Javadoc</h2>
http://claude-martin.ch/smallset/doc/

<h2>About</h2>
I did this when I had an assignment for a sudoku solver/generator. I needed a fast way to handle sets that could contain 1 to 9. Then I extended it with the common methods you'd need for a set. You can use this whenever you need some very fast way to handle sets of small integers.

An int only allows 32 values (0 to 31), but it would be easy to switch to long. That would be mostly search/replace. The elements in the set would still be bytes. But don't forget to replace `1` by `1L`. 

Newer versions of Java might support value types (cf [http://cr.openjdk.java.net/~jrose/values/values-0.html]) . Then this can be changed to such a value type and the methods would not need to be static. 

<h2>Example</h2>
```java
import static ch.claude_martin.smallset.SmallSet.*;
import ch.claude_martin.smallset.SmallSet;
// ...
void static process(byte b) { // ...
// ...
  int set = of(1,3,4,7); // [1,3,4,7]
  set = add(set, (byte) 9); // [1,3,4,7,9]
  set = union(set, ofRange(12, 32)); // [1,3,4,7,9,12,...,31]
  containsAll(set, Arrays.asList(3,7));// true
  set = complement(set); // [0,2,5,6,8,10,11]
  System.out.println(SmallSet.toString(set)); // "(0,2,5,6,8,10,11)"
  for(byte b : iterate(set)) process(b);
  forEach(set, b -> process(b)); 
  int sum;
  sum = reduce(set, 0, Integer::sum); // 42
  sum = reduce(set, Integer::sum).orElse(0); // 42
  sum = sum(set); // 42
```

Check out the javadoc for more methods:
http://claude-martin.ch/smallset/doc/

Or browser the code. There are only 3 classes (plus a JUnit test class).
