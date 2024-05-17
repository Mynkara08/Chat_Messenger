package mynk.ara.chat_messenger

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.futuremind.recyclerviewfastscroll.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.util.Util
import mynk.ara.chat_messenger.utils.AndroidUtil
import java.util.concurrent.TimeUnit



class LoginOtpActivity : AppCompatActivity() {
    var phoneNumber: String = ""
    val timeoutSeconds: Long = 60L
    var verificationCode: String = ""
    var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    var otpInput: EditText? = null
    var nextBtn: Button? = null
    var progressBar: ProgressBar? = null
    var resendOtpTextView: TextView? = null
    val mAuth = FirebaseAuth.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_otp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        otpInput=findViewById(R.id.login_otp)
        nextBtn=findViewById(R.id.login_next_btn)
        progressBar=findViewById(R.id.login_progress_bar)
        resendOtpTextView=findViewById(R.id.resend_otp_textview)
        val phoneNumber = intent.getStringExtra("phone")
        phoneNumber?.let {
            sendOtp(it, false)
        }


    }
    fun sendOtp(phoneNumber: String, isResend: Boolean) {
        setInProgress(true)
        val builder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                    signIn(credential)
                    setInProgress(false)


                }

                override fun onVerificationFailed(e: FirebaseException) {
                    AndroidUtil.showToast(this, "OTP verification failed")
                    setInProgress(false)

                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

                    verificationCode(verificationId)
                    resendingToken(token)
                    AndroidUtil.showToast(this, "OTP sent successfully")
                    setInProgress(false)
                }

            })
        val options = if (isResend) {

            builder.setForceResendingToken(resendingToken).build()
            
        } else {

            builder.build()
        }

        PhoneAuthProvider.verifyPhoneNumber(options)


    }


    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar?.visibility = View.VISIBLE
            nextBtn?.visibility = View.GONE
        } else {
            progressBar?.visibility = View.GONE
            nextBtn?.visibility = View.VISIBLE
        }
    }
    fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        // Implementation goes here
    }


}