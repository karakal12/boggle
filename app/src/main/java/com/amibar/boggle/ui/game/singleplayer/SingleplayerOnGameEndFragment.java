package com.amibar.boggle.ui.game.singleplayer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.databinding.FragmentSingleplayerOnGameEndBinding;
import com.amibar.boggle.ui.shared.WordsAdapter;

import java.util.ArrayList;
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
    /** Key for the score value in the arguments bundle. */
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

    /** View binding for the fragment layout. */
    private FragmentSingleplayerOnGameEndBinding binding;

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
     * @param score      The final score to display.
     * @return A configured SingleplayerOnGameEndFragment.
     */
    public static SingleplayerOnGameEndFragment newInstance(Map<String, String> solutions, List<String> foundWords, int score) {
        SingleplayerOnGameEndFragment fragment = new SingleplayerOnGameEndFragment();
        Bundle args = new Bundle();
        // Storing data in the arguments bundle to survive configuration changes.
        args.putSerializable(ARG_SOLUTIONS, new HashMap<>(solutions));
        args.putStringArrayList(ARG_FOUND_WORDS, new ArrayList<>(foundWords));
        args.putInt(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Ensure arguments are present, as they are required for the dialog to function.
        if (getArguments() == null) {
             return super.onCreateDialog(savedInstanceState);
        }

        @SuppressWarnings("unchecked")
        Map<String, String> solutions = (Map<String, String>) getArguments().getSerializable(ARG_SOLUTIONS);
        List<String> foundWords = getArguments().getStringArrayList(ARG_FOUND_WORDS);
        int score = getArguments().getInt(ARG_SCORE);

        // Inflate the custom layout for the dialog content using ViewBinding.
        binding = FragmentSingleplayerOnGameEndBinding.inflate(getLayoutInflater());

        // Create an AlertDialog builder to construct the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Game Over");

        // Bind UI components and set data.
        binding.setScore(score);

        if (solutions != null && foundWords != null) {
            // Set up the RecyclerView with an adapter that highlights found words and handles clicks.
            binding.wordsList.setAdapter(new WordsAdapter(solutions, foundWords, (word, path) -> {
                if (listener != null) {
                    listener.onWordClick(word, path);
                }
                // Dismiss the dialog once a word is selected to show its path on the board behind it.
                dismiss();
            }));
        }

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

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog width to match parent for better usability
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
