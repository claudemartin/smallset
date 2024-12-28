package ch.claude_martin.smallset;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/** Simple sudoku generator and solver. This uses quite naive brute force. */
public class Sudoku implements Cloneable {
  enum STATE {
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

  final static SmallSet ALL               = SmallSet.ofRangeClosed(1, 9); // All possible values

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

  final SmallSet[]      grid              = new SmallSet[9 * 9];

  Sudoku() {
    for (int i = 0; i < this.grid.length; i++) {
      this.grid[i] = Sudoku.ALL;
    }
  }

  Sudoku(final SmallSet[] grid) {
    if (grid.length != 9 * 9) {
      throw new IllegalArgumentException();
    }
    System.arraycopy(grid, 0, this.grid, 0, 9 * 9);
  }

  @Override
  public Sudoku clone() {
    return new Sudoku(this.grid);
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

  /** This is the most naive approach where you only check the three rules of sudoku. This might already solve it. it
   * returns true if anything was changed. */
  private void naive() {
    int max = 1000; // just to be absolutely sure we don't end in an endless
                    // loop.
    while (true) {
      --max;
      boolean changed = false;

      for (int pos = 0; pos < this.grid.length; pos++) {
        var values = this.grid[pos];
        if (values.size() <= 1) {
          continue;
        }

        final var col = pos % 9; // pos 9 => 0 (fist column)
        final var row = (pos - col) / 9;

        for (int i = 0; i < 9; i++) {
          if (row != i) {
            final var other = this.get(i, col).singleElement();
            if (other.isPresent()) {
              this.grid[pos] = values = values.remove(other.getAsByte());
              changed = true;
            }
          }
          if (col != i) {
            final var other = this.get(row, i).singleElement();
            if (other.isPresent()) {
              this.grid[pos] = values = values.remove(other.getAsByte());
              changed = true;
            }
          }
          for (final int pos2 : this.subgrid(pos)) {
            if (pos != pos2) {
              final var other = this.get(pos2).singleElement();
              if (other.isPresent()) {
                this.grid[pos] = values = values.remove(other.getAsByte());
                changed = true;
              }
            }
          }
        }
      }

      // Fill those that could now only be in one place of a row / column / subgrid:
      value:
      for (byte v = 1; v <= 9; v++) {
        final var single = SmallSet.singleton(v);
        for (int x = 0; x < 9; x++) {
          byte occurancesInRow = 0;
          int columnWithValue = -1;
          byte occurancesInCol = 0;
          int rowWithValue = -1;
          for (int y = 0; y < 9; y++) {
            { // x for row and y for column
              final SmallSet set = this.get(x, y);
              if (set == single) {
                continue value;
              }
              if (set.contains(v)) {
                occurancesInRow++;
                columnWithValue = y;
              }
            }
            { // x for column and y for row
              final SmallSet set = this.get(y, x);
              if (set == single) {
                continue value;
              }
              if (set.contains(v)) {
                occurancesInCol++;
                rowWithValue = y;
              }
            }
          }
          if (occurancesInRow == 1) {
            changed = true;
            this.set(x, columnWithValue, single);
          }
          if (occurancesInCol == 1) {
            changed = true;
            this.set(x, rowWithValue, single);
          }
        }
        for (final int[] subgrid : Sudoku.SUBGRID_POSITIONS) {
          byte occurancesInSubgrid = 0;
          int posWithValue = -1;
          for (final int pos : subgrid) {
            final SmallSet set = this.get(pos);
            if (set == single) {
              continue value;
            }
            if (set.contains(v)) {
              occurancesInSubgrid++;
              posWithValue = pos;
            }
          }
          if (occurancesInSubgrid == 1) {
            changed = true;
            this.set(posWithValue, single);
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
    public long count() {
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
        final int finalPost = pos;
        if (this.mode != Mode.FIND_TWO && this.adder.sum() > 0) {
          return null;
        }

        final SmallSet options = this.sudoku.get(pos);
        if (options.size() <= 1) {
          continue;
        }

        final var subtasks = options.stream().map(value -> {
          final var clone = this.sudoku.clone();
          clone.set(finalPost, SmallSet.singleton(value)); // remove some values
          clone.naive(); // solve the obvious fields
          return new BruteForce(this.mode, clone, finalPost + 1, this.adder);
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
  private Sudoku solve() {
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

  /** Returns all the subgrid positions that will contain the given position. */
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

  STATE check() {
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
              return STATE.INVALID;
            }
            row = next;
          }
        }
        {
          final byte element = copy[i + 9 * j];
          if (element > 0) {
            final var next = col.add(element);
            if (next == col) {
              return STATE.INVALID;
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
            return STATE.INVALID;
          }
          set = next;
        }
      }
    }

    for (int i = 0; i < 9 * 9; i++) {
      if (this.grid[i].size() != 1) {
        return STATE.UNSOLVED;
      }
    }

    return STATE.SOLVED;
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

  /** This sudoku as a "puzzle string". */
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
    System.out.println("As Puzzle String: " + genrated.sudoku().toString());
    System.out.println();
    System.out.println("Solution: ");
    genrated.solution().print();
    System.out.println();
    System.out.println("Sudoku genarated in " + timeElapsed + " ms.");

  }

}
