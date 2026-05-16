package co.il.leah.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LevelSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        setupLevelButtons();
    }

    private void setupLevelButtons() {
        int[] buttonIds = {
            R.id.level1, R.id.level2, R.id.level3,
            R.id.level4, R.id.level5, R.id.level6,
            R.id.level7, R.id.level8, R.id.level9
        };

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
