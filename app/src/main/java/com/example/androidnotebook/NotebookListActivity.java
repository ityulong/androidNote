package com.example.androidnotebook;

import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NotebookListActivity extends AppCompatActivity implements NotebookAdapter.OnNotebookItemInteractionListener {

    private NotebookManager notebookManager;
    private NoteManager noteManager; // Needed for notebook deletion side effects
    private NotebookAdapter notebookAdapter;
    private RecyclerView notebooksRecyclerView;
    private Notebook notebookForContextMenu; // To store the notebook for context menu operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_list);

        notebookManager = NotebookManager.getInstance();
        noteManager = NoteManager.getInstance(); // Get instance of NoteManager

        notebooksRecyclerView = findViewById(R.id.notebooks_recycler_view);
        FloatingActionButton fabAddNotebook = findViewById(R.id.fab_add_notebook);

        setupRecyclerView();
        registerForContextMenu(notebooksRecyclerView);

        fabAddNotebook.setOnClickListener(view -> showCreateNotebookDialog());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Notebooks");
        }
    }

    private void setupRecyclerView() {
        notebookAdapter = new NotebookAdapter();
        notebookAdapter.setOnNotebookItemInteractionListener(this);
        notebooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notebooksRecyclerView.setAdapter(notebookAdapter);
    }

    private void loadNotebooks() {
        List<Notebook> notebooks = notebookManager.getAllNotebooks();
        notebookAdapter.setNotebooks(notebooks);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotebooks();
    }

    private void showCreateNotebookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Notebook");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("Notebook Name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                notebookManager.createNotebook(name);
                loadNotebooks();
                Toast.makeText(this, "Notebook \"" + name + "\" created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notebook name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onNotebookClick(Notebook notebook) {
        // For now, clicking a notebook item does nothing.
        // Could navigate to a view showing only notes from this notebook.
        Toast.makeText(this, "Clicked: " + notebook.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotebookLongClick(Notebook notebook, View view) {
        notebookForContextMenu = notebook;
        view.showContextMenu(); // This tells the RecyclerView item to show its context menu
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notebook_context_menu, menu);
        if (notebookForContextMenu != null) {
            menu.setHeaderTitle(notebookForContextMenu.getName());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (notebookForContextMenu == null) {
            Toast.makeText(this, "Error: Notebook not selected", Toast.LENGTH_SHORT).show();
            return super.onContextItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.rename_notebook_option) {
            showRenameNotebookDialog(notebookForContextMenu);
            return true;
        } else if (itemId == R.id.delete_notebook_option) {
            showDeleteNotebookDialog(notebookForContextMenu);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showRenameNotebookDialog(final Notebook notebook) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Notebook");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(notebook.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                notebookManager.updateNotebookName(notebook.getId(), newName);
                loadNotebooks();
                Toast.makeText(this, "Notebook renamed to \"" + newName + "\"", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notebook name cannot be empty", Toast.LENGTH_SHORT).show();
            }
            notebookForContextMenu = null; // Clear context
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            notebookForContextMenu = null; // Clear context
        });
        builder.show();
    }

    private void showDeleteNotebookDialog(final Notebook notebook) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notebook")
                .setMessage("Are you sure you want to delete notebook \"" + notebook.getName() + "\"? Notes within this notebook will become unassigned.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    notebookManager.deleteNotebook(notebook.getId()); // This also calls noteManager.handleNotebookDeleted
                    loadNotebooks();
                    Toast.makeText(this, "Notebook \"" + notebook.getName() + "\" deleted", Toast.LENGTH_SHORT).show();
                    notebookForContextMenu = null; // Clear context
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    notebookForContextMenu = null; // Clear context
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to the previous activity
        return true;
    }
}
