package co.il.leah.project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    Board board;
    BoardUI boardUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        board = new Board();
        boardUI = new BoardUI(this, board);

        boardUI.setupClicks(() -> {
            sendToServer();
        });

        boardUI.updateUI(); // הצגת החור השחור בהתחלה

        sendToServer(); // מחשב מתחיל
    }

    void sendToServer() {
        ApiClient.sendBoard(board, (newSquares, newHole) -> {
            runOnUiThread(() -> {
                board.updateFromServer(newSquares, newHole);
                boardUI.updateUI();
            });
        });
    }
}