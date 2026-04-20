package com.example.slay_vault.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slay_vault.R;
import com.example.slay_vault.data.models.Queen;
import com.example.slay_vault.ui.utils.QueenPhotoLoader;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adapter del RecyclerView de Queens. Gestiona la lista, DiffUtil, ordenación y clics.
public class QueensAdapter extends RecyclerView.Adapter<QueensAdapter.QueenViewHolder> {

    private List<Queen> queens;
    private OnQueenClickListener clickListener;

    // Callbacks de click simple y largo.
    public interface OnQueenClickListener {
        void onQueenClick(Queen queen, int position);
        void onQueenLongClick(Queen queen, int position);
    }

    public QueensAdapter() {
        this.queens = new ArrayList<>();
    }

    public void setOnQueenClickListener(OnQueenClickListener listener) {
        this.clickListener = listener;
    }

    // Reemplaza la lista completa usando DiffUtil para animar solo los cambios
    public void setQueens(List<Queen> newQueens) {
        List<Queen> safeNew = newQueens != null ? newQueens : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new QueenDiffCallback(queens, safeNew));
        queens = safeNew;
        result.dispatchUpdatesTo(this);
    }

    public Queen getQueen(int position) {
        return position >= 0 && position < queens.size() ? queens.get(position) : null;
    }

    // Criterios de ordenación disponibles
    public enum SortOrder { NAME, ENVY_DESC, SHADES_DESC, DATE_DESC }

    // Ordena la lista in-place y notifica al RecyclerView
    public void sortBy(SortOrder order) {
        Comparator<Queen> comparator;
        switch (order) {
            case NAME:
                comparator = (a, b) -> a.getName().compareToIgnoreCase(b.getName());
                break;
            case ENVY_DESC:
                comparator = (a, b) -> Float.compare(b.getEnvyLevel(), a.getEnvyLevel());
                break;
            case SHADES_DESC:
                comparator = (a, b) -> Integer.compare(b.getShadesCount(), a.getShadesCount());
                break;
            case DATE_DESC:
            default:
                comparator = (a, b) -> {
                    Date da = a.getCreatedAt();
                    Date db = b.getCreatedAt();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da);
                };
                break;
        }
        queens.sort(comparator);
        notifyDataSetChanged();
    }

    private static class QueenDiffCallback extends DiffUtil.Callback {
        private final List<Queen> oldList;
        private final List<Queen> newList;

        QueenDiffCallback(List<Queen> oldList, List<Queen> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            String oldId = oldList.get(oldPos).getId();
            String newId = newList.get(newPos).getId();
            return oldId != null && oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Queen o = oldList.get(oldPos);
            Queen n = newList.get(newPos);
            return o.getName().equals(n.getName())
                    && o.getEnvyLevel() == n.getEnvyLevel()
                    && o.getShadesCount() == n.getShadesCount()
                    && java.util.Objects.equals(o.getDescription(), n.getDescription())
                    && java.util.Objects.equals(o.getPhotoUri(), n.getPhotoUri());
        }
    }

    @NonNull
    @Override
    public QueenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queen_card, parent, false);
        return new QueenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueenViewHolder holder, int position) {
        holder.bind(queens.get(position), position, clickListener);
    }

    @Override
    public int getItemCount() {
        return queens.size();
    }

    // ViewHolder que enlaza los datos de una Queen con las vistas
    public static class QueenViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final ShapeableImageView queenPhoto;
        private final TextView queenName;
        private final TextView queenDescription;
        private final RatingBar queenEnvyRating;
        private final Chip shadesCountBadge;
        private final TextView lastShadeDate;

        public QueenViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.queen_card);
            queenPhoto = itemView.findViewById(R.id.queen_photo);
            queenName = itemView.findViewById(R.id.queen_name);
            queenDescription = itemView.findViewById(R.id.queen_description);
            queenEnvyRating = itemView.findViewById(R.id.queen_envy_rating);
            shadesCountBadge = itemView.findViewById(R.id.shades_count_badge);
            lastShadeDate = itemView.findViewById(R.id.last_shade_date);
        }

        public void bind(Queen queen, int position, OnQueenClickListener clickListener) {
            queenName.setText(queen.getName());

            if (queen.getDescription() != null && !queen.getDescription().isEmpty()) {
                queenDescription.setText(queen.getDescription());
                queenDescription.setVisibility(View.VISIBLE);
            } else {
                queenDescription.setVisibility(View.GONE);
            }

            queenEnvyRating.setRating(queen.getEnvyLevel());

            QueenPhotoLoader.load(queenPhoto, queen.getPhotoUri(), R.mipmap.ic_launcher);

            int shadesCount = queen.getShadesCount();
            if (shadesCount == 0) {
                shadesCountBadge.setText(itemView.getContext().getString(R.string.no_shades_yet));
            } else if (shadesCount == 1) {
                shadesCountBadge.setText(itemView.getContext().getString(R.string.shade_count_single));
            } else {
                shadesCountBadge.setText(itemView.getContext().getString(R.string.shades_count, shadesCount));
            }

            if (queen.getLastShadeDate() != null) {
                lastShadeDate.setText(formatRelativeDate(queen.getLastShadeDate()));
                lastShadeDate.setVisibility(View.VISIBLE);
            } else {
                lastShadeDate.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onQueenClick(queen, position);
            });
            cardView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onQueenLongClick(queen, position);
                    return true;
                }
                return false;
            });
            cardView.setCheckable(false);
        }

        // Convierte fecha dd/MM/yyyy a texto relativo.
        private String formatRelativeDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) return "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date parsed = sdf.parse(dateString);
                if (parsed == null) return dateString;
                long diffDays = (new Date().getTime() - parsed.getTime()) / (1000 * 60 * 60 * 24);
                android.content.Context ctx = itemView.getContext();
                if (diffDays == 0)  return ctx.getString(R.string.today);
                if (diffDays == 1)  return ctx.getString(R.string.yesterday);
                if (diffDays < 0)   return sdf.format(parsed);
                if (diffDays < 30)  return ctx.getString(R.string.days_ago, (int) diffDays);
                long months = diffDays / 30;
                if (months == 1)    return ctx.getString(R.string.month_ago);
                if (diffDays < 365) return ctx.getString(R.string.months_ago, (int) months);
                return sdf.format(parsed);
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}

