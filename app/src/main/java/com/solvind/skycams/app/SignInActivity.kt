package com.solvind.skycams.app

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint


/**
 * Main purpose: Decide if user should be sent to main activity or be displayed the login screen
 */
@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 0
    private val mAuth = FirebaseAuth.getInstance()
    private val mAuthStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) launchSignInFirebaseActivity()
        else launchMainActivityAndFinish()
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                launchMainActivityAndFinish()
            } else {

                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private fun launchSignInFirebaseActivity() {
        val loginProviders = arrayListOf(
            AuthUI.IdpConfig.AnonymousBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )

        val customLayout = AuthMethodPickerLayout.Builder(R.layout.activity_signin)
            .setFacebookButtonId(R.id.signIn_facebook_button)
            .setAnonymousButtonId(R.id.signIn_anon_button)
            .setTosAndPrivacyPolicyId(R.id.tos_textView)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.ic_solvind)
                .setTheme(R.style.AppTheme)
                .setTosAndPrivacyPolicyUrls("https://lyngen-north.com/information/terms-and-conditions", "https://lyngen-north.com/information/privacy-policy")
                .setAvailableProviders(loginProviders)
                .setAuthMethodPickerLayout(customLayout)
                .build(), RC_SIGN_IN
        )
    }

    private fun launchMainActivityAndFinish() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}