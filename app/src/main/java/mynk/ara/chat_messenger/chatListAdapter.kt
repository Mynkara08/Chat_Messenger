package mynk.ara.chat_messenger

import ChatItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mynk.ara.chat_messenger.databinding.RvChatlistItemviewBinding

class chatListAdapter(private val context: Context,private val userList:List<ChatItem> ): RecyclerView.Adapter<chatListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var binding: RvChatlistItemviewBinding = RvChatlistItemviewBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RvChatlistItemviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view.root)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvUsername.text=userList[position].username
        holder.binding.tvLastMessage.text=userList[position].lastMessage
    }
}