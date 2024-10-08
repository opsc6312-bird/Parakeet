package com.example.parakeet_application.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.parakeet_application.R
import com.example.parakeet_application.data.model.UserModel
import com.example.parakeet_application.databinding.ActivityMainBinding
import com.example.parakeet_application.databinding.NavigationDrawerLayoutBinding
import com.example.parakeet_application.databinding.ToolbarLayoutBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {
    private lateinit var navigationDrawerLayoutBinding: NavigationDrawerLayoutBinding
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var toolbarLayoutBinding: ToolbarLayoutBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imgHeader: CircleImageView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationDrawerLayoutBinding = NavigationDrawerLayoutBinding.inflate(layoutInflater)
        setContentView(navigationDrawerLayoutBinding.root)
        mainBinding = navigationDrawerLayoutBinding.mainActivity
        toolbarLayoutBinding = mainBinding.toolbarMain

        setSupportActionBar(toolbarLayoutBinding.toolbar)

        firebaseAuth = Firebase.auth
        getUserData()
        val toggle = ActionBarDrawerToggle(
            this,
            navigationDrawerLayoutBinding.navDrawer,
            toolbarLayoutBinding.toolbar,
            R.string.open_navigation_drawer,
            R.string.close_navigation_drawer
        )

        navigationDrawerLayoutBinding.navDrawer.addDrawerListener(toggle)
        toggle.syncState()
        val navController = Navigation.findNavController(this, R.id.fragmentContainer)
       NavigationUI.setupWithNavController(
            navigationDrawerLayoutBinding.navView,
            navController
        )
       val headerLayout = navigationDrawerLayoutBinding.navView.getHeaderView(0)
        imgHeader = headerLayout.findViewById(R.id.imgHeader)
        txtName = headerLayout.findViewById(R.id.txtHeaderName)
        txtEmail = headerLayout.findViewById(R.id.txtHeaderEmail)
    }

    override fun onBackPressed() {
        if (navigationDrawerLayoutBinding.navDrawer.isDrawerOpen(GravityCompat.START))
            navigationDrawerLayoutBinding.navDrawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()


    }

    private fun getUserData(){
        val uid = firebaseAuth.uid
        if (uid != null){
            val database = Firebase.database.getReference("Users").child(firebaseAuth.uid!!)
            database.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val userModel = snapshot.getValue(UserModel::class.java)
                        Glide.with(this@MainActivity).load(userModel?.image).into(imgHeader)
                        txtName.text = userModel?.username
                        txtEmail.text = userModel?.email
                    }
                    else {
                      //  Glide.with(this@MainActivity).load(?.image).into(imgHeader)
                        txtName.text = firebaseAuth.currentUser?.uid
                        txtEmail.text = firebaseAuth.currentUser?.email
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(mainBinding.root, "Failed to retrieve user data", Snackbar.LENGTH_SHORT).show()
                }
            });
        } else {
            Snackbar.make(mainBinding.root, "User not logged in", Snackbar.LENGTH_SHORT).show()
        }
         }
}