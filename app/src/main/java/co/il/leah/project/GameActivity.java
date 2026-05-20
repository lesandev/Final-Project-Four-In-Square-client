package co.il.leah.project;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    Board board;
    BoardUI boardUI;
    int level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        level = getIntent().getIntExtra("level", 2);
        startGame();
    }

    void startGame() {
        board = new Board();
        boardUI = new BoardUI(this, board);

        boardUI.setupClicks(() -> {
            // אחרי תור השחקן — שואלים את השרת אם יש ניצחון
            ApiClient.checkWin(board, winner -> {
                runOnUiThread(() -> {
                    if (winner != 0) {
                        boardUI.enabled = true;
                        showGameOverDialog(winner);
                    } else {
                        sendComputerMove();
                    }
                });
            });
        });

        boardUI.enabled = false;
        boardUI.setStatus("Computer's turn…", false);
        boardUI.updateUI();
        sendComputerMove();
    }

    void sendComputerMove() {
        ApiClient.sendBoard(board, level, (newSquares, newHole) -> {
            runOnUiThread(() -> {
                int oldHole = board.holeIndex;
                int slidingSquare = newHole; // הריבוע שיזוז לתוך החור

                // שלב 1: הצג את האבן המונחת מיד (מצב ביניים — לפני הזזת החור)
                int[][] intermediate = buildIntermediateState(newSquares, newHole, oldHole);
                board.updateFromServer(intermediate, oldHole);
                boardUI.updateUI();

                // שלב 2: הריבוע חולק לאט לתוך החור (אנימציה מיידית אחרי הצגת האבן)
                boardUI.animateSlideForward(slidingSquare, oldHole, () -> {

                    // שלב 3: אחרי האנימציה — עדכן למצב הסופי
                    board.lastHoleIndex = oldHole;
                    board.updateFromServer(newSquares, newHole);
                    boardUI.updateUI();

                    ApiClient.checkWin(board, winner -> {
                        runOnUiThread(() -> {
                            boardUI.enabled = true;
                            if (winner != 0) {
                                showGameOverDialog(winner);
                            } else {
                                boardUI.setStatus("Your turn — Place a piece", true);
                            }
                        });
                    });
                });
            });
        });
    }

    /**
     * מחשב מצב ביניים: האבן כבר מונחת, אבל החור עדיין במקומו הישן (הזזה עדיין לא בוצעה חזותית).
     * עושה זאת על ידי ביטול ההזזה מה-newSquares הסופי.
     */
    private int[][] buildIntermediateState(int[][] newSquares, int newHole, int oldHole) {
        int[][] intermediate = new int[newSquares.length][];
        for (int i = 0; i < newSquares.length; i++) {
            intermediate[i] = newSquares[i].clone();
        }

        // מחזיר את תוכן ריבוע החור הישן (שהוזז לשם) בחזרה לריבוע המקורי שלו
        int oldHoleRow = (oldHole / 3) * 2;
        int oldHoleCol = (oldHole % 3) * 2;
        int newHoleRow = (newHole / 3) * 2;
        int newHoleCol = (newHole % 3) * 2;

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                intermediate[newHoleRow + r][newHoleCol + c] = newSquares[oldHoleRow + r][oldHoleCol + c];
                intermediate[oldHoleRow + r][oldHoleCol + c] = 0;
            }
        }

        return intermediate;
    }

    void showGameOverDialog(int winner) {
        String message;
        if (winner == 1) {
            message = "You won!";
        } else if (winner == 2) {
            message = "Computer won!";
        } else {
            message = "It's a Tie!";
        }

        new AlertDialog.Builder(this)
                .setTitle("Game Over!")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> startGame())
                .setNegativeButton("Back to Levels", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
