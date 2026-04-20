package com.example.slay_vault.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.slay_vault.R;
import com.example.slay_vault.data.auth.SessionManager;
import com.example.slay_vault.ui.DivaStrings;
import com.example.slay_vault.workers.SendShoutWorker;

import java.util.concurrent.TimeUnit;

public class GritoDiaFragment extends Fragment {

    private EditText etMensaje;
    private Button btnEnviar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_shout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMensaje = view.findViewById(R.id.et_global_shout_message);
        btnEnviar = view.findViewById(R.id.btn_send_global_shout);
        TextView title = view.findViewById(R.id.tv_global_shout_title);
        TextView subtitle = view.findViewById(R.id.tv_global_shout_subtitle);

        title.setText(DivaStrings.globalShoutTitle(requireContext()));
        subtitle.setText(DivaStrings.globalShoutSubtitle(requireContext()));
        etMensaje.setHint(DivaStrings.globalShoutHint(requireContext()));
        btnEnviar.setText(DivaStrings.globalShoutSend(requireContext()));

        btnEnviar.setOnClickListener(v -> enviarGrito());
    }

    private void enviarGrito() {
        String mensaje = etMensaje.getText() != null ? etMensaje.getText().toString().trim() : "";

        if (mensaje.isEmpty()) {
            Toast.makeText(requireContext(), DivaStrings.globalShoutMessageRequired(requireContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mensaje.length() > 280) {
            Toast.makeText(requireContext(), DivaStrings.globalShoutSendFailed(requireContext()), Toast.LENGTH_SHORT).show();
            return;
        }

        String username = SessionManager.getUsername(requireContext());
        if (username == null || username.trim().isEmpty()) {
            username = getString(R.string.auth_username);
        }

        Data inputData = new Data.Builder()
                .putString(SendShoutWorker.INPUT_MESSAGE, mensaje)
                .putString(SendShoutWorker.INPUT_USERNAME, username)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendShoutWorker.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                )
                .build();

        WorkManager.getInstance(requireContext()).enqueue(workRequest);

        Toast.makeText(requireContext(), DivaStrings.globalShoutSent(requireContext()), Toast.LENGTH_SHORT).show();
        etMensaje.setText("");
    }
}

