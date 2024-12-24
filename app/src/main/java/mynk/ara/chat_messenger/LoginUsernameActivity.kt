package mynk.ara.chat_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import mynk.ara.chat_messenger.model.UserModel
import mynk.ara.chat_messenger.utils.AndroidUtil
import mynk.ara.chat_messenger.utils.FirebaseUtil

class LoginUsernameActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var letMeInBtn: Button
    lateinit var progressBar: ProgressBar
    private lateinit var auth:FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    var phoneNumber: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_username)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usernameInput=findViewById(R.id.login_username)
        letMeInBtn=findViewById(R.id.login_let_me_in_btn)
        progressBar=findViewById(R.id.login_progress_bar)

        phoneNumber = intent.extras?.getString("phone") ?: ""
        getUsername()
        letMeInBtn.setOnClickListener { v ->
            when (usernameInput.text.toString().length) {
                0 -> usernameInput.setError("Username is mandatory")
                in 1..2 -> usernameInput.error = "Username length should be at least 3 chars"
                else -> setUsername()
            }
        }


    }
    private fun setUsername(){

        val username: String = usernameInput.text.toString()
        val map = mutableMapOf<String, String>()
        map["username"] = username
        map["phone"] = phoneNumber
        map["createdTimestamp"] = Timestamp.now().toString()

        db.collection("users").document(phoneNumber).set(map)
            .addOnSuccessListener {
                moveToHomeScreen()
                Toast.makeText(this, "Username saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save username: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun getUsername() {
        setInProgress(true)
        db.collection("users").document(phoneNumber).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    usernameInput.setText(username)
                    moveToHomeScreen()
                } else {
                    Toast.makeText(this, "User not found in the database", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve username: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                setInProgress(false)
            }
    }


    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            letMeInBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            letMeInBtn.visibility = View.VISIBLE
        }
    }


    private fun moveToHomeScreen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}