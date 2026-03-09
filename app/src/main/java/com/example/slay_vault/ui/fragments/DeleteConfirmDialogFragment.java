package com.example.slay_vault.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.slay_vault.R;
import com.example.slay_vault.ui.DivaStrings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// Diálogo de confirmación de borrado con botones "Slay" / "Sashay Away" y textos Diva.
public class DeleteConfirmDialogFragment extends DialogFragment {

    public static final String RESULT_CONFIRMED = "result_confirmed";
    public static final String RESULT_ITEM_ID = "result_item_id";

    private static final String ARG_REQUEST_KEY = "arg_request_key";
    private static final String ARG_ITEM_ID = "arg_item_id";
    private static final String ARG_ITEM_NAME = "arg_item_name";

    public static DeleteConfirmDialogFragment newInstance(
            String requestKey,
            @Nullable String itemId,
            @Nullable String itemName
    ) {
        DeleteConfirmDialogFragment fragment = new DeleteConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putString(ARG_ITEM_ID, itemId);
        args.putString(ARG_ITEM_NAME, itemName);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_confirm, null, false);

        ImageView dragImage = dialogView.findViewById(R.id.dialog_drag_image);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        MaterialButton sashayButton = dialogView.findViewById(R.id.button_sashay_away);
        MaterialButton slayButton = dialogView.findViewById(R.id.button_slay);

        String itemName = getItemName();

        dragImage.setImageResource(R.mipmap.ic_launcher_round);
        titleView.setText(DivaStrings.dialogDeleteTitle(requireContext()));
        messageView.setText(DivaStrings.dialogDeleteMessage(requireContext(), itemName));
        sashayButton.setText(DivaStrings.dialogDeleteCancel(requireContext()));
        slayButton.setText(DivaStrings.dialogDeleteConfirm(requireContext()));

        sashayButton.setOnClickListener(v -> {
            dispatchResult(false);
            dismiss();
        });

        slayButton.setOnClickListener(v -> {
            dispatchResult(true);
            dismiss();
        });

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        dialog.setCancelable(true);
        return dialog;
    }

    private void dispatchResult(boolean confirmed) {
        Bundle result = new Bundle();
        result.putBoolean(RESULT_CONFIRMED, confirmed);
        result.putString(RESULT_ITEM_ID, getItemId());
        getParentFragmentManager().setFragmentResult(getRequestKey(), result);
    }

    private String getRequestKey() {
        Bundle args = getArguments();
        if (args == null) {
            return "delete_confirm_default";
        }
        return args.getString(ARG_REQUEST_KEY, "delete_confirm_default");
    }

    @Nullable
    private String getItemId() {
        Bundle args = getArguments();
        return args != null ? args.getString(ARG_ITEM_ID) : null;
    }

    @NonNull
    private String getItemName() {
        Bundle args = getArguments();
        String name = args != null ? args.getString(ARG_ITEM_NAME) : null;
        if (name == null || name.trim().isEmpty()) {
            return getString(R.string.dialog_delete_fallback_name);
        }
        return name;
    }
}
