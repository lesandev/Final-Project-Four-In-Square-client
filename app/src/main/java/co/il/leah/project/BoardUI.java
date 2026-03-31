package co.il.leah.project;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

public class BoardUI {

    Activity activity;
    Board board;

    public interface OnMoveListener {
        void onPlayerMove();
    }

    public BoardUI(Activity activity, Board board) {
        this.activity = activity;
        this.board = board;
    }

    public void setupClicks(OnMoveListener listener) {

        for (int square = 0; square < 9; square++) {

            if (square == 4) continue;

            for (int slot = 0; slot < 4; slot++) {

                String idName = "cell_" + square + "_" + slot;
                int resId = activity.getResources()
                        .getIdentifier(idName, "id", activity.getPackageName());

                View cell = activity.findViewById(resId);

                int finalSquare = square;
                int finalSlot = slot;

                cell.setOnClickListener(v -> {

                    if (!board.canPlace(finalSquare, finalSlot)) return;

                    board.place(finalSquare, finalSlot, 1);

                    updateUI();

                    listener.onPlayerMove();
                });
            }
        }
    }

    public void updateUI() {

        for (int square = 0; square < 9; square++) {

            if (square == board.holeIndex) continue;

            for (int slot = 0; slot < 4; slot++) {

                String idName = "cell_" + square + "_" + slot;
                int resId = activity.getResources()
                        .getIdentifier(idName, "id", activity.getPackageName());

                View cell = activity.findViewById(resId);

                int value = board.squares[square][slot];

                if (value == 1) {
                    cell.setBackgroundColor(Color.BLUE);
                } else if (value == 2) {
                    cell.setBackgroundColor(Color.RED);
                } else {
                    cell.setBackgroundColor(Color.LTGRAY);
                }
            }
        }
    }
}