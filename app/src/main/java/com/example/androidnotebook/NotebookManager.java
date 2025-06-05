package com.example.androidnotebook;

import android.content.Context;
import com.example.androidnotebook.dao.NotebookDao;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
// Removed AtomicLong and in-memory list

public class NotebookManager {
    private static NotebookManager instance;
    private final NotebookDao notebookDao;
    private final ExecutorService databaseWriteExecutor;
    private final Context applicationContext; // Store context for cross-manager calls

    private NotebookManager(Context context) {
        this.applicationContext = context.getApplicationContext(); // Store app context
        AppDatabase database = AppDatabase.getDatabase(this.applicationContext);
        this.notebookDao = database.notebookDao();
        this.databaseWriteExecutor = AppDatabase.databaseWriteExecutor;
    }

    public static synchronized NotebookManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotebookManager(context.getApplicationContext());
        }
        return instance;
    }

    // createNotebook now returns void.
    public void createNotebook(String name) {
        // Use the constructor that Room ignores for new entities
        Notebook newNotebook = new Notebook(name);
        databaseWriteExecutor.execute(() -> notebookDao.insert(newNotebook));
    }

    public Notebook getNotebookById(long id) {
        try {
            return databaseWriteExecutor.submit(() -> notebookDao.getNotebookById(id)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Notebook> getAllNotebooks() {
        try {
            return databaseWriteExecutor.submit(() -> notebookDao.getAllNotebooks()).get();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean updateNotebookName(long id, String newName) {
        try {
            return databaseWriteExecutor.submit(() -> {
                Notebook notebookToUpdate = notebookDao.getNotebookById(id);
                if (notebookToUpdate != null) {
                    notebookToUpdate.setName(newName);
                    // In Room, the @Update annotation handles the actual db update.
                    // The Notebook entity itself doesn't have a lastModifiedDate, but if it did, update here.
                    notebookDao.update(notebookToUpdate);
                    return true;
                }
                return false;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotebook(long notebookId) {
        try {
            return databaseWriteExecutor.submit(() -> {
                Notebook notebookToDelete = notebookDao.getNotebookById(notebookId);
                if (notebookToDelete != null) {
                    // Handle associated notes FIRST
                    NoteManager.getInstance(applicationContext).handleNotebookDeleted(notebookId);
                    // Then delete the notebook
                    notebookDao.delete(notebookToDelete);
                    return true;
                }
                return false;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
