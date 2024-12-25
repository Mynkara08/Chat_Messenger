package mynk.ara.chat_messenger

import mynk.ara.chat_messenger.databinding.ItemReceivedMessageBinding
import mynk.ara.chat_messenger.databinding.ItemSendMessageBinding
import mynk.ara.chat_messenger.model.Message
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatAdapter(
    private var activity: ConversationActivity,
    private val messages: ArrayList<Message?>?,
    senderRoom: String,
    receiverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    private val senderRoom = senderRoom
    private val receiverRoom = receiverRoom



    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: ItemSendMessageBinding
        init {
            binding = ItemSendMessageBinding.bind(view)
        }
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var binding: ItemReceivedMessageBinding
        init {
            binding = ItemReceivedMessageBinding.bind(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(activity.applicationContext)
                .inflate(R.layout.item_send_message, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(activity.applicationContext)
                .inflate(R.layout.item_received_message, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages?.get(position)
        if (message != null) {
            if (FirebaseAuth.getInstance().uid == message.senderId) {
                return ITEM_SENT
            }
        }
        return ITEM_RECEIVED
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val message = messages?.get(position)


        var viewHolder: RecyclerView.ViewHolder

        if (holder.javaClass == SentViewHolder::class.java) {
            viewHolder = holder as SentViewHolder

            when (message?.status) {
                "Seen" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.received), android.graphics.PorterDuff.Mode.SRC_IN)
                    (viewHolder as SentViewHolder).binding.tick2.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.received), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                "Delivered" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                    (viewHolder as SentViewHolder).binding.tick2.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                }
                "Sent" -> {
                    (viewHolder as SentViewHolder).binding.tick1.visibility = View.VISIBLE
                    (viewHolder as SentViewHolder).binding.tick2.visibility = View.GONE
                    (viewHolder as SentViewHolder).binding.tick1.setColorFilter(ContextCompat.getColor(activity.applicationContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
            if(message!!.message.isNotBlank()) {
                (viewHolder as SentViewHolder).binding.message.text = message.message
            }
            if(message.message == "This message is removed"){
                (viewHolder as SentViewHolder).binding.message.setTypeface(null, android.graphics.Typeface.ITALIC)
            }
            if(message.timestamp.isNotBlank()) {
                (viewHolder as SentViewHolder).binding.time.text = message.timestamp
            }

        }
        else {

            viewHolder = holder as ReceivedViewHolder

            Firebase.database.reference.child("chats")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(receiverRoom)) {
                            if (snapshot.child(receiverRoom).hasChild("messages")) {
                                if(snapshot.child(receiverRoom).child("messages").hasChild(message!!.messageId)){
                                    if(snapshot.child(receiverRoom).child("messages").child(message.messageId).hasChild("status")){
                                        FirebaseDatabase.getInstance().reference.child("chats")
                                            .child(receiverRoom)
                                            .child("messages").child(message.messageId).child("status")
                                            .setValue("Seen")
                                            .addOnSuccessListener {
                                                notifyDataSetChanged()
                                                FirebaseDatabase.getInstance().reference.child("chats")
                                                    .child(senderRoom).child("unseen")
                                                    .setValue(0)
                                                notifyDataSetChanged()
                                            }
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            if(message!!.message.isNotBlank()) {
                (viewHolder as ReceivedViewHolder).binding.message.text = message.message
            }
            if (message.message == "This message is removed") {
                (viewHolder as ReceivedViewHolder).binding.message.setTypeface(null,android.graphics.Typeface.ITALIC)
            }
            if(message.timestamp.isNotBlank()) {
                (viewHolder as ReceivedViewHolder).binding.time.text = message.timestamp
            }

        }
    }
    override fun getItemCount(): Int {
        return messages!!.size
    }
}