package com.example.uploaddocs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;

public class LinkShareHelper {
  
  private final String TAG = this.getClass().getSimpleName();
  
  String currentLink = null;
  
  public void shareLink(Activity activity) {
    
    if(currentLink == null)
      return;
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name) + " File");
    intent.putExtra(android.content.Intent.EXTRA_TEXT, currentLink);
    Intent chooser = Intent.createChooser(intent, activity.getString(R.string.app_name));

    try {
      activity.startActivity(chooser);
    } catch (ActivityNotFoundException e) {
      // no activity can handle this intent
      Log.e(TAG, "No Activity to handle sharing link intent");
    }
  }
}
