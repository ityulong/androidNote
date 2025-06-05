package com.example.androidnotebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NoteEditActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "NOTE_ID";
    public static final long INVALID_NOTE_ID = -1;

    private EditText editTextNoteTitle;
    private EditText editTextNoteContent;
    private Button buttonSaveNote;
    private Spinner spinnerNotebook; // Added Spinner
    private NoteManager noteManager;
    private NotebookManager notebookManager; // Added NotebookManager
    private long currentNoteId = INVALID_NOTE_ID;
    private Note currentNote = null;

    private List<Notebook> availableNotebooks; // To hold notebooks for spinner
    private List<Long> notebookIds; // To map spinner position to notebook ID
    private List<String> notebookNames; // To display in spinner


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        editTextNoteTitle = findViewById(R.id.edit_text_note_title);
        editTextNoteContent = findViewById(R.id.edit_text_note_content);
        buttonSaveNote = findViewById(R.id.button_save_note);
        spinnerNotebook = findViewById(R.id.spinner_notebook); // Initialize spinner

        noteManager = NoteManager.getInstance();
        notebookManager = NotebookManager.getInstance(); // Initialize NotebookManager

        setupNotebookSpinner();

        currentNoteId = getIntent().getLongExtra(EXTRA_NOTE_ID, INVALID_NOTE_ID);

        if (currentNoteId != INVALID_NOTE_ID) {
            setTitle("Edit Note");
            currentNote = noteManager.getNoteById(currentNoteId);
            if (currentNote != null) {
                editTextNoteTitle.setText(currentNote.getTitle());
                editTextNoteContent.setText(currentNote.getContent());
                // Set spinner selection
                int selectionPosition = findNotebookPosition(currentNote.getNotebookId());
                spinnerNotebook.setSelection(selectionPosition);
            } else {
                // Note not found, perhaps it was deleted. Handle this case.
                Toast.makeText(this, "Error: Note not found.", Toast.LENGTH_SHORT).show();
                finish(); // Close activity if note is not found
                return;
            }
        } else {
            setTitle("Add Note");
        }

        buttonSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote() {
        String title = editTextNoteTitle.getText().toString().trim();
        String content = editTextNoteContent.getText().toString().trim();
        long selectedNotebookId = getSelectedNotebookId();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Cannot save an empty note.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentNoteId != INVALID_NOTE_ID) { // Existing note, update it
            if (currentNote != null) {
                boolean updated = noteManager.updateNote(currentNoteId, title, content, selectedNotebookId);
                if (updated) {
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating note (perhaps invalid notebook?)", Toast.LENGTH_SHORT).show();
                }
            }
        } else { // New note, add it
            noteManager.addNote(title, content, selectedNotebookId);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        }
        finish(); // Return to MainActivity
    }

    private void setupNotebookSpinner() {
        availableNotebooks = notebookManager.getAllNotebooks();
        notebookIds = new ArrayList<>();
        notebookNames = new ArrayList<>();

        // Add "Unassigned" option first
        notebookNames.add("Unassigned");
        notebookIds.add(Note.UNASSIGNED_NOTEBOOK_ID);

        for (Notebook nb : availableNotebooks) {
            notebookNames.add(nb.getName());
            notebookIds.add(nb.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, notebookNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotebook.setAdapter(adapter);
    }

    private int findNotebookPosition(long notebookIdToFind) {
        for (int i = 0; i < notebookIds.size(); i++) {
            if (notebookIds.get(i) == notebookIdToFind) {
                return i;
            }
        }
        return 0; // Default to "Unassigned" if not found or if ID is for some reason invalid
    }

    private long getSelectedNotebookId() {
        int selectedPosition = spinnerNotebook.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < notebookIds.size()) {
            return notebookIds.get(selectedPosition);
        }
        return Note.UNASSIGNED_NOTEBOOK_ID; // Default or error case
    }
}
