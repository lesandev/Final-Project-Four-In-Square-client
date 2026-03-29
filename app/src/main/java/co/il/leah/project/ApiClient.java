package co.il.leah.project;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ApiClient {

    public interface Callback {
        void onResponse(int[][] squares, int holeIndex);
    }

    public static void sendBoard(Board board, Callback callback) {

        new Thread(() -> {
            try {

                // 🌐 כתובת השרת
                URL url = new URL("http://10.0.2.2:5000/move");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 📤 יצירת JSON
                JSONObject json = new JSONObject();
                json.put("holeIndex", board.holeIndex);

                JSONArray squaresArray = new JSONArray();
                for (int i = 0; i < 9; i++) {
                    JSONArray row = new JSONArray();
                    for (int j = 0; j < 4; j++) {
                        row.put(board.squares[i][j]);
                    }
                    squaresArray.put(row);
                }

                json.put("squares", squaresArray);

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
                JSONArray arr = res.getJSONArray("squares");

                int[][] newSquares = new int[9][4];

                for (int i = 0; i < 9; i++) {
                    JSONArray inner = arr.getJSONArray(i);
                    for (int j = 0; j < 4; j++) {
                        newSquares[i][j] = inner.getInt(j);
                    }
                }

                int newHole = res.getInt("holeIndex");

                // 🔁 החזרה ל־MainActivity
                callback.onResponse(newSquares, newHole);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
