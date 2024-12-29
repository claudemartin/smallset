package ch.claude_martin.smallset;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Simple sudoku generator and solver. This uses quite naive brute force. */
public class Sudoku implements Cloneable {
  enum State {
    /** Solved and all values are valid. */
    SOLVED,
    /** Not solved but maybe it can be solved. */
    UNSOLVED,
    /** Can't be solved. */
    INVALID;
  }

  enum Mode {
    /** Find first solution. Pick random numbers. */
    GENERATE,
    /** Find any solution. Used to find the first best solution. */
    FIND_ANY,
    /** Find up to two solutions. Used to check that there is only one. */
    FIND_TWO;
  }

  final static SmallSet ALL               = SmallSet.ofRangeClosed(1, 9);         // All possible
                                                                                  // values

  final static int[][]  SUBGRID_POSITIONS = new int[][] {
      // @formatter:off
      new int[] {  0,  1,  2,  9, 10, 11, 18, 19, 20 },
      new int[] {  3,  4,  5, 12, 13, 14, 21, 22, 23 },
      new int[] {  6,  7,  8, 15, 16, 17, 24, 25, 26 },
      new int[] { 27, 28, 29, 36, 37, 38, 45, 46, 47 },
      new int[] { 30, 31, 32, 39, 40, 41, 48, 49, 50 },
      new int[] { 33, 34, 35, 42, 43, 44, 51, 52, 53 },
      new int[] { 54, 55, 56, 63, 64, 65, 72, 73, 74 },
      new int[] { 57, 58, 59, 66, 67, 68, 75, 76, 77 },
      new int[] { 60, 61, 62, 69, 70, 71, 78, 79, 80 },
      // @formatter:on
  };

  final static int[][]  ROW_POSITIONS     = new int[][] {
    // @formatter:off
    new int[] {  0,  1,  2,  3,  4,  5,  6,  7,  8 },
    new int[] {  9, 10, 11, 12, 13, 14, 15, 16, 17 },
    new int[] { 18, 19, 20, 21, 22, 23, 24, 25, 26 },
    new int[] { 27, 28, 29, 30, 31, 32, 33, 34, 35 },
    new int[] { 36, 37, 38, 39, 40, 41, 42, 43, 44 },
    new int[] { 45, 46, 47, 48, 49, 50, 51, 52, 53 },
    new int[] { 54, 55, 56, 57, 58, 59, 60, 61, 62 },
    new int[] { 63, 64, 65, 66, 67, 68, 69, 70, 71 },
    new int[] { 72, 73, 74, 75, 76, 77, 78, 79, 80 },
    // @formatter:on
  };

  final static int[][]  COLUMN_POSITIONS  = new int[][] {
    // @formatter:off
    new int[] { 0,  9, 18, 27, 36, 45, 54, 63, 72 },
    new int[] { 1, 10, 19, 28, 37, 46, 55, 64, 73 },
    new int[] { 2, 11, 20, 29, 38, 47, 56, 65, 74 },
    new int[] { 3, 12, 21, 30, 39, 48, 57, 66, 75 },
    new int[] { 4, 13, 22, 31, 40, 49, 58, 67, 76 },
    new int[] { 5, 14, 23, 32, 41, 50, 59, 68, 77 },
    new int[] { 6, 15, 24, 33, 42, 51, 60, 69, 78 },
    new int[] { 7, 16, 25, 34, 43, 52, 61, 70, 79 },
    new int[] { 8, 17, 26, 35, 44, 53, 62, 71, 80 },
    // @formatter:on
  };

  final static int[][]  HOUSES            = Stream
      .of(Sudoku.SUBGRID_POSITIONS, Sudoku.ROW_POSITIONS, Sudoku.COLUMN_POSITIONS)
      .flatMap(Arrays::stream).toArray(int[][]::new);

  final SmallSet[]      grid;

  public Sudoku() {
    this.grid = new SmallSet[9 * 9];
    for (int i = 0; i < this.grid.length; i++) {
      this.grid[i] = Sudoku.ALL;
    }
  }

  /** Create sudoku from given string. Line breaks are ignored. Space, 0, and _ is interpreted as empty. */
  public Sudoku(String string) {
    this(string.codePoints()
        .mapToObj(chr -> switch (chr) {
          case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> SmallSet.singleton(chr - '0');
          case '0', ' ', '_' -> Sudoku.ALL;
          default -> null;
        })
        .filter(Objects::nonNull)
        .toArray(SmallSet[]::new));
  }

  private Sudoku(final SmallSet[] grid) {
    this.grid = grid;
    Objects.requireNonNull(grid);
    if (grid.length != 9 * 9) {
      throw new IllegalArgumentException(
          "Sudoku must contain exactly 81 values. Given data has " + grid.length);
    }
  }

  @Override
  public Sudoku clone() {
    var gridCopy = new SmallSet[9 * 9];
    System.arraycopy(this.grid, 0, gridCopy, 0, 9 * 9);
    return new Sudoku(gridCopy);
  }

  /** Number of fields with exactly one value. These are the "clues" because the exact value is known. A solved sudoku
   * (full grid) has 81 clues. */
  private int clues() {
    int result = 0;
    for (int i = 0; i < 9 * 9; i++) {
      if (this.grid[i].size() == 1) {
        result++;
      }
    }
    return result;
  }

  /** This is the most basic approach. This might already solve it. it returns true if anything was changed. */
  private void basic() {
    int max = 1000; // just to be absolutely sure we don't end in an endless loop.
    while (true) {
      --max;
      boolean changed = false;

      for (int pos = 0; pos < 9 * 9; pos++) {
        var values = this.grid[pos];
        if (values.size() <= 1) {
          continue;
        }

        for (final int[] house : this.houses(pos)) {
          for (final int pos2 : house) {
            if (pos != pos2) {
              final var values2 = this.get(pos2);
              final var other = values2.singleElement();
              if (other.isPresent()) {
                this.grid[pos] = values = values.remove(other.getAsByte());
                changed = true;
              }
            }
          }
        }
      }

      // Hidden Singles: Fill those that could now only be in one place of a house.
      for (byte v = 1; v <= 9; v++) {
        final var single = SmallSet.singleton(v);
        house:
        for (final int[] house : Sudoku.HOUSES) {
          byte occurances = 0;
          int hiddenSinglePos = -1;
          for (final int pos : house) {
            final SmallSet set = this.get(pos);
            if (set == single) {
              continue house;
            }
            if (set.contains(v)) {
              occurances++;
              hiddenSinglePos = pos;
            }
          }
          if (occurances == 1) {
            changed = true;
            this.set(hiddenSinglePos, single);
          }
        }
      }

      // Hidden Pairs: Remove those that can only be in two fields
      for (final int[] house : Sudoku.HOUSES) {
        for (int i = 0; i < house.length; i++) {
          final int pos1 = house[i];
          for (int j = i + 1; j < house.length; j++) {
            final int pos2 = house[j];
            final var v1 = this.get(pos1);
            if (v1.size() == 2) {
              final var v2 = this.get(pos2);
              if (v1.size() == 2 && v2 == v1) {
                for (final int pos3 : house) {
                  if (pos3 == pos1 || pos3 == pos2) {
                    continue;
                  }
                  changed = true;
                  final var v3 = this.get(pos3);
                  final var v4 = v3.minus(v1);
                  if (v3 != v4) {
                    changed = true;
                    this.set(pos3, v4);
                  }
                }
              }
            }
          }
        }
      }

      if (!changed || max < 0) {
        break;
      }
    }
  }

  static class BruteForce extends RecursiveTask<Sudoku> {
    private static final long serialVersionUID = 1L;

    private final Mode        mode;
    private final Sudoku      sudoku;
    private final int         offset;
    private final LongAdder   adder;

    /** How many solutions were found (only makes sense when using {@link Mode#FIND_TWO}). There might more solutions
     * that were not found. */
    protected long count() {
      if (this.mode != Mode.FIND_TWO) {
        throw new IllegalStateException("This brute force didn't try to find multiple solutions.");
      }
      return this.adder.sum();
    }

    BruteForce(final Mode mode, final Sudoku sudoku) {
      this(mode, sudoku, 0, new LongAdder());
    }

    private BruteForce(
        final Mode mode,
        final Sudoku sudoku,
        final int offset,
        final LongAdder adder) {
      this.mode = mode;
      this.sudoku = sudoku;
      this.offset = offset;
      this.adder = adder;
    }

    @Override
    protected Sudoku compute() {
      if (this.mode != Mode.FIND_TWO && this.adder.sum() > 0) {
        return null;
      }
      final var state = this.sudoku.check();
      switch (state) {
        case SOLVED:
          this.adder.increment();
          return this.sudoku;
        case INVALID:
          return null;
        case UNSOLVED:
          break;
      }
      final var rnd = ThreadLocalRandom.current();
      for (int pos = this.offset; pos < 9 * 9; pos++) {
        if (this.mode != Mode.FIND_TWO && this.adder.sum() > 0) {
          return null;
        }
        final int finalPos = pos;

        final SmallSet options = this.sudoku.get(pos);
        if (options.size() <= 1) {
          continue;
        }

        final var subtasks = options.stream().map(value -> {
          final var clone = this.sudoku.clone();
          clone.set(finalPos, SmallSet.singleton(value)); // remove some values
          clone.basic(); // solve the obvious fields
          return new BruteForce(this.mode, clone, finalPos + 1, this.adder);
        }).collect(Collectors.toCollection(ArrayList::new));

        if (this.mode == Mode.GENERATE) {
          Collections.shuffle(subtasks, rnd);
        }

        final var solutions = ForkJoinTask.invokeAll(subtasks).stream().map(ForkJoinTask::join).filter(Objects::nonNull)
            .toList();

        // We never really need multiple solutions, so we return the first:
        if (!solutions.isEmpty()) {
          return solutions.getFirst();
        }
      }
      return null;
    }
  }

  /** Finds and returns a solution. This doesn't check if there are more solutions. */
  public Sudoku solve() {
    final ForkJoinPool commonPool = ForkJoinPool.commonPool();
    return commonPool.invoke(new BruteForce(Mode.FIND_ANY, this.clone()));
  }

  /** Checks if there are more than one solution. */
  private boolean hasMultipleSolutions() {
    final ForkJoinPool commonPool = ForkJoinPool.commonPool();
    final BruteForce task = new BruteForce(Mode.FIND_TWO, this);
    commonPool.invoke(task);
    return task.count() > 1;
  }

  public record SolvedSudoku(Sudoku sudoku, Sudoku solution) {
  }

  /** Generates a random Sudoku. */
  static SolvedSudoku generate(final int seconds) {
    final var seed = new Sudoku();
    final var rng = ThreadLocalRandom.current();
    {
      // Fill a complete row:
      final int row = rng.nextInt(9);
      var set = Sudoku.ALL;
      for (int col = 0; col < 9; col++) {
        final int value = set.random(rng);
        set = set.remove(value);
        seed.set(row, col, SmallSet.singleton(value));
      }
    }
    // Find a valid, random sudoku based on given first row of numbers.
    final var solution = ForkJoinPool.commonPool().invoke(new BruteForce(Mode.GENERATE, seed));
    // now we have a randomly filled out (solved) sudoku.

    // Find a sudoku with only one solution but not too many clues.
    final AtomicReference<Sudoku> candidate = new AtomicReference<>(null);
    final int availableProcessors = Runtime.getRuntime().availableProcessors();
    final var pool = Executors.newFixedThreadPool(availableProcessors, r -> {
      final Thread thread = new Thread(r);
      thread.setDaemon(true);
      thread.setName("Sudoku - " + r.hashCode());
      return thread;
    });
    for (int i = 0; i < availableProcessors; i++) {
      pool.execute(() -> {
        while (true) {
          final var sudoku = Sudoku.createFromSolution(solution);
          candidate.accumulateAndGet(sudoku, (a, b) -> a == null ? b : (a.clues() < b.clues() ? a : b));
        }
      });
    }
    try {
      pool.shutdown();
      pool.awaitTermination(seconds, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      // ignore
    }

    // return the one with fewest clues, which is probably the hardest one.
    if (candidate.get() == solution) {
      throw new RuntimeException("Failed to generate a Sudoko. Try again.");
    }
    return new SolvedSudoku(candidate.get(), solution);
  }

  /** Clone and then remove numbers at random positions for as long as there is more than one solution. */
  private static Sudoku createFromSolution(final Sudoku solution) {
    Sudoku sudoku = solution;
    final var rng = ThreadLocalRandom.current();
    while (true) {
      final int pos = rng.nextInt(9 * 9);
      final var next = sudoku.clone();
      next.set(pos, Sudoku.ALL);
      if (next.hasMultipleSolutions()) {
        return sudoku;
      }
      sudoku = next;
    }
  }

  SmallSet get(final int pos) {
    return this.grid[pos];
  }

  SmallSet get(final int row, final int col) {
    return this.grid[9 * row + col];
  }

  boolean isSingleValue(final int pos) {
    return this.grid[pos].size() == 1;
  }

  boolean isSingleValue(final int row, final int col) {
    return this.grid[9 * row + col].size() == 1;
  }

  /** Returns array of all the subgrid positions that contains the given position. */
  int[] subgrid(final int pos) {
    // switch-case is probably faster than calculating it for each call
    final int i = switch (pos) {
      // @formatter:off
      case  0,  1,  2,  9, 10, 11, 18, 19, 20 -> 0;
      case  3,  4,  5, 12, 13, 14, 21, 22, 23 -> 1;
      case  6,  7,  8, 15, 16, 17, 24, 25, 26 -> 2;
      case 27, 28, 29, 36, 37, 38, 45, 46, 47 -> 3;
      case 30, 31, 32, 39, 40, 41, 48, 49, 50 -> 4;
      case 33, 34, 35, 42, 43, 44, 51, 52, 53 -> 5;
      case 54, 55, 56, 63, 64, 65, 72, 73, 74 -> 6;
      case 57, 58, 59, 66, 67, 68, 75, 76, 77 -> 7;
      case 60, 61, 62, 69, 70, 71, 78, 79, 80 -> 8;
      // @formatter:on
      default -> throw new ArrayIndexOutOfBoundsException(pos);
    };
    return Sudoku.SUBGRID_POSITIONS[i];
  }

  /** Returns array of all the row positions that contains the given position. */
  private int[] row(final int pos) {
    return Sudoku.ROW_POSITIONS[pos / 9];
  }

  /** Returns array of all the colum positions that contains the given position. */
  private int[] column(final int pos) {
    return Sudoku.COLUMN_POSITIONS[pos % 9];
  }

  private int[][] houses(final int pos) {
    return new int[][] { this.subgrid(pos), this.row(pos), this.column(pos) };
  }

  void set(final int pos, final SmallSet value) {
    this.grid[pos] = value;
  }

  void set(final int row, final int col, final SmallSet value) {
    this.grid[9 * row + col] = value;
  }

  SmallSet add(final int pos, final byte value) {
    return this.grid[pos] = this.grid[pos].add(value);
  }

  SmallSet add(final int row, final int col, final byte value) {
    return this.add(9 * row + col, value);
  }

  SmallSet remove(final int pos, final byte value) {
    return this.grid[pos] = this.grid[pos].remove(value);
  }

  SmallSet remove(final int row, final int col, final byte value) {
    return this.remove(9 * row + col, value);
  }

  State check() {
    final byte[] copy = this.asBytes();

    // all 9 rows and 9 columns:
    for (int i = 0; i < 9; i++) {
      var row = SmallSet.empty();
      var col = SmallSet.empty();
      for (int j = 0; j < 9; j++) {
        {
          final byte element = copy[9 * i + j];
          if (element > 0) {
            final var next = row.add(element);
            if (next == row) {
              return State.INVALID;
            }
            row = next;
          }
        }
        {
          final byte element = copy[i + 9 * j];
          if (element > 0) {
            final var next = col.add(element);
            if (next == col) {
              return State.INVALID;
            }
            col = next;
          }
        }
      }
    }

    // all 9 subgrids:
    for (final int[] subgrid : Sudoku.SUBGRID_POSITIONS) {
      var set = SmallSet.empty();
      for (final int pos : subgrid) {
        final byte element = copy[pos];
        if (element > 0) {
          final var next = set.add(element);
          if (next == set) {
            return State.INVALID;
          }
          set = next;
        }
      }
    }

    for (int i = 0; i < 9 * 9; i++) {
      if (this.grid[i].size() != 1) {
        return State.UNSOLVED;
      }
    }

    return State.SOLVED;
  }

  private byte[] asBytes() {
    final var result = new byte[9 * 9];
    for (int i = 0; i < this.grid.length; i++) {
      result[i] = this.grid[i].singleElement().orElse((byte) -1);
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final Sudoku other) {
      return Arrays.equals(this.grid, other.grid);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 1;
    for (final var element : this.grid) {
      result = 31 * result + element.hashCode();
    }
    return result;
  }

  /** This sudoku as a String. */
  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder(81);
    for (int i = 0; i < 9 * 9; i++) {
      b.append(Byte.toString(this.grid[i].singleElement().or0()));
    }
    return b.toString();
  }

  public void print() {
    for (int row = 0; row < 9; row++) {
      if (row % 3 == 0) {
        System.out.println("+---+---+---+");
      }
      for (int col = 0; col < 9; col++) {
        if (col % 3 == 0) {
          System.out.print('|');
        }
        final SmallSet value = this.get(row, col);
        switch (value.size()) {
          case 1:
            System.out.print(value.singleElement().orElseThrow());
            break;
          case 0:
            System.out.print('X');
            break;
          default:
            System.out.print(' ');
        }
      }
      System.out.println('|');

    }
    System.out.println("+---+---+---+");
    System.out.flush();
  }

  public static void demo() {
     System.out.println();
     System.out.println("=== SUDOKU ===");
     System.out.println();
     System.out.println("Generating random sudoku. This will take a minute. ");
     System.out.println();
     System.out.flush();
    
     final var start = System.nanoTime();
     final var genrated = Sudoku.generate(30);
     final var finish = System.nanoTime();
     final var timeElapsed = Duration.ofNanos(finish - start).toMillis();
    
     genrated.sudoku().print();
     System.out.println();
     System.out.println("No. of clues: " + genrated.sudoku().clues());
     System.out.println();
     System.out.println("As String: " + genrated.sudoku().toString());
     System.out.println();
     System.out.println("Solution: ");
     genrated.solution().print();
     System.out.println();
     System.out.println("Sudoku genarated in " + timeElapsed + " ms.");

  }

}
