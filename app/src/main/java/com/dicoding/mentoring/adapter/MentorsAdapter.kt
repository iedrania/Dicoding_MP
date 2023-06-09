package com.dicoding.mentoring.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.mentoring.R
import com.dicoding.mentoring.data.local.Mentors
import com.dicoding.mentoring.databinding.ItemMentorBinding
import com.dicoding.mentoring.ui.chat.ChatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MentorsAdapter(
    private val user: FirebaseUser,
    private val db: FirebaseFirestore,
    private val mentorsResponse: List<Mentors>
) : RecyclerView.Adapter<MentorsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMentorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mentor = mentorsResponse[position]

        holder.binding.tvItemName.text = mentor.user.name
        holder.binding.tvItemBio.text = mentor.user.bio
        holder.binding.rbItemRating.rating = mentor.averageRating ?: 0.toFloat()

        setGenderIcon(mentor.user.genderId, holder.binding.ivItemIcon)

        val listInterest = ArrayList<String>()
        if (mentor.user.isPathAndroid) listInterest.add("Android")
        if (mentor.user.isPathWeb) listInterest.add("Web")
        if (mentor.user.isPathIos) listInterest.add("iOS")
        if (mentor.user.isPathMl) listInterest.add("ML")
        if (mentor.user.isPathFlutter) listInterest.add("Flutter")
        if (mentor.user.isPathFe) listInterest.add("FE")
        if (mentor.user.isPathBe) listInterest.add("BE")
        if (mentor.user.isPathReact) listInterest.add("React")
        if (mentor.user.isPathDevops) listInterest.add("DevOps")
        if (mentor.user.isPathGcp) listInterest.add("GCP")
        listInterest.forEach { holder.binding.cgItemInterests.addChip(it) }

        val listDays = ArrayList<String>()
        if (mentor.user.isMondayAvailable) listDays.add("Senin")
        if (mentor.user.isTuesdayAvailable) listDays.add("Selasa")
        if (mentor.user.isWednesdayAvailable) listDays.add("Rabu")
        if (mentor.user.isThursdayAvailable) listDays.add("Kamis")
        if (mentor.user.isFridayAvailable) listDays.add("Jumat")
        if (mentor.user.isSaturdayAvailable) listDays.add("Sabtu")
        if (mentor.user.isSundayAvailable) listDays.add("Minggu")
        listDays.forEach { holder.binding.cgItemDays.addChip(it) }

        Glide.with(holder.itemView.context).load(mentor.user.profilePictureUrl)
            .into(holder.binding.ivItemPhoto)

        // onclick: redirect to chat
        holder.itemView.setOnClickListener {
            var groupId: String? = null
            var groupData: Map<String, Any>? = null
            val listGroup: MutableList<Pair<String, Map<String, Any>>> = mutableListOf()

            db.collection("groups").whereArrayContains("members", user.uid).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.let {
                            val (id, data) = document.id to document.data
                            val tuple = id to data
                            listGroup.add(tuple)
                        }
                    }
                    Log.d(TAG, "groups for this user $listGroup")

                    // get groups for this user that contains this mentor
                    for ((id, data) in listGroup) {
                        val members = data["members"] as ArrayList<String>
                        if (members.contains(mentor.user.id)) {
                            groupId = id
                            groupData = data
                            break
                        }
                    }
                    Log.d(TAG, "groups for this user and mentor ${groupData.toString()}")

                    if (groupId == null && groupData == null) {
                        // create new group if not exist
                        val group = hashMapOf(
                            "createdAt" to Timestamp.now(),
                            "createdBy" to user.uid,
                            "displayName" to hashMapOf(
                                "group" to "",
                                "mentor" to mentor.user.name,
                                "mentee" to user.displayName,
                            ),
                            "isPrivate" to true,
                            "members" to arrayListOf(
                                user.uid,
                                mentor.user.id,
                            ),
                            "modifiedAt" to Timestamp.now(),
                            "photoUrl" to hashMapOf(
                                "mentee" to user.photoUrl.toString(),
                                "mentor" to mentor.user.profilePictureUrl.toString(),
                            ),
                            "recentMessage" to hashMapOf(
                                "messageText" to null,
                                "senderName" to null,
                                "senderPhotoUrl" to null,
                                "sentAt" to null,
                                "sentBy" to null,
                            )
                        )

                        db.collection("groups").add(group)
                            .addOnSuccessListener { documentReference ->
                                groupId = documentReference.id
                                Log.d(
                                    TAG, "DocumentSnapshot written with ID: ${documentReference.id}"
                                )

                                // update user
                                db.collection("users").document(user.uid)
                                    .update("groups", FieldValue.arrayUnion(groupId))
                                    .addOnSuccessListener {
                                        Log.d(TAG, "group added to user")

                                        // update mentor
                                        db.collection("users").document(mentor.user.id)
                                            .update("groups", FieldValue.arrayUnion(groupId))
                                            .addOnSuccessListener {
                                                Log.d(TAG, "group added to mentor")
                                            }.addOnFailureListener { e ->
                                                Log.w(TAG, "Error updating mentor groups", e)
                                            }

                                        // open chat
                                        openChatActivity(
                                            holder.itemView.context,
                                            documentReference.id,
                                            group["displayName"] as Map<String, String>
                                        )
                                    }.addOnFailureListener { e ->
                                        Log.w(TAG, "Error updating user groups", e)
                                    }
                            }.addOnFailureListener { e ->
                                Log.w(TAG, "Error adding group document", e)
                            }
                    } else {
                        // open old group if exist
                        openChatActivity(
                            holder.itemView.context,
                            groupId!!,
                            groupData!!["displayName"] as Map<String, String>
                        )
                    }
                }.addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting groups documents: ", exception)
                }
        }
    }

    private fun setGenderIcon(genderId: Int?, ivItemIcon: ImageView) {
        if (genderId == 1) {
            ivItemIcon.setImageResource(R.drawable.baseline_male_24)
        } else if (genderId == 2) {
            ivItemIcon.setImageResource(R.drawable.baseline_female_24)
        }
    }

    private fun openChatActivity(
        context: Context, groupId: String, mapDisplayName: Map<String, String>
    ) {
        if (groupId.isNotEmpty()) {
            // TODO move activity fragment to messages
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("extra_group", groupId)

            user.getIdToken(false).addOnSuccessListener {
                val claims = it.claims
                val role = if (claims["role"] == "mentor") "mentor" else "mentee"

                if (role == "mentor") {
                    intent.putExtra("extra_title", mapDisplayName["mentee"])
                } else {
                    intent.putExtra("extra_title", mapDisplayName["mentor"])
                }
                context.startActivity(intent)
            }.addOnFailureListener { e ->
                Log.d(TAG, "get token failed with ", e)
            }
        } else {
            Log.d(TAG, "groupId empty")
        }
    }

    private fun ChipGroup.addChip(label: String) {
        Chip(context).apply {
            text = label
            addView(this)
        }
    }

    override fun getItemCount() = mentorsResponse.size

    class ViewHolder(var binding: ItemMentorBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "MentorsAdapter"
    }
}