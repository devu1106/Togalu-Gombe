package com.togalugombe.aiguide.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.togalugombe.aiguide.data.model.*
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // getCurrentUser
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // 1. LOGIN
    suspend fun loginUser(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Auth succeeded but user reference was null")
    }

    // 2. REGISTER
    suspend fun registerUser(name: String, email: String, password: String): FirebaseUser {
        // Create user in Firebase Auth
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Auth registration succeeded but user was null")

        // Store user in Firestore under users collection
        val newUser = User(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            createdAt = Timestamp.now()
        )
        
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(newUser)
            .await()

        return firebaseUser
    }

    // Get specific user detail
    suspend fun getUserDetails(uid: String): User {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java) ?: throw Exception("User details not found")
    }

    // 3. LOGOUT
    fun logoutUser() {
        auth.signOut()
    }

    // 4. GET PLAYS (LIVE ASSIST)
    suspend fun getPlays(): List<Play> {
        val snapshot = firestore.collection("plays").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Play::class.java)?.copy(id = doc.id)
        }
    }

    // 5. GET SCENES (FILTERED BY PLAY ID)
    suspend fun getScenesForPlay(playId: String): List<Scene> {
        val snapshot = firestore.collection("scenes")
            .whereEqualTo("playId", playId)
            .get()
            .await()
            
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Scene::class.java)?.copy(id = doc.id)
        }.sortedBy { it.orderNumber }
    }

    // 6. GET SCENE DETAILS
    suspend fun getSceneDetails(sceneId: String): Scene {
        val doc = firestore.collection("scenes").document(sceneId).get().await()
        return doc.toObject(Scene::class.java)?.copy(id = doc.id) 
            ?: throw Exception("Scene details not found")
    }

    // 7. GET PUPPETS (PUPPET SCAN)
    suspend fun getPuppets(): List<Puppet> {
        val snapshot = firestore.collection("puppets").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Puppet::class.java)?.copy(id = doc.id)
        }
    }

    // 8. GET PUPPET DETAILS
    suspend fun getPuppetDetails(puppetId: String): Puppet {
        val doc = firestore.collection("puppets").document(puppetId).get().await()
        return doc.toObject(Puppet::class.java)?.copy(id = doc.id)
            ?: throw Exception("Puppet details not found")
    }

    // 9. GET ARTISTS (ARTIST CONNECT)
    suspend fun getArtists(): List<Artist> {
        val snapshot = firestore.collection("artists").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Artist::class.java)?.copy(id = doc.id)
        }
    }

    // 10. GET HISTORY FEED
    suspend fun getHistoryPosts(): List<HistoryPost> {
        val snapshot = firestore.collection("history")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(HistoryPost::class.java)?.copy(id = doc.id)
        }
    }
}
