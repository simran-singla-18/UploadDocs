package com.example.uploaddocs;

import static com.example.uploaddocs.MainActivity.folderId;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.Metadata;

/**
 * A utility for performing creating folder if not present, get the file, upload the file, download the file and
 * delete the file from google drive
 */
public class GoogleDriveServiceHelper {
  
  public String sharedFileLink = null;
  
  private static final String TAG = "GoogleDriveService";
  private final Executor mExecutor = Executors.newSingleThreadExecutor();
  private final Drive mDriveService;
  
  private final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
  private final String FOLDER_NAME = "Example_Folder";
  
  public GoogleDriveServiceHelper(Drive driveService) {
    mDriveService = driveService;
  }
  
  /**
   * Check Folder present or not in the user's My Drive.
   */
  public Task<String> isFolderPresent() {
    return Tasks.call(mExecutor, () -> {
      FileList result = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false").execute();
      for (File file : result.getFiles()) {
        if (file.getName().equals(FOLDER_NAME))
          return file.getId();
      }
      return "";
    });
  }
  
  /**
   * Creates a Folder in the user's My Drive.
   */
  public Task<String> createFolder() {
    return Tasks.call(mExecutor, () -> {
      File metadata = new File()
          .setParents(Collections.singletonList("root"))
          .setMimeType(FOLDER_MIME_TYPE)
          .setName(FOLDER_NAME);
      
      File googleFolder = mDriveService.files().create(metadata).execute();
      
      if (googleFolder == null) {
        throw new IOException("Null result when requesting Folder creation.");
      }
      
      return googleFolder.getId();
    });
  }
  
  /**
   * Upload the file to the user's My Drive Folder.
   */
  public Task<Boolean> uploadFileToGoogleDrive(String path) {
    
    if (folderId.isEmpty()) {
      isFolderPresent().addOnSuccessListener(id -> folderId = id)
          .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create file.", exception));
    }
    
    return Tasks.call(mExecutor, () -> {
      
      java.io.File filePath = new java.io.File(path);
      
      File fileMetadata = new File();
      fileMetadata.setName(filePath.getName());
      fileMetadata.setParents(Collections.singletonList(folderId));
      fileMetadata.setMimeType("application/pdf");
      
      FileContent mediaContent = new FileContent("application/pdf", filePath);
      File file = mDriveService.files().create(fileMetadata, mediaContent)
          .setFields("id")
          .execute();
      sharedFileLink = file.getId();
      return false;
    });
  }
}
