package com.amibar.boggle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SinglePlayerActivity extends AppCompatActivity {
    private TextView[] cells;
    private TextView msg;
    private BoggleGame game;
    private Button submit;
    private android.widget.TextView score;
    private android.widget.TextView word;


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
        init();
    }

    private void init(){
        game = new BoggleGame();
        cells = new TextView[16];
        for (int i = 0; i < 16; i++) {
            TextView cell = findViewById(getResources().getIdentifier(
                    "cell" + i,
                    "id",
                    getPackageName()
            ));
            final int finalId = i;
            cell.setOnClickListener((v) -> {
                if (game.selectDie(finalId)){
                    updateWord();
                }
            });
            cell.setText(""+game.getDie(i));
            cells[i] = cell;
        }
        submit = findViewById(R.id.bSubmit);
        submit.setOnClickListener(this::onClick);

        score = findViewById(R.id.tvScore);
        word = findViewById(R.id.tvWord);
        msg = findViewById(R.id.tvErrors);
        updateScore();
    }

    private void updateScore(){
        this.score.setText(getString(R.string.score, game.getScore()));
    }

    private void updateWord(){
        this.word.setText(getString(R.string.word, game.getWord()));
    }

    private void onClick(View v) {
        String lastWord = game.getWord();
        if (game.submitWord()) {
            msg.setText("");
            updateScore();
        } else {
            msg.setText(getString(R.string.invalid_word, lastWord));
        }
        updateWord();
    }
}