package com.example.androidnotebook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.androidnotebook.dao.NoteDao;
import com.example.androidnotebook.dao.NotebookDao;
import com.example.androidnotebook.Note; // Make sure this is the entity
import com.example.androidnotebook.Notebook; // Make sure this is the entity


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Notebook.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract NoteDao noteDao();
    public abstract NotebookDao notebookDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "android_notebook_db")
                            // .addCallback(sRoomDatabaseCallback) // Optional pre-population
                            // .fallbackToDestructiveMigration() // Use migrations in production
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Example callback for pre-populating database (optional)
    /*
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with some default data, add it here.
                NoteDao noteDao = INSTANCE.noteDao();
                NotebookDao notebookDao = INSTANCE.notebookDao();

                // Clear existing data (optional, good for testing pre-population)
                // noteDao.deleteAll(); // Assuming you add deleteAll methods to DAOs
                // notebookDao.deleteAll();

                // Add sample data
                Notebook generalNotebook = new Notebook("General"); // Using the app-facing constructor
                long notebookId = notebookDao.insert(generalNotebook);

                Note note1 = new Note("First Sample Note", "This is the content of the first sample note.", notebookId);
                Note note2 = new Note("Second Sample Note", "Content for note 2.", Note.UNASSIGNED_NOTEBOOK_ID);
                noteDao.insert(note1);
                noteDao.insert(note2);
            });
        }
    };
    */
}
