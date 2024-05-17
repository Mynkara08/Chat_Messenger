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
import com.hbb20.CountryCodePicker

class LoginNumberActivity : AppCompatActivity() {
    var countryCodePicker: CountryCodePicker? = null
    var phoneInput: EditText? = null
    var sendOtpBtn: Button? = null
    var progressBar: ProgressBar? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_number)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }
        countryCodePicker = findViewById(R.id.login_countrycode)
        phoneInput = findViewById(R.id.login_mobile_number)
        sendOtpBtn=findViewById(R.id.sent_otp_btn)
        progressBar=findViewById(R.id.login_progress_bar)
        countryCodePicker?.registerCarrierNumberEditText(phoneInput)
        progressBar?.visibility = View.GONE

        sendOtpBtn?.setOnClickListener {
            if (!countryCodePicker?.isValidFullNumber()!!) {
                phoneInput?.setError("Phone number not valid")
                return@setOnClickListener
            }
            val intent = Intent(this@LoginNumberActivity, LoginOtpActivity::class.java)
            startActivity(intent)
            intent.putExtra("phone", countryCodePicker?.fullNumberWithPlus ?: "")
            startActivity(intent)


        }


    }
}