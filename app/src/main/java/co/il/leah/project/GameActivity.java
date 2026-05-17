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

        boardUI.setupClicks(() -> {
            // שלב 1: בדיקת ניצחון שחקן — מקומית על הטלפון
            int winner = board.checkWinner();
            if (winner != 0) {
                boardUI.enabled = true;
                showGameOverDialog(winner);
                return;
            }
            // שלב 2: אם השחקן לא ניצח — שולח לשרת כדי שהמחשב יזוז
            sendComputerMove();
        });

        boardUI.updateUI();
    }

    // שולח את הלוח לשרת — המחשב עושה מהלך, ואז בודקים ניצחון מקומית
    void sendComputerMove() {
        ApiClient.sendBoard(board, (newSquares, newHole, winner) -> {
            runOnUiThread(() -> {
                board.updateFromServer(newSquares, newHole);
                boardUI.enabled = true;
                boardUI.updateUI();

                // בדיקה מקומית — השרת לא מחזיר winner
                int localWinner = board.checkWinner();
                if (localWinner != 0) {
                    showGameOverDialog(localWinner);
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