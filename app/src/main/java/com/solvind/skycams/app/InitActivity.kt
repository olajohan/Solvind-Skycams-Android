package com.solvind.skycams.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.auth.FirebaseAuth
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject @MainDispatcher lateinit var mDispatcher: CoroutineDispatcher

    @Inject
    lateinit var mConsentInformation: ConsentInformation

    private val mAuthStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) launchSignInFirebaseActivityForResult()
        else initializeAndLaunchMainActivity()
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

    private fun initializeAndLaunchMainActivity() = lifecycleScope.launch(mDispatcher) {

        when (requestIsConsentFormStatus()) {
            is Resource.Success -> Timber.i("Consent form loaded")
            is Resource.Error -> {
                setErrorState("Failed to load consent form. Please check your internet connection")
                cancel()
            }
        }

        ensureActive()

        while (mConsentInformation.consentStatus != ConsentInformation.ConsentStatus.OBTAINED) {
            when (showConsentForm()) {
                is Resource.Success -> Timber.i("Clicked consent / dismissed form")
                is Resource.Error -> {
                    setErrorState("Failed to show consent form. Please check your internet connection")
                    cancel()
                }
            }
        }
        Timber.i("User consent obtained")

        ensureActive()

        when (initializeAds()) {
            is Resource.Success -> Timber.i("Ads initialized")
            is Resource.Error -> {
                setErrorState("Failed to initialize ads. Please check your internet connection")
            }
        }

        ensureActive()

        /**
         * Initialization is done. Launch main activity
         * */
        launchMainActivity()
    }

    private suspend fun requestIsConsentFormStatus() =
        suspendCoroutine<Resource<Unit>> { continuation ->
            val consentRequestParams = ConsentRequestParameters.Builder().build()
            mConsentInformation.requestConsentInfoUpdate(this, consentRequestParams, {

                if (mConsentInformation.isConsentFormAvailable)
                    continuation.resume(Resource.Success(Unit))
                else continuation.resume(Resource.Error(Failure.ConsentFormLoadingError))
            }, {
                continuation.resume(Resource.Error(Failure.ConsentFormLoadingError))
            })
        }

    private suspend fun showConsentForm() = suspendCoroutine<Resource<Unit>> { continuation ->
        UserMessagingPlatform.loadConsentForm(this, {
            it.show(this) { formError ->
                Timber.i("Form error?: ${formError?.message}")

                if (formError != null) continuation.resume(Resource.Error(Failure.ShowConsentFormError))
                else continuation.resume(Resource.Success(Unit))
            }
        }, {
            continuation.resume(Resource.Error(Failure.ShowConsentFormError))
        })
    }

    private suspend fun initializeAds() = suspendCoroutine<Resource<Unit>> { continuation ->
        MobileAds.initialize(applicationContext) { initializationStatus ->
            val numNetworksReady =
                initializationStatus.adapterStatusMap.filter { it.value.initializationState == AdapterStatus.State.READY }.size
            if (numNetworksReady >= 1) continuation.resume(Resource.Success(Unit))
            else continuation.resume(Resource.Error(Failure.NoAdNetworksInitialized))
        }
    }

    private fun launchMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Called from the refresh button in activity_init_error
     * */
    fun refresh(view: View) {
        initializeAndLaunchMainActivity()
    }

    private fun setLoadingState() = setContentView(R.layout.activity_init_loading)

    private fun setErrorState(userMessage: String) {
        Toast.makeText(this, userMessage, Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_init_error)

    }
}