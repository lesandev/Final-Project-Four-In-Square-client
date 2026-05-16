package co.il.leah.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);
        Button playButton = findViewById(R.id.playButton);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        titleText.startAnimation(fadeIn);

        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(800);
        slideUp.setStartOffset(500);
        subtitleText.startAnimation(slideUp);

        Animation bounce = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        bounce.setDuration(600);
        bounce.setStartOffset(1000);
        playButton.startAnimation(bounce);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
                startActivity(intent);
            }
        });
    }
}
