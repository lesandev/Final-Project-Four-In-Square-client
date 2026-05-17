package co.il.leah.project;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    Board board;
    BoardUI boardUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        startGame();
    }

    void startGame() {
        board = new Board();
        boardUI = new BoardUI(this, board);

        boardUI.setupClicks(() -> checkPlayerWinThenComputerMove());

        boardUI.updateUI();
        sendComputerMove();
    }

    // שלב 1: אחרי תור השחקן — שולח לשרת לבדוק אם השחקן ניצח
    void checkPlayerWinThenComputerMove() {
        ApiClient.sendBoard(board, true, (newSquares, newHole, winner) -> {
            runOnUiThread(() -> {
                if (winner != 0) {
                    // השחקן ניצח או תיקו — מציג דיאלוג
                    showGameOverDialog(winner);
                } else {
                    // אין ניצחון — עכשיו המחשב עושה את שלו
                    sendComputerMove();
                }
            });
        });
    }

    // שלב 2: שולח לשרת כדי שהמחשב יזוז ובודק אם המחשב ניצח
    void sendComputerMove() {
        ApiClient.sendBoard(board, false, (newSquares, newHole, winner) -> {
            runOnUiThread(() -> {
                board.updateFromServer(newSquares, newHole);
                boardUI.updateUI();

                if (winner != 0) {
                    showGameOverDialog(winner);
                }
            });
        });
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