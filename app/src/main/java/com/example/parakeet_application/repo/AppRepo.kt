package com.example.parakeet_application.repo

import android.content.SharedPreferences
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

    fun getPlaces(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val response = RetrofitClient.retrofitApi.getNearbyPlaces(url)

        // Log the entire response for better debugging
        Log.d("TAG", "getPlaces Response: $response")

        val body = response.body()
        Log.d("TAG", "getPlaces: Body: $body")
        // Null checks and handling
        if (body != null) {
            val placeList = body.googlePlaceModelList

            // Check if the list exists and has elements
            if (!placeList.isNullOrEmpty()) {
                Log.d("TAG", "getPlaces: Success with ${placeList.size} places")
                emit(State.success(body))
            } else {
                Log.d("TAG", "getPlaces: Empty or null googlePlaceModelList")
                emit(State.failed("No places found"))
            }
        } else {
            Log.d("TAG", "getPlaces: Null response body")
            emit(State.failed("Response body is null"))
        }
    }.catch { exception ->
        emit(State.failed(exception.message ?: "Unknown error occurred"))
        Log.e("TAG", "getPlaces: Exception: ${exception.message}")
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
            emit(State.loading(true))
            val auth = Firebase.auth
            val userDatabase =
                Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

            val placeId = googlePlaceModel.placeId
            val name = googlePlaceModel.name
            val vicinity = googlePlaceModel.vicinity
            val userRatingsTotal = googlePlaceModel.userRatingsTotal
            val rating = googlePlaceModel.rating
            val lat = googlePlaceModel.geometry?.location?.lat
            val lng = googlePlaceModel.geometry?.location?.lng

            // Log the fields to see what's missing
            Log.d("GooglePlaceModel", "placeId: $placeId")
            Log.d("GooglePlaceModel", "name: $name")
            Log.d("GooglePlaceModel", "vicinity: $vicinity")
            Log.d("GooglePlaceModel", "userRatingsTotal: $userRatingsTotal")
            Log.d("GooglePlaceModel", "rating: $rating")
            Log.d("GooglePlaceModel", "lat: $lat")
            Log.d("GooglePlaceModel", "lng: $lng")

            // Check for null values before proceeding
            if (placeId != null && name != null && vicinity != null && userRatingsTotal != null && rating != null && lat != null && lng != null) {
                val database = Firebase.database.getReference("Places").child(placeId).get().await()
                if (!database.exists()) {
                    val savedPlaceModel = SavedPlacesModel(
                        name, vicinity, placeId, userRatingsTotal, rating, lat, lng
                    )

                    addPlace(savedPlaceModel)
                }

                userSavedLocationId.add(placeId)
                userDatabase.setValue(userSavedLocationId).await()
                emit(State.success(googlePlaceModel))
            } else {
                Log.e("AddUserPlaceError", "Some required fields are missing in googlePlaceModel")
                emit(State.failed("Some required fields are missing in googlePlaceModel"))
            }
        }.flowOn(Dispatchers.IO).catch {
            Log.e("AddUserPlaceError", "Error occurred: ${it.message}")
            emit(State.failed(it.message ?: "An unknown error occurred"))
        }



    private suspend fun addPlace(savedPlaceModel: SavedPlacesModel) {
        val database = Firebase.database.getReference("Places")
        database.child(savedPlaceModel.placeId).setValue(savedPlaceModel).await()
    }

    fun removePlace(userSavedLocationId: ArrayList<String>) = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val database = Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
        database.setValue(userSavedLocationId).await()
        emit(State.success("Remove Successful"))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    fun getDistanceUnitPreferences(sharedPreferences: SharedPreferences): Pair<Boolean, Int> {
        val isKilometers = sharedPreferences.getBoolean("isKilometers", true)
        val maxDistance = sharedPreferences.getInt("maxDistance", 5)
      return Pair(isKilometers, maxDistance)
    }

    fun saveDistanceUnitPreferences(sharedPreferences: SharedPreferences, isKilometers: Boolean, maxDistance: Int) {
        val editor = sharedPreferences.edit()
        val auth = Firebase.auth
        val database = Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
        database.setValue(isKilometers)
        editor.putBoolean("isKilometers", isKilometers)
        editor.putInt("maxDistance", maxDistance)
        editor.apply()
    }
}
