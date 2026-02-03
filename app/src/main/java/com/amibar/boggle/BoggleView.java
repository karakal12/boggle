package com.amibar.boggle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.WindowDecorActionBar;

public class BoggleView extends LinearLayout {

    private BoggleGame game;
    private TextView[] cells;
    private TextView lastSelected;

    private TextView word;
    private Button submit;
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

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView(){
        inflate(getContext(), R.layout.boggleview, this);

        game = new BoggleGame();
        cells = new TextView[16];
        GridLayout gl = findViewById(R.id.glGameLayout);
        for (int i = 0; i < gl.getChildCount(); i++) {
            cells[i] = (TextView) gl.getChildAt(i);
            cells[i].setOnClickListener(cellOnClickListener(i));
            cells[i].setText(""+game.getDie(i));
        }
        submit = findViewById(R.id.bSubmit);
        submit.setOnClickListener(this::onClickSubmit);

        score = findViewById(R.id.tvScore);
        word = findViewById(R.id.tvWord);
        msg = findViewById(R.id.tvErrors);
        updateScore();
    }

    private void onClickSubmit(View v) {
        for (TextView cell : cells){
            cell.setBackgroundColor(getColor(R.color.unselected));
        }
        String lastWord = game.getWord();
        if (game.submitWord()) {
            msg.setText("");
            updateScore();
        } else {
            msg.setText(getContext().getString(R.string.invalid_word, lastWord));
        }
        updateWord();
        lastSelected = null;
    }

    private void updateScore() {
        this.score.setText(getContext().getString(R.string.score, game.getScore()));
    }

    private OnClickListener cellOnClickListener(int index) {
        return (view) -> {
            if (game.selectDie(index)){
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
