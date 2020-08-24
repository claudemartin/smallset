package ch.claude_martin.smallset;

// This only exists because Valhalla is early alpha and I don't use an IDE or JUNit for tests.

public class Main {
    public static void main(String[] args) {
       var set1 = SmallSet.of(1,2,3);
       System.out.println(set1);

       System.out.println(set1.compareTo(set1));

       var set2 = SmallSet.of(7,6,5);
       System.out.println(set2);
       System.out.println(set1.compareTo(set2));
       System.out.println(set2.compareTo(set1));

       var set3 = set2.remove((byte) 2).union(set1).powerset();
       System.out.println(set3.filter(s->s.size()==2).map(String::valueOf).collect(java.util.stream.Collectors.joining(" ")));
    }


}