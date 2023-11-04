package com.shiva.easynotes.Models

data class InformationModel(
    var userName:String,
    val userEmail:String,
    var userPassword:String,
    var userProfilePhoto:String,
    var userId:String
)
{
constructor():this(
"",
"",
"",
"",
""
)

}