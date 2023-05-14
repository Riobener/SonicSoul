package com.riobener.sonicsoul.utils

interface ServiceConfig{
    val BASE_URL: String
    val REDIRECT_URI: String
    val AUTHORIZE_URL: String
    val TOKEN_URL: String
    val SCOPES: List<String>
    val CLIENT_ID: String
    val CLIENT_SECRET: String
}