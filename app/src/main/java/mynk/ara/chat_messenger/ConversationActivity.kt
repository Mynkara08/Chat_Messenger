package mynk.ara.chat_messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mynk.ara.chat_messenger.databinding.ActivityConversationBinding
import mynk.ara.chat_messenger.model.Message
import mynk.ara.chat_messenger.model.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap

class ConversationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConversationBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var messages: ArrayList<Message?>
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog
    lateinit var senderUid: String
    lateinit var receiverUid: String
    lateinit var receiverName: String
    lateinit var senderRoom: String
    lateinit var receiverRoom: String
    lateinit var sender: User
    private lateinit var name: String

    @SuppressLint("SimpleDateFormat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database
        storage = Firebase.storage
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading image...")
        dialog.setCancelable(false)
        messages = ArrayList()



        database.reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("name")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.getValue(String::class.java).toString()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        val profileImg = intent.getStringExtra("profileImage")
        receiverName = intent.getStringExtra("name").toString()

        binding.name.text = receiverName
        binding.backArrow.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid").toString()


        senderUid = Firebase.auth.currentUser!!.uid

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                database.reference.child("users").child(senderUid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            sender = snapshot.getValue(User::class.java)!!
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}

                })
            }
        }
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        Log.d("senderRoom", senderRoom)
        Log.d("receiverRoom", receiverRoom)
        Log.d("senderUid", senderUid)
        Log.d("receiverUid", receiverUid)


        getMessages()

        adapter = ChatAdapter(this, messages,senderRoom,receiverRoom)
        binding.messagesRv.layoutManager = LinearLayoutManager(this)
        binding.messagesRv.adapter = adapter

        binding.messagesRv.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                val lastVisiblePosition =
                    (binding.messagesRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                val lastItem = binding.messagesRv.adapter!!.itemCount - 1
                Log.d("lastVisiblePosition", lastVisiblePosition.toString())
                Log.d("lastItem", lastItem.toString())
                binding.messagesRv.postDelayed({
                    if (lastVisiblePosition + 5 == lastItem || lastVisiblePosition + 6 == lastItem) binding.messagesRv.scrollToPosition(
                        lastItem
                    )
                    else binding.messagesRv.scrollToPosition(lastVisiblePosition)
                }, 100)
            }
        }

        binding.sendBtn.setOnClickListener {
            if(binding.message.text.toString().isNotEmpty()){
                val messageTxt: String = binding.message.text.toString()
                binding.message.setText("")

                val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
                val time: String = dateFormat.format(Date()).toString()

                val message = Message(messageTxt, senderUid, time)
                val randomKey = database.reference.push().key
                val lastMsgObj: HashMap<String, Any> = HashMap()
                lastMsgObj["lastMsg"] = message.message
                lastMsgObj["lastMsgTime"] = time

                database.reference.child("chats").child(senderRoom).updateChildren(lastMsgObj)
                database.reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj)
                database.reference.child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey!!)
                    .setValue(message).addOnSuccessListener {
                        database.reference.child("chats").child(receiverRoom).child("messages")
                            .child(randomKey).setValue(message)
                        database.reference.child("chats").child(senderRoom).child("messages")
                            .child(randomKey).child("status").setValue("Sent")
                        adapter.notifyItemInserted(messages.size)
                        binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount - 1)
                        database.reference.child("chats").child(senderRoom).child("messages")
                            .child(randomKey).child("status").setValue("Delivered")
//                        database.reference.child("chats").child(receiverRoom).child("unseen")
//                            .setValue(ServerValue.increment(1))
                        adapter.notifyDataSetChanged()

                    }
            }}



        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                val calendar = Calendar.getInstance()
                val reference = storage.reference.child("chats")
                    .child(calendar.timeInMillis.toString() + "")
                dialog.show()
                reference.putFile(uri).addOnCompleteListener { task ->
                    dialog.dismiss()
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt: String =
                                        binding.message.text.toString()
                                    val dateFormat: DateFormat = SimpleDateFormat("hh.mm aa")
                                    val time: String = dateFormat.format(Date()).toString()

                                    val message = Message(messageTxt, senderUid, time)

                                    message.message = ("Photo")
                                    message.imageUrl = (filePath)
                                    binding.message.setText("")
                                    val randomKey = database.reference.push().key
                                    val lastMsgObj: HashMap<String, Any> = HashMap()
                                    lastMsgObj["lastMsg"] = message.message
                                    lastMsgObj["lastMsgTime"] = time
                                    database.reference.child("chats").child(senderRoom)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats").child(receiverRoom)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database.reference.child("chats")
                                                .child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener {
                                                    adapter.notifyItemInserted(messages.size)
                                                    binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount)
                                                }
                                        }
                                }
                            }
                        }}
                }
            }

        }
        val handler = Handler()
        binding.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        database.reference.child("activity").child(senderUid).setValue("typing...")
                        handler.removeCallbacksAndMessages(null)
                        handler.postDelayed(userStoppedTyping, 1000)}
                }
            }
            var userStoppedTyping =
                Runnable {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            database.reference.child("activity").child(senderUid).setValue("Online")
                        }
                    }}
        })
    }

    private fun getMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                database.reference.child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val count = messages.size
                            messages.clear()
                            for (snapshot1 in snapshot.children) {
                                val message = snapshot1.getValue(Message::class.java)
                                if (message != null) {
                                    message.messageId = snapshot1.key.toString()
                                    messages.add(message)
                                }
                            }
                            adapter.notifyDataSetChanged()
                            if(messages.size>count) {
                                binding.messagesRv.scrollToPosition(binding.messagesRv.adapter!!.itemCount-1)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }

    }
    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Offline")
    }
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("activity").child(currentId!!).setValue("Online")

    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
