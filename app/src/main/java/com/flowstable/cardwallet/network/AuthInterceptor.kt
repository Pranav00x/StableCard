package com.flowstable.cardwallet.network

import com.flowstable.cardwallet.security.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val accessToken = runBlocking { tokenStore.getAccessToken() }
        val requestBuilder = original.newBuilder()
        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            // Unauthorized – clear any stored tokens so app can force re-login.
            runBlocking { tokenStore.clear() }
        }

        return response
    }
}

