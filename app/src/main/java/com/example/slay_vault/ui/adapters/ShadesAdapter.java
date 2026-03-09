package com.example.slay_vault.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slay_vault.R;
import com.example.slay_vault.data.models.Shade;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adapter del RecyclerView de Shades. Gestiona la lista, DiffUtil y clics.
public class ShadesAdapter extends RecyclerView.Adapter<ShadesAdapter.ShadeViewHolder> {

    private List<Shade> shades;
    private OnShadeClickListener clickListener;

    // Callback para clic simple y long click sobre una card
    public interface OnShadeClickListener {
        void onShadeClick(Shade shade, int position);
        void onShadeLongClick(Shade shade, int position);
    }

    public ShadesAdapter() {
        this.shades = new ArrayList<>();
    }

    public void setOnShadeClickListener(OnShadeClickListener listener) {
        this.clickListener = listener;
    }

    // Reemplaza la lista completa usando DiffUtil para animar solo los cambios
    public void setShades(List<Shade> newShades) {
        List<Shade> safeNew = newShades != null ? newShades : new ArrayList<>();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new ShadeDiffCallback(shades, safeNew));
        shades = safeNew;
        result.dispatchUpdatesTo(this);
    }

    private static class ShadeDiffCallback extends DiffUtil.Callback {
        private final List<Shade> oldList;
        private final List<Shade> newList;

        ShadeDiffCallback(List<Shade> oldList, List<Shade> newList) {
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
            Shade o = oldList.get(oldPos);
            Shade n = newList.get(newPos);
            return o.getTitle().equals(n.getTitle())
                    && o.getIntensity() == n.getIntensity();
        }
    }

    public Shade getShade(int position) {
        return position >= 0 && position < shades.size() ? shades.get(position) : null;
    }

    @NonNull
    @Override
    public ShadeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shade_card, parent, false);
        return new ShadeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShadeViewHolder holder, int position) {
        holder.bind(shades.get(position), position, clickListener);
    }

    @Override
    public int getItemCount() {
        return shades.size();
    }

    // ViewHolder que enlaza los datos de un Shade con las vistas
    public static class ShadeViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final ImageView shadeCategoryIcon;
        private final TextView shadeCategory;
        private final TextView shadeDate;
        private final TextView shadeTitle;
        private final TextView shadeDescription;
        private final RatingBar shadeIntensityRating;
        private final ChipGroup shadeTags;

        public ShadeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.shade_card);
            shadeCategoryIcon = itemView.findViewById(R.id.shade_category_icon);
            shadeCategory = itemView.findViewById(R.id.shade_category);
            shadeDate = itemView.findViewById(R.id.shade_date);
            shadeTitle = itemView.findViewById(R.id.shade_title);
            shadeDescription = itemView.findViewById(R.id.shade_description);
            shadeIntensityRating = itemView.findViewById(R.id.shade_intensity_rating);
            shadeTags = itemView.findViewById(R.id.shade_tags);
        }

        public void bind(Shade shade, int position, OnShadeClickListener clickListener) {
            shadeTitle.setText(shade.getTitle());

            if (shade.getDescription() != null && !shade.getDescription().isEmpty()) {
                shadeDescription.setText(shade.getDescription());
                shadeDescription.setVisibility(View.VISIBLE);
            } else {
                shadeDescription.setVisibility(View.GONE);
            }

            shadeCategory.setText(shade.getCategory());
            shadeCategoryIcon.setImageResource(getCategoryIcon(shade.getCategory()));
            shadeDate.setText(formatDate(shade.getDate()));
            shadeIntensityRating.setRating(shade.getIntensity());

            shadeTags.removeAllViews();
            if (shade.getTags() != null && !shade.getTags().isEmpty()) {
                for (String tag : shade.getTags()) {
                    shadeTags.addView(createTagChip(tag));
                }
                shadeTags.setVisibility(View.VISIBLE);
            } else {
                shadeTags.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onShadeClick(shade, position);
            });
            cardView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onShadeLongClick(shade, position);
                    return true;
                }
                return false;
            });
        }

        // Devuelve un icono según la categoría
        private int getCategoryIcon(String category) {
            switch (category.toLowerCase()) {
                case "outfit":
                case "look":
                    return android.R.drawable.ic_menu_gallery;
                case "comentario":
                case "comentario shady":
                    return android.R.drawable.ic_menu_info_details;
                case "actitud":
                    return android.R.drawable.ic_dialog_alert;
                case "evento":
                    return android.R.drawable.ic_menu_my_calendar;
                case "redes sociales":
                    return android.R.drawable.ic_menu_share;
                default:
                    return android.R.drawable.ic_dialog_info;
            }
        }

        // Crea un Chip para el tag
        private Chip createTagChip(String tagText) {
            Chip chip = new Chip(itemView.getContext());
            chip.setText(tagText);
            chip.setChipBackgroundColorResource(R.color.diva_fuchsia_container);
            chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall);
            chip.setClickable(false);
            chip.setCheckable(false);
            return chip;
        }

        // Formatea una fecha a "dd/MM/yyyy"
        private String formatDate(Date date) {
            if (date == null) return "";
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        }
    }
}
