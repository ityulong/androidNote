package com.example.androidnotebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> implements Filterable {

    public interface OnNoteItemClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note, View view);
    }

    private List<Note> notesList; // Will hold the filtered list
    private List<Note> notesListFull; // Will hold the original, complete list
    private OnNoteItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    // private int selectedPosition = RecyclerView.NO_POSITION; // selectedPosition logic might need review with filtering

    public void setOnNoteItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    // This method replaces the old setNotes
    public void setNotes(List<Note> notes) {
        this.notesList = new ArrayList<>(notes);
        this.notesListFull = new ArrayList<>(notes); // Keep a full copy for filtering
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        // Pass notesList (the filtered list) to ViewHolder for click handling
        return new NoteViewHolder(itemView, listener, notesList);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notesList.get(position); // Use notesList (filtered)
        holder.bind(currentNote, dateFormat);

        if (currentNote.getNotebookId() != Note.UNASSIGNED_NOTEBOOK_ID) {
            Notebook notebook = NotebookManager.getInstance().getNotebookById(currentNote.getNotebookId());
            if (notebook != null) {
                holder.notebookNameTextView.setText("Notebook: " + notebook.getName());
                holder.notebookNameTextView.setVisibility(View.VISIBLE);
            } else {
                holder.notebookNameTextView.setVisibility(View.GONE);
            }
        } else {
            holder.notebookNameTextView.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            // setSelectedPosition(holder.getAdapterPosition()); // Re-evaluate if selectedPosition is needed with filtering
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                // Ensure we get the note from the potentially filtered list
                listener.onNoteLongClick(notesList.get(holder.getAdapterPosition()), v);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notesList == null ? 0 : notesList.size(); // Use notesList
    }

    // REMOVE THE OLD setNotes method that was here. It's now at the top and modified.

    @Override
    public Filter getFilter() {
        return noteFilter;
    }

    private final Filter noteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Note> filteredList = new ArrayList<>();
            if (notesListFull == null) { // Ensure notesListFull is initialized
                notesListFull = new ArrayList<>();
            }
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(notesListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Note item : notesListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) ||
                        item.getContent().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notesList.clear();
            if (results.values != null) {
                notesList.addAll((List<Note>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView subtitleTextView;
        private final TextView notebookNameTextView; // Added for notebook name
        private List<Note> notesListRef; // Reference to the notes list for click listener

        public NoteViewHolder(@NonNull View itemView, final OnNoteItemClickListener listener, List<Note> currentNotesList) {
            super(itemView);
            this.notesListRef = currentNotesList; // This should be the filtered list
            titleTextView = itemView.findViewById(R.id.note_title_text_view);
            subtitleTextView = itemView.findViewById(R.id.note_subtitle_text_view);
            notebookNameTextView = itemView.findViewById(R.id.note_notebook_text_view);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                // Ensure notesListRef is not null and position is valid
                if (listener != null && position != RecyclerView.NO_POSITION && notesListRef != null && position < notesListRef.size()) {
                    listener.onNoteClick(notesListRef.get(position));
                }
            });
        }

        public void bind(Note note, SimpleDateFormat dateFormat) {
            titleTextView.setText(note.getTitle());
            subtitleTextView.setText(dateFormat.format(note.getLastModifiedDate()));
        }
    }

    // The getSelectedPosition and getNoteAt might be problematic with filtering if MainActivity
    // relies on them for context menu after filtering.
    // The context menu in MainActivity currently uses a `noteForContextMenu` object,
    // which is set on long click. This should still work as long as the correct Note object
    // from the filtered list is passed.
    // For now, let's remove/comment out setSelectedPosition, getSelectedPosition, and getNoteAt as they are not robust with filtering.
    // public int getSelectedPosition() {
    //     return selectedPosition;
    // }

    // public void setSelectedPosition(int position) {
    //     this.selectedPosition = position;
    //     // This might need to map to notesListFull if used with filtering for a persistent selection
    // }

    // public Note getNoteAt(int position) {
    //     // This should ideally return from notesList (filtered list)
    //     // However, if MainActivity uses it for context menu with a stored position that
    //     // doesn't update with filtering, it's an issue.
    //     if (notesList != null && position >= 0 && position < notesList.size()) {
    //         return notesList.get(position);
    //     }
    //     return null;
    // }
}
