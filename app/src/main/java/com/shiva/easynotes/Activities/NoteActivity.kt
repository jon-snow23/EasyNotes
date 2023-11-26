package com.shiva.easynotes.Activities

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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
import com.shiva.easynotes.TaskDatabase
import com.shiva.easynotes.TaskRepository
import com.shiva.easynotes.TaskViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class NoteActivity : AppCompatActivity() {

    private lateinit var parentLayout:ConstraintLayout

    private lateinit var mainLayout: FrameLayout

    private lateinit var noInternet: LottieAnimationView

    private lateinit var connectivityObserver: ConnectivityObserver
    lateinit var viewModel : TaskViewModel
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)


        val db = TaskDatabase.getDatabase(this)
        val repository = TaskRepository(db)
        viewModel = TaskViewModel(application, repository, this)

        parentLayout=findViewById(R.id.parent_layout)

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

        // Create a new SharedPreferences file or access an existing one with a specific name
        sharedPreferences = this.getSharedPreferences("EasyNotesSharedPreference", Context.MODE_PRIVATE)

    }


    private fun checkUser(){

        val firebaseUser=FirebaseAuth.getInstance().currentUser


        if (firebaseUser!=null && firebaseUser.isEmailVerified){



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
            catch (e:Exception){


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