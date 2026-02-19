package com.amibar.boggle.ui.singleplayer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.R;

/**
 * A DialogFragment that appears when a single-player game ends.
 * Displays the final score and a list of all possible words.
 */
public class SinglePlayerGameEndDialogFragment extends DialogFragment {
    /** Tag used for identifying this fragment in the FragmentManager. */
    public static final String TAG = "SinglePlayerGameEndDialogFragment";

    private static final String ARG_SPANNABLE_TEXT = "arg_spannable_text";
    private static final String ARG_SCORE = "arg_score";

    /**
     * Creates a new instance of the dialog with the provided data.
     *
     * @param text  The formatted text (usually a list of words) to display.
     * @param score The final score string to display.
     * @return A configured SinglePlayerGameEndDialogFragment.
     */
    public static SinglePlayerGameEndDialogFragment newInstance(CharSequence text, CharSequence score) {
        SinglePlayerGameEndDialogFragment fragment = new SinglePlayerGameEndDialogFragment();
        Bundle args = new Bundle();
        args.putCharSequence(ARG_SPANNABLE_TEXT, text);
        args.putCharSequence(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Ensure arguments are present
        assert getArguments() != null;
        CharSequence text = getArguments().getCharSequence(ARG_SPANNABLE_TEXT);
        CharSequence score = getArguments().getCharSequence(ARG_SCORE);

        // Inflate the custom layout for the dialog content
        View view = getLayoutInflater().inflate(R.layout.fragment_game_end_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle("Game Over");

        // Bind UI components and set data
        TextView tvScore = view.findViewById(R.id.tvScore);
        tvScore.setText(score);

        TextView tvGameWords = view.findViewById(R.id.tvGameWords);
        tvGameWords.setText(text);
        // Enable scrolling for the word list since it can be very long
        tvGameWords.setMovementMethod(ScrollingMovementMethod.getInstance());

        builder.setView(view);

        // Finish the activity when OK is pressed (returning to main menu)
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (getContext() instanceof Activity activity) {
                activity.finish();
            }
        });

        // Prevent dismissal via back button or clicking outside
        builder.setCancelable(false);

        return builder.create();
    }
}
