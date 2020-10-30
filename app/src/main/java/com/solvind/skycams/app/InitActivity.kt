package com.solvind.skycams.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.auth.FirebaseAuth
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


/**
 * 1. In the onStart method we attach the firebaseAuth authStateListener
 * 2. The listener will launch the firebaseUI activity if the user is not signed in.
 * ---- The firebaseUI will return the result to this activity, which in turn again attaches the
 * ---- authStateListener in onStart.
 * 3. After the user has signed in, we make sure the user has consent to be shown ads by showing the consent form
 * 4. Once we have the users consent, we initialize ads
 * 5. When MobileAds have finished, we finally launch MainActivity
 *
 * Error content view: activity_init_error
 * Loading content view: activity_init_loading
 *
 */
@AndroidEntryPoint
class InitActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 0
    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var mConsentForm: ConsentForm
    private lateinit var mConsentInformation: ConsentInformation

    private val mAuthStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) launchSignInFirebaseActivityForResult()
        else {
            /**
             * We can say for sure that the user is logged in from here on out.
             * */
            requestConsentInfoUpdateThenLoadConsentForm()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLoadingState()
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }

    private fun launchSignInFirebaseActivityForResult() {
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
                .setTosAndPrivacyPolicyUrls(
                    "https://lyngen-north.com/information/terms-and-conditions",
                    "https://lyngen-north.com/information/privacy-policy"
                )
                .setAvailableProviders(loginProviders)
                .setAuthMethodPickerLayout(customLayout)
                .build(), RC_SIGN_IN
        )
    }

    private fun requestConsentInfoUpdateThenLoadConsentForm() {
        val consentRequestParams = ConsentRequestParameters.Builder().build()

        mConsentInformation = UserMessagingPlatform.getConsentInformation(this)
        mConsentInformation.requestConsentInfoUpdate(this, consentRequestParams, {

            if (mConsentInformation.isConsentFormAvailable) loadConsentFormThenInitializeAds()

        }, {
            setErrorState("Network error, check your internet connection")
        })
    }

    private fun loadConsentFormThenInitializeAds() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm ->
            this.mConsentForm = consentForm
            when (mConsentInformation.consentStatus) {
                ConsentInformation.ConsentStatus.OBTAINED -> {
                    Timber.i("User consent was obtained")
                    initializeAdsThenLaunchMainActivity()
                }
                /**
                 * Keep showing the form until the user has consent. We don't allow to launch
                 * MainActivity without having the users consent.
                 * */
                else -> {
                    mConsentForm.show(this) {
                        loadConsentFormThenInitializeAds()
                    }
                }
            }
        }, {
            setErrorState("Network error, check your internet connection")
        })
    }

    private fun initializeAdsThenLaunchMainActivity() {
        MobileAds.initialize(applicationContext) {
            launchMainActivityThenFinish()
        }
    }

    private fun launchMainActivityThenFinish() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Called from the refresh button in activity_init_error
     * */
    fun refresh(view: View) {
        setLoadingState()
        requestConsentInfoUpdateThenLoadConsentForm()
    }

    private fun setLoadingState() = setContentView(R.layout.activity_init_loading)
    private fun setErrorState(userMessage: String) {
        Toast.makeText(this, userMessage, Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_init_error)

    }
}