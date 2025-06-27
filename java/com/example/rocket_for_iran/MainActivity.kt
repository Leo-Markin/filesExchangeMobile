package com.example.rocket_for_iran

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.FieldMap
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.annotations.SerializedName
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.FormUrlEncoded
import android.content.SharedPreferences
import androidx.preference.PreferenceManager



data class LoginResponse(
    @SerializedName("token_api") val token_api: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?
)

interface AuthService {
    @POST("api/auth.php") //
    @FormUrlEncoded
    fun login(@FieldMap request: Map<String, String>): Call<LoginResponse>
}

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private val url = "https://filesexchange.ru.tuna.am/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authService = retrofit.create(AuthService::class.java)

    fun makeAuth(username: String, password: String) {
        val request = mapOf("username" to username, "password" to password)
        authService.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                val loginResponse = response.body()
                val token_api = loginResponse?.token_api
                val status = loginResponse?.status
                val role = loginResponse?.role
                if (status == "success") {
                    with (sharedPreferences.edit()) {
                        putString("token_api", token_api)
                        putString("role", role)
                        apply()
                    }
                    var intent = Intent(this@MainActivity, Window2::class.java)
                    if (role == "admin") intent = Intent(this@MainActivity, Navi::class.java)
                    startActivity(intent)
                    finish() //
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Ошибка")
                        .setMessage("Неверный логин или пароль")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Ошибка")
                    .setMessage(t.message)
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getString("token_api", null)?.isNotEmpty() == true) {
            val role = sharedPreferences.getString("role", "user")
            var intent = Intent(this@MainActivity, Window2::class.java)
            if (role == "admin") intent = Intent(this@MainActivity, Navi::class.java)
            startActivity(intent)
            finish()
        }
        button1()
    }
    fun button1() {
        val button2 = findViewById<Button>(R.id.button)
        button2.setOnClickListener {
            val login = findViewById<EditText>(R.id.editTextText).text.toString()
            val password = findViewById<EditText>(R.id.editTextTextPassword).text.toString()
            makeAuth(login, password)
        }
    }

    /* fun getToken(): String? {
        return sharedPreferences.getString("token_api", null)
    } */
}