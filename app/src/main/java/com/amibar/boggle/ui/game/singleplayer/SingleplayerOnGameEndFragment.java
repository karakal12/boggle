package com.amibar.boggle.ui.game.singleplayer;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.databinding.FragmentSingleplayerOnGameEndBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link DialogFragment} that appears when a single-player game ends.
 * It displays the final score and a list of all possible words that could have been found on the board.
 * Words found by the player are highlighted in green.
 * Clicking on a word triggers a callback to show its path on the board.
 */
public class SingleplayerOnGameEndFragment extends DialogFragment {

    /** Tag used for identifying this fragment in the FragmentManager. */
    public static final String TAG = "SinglePlayerGameEndDialogFragment";

    /** Key for the solutions map in the arguments bundle. */
    private static final String ARG_SOLUTIONS = "arg_solutions";
    /** Key for the found words list in the arguments bundle. */
    private static final String ARG_FOUND_WORDS = "arg_found_words";
    /** Key for the score string in the arguments bundle. */
    private static final String ARG_SCORE = "arg_score";

    /**
     * Interface definition for a callback to be invoked when a word is clicked.
     */
    public interface OnWordClickListener {
        /**
         * Called when a word in the dialog is clicked.
         *
         * @param word The word that was clicked.
         * @param path The path of the word on the board (encoded as a string).
         */
        void onWordClick(String word, String path);
    }

    /** Listener for word click events. */
    private OnWordClickListener listener;

    /**
     * Sets the listener for word click events.
     *
     * @param listener The listener to set.
     */
    public void setOnWordClickListener(OnWordClickListener listener) {
        this.listener = listener;
    }

    /**
     * Creates a new instance of the dialog with the provided data.
     *
     * @param solutions  Map of all possible words to their paths.
     * @param foundWords List of words found by the player.
     * @param score      The final score string to display.
     * @return A configured SingleplayerOnGameEndFragment.
     */
    public static SingleplayerOnGameEndFragment newInstance(Map<String, String> solutions, List<String> foundWords, String score) {
        SingleplayerOnGameEndFragment fragment = new SingleplayerOnGameEndFragment();
        Bundle args = new Bundle();
        // Storing data in the arguments bundle to survive configuration changes.
        args.putSerializable(ARG_SOLUTIONS, new HashMap<>(solutions));
        args.putStringArrayList(ARG_FOUND_WORDS, new ArrayList<>(foundWords));
        args.putString(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the dialog displayed by this fragment.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a new instance.
     * @return A new {@link Dialog} instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Ensure arguments are present, as they are required for the dialog to function.
        assert getArguments() != null;
        @SuppressWarnings("unchecked")
        Map<String, String> solutions = (Map<String, String>) getArguments().getSerializable(ARG_SOLUTIONS);
        List<String> foundWords = getArguments().getStringArrayList(ARG_FOUND_WORDS);
        String score = getArguments().getString(ARG_SCORE);

        // Inflate the custom layout for the dialog content using ViewBinding.
        FragmentSingleplayerOnGameEndBinding binding =
                FragmentSingleplayerOnGameEndBinding.inflate(getLayoutInflater());

        // Create an AlertDialog builder to construct the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Game Over");

        // Bind UI components and set data.
        TextView tvScore = binding.tvScore;
        tvScore.setText(score);

        TextView tvGameWords = binding.tvGameWords;
        assert solutions != null;
        // Build and set the spannable string containing all possible words.
        tvGameWords.setText(buildSpannableWords(solutions, foundWords));
        // Enable clicking and scrolling within the TextView.
        tvGameWords.setMovementMethod(LinkMovementMethod.getInstance());

        // Set the custom view for the dialog.
        builder.setView(binding.getRoot());

        // Finish the activity when "OK" is pressed, typically returning the user to the main menu.
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // Create the dialog and prevent it from being dismissed by clicking outside.
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * Builds a {@link CharSequence} containing all possible words, formatted with colors and click listeners.
     *
     * @param solutions  Map of all possible words to their paths.
     * @param foundWords List of words found by the player.
     * @return A {@link SpannableStringBuilder} containing the formatted text.
     */
    private CharSequence buildSpannableWords(Map<String, String> solutions, List<String> foundWords) {
        // Sort the possible words alphabetically.
        List<String> sortedKeys = new ArrayList<>(solutions.keySet());
        Collections.sort(sortedKeys);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        // Add a header showing the total count of possible words.
        ssb.append("Possible words (").append(String.valueOf(sortedKeys.size())).append("):\n");
        int hintStart = ssb.length();
        ssb.append("hint: click on the words to see solution");
        ssb.setSpan(new ForegroundColorSpan(Color.GRAY), hintStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new RelativeSizeSpan(0.5f), hintStart, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("\n\n");

        for (int i = 0; i < sortedKeys.size(); i++) {
            String word = sortedKeys.get(i);
            String path = solutions.get(word);
            int start = ssb.length();
            ssb.append(word);

            // Highlight words found by the player in green.
            if (foundWords.contains(word)) {
                ssb.setSpan(new ForegroundColorSpan(Color.GREEN), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // Add a click listener to each word to show its path on the board.
            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (listener != null) {
                        listener.onWordClick(word, path);
                    }
                    // Dismiss the dialog after a word is clicked to show the path on the game screen.
                    dismiss();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                }
            }, start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Add a newline after each word except the last one.
            if (i < sortedKeys.size() - 1) {
                ssb.append("\n");
            }
        }
        return ssb;
    }
}
