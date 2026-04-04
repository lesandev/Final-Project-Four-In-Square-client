package co.il.leah.project;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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

            for (int slot = 0; slot < 4; slot++) {

                String idName = "cell_" + square + "_" + slot;
                int resId = activity.getResources()
                        .getIdentifier(idName, "id", activity.getPackageName());

                View cell = activity.findViewById(resId);
                if (cell == null) continue;

                int finalSquare = square;
                int finalSlot = slot;

                cell.setOnClickListener(v -> {

                    if (board.waitingForSlide) {
                        if (!board.canSlide(finalSquare)) return;

                        board.lastMyHoleIndex = board.holeIndex;
                        board.slide(finalSquare);
                        board.waitingForSlide = false;

                        updateUI();
                        listener.onPlayerMove();
                    } else {
                        if (!board.canPlace(finalSquare, finalSlot)) return;

                        board.place(finalSquare, finalSlot, 1);
                        board.waitingForSlide = true;

                        updateUI();
                    }
                });
            }
        }
    }

    public void updateUI() {

        for (int square = 0; square < 9; square++) {

            for (int slot = 0; slot < 4; slot++) {

                String idName = "cell_" + square + "_" + slot;
                int resId = activity.getResources()
                        .getIdentifier(idName, "id", activity.getPackageName());

                View cell = activity.findViewById(resId);
                if (cell == null) continue;

                if (square == board.holeIndex) {
                    cell.setBackgroundColor(Color.BLACK);
                    continue;
                }

                int value = board.squares[square][slot];

                boolean canSlide = board.waitingForSlide && board.canSlide(square);

                if (value == 1) {
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(canSlide ? Color.GREEN : Color.WHITE);

                    GradientDrawable circle = new GradientDrawable();
                    circle.setShape(GradientDrawable.OVAL);
                    circle.setColor(Color.BLUE);

                    LayerDrawable layers = new LayerDrawable(
                            new GradientDrawable[]{background, circle});
                    cell.setBackground(layers);
                } else if (value == 2) {
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(canSlide ? Color.GREEN : Color.WHITE);

                    GradientDrawable circle = new GradientDrawable();
                    circle.setShape(GradientDrawable.OVAL);
                    circle.setColor(Color.RED);

                    LayerDrawable layers = new LayerDrawable(
                            new GradientDrawable[]{background, circle});
                    cell.setBackground(layers);
                } else {
                    cell.setBackgroundColor(canSlide ? Color.GREEN : Color.WHITE);
                }
            }
        }
    }
}