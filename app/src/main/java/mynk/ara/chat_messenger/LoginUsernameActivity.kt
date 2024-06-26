package mynk.ara.chat_messenger

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import mynk.ara.chat_messenger.model.UserModel
import mynk.ara.chat_messenger.utils.AndroidUtil
import mynk.ara.chat_messenger.utils.FirebaseUtil

class LoginUsernameActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var letMeInBtn: Button
    lateinit var progressBar: ProgressBar
    var phoneNumber: String = ""
    private var userModel: UserModel?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_username)
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
            setUsername()
        }


    }
    fun setUsername(){

        val username: String = usernameInput.text.toString()
        val data = hashMapOf("username" to username)
        if(username.isEmpty() || username.length<3){
            usernameInput.setError("Username length should be at least 3 chars")
            return
        }
        setInProgress(true)
        if (userModel != null) {
            userModel!!.setUsername(username)

        }else {
            userModel = UserModel(phoneNumber, username, Timestamp.now())
        }
        FirebaseUtil.currentUserDetails().set(data).addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
                moveToHomeScreen()
            }
        }



    }
    fun getUsername() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails().get()
            .addOnSuccessListener {
                if(it.exists()){
                    userModel = it.toObject(UserModel::class.java)!!
                    if(userModel!=null){
                        moveToHomeScreen()
                        usernameInput.setText(userModel!!.getUsername())
                    }
                }else {
                    AndroidUtil.showToast(this, "You're not registered yet!!")
                }
                setInProgress(false)
            }.addOnFailureListener {
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
        val intent = Intent(this@LoginUsernameActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}