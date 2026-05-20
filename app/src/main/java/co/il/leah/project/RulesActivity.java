package co.il.leah.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(RulesActivity.this, LevelSelectActivity.class);
            startActivity(intent);
        });
    }
}
