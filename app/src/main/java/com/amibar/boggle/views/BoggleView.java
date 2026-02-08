package com.amibar.boggle.views;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amibar.boggle.R;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.ui.Timer;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class BoggleView extends LinearLayout {

    private BoggleGame game;
    private TextView[] cells;
    private TextView lastSelected;

    private TextView word;
    private TextView msg;
    private TextView score;

    public BoggleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @SuppressWarnings("unused")
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView(){
        inflate(getContext(), R.layout.boggleview, this);

        game = new BoggleGame();
        
        cells = new TextView[16];
        GridLayout gl = findViewById(R.id.glGameLayout);
        for (int i = 0; i < gl.getChildCount(); i++) {
            cells[i] = (TextView) gl.getChildAt(i);
            cells[i].setOnClickListener(cellOnClickListener(i));
            char letter = game.getDie(i);
            cells[i].setText(letter == 'Q' ? "Qu" : String.valueOf(letter));
        }
        Button submit = findViewById(R.id.bSubmit);
        submit.setOnClickListener(this::onClickSubmit);

        score = findViewById(R.id.tvScore);
        updateScore();

        word = findViewById(R.id.tvWord);
        updateWord();

        msg = findViewById(R.id.tvErrors);

        TextView timerText = findViewById(R.id.tvTime);
        LinearProgressIndicator timerIndicator = findViewById(R.id.progressBar);

        new Timer(timerText, timerIndicator, BoggleGame.GAME_TIME_MILLIS,
                ()-> game.endGame()).start();
    }

    public BoggleGame getGame() {
        return game;
    }

    private void onClickSubmit(View v) {
        if (game.isEnded()) return;
        for (TextView cell : cells){
            cell.setBackgroundColor(getColor(R.color.unselected));
        }
        String lastWord = game.getWord();
        BoggleGame.WordCheckResult result = game.submitWord();
        if (result == VALID)
            updateScore();
        msg.setText(getContext().getString(result.getMessageId(), lastWord));
        updateWord();
        lastSelected = null;
    }

    private void updateScore() {
        this.score.setText(getContext().getString(R.string.score, game.getScore()));
    }

    private OnClickListener cellOnClickListener(int cellId) {
        return (view) -> {
            if (game.isEnded())
                return;
            if (game.selectDie(cellId)){
                if (lastSelected != null) {
                    lastSelected.setBackgroundColor(getColor(R.color.selected));
                }
                updateWord();
                view.setBackgroundColor(getColor(R.color.lastSelected));
                lastSelected = (TextView) view;
            }
        };
    }

    private int getColor(int colorRes) {
        return getContext().getColor(colorRes);
    }

    private void updateWord(){
        this.word.setText(getContext().getString(R.string.word, game.getWord()));
    }
}
