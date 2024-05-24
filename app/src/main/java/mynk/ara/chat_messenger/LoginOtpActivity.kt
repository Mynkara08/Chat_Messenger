
package mynk.ara.chat_messenger

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import mynk.ara.chat_messenger.utils.AndroidUtil
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class LoginOtpActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private val timeoutSeconds: Long = 60L
    private lateinit var verificationCode: String
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var otpInput: EditText
    private lateinit var nextBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resendOtpTextView: TextView
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_otp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        otpInput = findViewById(R.id.login_otp)
        nextBtn = findViewById(R.id.login_next_btn)
        progressBar = findViewById(R.id.login_progress_bar)
        resendOtpTextView = findViewById(R.id.resend_otp_textview)

        phoneNumber = intent.getStringExtra("phone") ?: ""
        if (phoneNumber.isNotEmpty()) {
            sendOtp(phoneNumber, false)
        }

        nextBtn.setOnClickListener {
            val otp = otpInput.text.toString().trim()
            if (otp.isNotEmpty() && ::verificationCode.isInitialized) {
                verifyOtp(otp)
            } else {
                AndroidUtil.showToast(this, "Please enter OTP")
            }
        }

        resendOtpTextView.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                sendOtp(phoneNumber, true)
            }
        }
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        if (!isResend) {
            startResendTimer()
        }
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
                    AndroidUtil.showToast(applicationContext, "OTP verification failed: ${e.message}")
                    setInProgress(false)
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationCode = verificationId
                    resendingToken = token
                    AndroidUtil.showToast(applicationContext, "OTP sent successfully")
                    setInProgress(false)
                }
            })

        val options = if (isResend) {
            resendingToken?.let { builder.setForceResendingToken(it).build() }
        } else {
            builder.build()
        }

        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }



    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            nextBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        }
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    setInProgress(false)
                    val intent = Intent(this@LoginOtpActivity, LoginUsernameActivity::class.java)
                    intent.putExtra("phone", phoneNumber)
                    startActivity(intent)
                } else {

                    AndroidUtil.showToast(this, "Sign in failed: ${task.exception?.message}")
                }
            }
    }


    private fun startResendTimer() {
        var secondsRemaining = timeoutSeconds
        resendOtpTextView.isEnabled = false
        val timer = Timer()
        val handler = Handler(Looper.getMainLooper())

        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    secondsRemaining--
                    resendOtpTextView.text = "Resend OTP in $secondsRemaining seconds"
                    if (secondsRemaining <= 0) {
                        secondsRemaining = timeoutSeconds
                        timer.cancel()
                        resendOtpTextView.isEnabled = true
                    }
                }
            }
        }, 0, 1000)
    }
    private fun verifyOtp(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationCode, code)
        signIn(credential)
    }
}
