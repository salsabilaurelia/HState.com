package com.example.h_state.Activity

// Import library yang diperlukan
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.h_state.API.APIClient
import com.example.h_state.Modal.LoginResponse
import com.example.h_state.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Kelas utama untuk aktivitas login
class LoginActivity : AppCompatActivity() {
    // Deklarasi variabel untuk elemen UI
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Metode yang dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi elemen UI
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.tvRegister)

        // Menambahkan listener untuk tombol login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(email, password)
            }
        }

        // Menambahkan listener untuk tombol register
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Metode untuk melakukan proses login
    private fun performLogin(email: String, password: String) {
        APIClient.api.login(email, password).enqueue(object : Callback<LoginResponse> {
            // Callback untuk respons dari server
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.response == true) {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        // Menyimpan email di SharedPreferences
                        val sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("USER_EMAIL", email)
                            apply()
                        }
                        // Navigasi ke HomeActivity
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Error: ${response.code()} ${response.message()}")
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            // Callback untuk kegagalan jaringan
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}