package com.example.androidnotebook.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.androidnotebook.Notebook;

import java.util.List;

@Dao
public interface NotebookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Notebook notebook);

    @Update
    void update(Notebook notebook); // Can also return int for rows affected

    @Delete
    void delete(Notebook notebook); // Can also return int for rows affected

    @Query("SELECT * FROM notebooks ORDER BY name ASC")
    List<Notebook> getAllNotebooks();

    @Query("SELECT * FROM notebooks WHERE id = :notebookId")
    Notebook getNotebookById(long notebookId);
}
