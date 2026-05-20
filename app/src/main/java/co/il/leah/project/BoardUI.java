package co.il.leah.project;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public class BoardUI {

    private static final long SLIDE_DURATION_MS = 450;

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
        statusLabel.setTextColor(isPlayerTurn ? Color.parseColor("#1565C0") : Color.parseColor("#C62828"));
    }

    /**
     * Animates the GridLayout of fromSquare sliding toward toHole.
     * Call this AFTER updateUI() has already drawn the intermediate state,
     * so the piece is visible before the slide begins.
     */
    public void animateSlideForward(int fromSquare, int toHole, Runnable onComplete) {
        View fromCell = getCellView(fromSquare, 0);
        View holeCell = getCellView(toHole, 0);

        if (fromCell == null || holeCell == null) {
            onComplete.run();
            return;
        }

        ViewGroup fromView = (ViewGroup) fromCell.getParent();
        ViewGroup holeView = (ViewGroup) holeCell.getParent();

        if (fromView == null || holeView == null) {
            onComplete.run();
            return;
        }

        fromView.post(() -> {
            float dx = holeView.getLeft() - fromView.getLeft();
            float dy = holeView.getTop() - fromView.getTop();

            fromView.setElevation(8f);

            ObjectAnimator animX = ObjectAnimator.ofFloat(fromView, "translationX", 0f, dx);
            ObjectAnimator animY = ObjectAnimator.ofFloat(fromView, "translationY", 0f, dy);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animX, animY);
            set.setDuration(SLIDE_DURATION_MS);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    fromView.setTranslationX(0f);
                    fromView.setTranslationY(0f);
                    fromView.setElevation(0f);
                    onComplete.run();
                }
            });
            set.start();
        });
    }

    View getCellView(int square, int slot) {
        String idName = "cell_" + square + "_" + slot;
        int resId = activity.getResources()
                .getIdentifier(idName, "id", activity.getPackageName());
        return activity.findViewById(resId);
    }

    public void setupClicks(OnMoveListener listener) {

        for (int square = 0; square < 9; square++) {

            for (int slot = 0; slot < 4; slot++) {

                View cell = getCellView(square, slot);
                if (cell == null) continue;

                int finalSquare = square;
                int finalSlot = slot;

                cell.setOnClickListener(v -> {

                    if (!enabled) return;

                    if (board.waitingForSlide) {
                        if (!board.canSlide(finalSquare)) return;

                        enabled = false;
                        int currentHole = board.holeIndex;

                        // אנימציה: הריבוע חולק לתוך החור, ואז מתעדכן המצב
                        animateSlideForward(finalSquare, currentHole, () -> {
                            board.lastHoleIndex = board.holeIndex;
                            board.slide(finalSquare);
                            board.waitingForSlide = false;
                            setStatus("Computer's turn…", false);
                            updateUI();
                            listener.onPlayerMove();
                        });
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

                View cell = getCellView(square, slot);
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
