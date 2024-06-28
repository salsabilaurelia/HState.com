package com.example.h_state.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.h_state.API.APIClient
import com.example.h_state.Modal.User
import com.example.h_state.R
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi elemen UI
        val btnRegist = findViewById<Button>(R.id.btnRegist)
        val etFullname = findViewById<EditText>(R.id.etFullname)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfPass = findViewById<EditText>(R.id.etConfPass)
        val btnLogin = findViewById<Button>(R.id.btnBackLogin)

        // Set listener untuk tombol registrasi
        btnRegist.setOnClickListener {
            // Mengambil input dari pengguna
            val fullname = etFullname.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfPass.text.toString()

            // Validasi input
            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@RegisterActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this@RegisterActivity, "Email must be a @gmail.com address", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this@RegisterActivity, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (!password.matches(".*[A-Z].*".toRegex())) {
                Toast.makeText(this@RegisterActivity, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this@RegisterActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Jika semua validasi berhasil, buat objek User dan lakukan registrasi
                val user = User(fullname, email, password, profil = null)
                registerUser(user)
            }
        }

        // Set listener untuk tombol kembali ke halaman login
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fungsi untuk melakukan registrasi pengguna
    private fun registerUser(user: User) {
        APIClient.api.register(user.fullname.toString(), user.email.toString(), user.password.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    try {
                        // Parsing respons JSON
                        val jsonResponse = JSONObject(responseString)
                        if (jsonResponse.getBoolean("response")) {
                            // Jika registrasi berhasil
                            Toast.makeText(this@RegisterActivity, "Registered successfully", Toast.LENGTH_SHORT).show()
                            // Navigasi ke LoginActivity
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Jika registrasi gagal
                            Toast.makeText(this@RegisterActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        // Jika terjadi kesalahan saat parsing JSON
                        Toast.makeText(this@RegisterActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Jika respons dari server tidak berhasil
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Jika terjadi kesalahan jaringan atau lainnya
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}