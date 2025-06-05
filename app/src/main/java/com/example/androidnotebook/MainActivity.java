package com.example.androidnotebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.app.SearchManager;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteItemClickListener {

    private NoteManager noteManager;
    private NoteAdapter noteAdapter;
    private Note noteForContextMenu; // To store the note for context menu operations
    private RecyclerView notesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteManager = NoteManager.getInstance(); // Use singleton instance

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);

        setupRecyclerView();
        noteAdapter.setOnNoteItemClickListener(this); // Set listener
        registerForContextMenu(notesRecyclerView); // Register RecyclerView for context menu


        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                // Indicate it's a new note
                intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, NoteEditActivity.INVALID_NOTE_ID);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter();
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);
        // noteAdapter.setOnNoteItemClickListener(this); // Moved up to onCreate after adapter init
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        List<Note> notes = noteManager.getAllNotes();
        noteAdapter.setNotes(notes);
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
        intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note, View view) {
        noteForContextMenu = note; // Store the note for context menu
        view.showContextMenu(); // Show context menu on the view itself
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); // May not be reliable
        if (noteForContextMenu == null) { // Use the stored note
            Toast.makeText(this, "Error: Note not selected for action", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }

        if (item.getItemId() == R.id.delete_note_option) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete \"" + noteForContextMenu.getTitle() + "\"?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        noteManager.deleteNote(noteForContextMenu.getId());
                        loadNotes(); // Refresh the list
                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        noteForContextMenu = null; // Clear the stored note
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        noteForContextMenu = null; // Clear the stored note
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);

        // Search view setup
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true); // Collapse search view initially

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Optionally, you can handle the submission differently
                    // For live filtering, onQueryTextChange is usually enough
                    noteAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Live filtering as user types
                    noteAdapter.getFilter().filter(newText);
                    return true;
                }
            });

            // Optional: Handle search view close event to reload all notes
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true; // True to expand the view
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // When search view is collapsed, show all notes
                    noteAdapter.getFilter().filter(""); // Empty query shows all
                    return true; // True to collapse the view
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_notebooks) {
            Intent intent = new Intent(this, NotebookListActivity.class);
            startActivity(intent);
            return true;
        }
        // Handle search icon click if not using app:showAsAction="ifRoom|collapseActionView"
        // if (item.getItemId() == R.id.action_search) {
        //    return true; // Or expand the search view programmatically
        // }
        return super.onOptionsItemSelected(item);
    }
}
