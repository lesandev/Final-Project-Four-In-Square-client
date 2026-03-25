package co.il.leah.project;

public class Board {

    public int[][] squares;
    public int holeIndex;
    public int lastMyHoleIndex = -1;

    // constructor
    public Board() {
        squares = new int[9][4];
        holeIndex = 4;
    }

    // בדיקה אם אפשר לשים
    public boolean canPlace(int square, int slot) {
        return square != holeIndex && squares[square][slot] == 0;
    }

    // הנחת אבן
    public void place(int square, int slot, int value) {
        squares[square][slot] = value;
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

    // הזזה
    public void slide(int fromSquare) {
        int[] temp = squares[fromSquare];
        squares[fromSquare] = new int[4];
        squares[holeIndex] = temp;

        holeIndex = fromSquare;
    }

    // עדכון מהשרת
    public void updateFromServer(int[][] newSquares, int newHoleIndex) {
        squares = newSquares;
        holeIndex = newHoleIndex;
    }
}
