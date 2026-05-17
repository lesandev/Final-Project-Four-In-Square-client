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
    private static final String SERVER_URL = "http://192.168.1.52:8000/move";

    // winner: 0 = game continues, 1 = player wins, 2 = computer wins, -1 = tie
    public interface Callback {
        void onResponse(int[][] board, int holeIndex, int winner);
    }

    public static void sendBoard(Board board, Callback callback) {
        sendBoard(board, false, callback);
    }

    // checkOnly=true → שולח לשרת רק לבדיקת ניצחון, בלי שהמחשב יזוז
    public static void sendBoard(Board board, boolean checkOnly, Callback callback) {

        new Thread(() -> {
            try {
                Log.d(TAG, "Connecting to: " + SERVER_URL);

                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("holeIndex", board.holeIndex);
                json.put("checkOnly", checkOnly);

                JSONArray boardArray = new JSONArray();
                for (int i = 0; i < 6; i++) {
                    JSONArray row = new JSONArray();
                    for (int j = 0; j < 6; j++) {
                        row.put(board.cells[i][j]);
                    }
                    boardArray.put(row);
                }
                json.put("board", boardArray);

                Log.d(TAG, "Sending: " + json.toString());

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
                    int winner = res.optInt("winner", 0);
                    callback.onResponse(newBoard, newHole, winner);
                } else {
                    Log.e(TAG, "Server error: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
            }
        }).start();
    }
}
