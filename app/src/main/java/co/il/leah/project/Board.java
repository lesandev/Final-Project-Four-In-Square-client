package co.il.leah.project;

public class Board {

    public int[][] cells;  // 6x6 grid
    public int holeIndex;  // 0-8, which 2x2 block is the hole
    public int lastMyHoleIndex = -1;
    public boolean waitingForSlide = false;

    // constructor
    public Board() {
        cells = new int[6][6];
        holeIndex = 4;
    }

    // המרה מ-square+slot לקואורדינטות 6x6
    private int toGlobalRow(int square, int slot) {
        int squareRow = square / 3;
        int miniRow = slot / 2;
        return squareRow * 2 + miniRow;
    }

    private int toGlobalCol(int square, int slot) {
        int squareCol = square % 3;
        int miniCol = slot % 2;
        return squareCol * 2 + miniCol;
    }

    // קבלת ערך לפי square+slot
    public int getCell(int square, int slot) {
        return cells[toGlobalRow(square, slot)][toGlobalCol(square, slot)];
    }

    // הגדרת ערך לפי square+slot
    public void setCell(int square, int slot, int value) {
        cells[toGlobalRow(square, slot)][toGlobalCol(square, slot)] = value;
    }

    // בדיקה אם אפשר לשים
    public boolean canPlace(int square, int slot) {
        return square != holeIndex && getCell(square, slot) == 0;
    }

    // הנחת אבן
    public void place(int square, int slot, int value) {
        setCell(square, slot, value);
    }

    // בדיקה אם אפשר להזיז
    public boolean canSlide(int fromSquare) {

        // 1. בדיקת שכנות לחור
        int row = holeIndex / 3;
        int col = holeIndex % 3;

        int fromRow = fromSquare / 3;
        int fromCol = fromSquare % 3;

        boolean isNeighbor =
                (Math.abs(row - fromRow) + Math.abs(col - fromCol)) == 1;

        if (!isNeighbor) return false;

        // 2. לא לחזור לחור שאני יצרתי בתור הקודם שלי
        if (fromSquare == lastMyHoleIndex) return false;

        return true;
    }

    // הזזה - מעביר את התוכן מ-fromSquare לחור
    public void slide(int fromSquare) {
        // שמירת הערכים מהריבוע שמוזז
        int[] temp = new int[4];
        for (int slot = 0; slot < 4; slot++) {
            temp[slot] = getCell(fromSquare, slot);
        }

        // איפוס הריבוע שמוזז (הופך לחור)
        for (int slot = 0; slot < 4; slot++) {
            setCell(fromSquare, slot, 0);
        }

        // העתקת הערכים לחור הישן
        for (int slot = 0; slot < 4; slot++) {
            setCell(holeIndex, slot, temp[slot]);
        }

        holeIndex = fromSquare;
    }

    // עדכון מהשרת
    public void updateFromServer(int[][] newCells, int newHoleIndex) {
        cells = newCells;
        holeIndex = newHoleIndex;
    }
}
