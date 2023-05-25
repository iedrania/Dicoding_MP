package com.dicoding.mentoring.data.local

data class MentorsResponse(
    val mentors: List<Mentors>
)

data class Mentors(
    val averageRating: Float?,
    val User: Mentor,
)

data class Mentor(
    val id: Long?,
    val name: String?,
    val roleID: Int?,
    val genderID: Int?,
    val email: String?,
    val address: String?,
    val phone: String?,
    val bio: String?,
    val profile_picture_url: String?,
    val isPathAndroid: Boolean?,
    val isPathWeb: Boolean?,
    val isPathIos: Boolean?,
    val isPathMl: Boolean?,
    val isPathFlutter: Boolean?,
    val isPathFe: Boolean?,
    val isPathBe: Boolean?,
    val isPathReact: Boolean?,
    val isPathDevops: Boolean?,
    val isPathGcp: Boolean?,
    val is_monday_available: Boolean?,
    val is_tuesday_available: Boolean?,
    val is_wednesday_available: Boolean?,
    val is_thursday_available: Boolean?,
    val is_friday_available: Boolean?,
    val is_saturday_available: Boolean?,
    val is_sunday_available: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
)