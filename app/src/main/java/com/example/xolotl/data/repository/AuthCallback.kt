package com.example.xolotl.data.repository

interface AuthCallback {
    fun onSuccess()
    fun onError(errorMessage: String)
}
