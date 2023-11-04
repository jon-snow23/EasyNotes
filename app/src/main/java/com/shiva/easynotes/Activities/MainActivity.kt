package com.shiva.easynotes.Activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.shiva.easynotes.Connections.ConnectivityObserver
import com.shiva.deverse.Utils.NetworkConnectivityObserver
import com.shiva.easynotes.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {


    private lateinit var mainLayout: NestedScrollView

    private lateinit var noInternet: LottieAnimationView

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mainLayout=findViewById(R.id.main_layout)

        noInternet=findViewById(R.id.no_internet)


        connectivityObserver = NetworkConnectivityObserver(applicationContext)


        if (isNetworkAvailable(this)){

            mainLayout.visibility= View.VISIBLE

            noInternet.visibility= View.GONE

        }

        else{


            mainLayout.visibility= View.GONE

            noInternet.visibility= View.VISIBLE

        }


        connectivityObserver.observe().onEach {

            if (it==ConnectivityObserver.Status.Available){

                mainLayout.visibility= View.VISIBLE

                noInternet.visibility= View.GONE

            }

            else{


                mainLayout.visibility= View.GONE

                noInternet.visibility= View.VISIBLE


            }


        }.launchIn(lifecycleScope)






        checkUser()



    }



    private fun checkUser(){

        val firebaseUser=FirebaseAuth.getInstance().currentUser


        if (firebaseUser!=null && firebaseUser.isEmailVerified){

            startActivity(Intent(this, NoteActivity::class.java))

            overridePendingTransition(0,0)

            finish()
        }

        else{

            try {
                val googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("1077959185777-edbl86nrvdlt8un69mqhicjc64qake21.apps.googleusercontent.com")
                    .requestEmail()
                    .build()



                val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)

                Auth.GoogleSignInApi.signOut(googleSignInClient.asGoogleApiClient())

            }
            catch (_:Exception){


            }


            FirebaseAuth.getInstance().signOut()

        }

    }


    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }


    override fun onBackPressed() {

        if (noInternet.isVisible){

            finishAffinity()

        }


        super.onBackPressed()
    }




}