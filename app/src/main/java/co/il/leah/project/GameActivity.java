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

        boardUI.updateUI();
        sendComputerMove(); // המחשב תמיד מתחיל ראשון
    }

    // שולח לשרת כדי שהמחשב יזוז, ואז בודק ניצחון דרך השרת
    void sendComputerMove() {
        ApiClient.sendBoard(board, level, (newSquares, newHole) -> {
            runOnUiThread(() -> {
                board.lastAiHoleIndex = board.holeIndex; // חור לפני מהלך המחשב — המחשב לא יחזור לשם
                board.updateFromServer(newSquares, newHole);
                boardUI.updateUI();

                // אחרי תור המחשב — שואלים את השרת אם יש ניצחון
                ApiClient.checkWin(board, winner -> {
                    runOnUiThread(() -> {
                        boardUI.enabled = true;
                        if (winner != 0) {
                            showGameOverDialog(winner);
                        }
                    });
                });
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