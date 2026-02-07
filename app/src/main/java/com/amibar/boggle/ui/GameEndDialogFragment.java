package com.amibar.boggle.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.R;


public class GameEndDialogFragment extends DialogFragment {
    public static final String TAG = "GameEndDialogFragment";

    private static final String ARG_SPANNABLE_TEXT = "arg_spannable_text";
    private static final String ARG_SCORE = "arg_score";


    public static GameEndDialogFragment newInstance(CharSequence text, CharSequence score) {
        GameEndDialogFragment fragment = new GameEndDialogFragment();
        Bundle args = new Bundle();
        args.putCharSequence(ARG_SPANNABLE_TEXT, text);
        args.putCharSequence(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        CharSequence text = getArguments().getCharSequence(ARG_SPANNABLE_TEXT);
        CharSequence score = getArguments().getCharSequence(ARG_SCORE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_game_end_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle("Game Over");
        TextView tvScore = view.findViewById(R.id.tvScore);
        tvScore.setText(score);
        TextView tvGameWords = view.findViewById(R.id.tvGameWords);
        tvGameWords.setText(text);
        tvGameWords.setMovementMethod(ScrollingMovementMethod.getInstance());

        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> ((Activity) requireContext()).finish());
        builder.setCancelable(false);


        return builder.create();
    }
}