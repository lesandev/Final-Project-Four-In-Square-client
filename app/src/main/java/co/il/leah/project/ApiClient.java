package co.il.leah.project;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String MOVE_URL = "http://192.168.1.52:8000/move";
    private static final String CHECK_WIN_URL = "http://192.168.1.52:8000/check_win";

    public interface MoveCallback {
        void onResponse(int[][] board, int holeIndex);
    }

    public interface WinCallback {
        void onResult(int winner);
    }

    // שולח את הלוח לשרת כדי שהמחשב יעשה מהלך
    public static void sendBoard(Board board, MoveCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "POST /move");

                JSONObject json = buildBoardJson(board);

                String response = postJson(MOVE_URL, json);
                if (response == null) return;

                JSONObject res = new JSONObject(response);
                JSONArray arr = res.getJSONArray("board");

                int[][] newBoard = new int[6][6];
                for (int i = 0; i < 6; i++) {
                    JSONArray inner = arr.getJSONArray(i);
                    for (int j = 0; j < 6; j++) {
                        newBoard[i][j] = inner.getInt(j);
                    }
                }

                int newHole = res.getInt("holeIndex");
                callback.onResponse(newBoard, newHole);

            } catch (Exception e) {
                Log.e(TAG, "sendBoard error: " + e.getMessage(), e);
            }
        }).start();
    }

    // שולח את הלוח לשרת לבדיקת ניצחון — מחזיר 0/1/2
    public static void checkWin(Board board, WinCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "POST /check_win");

                JSONObject json = buildBoardJson(board);

                String response = postJson(CHECK_WIN_URL, json);
                if (response == null) return;

                JSONObject res = new JSONObject(response);
                int winner = res.optInt("winner", 0);
                callback.onResult(winner);

            } catch (Exception e) {
                Log.e(TAG, "checkWin error: " + e.getMessage(), e);
            }
        }).start();
    }

    private static JSONObject buildBoardJson(Board board) throws Exception {
        JSONObject json = new JSONObject();
        json.put("holeIndex", board.holeIndex);
        json.put("lastAiHoleIndex", board.lastAiHoleIndex);

        JSONArray boardArray = new JSONArray();
        for (int i = 0; i < 6; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < 6; j++) {
                row.put(board.cells[i][j]);
            }
            boardArray.put(row);
        }
        json.put("board", boardArray);
        return json;
    }

    private static String postJson(String urlStr, JSONObject json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        os.write(json.toString().getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        Log.d(TAG, "Response code: " + responseCode);

        if (responseCode == 200) {
            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            Log.d(TAG, "Response: " + response);
            return response;
        } else {
            Log.e(TAG, "Server error: " + responseCode);
            return null;
        }
    }
}
