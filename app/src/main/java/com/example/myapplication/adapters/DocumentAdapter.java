package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.DocumentItem;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<DocumentItem> docList;
    private OnDocumentClickListener listener;

    // 1. Updated Interface
    public interface OnDocumentClickListener {
        void onUploadClicked(DocumentItem item);
        void onViewClicked(DocumentItem item); // NEW: Triggered to view the file
    }

    public DocumentAdapter(List<DocumentItem> docList, OnDocumentClickListener listener) {
        this.docList = docList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentItem item = docList.get(position);

        holder.tvDocTitle.setText(item.getTitle());
        holder.tvFileType.setText(item.getFileType());

        if (item.getStatus() == 2) {
            holder.tvDocStatus.setText("VERIFIED");
            holder.tvDocStatus.setTextColor(Color.parseColor("#10B981"));
            holder.tvDocStatus.setBackgroundColor(Color.parseColor("#ECFDF5"));
            holder.tvDocAction.setText("View");
            holder.tvDocAction.setTextColor(Color.parseColor("#6B7280"));
        } else if (item.getStatus() == 1) {
            holder.tvDocStatus.setText("IN REVIEW");
            holder.tvDocStatus.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvDocStatus.setBackgroundColor(Color.parseColor("#FFFBEB"));
            holder.tvDocAction.setText("View");
            holder.tvDocAction.setTextColor(Color.parseColor("#6B7280"));
        } else {
            holder.tvDocStatus.setText("REQUIRED");
            holder.tvDocStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.tvDocStatus.setBackgroundColor(Color.parseColor("#FEF2F2"));
            holder.tvDocAction.setText("Upload");
            holder.tvDocAction.setTextColor(Color.parseColor("#3B82F6"));
        }

        // 2. Route the click to the correct listener method
        holder.itemView.setOnClickListener(v -> {
            if (item.getStatus() == 0) {
                listener.onUploadClicked(item);
            } else {
                listener.onViewClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() { return docList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocTitle, tvFileType, tvDocStatus, tvDocAction;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocTitle = itemView.findViewById(R.id.tvDocTitle);
            tvFileType = itemView.findViewById(R.id.tvFileType);
            tvDocStatus = itemView.findViewById(R.id.tvDocStatus);
            tvDocAction = itemView.findViewById(R.id.tvDocAction);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<DocumentItem> newList) {
        this.docList = newList;
        notifyDataSetChanged();
    }
}