package com.amibar.boggle.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.views.BoggleView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        BoggleGame game = boggleView.getGame();
        game.addOnGameEndListener(() -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_SCORE, game.getScore());
            setResult(RESULT_OK, data);

            showGameEndDialog(game);
        });
    }

    private void showGameEndDialog(BoggleGame game) {
        List<String> sortedSolutions = new ArrayList<>(game.getSolutions());
        Collections.sort(sortedSolutions);
        List<String> foundByPlayer = game.getFoundWords();

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Possible words (").append(String.valueOf(sortedSolutions.size())).append("):\n\n");

        for (int i = 0; i < sortedSolutions.size(); i++) {
            String s = sortedSolutions.get(i);
            int start = ssb.length();
            ssb.append(s);
            if (foundByPlayer.contains(s)) {
                ssb.setSpan(new ForegroundColorSpan(Color.GREEN), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (i < sortedSolutions.size() - 1) {
                ssb.append('\n');
            }
        }

        SinglePlayerGameEndDialogFragment.newInstance(ssb, getString(R.string.score, game.getScore())).show(
                getSupportFragmentManager(),
                SinglePlayerGameEndDialogFragment.TAG);
    }
}
