package com.test.glidetest

import android.util.Base64
import com.test.glidetest.BuildConfig
import okhttp3.OkHttpClient
import java.lang.Exception
import java.lang.RuntimeException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class UnsafeOkHttpClient() {
    // Install the all-trusting trust manager
    val unsafeOkHttpClient: OkHttpClient
        // Create an ssl socket factory with our all-trusting manager
        get() = try {
            val trustAllCerts =
                arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )


//            // Install the all-trusting trust manager
//            val sslContext =
//                SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, SecureRandom())


            val unsafeTrustManager = SSLTrust().createUnsafeTrustManager()
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(unsafeTrustManager), null)

            // Create an ssl socket factory with our all-trusting manager
            //val sslSocketFactory = sslContext.socketFactory
            //val builder = OkHttpClient().newBuilder()

            val okHttpClient = OkHttpClient().newBuilder()
                .sslSocketFactory(sslContext.socketFactory,  unsafeTrustManager)
                .connectTimeout(1000, TimeUnit.MINUTES)
                .readTimeout(1000, TimeUnit.MINUTES)
                .writeTimeout(1000, TimeUnit.MINUTES)
                .addInterceptor { chain ->
                    val original = chain.request()

                    val requestBuilder = original.newBuilder()
                        //.header("User-Agent", "${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME} $deviceModel")
                        .header("App-Version", "${BuildConfig.VERSION_CODE}")
                        .method(original.method(), original.body())

                    requestBuilder.header(
                        "Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6ImFsbC1tb2R1bGUiLCJkYXRhIjp7ImNvZGUiOiJhYWM4ODYyNy1kNTk4LTUzNTUtODY0Yi01ZTIzNzEzNDAwNTMiLCJyb2xlIjoiREVQQVJUTUVOVCIsIm5hbWUiOiJEZXBhcnRlbWVuIERpbmRhIEd1c3RpYW4gUy5PRyJ9LCJ1aWQiOiJjNW9WUl9Db1E5LW9hZXU1S3VMeTZ1OkFQQTkxYkc1UEZnRTBsRV9OaHNsX1hzek5ZYWo2OUppd21pbzB3ZWJSQ0dUcHJqUkVtMXpZMjB6X242UFB0MkNLaXFPZzlpN2VoRlh1enBkdk9IUDdWTG9zeW1rRVlGZTdKMXllckltQnFrNF9VMEhOOXU3Y1RndU95UkhkZ2xnMTJXaG95WXEwSWFxIiwiaWF0IjoxNjcwNTY3NjMxLCJleHAiOjE2NzA2NTQwMzF9.7lPmw_vt7CVLx8awLnjhw9d7H105Dza1nI4P2KGXkOY"
                    )
                    requestBuilder.header("regid", "123456")



                    val request = requestBuilder.build()
                    chain.proceed(request)
                }

//            //builder.hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
            okHttpClient.hostnameVerifier { hostname, session -> //return true;
                val hv: HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
                hv.verify("siloamhospitals.com", session)
            }

            okHttpClient.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
}