package com.example.androidnotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.NotebookViewHolder> {

    public interface OnNotebookItemInteractionListener {
        void onNotebookClick(Notebook notebook); // For potential future use (e.g., opening a notebook)
        void onNotebookLongClick(Notebook notebook, View view); // For context menu
    }

    private List<Notebook> notebooks = new ArrayList<>();
    private OnNotebookItemInteractionListener listener;

    public void setOnNotebookItemInteractionListener(OnNotebookItemInteractionListener listener) {
        this.listener = listener;
    }

    public void setNotebooks(List<Notebook> notebooks) {
        this.notebooks = new ArrayList<>(notebooks);
        notifyDataSetChanged(); // Consider DiffUtil later
    }

    @NonNull
    @Override
    public NotebookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notebook, parent, false);
        return new NotebookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotebookViewHolder holder, int position) {
        Notebook currentNotebook = notebooks.get(position);
        holder.bind(currentNotebook);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotebookClick(currentNotebook);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotebookLongClick(currentNotebook, v);
            }
            return true; // Consume the long click
        });
    }

    @Override
    public int getItemCount() {
        return notebooks.size();
    }

    static class NotebookViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        // private final TextView noteCountTextView; // If added in XML

        public NotebookViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.notebook_name_text_view);
            // noteCountTextView = itemView.findViewById(R.id.notebook_note_count_text_view);
        }

        public void bind(Notebook notebook) {
            nameTextView.setText(notebook.getName());
            // Here you could also set the note count if that feature is implemented
            // e.g., int count = NoteManager.getInstance().getNotesByNotebook(notebook.getId()).size();
            // noteCountTextView.setText(count + " notes");
        }
    }
}
