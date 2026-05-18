package co.il.leah.project;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.TextView;

public class BoardUI {

    Activity activity;
    Board board;
    TextView statusLabel;

    public interface OnMoveListener {
        void onPlayerMove();
    }

    public boolean enabled = true;

    public BoardUI(Activity activity, Board board) {
        this.activity = activity;
        this.board = board;
        statusLabel = activity.findViewById(R.id.statusLabel);
    }

    public void setStatus(String text, boolean isPlayerTurn) {
        if (statusLabel == null) return;
        statusLabel.setText(text);
        statusLabel.setBackgroundColor(isPlayerTurn ? Color.parseColor("#1565C0") : Color.parseColor("#C62828"));
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

                    // חסום לחיצות בזמן שמחכים לשרת
                    if (!enabled) return;

                    if (board.waitingForSlide) {
                        if (!board.canSlide(finalSquare)) return;

                        board.lastHoleIndex = board.holeIndex;
                        board.slide(finalSquare);
                        board.waitingForSlide = false;

                        enabled = false;
                        setStatus("Computer's turn…", false);
                        updateUI();
                        listener.onPlayerMove();
                    } else {
                        if (!board.canPlace(finalSquare, finalSlot)) return;

                        board.place(finalSquare, finalSlot, 1);
                        board.waitingForSlide = true;

                        setStatus("Your turn — Slide a square", true);
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

                int value = board.getCell(square, slot);

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