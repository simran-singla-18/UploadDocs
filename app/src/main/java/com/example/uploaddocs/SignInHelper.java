package com.example.uploaddocs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

public class SignInHelper {
  
  public static final Integer SIGN_IN_REQUEST_CODE = 111;
  
  public GoogleSignInClient getGoogleSignInClient(Context context) {
    GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
        .build();
    
    return GoogleSignIn.getClient(context, signInOptions);
  }
  
  public void signIn(Activity activity, GoogleSignInClient gg) {
    activity.startActivityForResult(gg.getSignInIntent(), SIGN_IN_REQUEST_CODE);
  }
}
