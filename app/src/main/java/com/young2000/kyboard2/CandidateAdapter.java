package com.young2000.kyboard2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {

    private List<String> candidates;
    private OnItemClickListener onItemClickListener;
    private InputConnection inputConnection; // assuming you have this variable

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setAdapter(this); // Set the adapter in the ViewHolder
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String candidate = candidates.get(position);
        holder.bind(candidate);
    }

    @Override
    public int getItemCount() {
        return candidates != null ? candidates.size() : 0;
    }

    public interface OnItemClickListener {
        void onItemClick(String selectedItem);
        void setInputConnection(InputConnection inputConnection);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
        // Notify the listener about the inputConnection if available
        if (listener != null && inputConnection != null) {
            listener.setInputConnection(inputConnection);
        }
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
        // Notify the listener about the inputConnection if available
        if (onItemClickListener != null) {
            onItemClickListener.setInputConnection(inputConnection);
        }
    }

    // Method to handle item selection
    public void onItemClicked(int position) {
        if (onItemClickListener != null && position < candidates.size()) {
            String selectedItem = candidates.get(position);
            onItemClickListener.onItemClick(selectedItem);
            // Clear the list of candidates
            candidates.clear();
            // Notify the adapter that the data set has changed
            notifyDataSetChanged();
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView candidateTextView;
        private CandidateAdapter adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            candidateTextView = itemView.findViewById(R.id.candidateTextView);
            // Set a click listener on the item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && adapter != null) {
                        // Call a method in the adapter to handle item selection
                        adapter.onItemClicked(position);
                    } else {
                        if (adapter == null) {
                            Log.i("New", "adapter is null");
                        }
                        if (position == RecyclerView.NO_POSITION) {
                            Log.i("New", "RecyclerView is no_position");
                        }
                    }
                }
            });
        }

        public void setAdapter(CandidateAdapter adapter) {
            this.adapter = adapter;
        }
        public void bind(String candidate) {
            candidateTextView.setText(candidate);
        }
    }
}
