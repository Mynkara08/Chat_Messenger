package mynk.ara.chat_messenger.model

import com.google.firebase.Timestamp

class UserModel {
    private var phone: String = ""
    private var username: String = ""
    private var createdTimestamp: Timestamp? = null

    constructor()
    constructor(phone: String, username: String, createdTimestamp: Timestamp?) {
        this.phone = phone
        this.username = username
        this.createdTimestamp = createdTimestamp
    }
    fun getPhone(): String {
        return phone
    }

    fun setPhone(phone: String) {
        this.phone = phone
    }


    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

}