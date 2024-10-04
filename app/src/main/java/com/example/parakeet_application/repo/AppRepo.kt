package com.example.parakeet_application.repo

import android.net.Uri
import android.util.Log
import com.example.parakeet_application.constants.AppConstant
import com.example.parakeet_application.data.api.Client.RetrofitClient
import com.example.parakeet_application.data.model.SavedPlacesModel
import com.example.parakeet_application.data.model.UserModel
import com.example.parakeet_application.data.model.mapsModel.GooglePlaceModel
//import com.example.parakeet_application.SavedPlaceModel
//import com.example.parakeet_application.data.model.UserModel
//import com.example.parakeet_application.constant.AppConstant
//import com.example.parakeet_application.models.googlePlaceModel.GooglePlaceModel
//import com.example.parakeet_application.network.RetrofitClient
import com.example.parakeet_application.utility.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class AppRepo {
    fun login(
        email: String,
        password: String
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val data = auth.signInWithEmailAndPassword(email, password).await()
        data?.let {
            if (auth.currentUser?.isEmailVerified!!) {
                emit(State.success("Login Successfully"))
            } else {
                emit(State.failed("Verify email first"))
            }
        }
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(
        Dispatchers.IO
    )

    fun signUp(
        email: String,
        password: String,
        username: String,
        image: Uri
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        try {
            val data = auth.createUserWithEmailAndPassword(email, password).await()
            data.user?.let {
                val path = uploadImage(it.uid, image).toString()
                var userModel = UserModel(
                    email, username, path
                )
                createUser(userModel, auth)
                auth.currentUser?.sendEmailVerification()?.await()
                emit(State.success("Email verification sent"))
            }?: throw Exception("User creation failed")
        }catch(e: Exception){
            Log.e("SignUp", "Error: ${e.message}")
            emit(State.failed(e.message!!))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun uploadImage(uid: String, image: Uri): Uri {
        val firebaseStorage = Firebase.storage
        val storageReference = firebaseStorage.reference
        val task = storageReference.child(uid + AppConstant.PROFILE_PATH)
            .putFile(image).await()

        return task.storage.downloadUrl.await()

    }

    private suspend fun createUser(userModel: UserModel, auth: FirebaseAuth) {
        try {
            val firebase = Firebase.database.getReference("Users")
            firebase.child(auth.uid!!).setValue(userModel).await()

            val profileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(userModel.username)
                .setPhotoUri(Uri.parse(userModel.image))
                .build()

            auth.currentUser?.apply {
                updateProfile(profileChangeRequest).await()
                sendEmailVerification().await()
            }
        } catch (e: Exception) {
            Log.e("CreateUser", "Error: ${e.message}")
            throw e
        }
    }

    fun forgetPassword(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        auth.sendPasswordResetEmail(email).await()
        emit(State.success("Password reset email sent."))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    fun getNearByPlaces(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.Loading(true))
        val response = RetrofitClient.retrofitApi.getNearbyPlaces(url)
        Log.d("TAG", "getPlaces: $response")
        if (response.body()?.googlePlaceModelList?.size!! > 0) {
            emit(State.success(response.body()!!.error!!))
        }
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    suspend fun getUserLocationId(): ArrayList<String> {
        val userPlaces = ArrayList<String>()
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
        val data = database.get().await()
        if (data.exists()) {
            for (ds in data.children) {
                val placeId = ds.getValue(String::class.java)
                placeId?.let {
                    userPlaces.add(it)
                }
            }
        }
        return userPlaces
    }

    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocationId: ArrayList<String>) =
        flow<State<Any>> {
            emit(State.Loading(true))
            val auth = Firebase.auth
            val userDatabase =
                Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
            val database =
                Firebase.database.getReference("Places").child(googlePlaceModel.placeId!!).get()
                    .await()
            if (!database.exists()) {
                val savedPlaceModel = SavedPlacesModel(
                    googlePlaceModel.name!!, googlePlaceModel.vicinity!!,
                    googlePlaceModel.placeId, googlePlaceModel.userRatingsTotal!!,
                    googlePlaceModel.rating!!, googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!,
                )
                addPlace(savedPlaceModel)
            }
            userSavedLocationId.add(googlePlaceModel.placeId)
            userDatabase.setValue(userSavedLocationId)
            emit(State.success(googlePlaceModel))
        }.flowOn(Dispatchers.IO).catch {
            emit(State.failed(it.message!!))
        }

    private suspend fun addPlace(savedPlaceModel: SavedPlacesModel) {
        val database = Firebase.database.getReference("Places")
        database.child(savedPlaceModel.placeId).setValue(savedPlaceModel).await()
    }

    fun removePlace(userSavedLocationId: ArrayList<String>) = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
        database.setValue(userSavedLocationId).await()
        emit(State.success("Remove Successful"))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)
}
