package com.example.uploaddocs;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.uploaddocs.databinding.MainActivityBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
  MainActivityBinding binding;
  
  SignInHelper signInHelper;
  
  static String folderId = "";
  
  GoogleSignInClient signInClient;
  
  private GoogleDriveServiceHelper mDriveServiceHelper;
  
  private final UploadHelper uploadHelper = new UploadHelper();
  private final LinkShareHelper shareLinkHelper = new LinkShareHelper();
  
  
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
    setOnClickListeners();
    
    signInHelper = new SignInHelper();
    signInClient = signInHelper.getGoogleSignInClient(this);
    
    //sign-in using google account
    signInHelper.signIn(this, signInClient);
    
  }
  
  private void setOnClickListeners() {
    binding.uploadBtn.setOnClickListener(this::createFolder);
    binding.shareLink.setOnClickListener(this::shareLink);
  }
  
  private void shareLink(View v) {
    shareLinkHelper.shareLink(this);
  }
  
  private void handleShareLinkVisibility(Boolean isVisible) {
    int isVisibleView = isVisible ? View.VISIBLE : View.GONE;
    binding.shareLink.setVisibility(isVisibleView);
    binding.tvLink.setVisibility(isVisibleView);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    try {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == UploadHelper.UPLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
        uploadFile(data);
      } else if (requestCode == SignInHelper.SIGN_IN_REQUEST_CODE && resultCode == RESULT_OK) {
        handleSignInResult(data);
      }
    } catch (Exception ex) {
      showMessage(ex.toString());
    }
  }
  
  
  private void handleSignInResult(Intent result) {
    GoogleSignIn.getSignedInAccountFromIntent(result)
        .addOnSuccessListener(googleAccount -> {
          
          // Use the authenticated account to sign in to the Drive service.
          GoogleAccountCredential credential =
              GoogleAccountCredential.usingOAuth2(
                  this, Collections.singleton(DriveScopes.DRIVE_FILE));
          credential.setSelectedAccount(googleAccount.getAccount());
          Drive googleDriveService =
              new Drive.Builder(
                  
                  AndroidHttp.newCompatibleTransport(),
                  new GsonFactory(),
                  credential)
                  .setApplicationName("UploadDocs")
                  .build();
          
          // The DriveServiceHelper encapsulates all REST API and SAF functionality.
          // Its instantiation is required before handling any onClick actions.
          mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService);
          
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception exception) {
          
          }
        });
  }
  
  public void createFolder(View view) {
    
    handleShareLinkVisibility(false);
    
    if (mDriveServiceHelper != null) {
      
      // check folder present or not
      mDriveServiceHelper.isFolderPresent()
          .addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String id) {
              if (id.isEmpty()) {
                mDriveServiceHelper.createFolder()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                      @Override
                      public void onSuccess(String fileId) {
                        folderId = fileId;
                        upload();
                      }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception exception) {
                        showMessage("Couldn't create file.");
                      }
                    });
              } else {
                folderId = id;
                upload();
              }
            }
          })
          .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
              showMessage("Couldn't create file..");
            }
          });
    }
  }
  
  private void upload() {
    uploadHelper.openFileChooser(this);
  }
  
  private void showMessage(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  }
  
  
  public void uploadFile(Intent resultData) {
    
    if (resultData == null) {
      //no data present
      return;
    }
    
    showMessage("Uploading file... ");
    
    // Get the Uri of the selected file
    Uri selectedFileUri = resultData.getData();
    
    // Get the path
    String selectedFilePath = FileUtils.getPath(this, selectedFileUri);
    
    if (selectedFilePath != null && !selectedFilePath.equals("")) {
      if (mDriveServiceHelper != null) {
        mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath)
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                
                showMessage("File uploaded successfully");
                shareLinkHelper.currentLink = String.format(getString(R.string.drive_share_link), mDriveServiceHelper.sharedFileLink);
                handleShareLinkVisibility(true);
                binding.tvLink.setText(shareLinkHelper.currentLink);
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                showMessage("Couldn't able to upload file, error: " + e);
              }
            });
      }
    } else {
      Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
    }
  }
}