package com.example.androidnotebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidnotebook.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    int update(Note note);

    @Delete
    int delete(Note note);

    @Query("SELECT * FROM notes ORDER BY last_modified_date DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    Note getNoteById(long noteId);

    @Query("SELECT * FROM notes WHERE notebook_id = :notebookId ORDER BY last_modified_date DESC")
    List<Note> getNotesByNotebookId(long notebookId);

    // Using Note.UNASSIGNED_NOTEBOOK_ID directly in query might be tricky if it's not a compile-time constant for annotation processor.
    // Passing it as an argument is safer.
    @Query("SELECT * FROM notes WHERE notebook_id = :unassignedId ORDER BY last_modified_date DESC")
    List<Note> getUnassignedNotes(long unassignedId);

    @Query("UPDATE notes SET notebook_id = :targetNotebookId, last_modified_date = :currentTimeMillis WHERE id = :noteId")
    void moveNoteToNotebook(long noteId, long targetNotebookId, long currentTimeMillis);

    @Query("UPDATE notes SET notebook_id = :unassignedNotebookId, last_modified_date = :currentTimeMillis WHERE notebook_id = :deletedNotebookId")
    void reassignNotesFromDeletedNotebook(long deletedNotebookId, long unassignedNotebookId, long currentTimeMillis);
}
