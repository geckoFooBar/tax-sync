package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocViewHolder> {

    private List<DocumentItem> documentList;

    public DocumentsAdapter(List<DocumentItem> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document_card, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        DocumentItem item = documentList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType());
        holder.tvDate.setText("Uploaded: " + item.getUploadedDate());
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvType, tvDate;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDocTitle);
            tvType = itemView.findViewById(R.id.tvDocType);
            tvDate = itemView.findViewById(R.id.tvUploadedDate);
        }
    }
}
