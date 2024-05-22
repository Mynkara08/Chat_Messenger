package mynk.ara.chat_messenger

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginUsernameActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var letMeInBtn: Button
    lateinit var progressBar: ProgressBar
    var phoneNumber: String = ""

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
}

