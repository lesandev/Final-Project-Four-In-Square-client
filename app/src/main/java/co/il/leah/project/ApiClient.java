package co.il.leah.project;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ApiClient {

    public interface Callback {
        void onResponse(int[][] board, int holeIndex);
    }

    public static void sendBoard(Board board, Callback callback) {

        new Thread(() -> {
            try {

                // 🌐 כתובת השרת
                // לאמולטור: "http://10.0.2.2:5000/move"
                // למכשיר פיזי: שני את ה-IP לכתובת המחשב שלך
                URL url = new URL("http://192.168.1.52:5000/move");
                //http://10.0.2.2:5000/move FOR EMULATOR

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 📤 יצירת JSON
                JSONObject json = new JSONObject();
                json.put("holeIndex", board.holeIndex);

                JSONArray boardArray = new JSONArray();
                for (int i = 0; i < 6; i++) {
                    JSONArray row = new JSONArray();
                    for (int j = 0; j < 6; j++) {
                        row.put(board.cells[i][j]);
                    }
                    boardArray.put(row);
                }

                json.put("board", boardArray);

                // 📤 שליחה
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                // 📥 קריאה מהשרת
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();

                JSONObject res = new JSONObject(response);

                // 📥 המרה חזרה למערך
                JSONArray arr = res.getJSONArray("board");

                int[][] newBoard = new int[6][6];

                for (int i = 0; i < 6; i++) {
                    JSONArray inner = arr.getJSONArray(i);
                    for (int j = 0; j < 6; j++) {
                        newBoard[i][j] = inner.getInt(j);
                    }
                }

                int newHole = res.getInt("holeIndex");

                // 🔁 החזרה ל־MainActivity
                callback.onResponse(newBoard, newHole);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
