package ch.claude_martin.smallset;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SudokuTest {

  @Test
  void test() {
    // This one is so difficult that brute force is necessary.
    var difficult = new Sudoku("407000009650400700009057000800003600900070000030000010000045900000060305002000080");
    var expected = new Sudoku("427386159651429738389157264874513692916278543235694817763845921148962375592731486");
    var solution = difficult.solve();
    assertEquals(expected, solution);
  }

}
