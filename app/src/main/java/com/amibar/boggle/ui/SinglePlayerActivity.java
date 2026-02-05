package com.amibar.boggle.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.views.BoggleView;

public class SinglePlayerActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extra_score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BoggleView boggleView = findViewById(R.id.boggle_view);
        boggleView.getGame().addOnGameEndListener(() -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_SCORE, boggleView.getGame().getScore());
            setResult(RESULT_OK, data);
            // We don't finish() here because BoggleView shows a dialog first.
            // The activity result is set, and it will be delivered when the activity finishes.
        });
    }
}
