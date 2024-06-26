package mynk.ara.chat_messenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import mynk.ara.chat_messenger.utils.FirebaseUtil
import java.util.logging.Handler

class Splash_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        android.os.Handler().postDelayed({
            if(FirebaseUtil.isLoggedIn()){
                startActivity(Intent(this@Splash_Activity,MainActivity::class.java))
            }
            else{
                startActivity(Intent(this@Splash_Activity,LoginNumberActivity::class.java))
            }
            finish()
        },1000)

    }
}