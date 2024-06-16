package ch.claude_martin.smallset;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

// This only exists because Valhalla is early alpha and I don't use an IDE or JUNit for tests.

public class Main {
  public static void main(String[] args) {
    System.out.println("Here we create sets and use them like values.");
    System.out.println("But Java can actually use them like primitive integers.");
    final var set1 = SmallSet.of(1, 2, 3);
    System.out.println(set1);

    System.out.println(set1.compareTo(set1));

    final var set2 = SmallSet.of(7, 6, 5);
    System.out.println(set2);
    System.out.println(set1.compareTo(set2));
    System.out.println(set2.compareTo(set1));

    System.out.println();
    System.out.println("Note that you see [Q here:");
    System.out.println(new SmallSet[] { set1, set2 });
    System.out.println("and [L here:");
    System.out.println(new Object[] { set1, set2 });
    System.out.println("'Q' seems to be for inline classes while 'L' is the FieldType term for 'reference'.");
    // See: '4.3. Descriptors' in the lastest JLS.
    // https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.3

    System.out.println();
    System.out.println("Filtered power set:");
    final var stream = set2.remove(2).union(set1).powerset();
    System.out.println(
        stream.filter(s -> s.size() == 2).map(String::valueOf).collect(java.util.stream.Collectors.joining(" ")));
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

    System.out.println();

    System.out.println("===== SUDOKU: =====");// TODO
    printSudoku(SUDOKU);
    int solved = solveSudoku(SUDOKU);
    System.out.println(solved);
    printSudoku(SUDOKU);
  }

  final static SmallSet[][] SUDOKU = new SmallSet[][] { Main.row(0, 0, 0, 8, 0, 1, 0, 0, 0),
      Main.row(0, 0, 0, 0, 0, 0, 4, 3, 0), Main.row(5, 0, 0, 0, 0, 0, 0, 0, 0), Main.row(0, 0, 0, 0, 7, 0, 8, 0, 0),
      Main.row(0, 0, 0, 0, 0, 0, 1, 0, 0), Main.row(0, 2, 0, 0, 3, 0, 0, 0, 0), Main.row(6, 0, 0, 0, 0, 0, 0, 7, 5),
      Main.row(0, 0, 3, 4, 0, 0, 0, 0, 0), Main.row(0, 0, 0, 2, 0, 0, 6, 0, 0) };

  static SmallSet[] row(int a, int b, int c, int d, int e, int f, int g, int h, int i) {
    var array = new SmallSet[9];
    array[0] = a == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(a);
    array[1] = b == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(b);
    array[2] = c == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(c);
    array[3] = d == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(d);
    array[4] = e == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(e);
    array[5] = f == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(f);
    array[6] = g == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(g);
    array[7] = h == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(h);
    array[8] = i == 0 ? SmallSet.ofRangeClosed(1, 9) : SmallSet.singleton(i);
    return array;
  }

  static void deepCopy(SmallSet[][] grid, SmallSet[][] copy) {
    for (int i = 0; i < 9; i++) {
      System.arraycopy(grid, 0, copy, 0, 9);
    }
  }

  /** Naive sudoku solver. Returns 0 if there are no solutions, 1 if there is one solution, 2 otherwise.*/
  static int solveSudoku(SmallSet[][] grid) {
    while(true) {
      boolean changed = false;
      for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
        var row = grid[rowIndex];
        for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
          final SmallSet field = row[columnIndex];
          if (field.isEmpty())
            return 0;
          else if (field.size() == 1) {
            // apply all the basic rules
            var value = field.min().getAsByte();
            // remove the number from all in same row
            for (int columnIndex2 = 0; columnIndex2 < 9; columnIndex2++) {
              if (columnIndex == columnIndex2)
                continue;
              changed |= remove(row, columnIndex2, value);
            }
            // remove the number from all in same column
            for (int rowIndex2 = 0; rowIndex2 < 9; rowIndex2++) {
              if (rowIndex == rowIndex2)
                continue;
              changed |= remove(grid[rowIndex2], columnIndex, value);
            }
            // remove the number from all remaining fields in same box
            var r = remainingInSameBox(rowIndex);
            var c = remainingInSameBox(columnIndex);
            changed |= remove(grid[r.first], c.first, value);
            changed |= remove(grid[r.first], c.second, value);
            changed |= remove(grid[r.second], c.first, value);
            changed |= remove(grid[r.second], c.second, value);
          } else {
            for (byte value : field) {
              final var firstRow = rowIndex / 3 * 3;
              final var firstCol = columnIndex / 3 * 3;
              // union used for "Last remaining cell"
              SmallSet union = SmallSet.empty();
              for (int rowIndex2 = firstRow; rowIndex2 < firstRow + 3; rowIndex2++) {
                for (int columnIndex2 = firstCol; columnIndex2 < firstCol + 3; columnIndex2++) {
                  // "Last remaining cell"
                  if (rowIndex2 == rowIndex && columnIndex2 == columnIndex) continue;
                  union = union.union(grid[rowIndex2][columnIndex2]);
                }
              }
              if(!union.contains(value)) {
                grid[rowIndex][columnIndex] = SmallSet.of(value);
                changed = true;
              }
              
            }
            
          }
        }
      }
      // "Obvious pairs"
      
      if(!changed) break;
    } 
    return isSolved(grid) ? 1 : 2;
  }

  static boolean remove(SmallSet[] row, int columnIndex, byte value) {
    var set = row[columnIndex];
    if (!set.contains(value))
      return false;
    row[columnIndex] = set.remove(value);
    return true;
  }

  primitive

  record IntPair(int first, int second) {
  }

  /** The other two in the same row/column. */
  static IntPair remainingInSameBox(int index) {
    return switch (index % 3) {
      case 0 -> new IntPair(index + 1, index + 2);
      case 1 -> new IntPair(index - 1, index + 1);
      case 2 -> new IntPair(index - 2, index - 1);
      default -> throw new RuntimeException();
    };
  }

  static boolean isSolved(SmallSet[][] grid) {
    for (int i = 0; i < 9; i++) {
      var row = grid[i];
      for (int j = 0; j < 9; j++) {
        final var size = row[j].size();
        if (size == 0)
          throw new RuntimeException(String.format("isSolved: grid[%s][%s] is empty", i, j)); // TODO
        if (size != 1)
          return false;
      }
    }
    return true;
  }

  static void printSudoku(SmallSet[][] grid) {
    for (var i = 0; i < 19; i++) {
      System.out.print("-");
    }
    System.out.println();
    for (var row : grid) {
      System.out.print("|");
      for (var field : row) {
        switch (field.size()) {
          case 1 -> System.out.print(field.min().orElseThrow(() -> new RuntimeException())); // single value
          default -> System.out.print(' '); // none or multiple
        }
        // System.out.print(":"+field.toBinaryString(10).substring(0, 9));
        // System.out.print(":"+String.format("%19s", field));

        System.out.print("|");
      }
      System.out.println();
    }
    for (var i = 0; i < 19; i++) {
      System.out.print("-");
    }
    System.out.println();
    System.out.flush();
  }
}
