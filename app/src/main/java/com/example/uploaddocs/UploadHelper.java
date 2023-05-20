package com.example.uploaddocs;


import android.app.Activity;
import android.content.Intent;

public class UploadHelper {
  
  public static final Integer UPLOAD_REQUEST_CODE = 100;
  
  public static final String UPLOAD_TITLE = "ChooseFile";
  
  public void openFileChooser(Activity activity) {
    activity.startActivityForResult(Intent.createChooser(
        getIntentToOpenFileChooser(),
        UPLOAD_TITLE), UPLOAD_REQUEST_CODE);
  }
  
  public Intent getIntentToOpenFileChooser() {
    String[] mimeTypes = getAllowedFileType();
    
    return new Intent(Intent.ACTION_GET_CONTENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*")
        .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
  }
  
  public String[] getAllowedFileType() {
    return new String[]{
        "application/pdf", //pdf
        "application/msword", //doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", //docx
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", //xsls
        "application/vnd.ms-powerpoint", //.pptx
        "application/vnd.ms-excel", "text/plain"}; //txt
  }
}
