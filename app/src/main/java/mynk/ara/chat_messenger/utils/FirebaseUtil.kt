package mynk.ara.chat_messenger.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

    object FirebaseUtil {

        fun currentUserId(): String? {
            return FirebaseAuth.getInstance().uid
        }
        fun isLoggedIn(): Boolean {
            return currentUserId() != null
        }


        fun currentUserDetails(): DocumentReference {
            val userId = currentUserId()
            if (userId != null) {
                return FirebaseFirestore.getInstance().collection("users").document(userId)
            } else {
                throw IllegalStateException("User ID is null")
            }
        }
    }
