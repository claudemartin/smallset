<h2>Abstract</h2>
Methods to use an int as a bit set of small values. Uses 32 bits for a bit set and offers high performance for algorithms. Designed for Java Valhalla and compatible with many common interfaces and classes.

<h2>About</h2>
I did this when I had an assignment for a sudoku solver/generator. I needed a fast way to handle sets that could contain 1 to 9. Then I extended it with the common methods you'd need for a set. You can use this whenever you need some very fast way to handle sets of small integers.

Later I migrated this to JDK 14 Valhalla preview. It wasn't stable enough as it crashed constantly.
Some years later I tried again with JDK 20 Valhalla preview and not it works except for serialisation.

An int only allows 32 values (0 to 31), but it would be easy to switch to long. That would be mostly search/replace. The elements in the set would still be bytes. But don't forget to replace `1` by `1L`. 

Newer versions of Java might support value types in Generics and I might update this once again.

<h2>Example</h2>

```java
import static ch.claude_martin.smallset.SmallSet.*;
import ch.claude_martin.smallset.SmallSet;
// ...
  SmallSet set = of(1,3,4,7); // [1,3,4,7]
  set.add(9); // [1,3,4,7,9]
  set = set.union(ofRange(12, 32)); // [1,3,4,7,9,12,...,31]
  set.containsAll(List.of(3,7));// true
  set = set.complement(); // [0,2,5,6,8,10,11]
  System.out.println(set); // "(0,2,5,6,8,10,11)"
  for(byte b : set) process(b);
  set.forEach(b -> process(b)); 
  int sum = set.sum(); // 42
  sum = set.reduce(0, Integer::sum); // 42
  sum = set.reduce(Integer::sum).orElse(0); // 42
// ...
  void static process(byte b) { // ...
```

There's also a Demo class that you can try out. Just run `ant demo` to see what it does.

<h2>Package</h2>

It's a package named "ch.claude_martin.smallset" that exports the package with the same name. But I don't publish to Maven Central because that simply would be too much work for me. 
The ant script is rather simple and easy to use. And this is mostly just a simple demonstration of Project Valhalla, so I have no motivation to create an account at Maven Central.

<h2>Further Reading:</h2>

If you are interested in Project Valhalla I recommend you read this post on reddit that I wrote about this project here:  
[r/java - I actually tested JDK 20 Valhalla, here are my findings](https://www.reddit.com/r/java/comments/1dnhgut/i_actually_tested_jdk_20_valhalla_here_are_my/)

Or just read about the project here:  
[openjdk.org - Project Valhalla](https://openjdk.org/projects/valhalla/)
