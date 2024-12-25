
package mynk.ara.chat_messenger

import ChatItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import mynk.ara.chat_messenger.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db:FirebaseDatabase
    private lateinit var adapter: chatListAdapter
    private lateinit var userChatRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db=FirebaseDatabase.getInstance()
        auth=FirebaseAuth.getInstance()
        userChatRef= auth.currentUser?.let { db.getReference("userChats").child(it.uid) }!!

        fetchFinalChatList("userId1") { chatList ->
            adapter = chatListAdapter(requireContext(), chatList)
        }
        binding.rvChats.layoutManager= LinearLayoutManager(requireContext())
        binding.rvChats.adapter=adapter

    }

    fun fetchFinalChatList(userId: String, callback: (List<ChatItem>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userChatsRef = database.getReference("userChats").child(userId)
        val usersRef = database.getReference("users")

        userChatsRef.get().addOnSuccessListener { chatSnapshot ->
            val chatItems = mutableListOf<ChatItem>()

            if (chatSnapshot.exists()) {
                val tasks = mutableListOf<Task<DataSnapshot>>()

                for (chat in chatSnapshot.children) {
                    val receiverId = chat.key
                    val details = chat.value as? Map<*, *>
                    val lastMessage = details?.get("lastMessage") as? String ?: ""
                    val lastMessageTimestamp = details?.get("lastMessageTimestamp") as? Long ?: 0L

                    if (receiverId != null) {
                        val userTask = usersRef.child(receiverId).get()
                        tasks.add(userTask)

                        userTask.addOnSuccessListener { userSnapshot ->
                            val name = userSnapshot.child("username").value as? String ?: "Unknown"

                            chatItems.add(
                                ChatItem(
                                    userId = receiverId,
                                    username = name,
                                    lastMessage = lastMessage,
                                    lastMessageTimestamp = lastMessageTimestamp
                                )
                            )
                        }
                    }
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    val sortedChatItems = chatItems.sortedByDescending { it.lastMessageTimestamp }
                    callback(sortedChatItems)
                }
            } else {
                callback(emptyList())
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            callback(emptyList())
        }
    }
}
