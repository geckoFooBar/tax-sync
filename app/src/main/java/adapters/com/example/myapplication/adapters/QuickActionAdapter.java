package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;

public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.ViewHolder> {

    String[] actions = {
            "Pay Tax",
            "Calendar",
            "File Return",
            "Tax History",
            "Calculator",
            "Documents",
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        ViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.actionText);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_action, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        h.text.setText(actions[i]);
    }

    @Override
    public int getItemCount() {
        return actions.length;
    }
}

