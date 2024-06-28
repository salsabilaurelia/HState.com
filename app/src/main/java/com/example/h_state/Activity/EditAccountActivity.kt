package com.example.h_state.Activity

// Import library yang diperlukan
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.h_state.API.APIClient
import com.example.h_state.Modal.User
import com.example.h_state.Modal.UserResponse
import com.example.h_state.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Kelas untuk aktivitas pengeditan akun
class EditAccountActivity : AppCompatActivity() {

    companion object {
        // Kode permintaan untuk pengeditan akun
        const val EDIT_ACCOUNT_REQUEST_CODE = 1001
    }

    // Deklarasi variabel untuk elemen UI dan data pengguna
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentUser: User

    // Metode yang dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_acc)

        initViews()
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        fetchUserData()
    }

    // Inisialisasi elemen UI dan menambahkan listener
    private fun initViews() {
        etFullName = findViewById(R.id.etFullname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener {
            if (validateInputs()) {
                updateUserAccount()
            }
        }
    }

    // Mengambil data pengguna dari server
    private fun fetchUserData() {
        val userEmail = sharedPreferences.getString("USER_EMAIL", "")
        Log.d("EditAccountActivity", "Fetching data for email: $userEmail")

        if (userEmail.isNullOrEmpty()) {
            Log.e("EditAccountActivity", "User email is null or empty")
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
            return
        }

        APIClient.api.getUserByEmail(userEmail).enqueue(object : Callback<UserResponse> {
            // Callback untuk respons dari server
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.status == "success" && userResponse.data != null) {
                        currentUser = userResponse.data
                        runOnUiThread {
                            etFullName.setText(currentUser.fullname ?: "")
                            etEmail.setText(currentUser.email ?: "")
                            // Tidak mengisi password untuk alasan keamanan
                        }
                        Log.d("EditAccountActivity", "User Fullname: ${currentUser.fullname}, Email: ${currentUser.email}")
                    } else {
                        Log.e("EditAccountActivity", "Error: ${userResponse?.status}")
                        Toast.makeText(this@EditAccountActivity, "Failed to get user data: ${userResponse?.status}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("EditAccountActivity", "Error response: ${response.errorBody()?.string()}")
                    Toast.makeText(this@EditAccountActivity, "Failed to fetch user data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            // Callback untuk kegagalan jaringan
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("EditAccountActivity", "Network error: ${t.message}")
                Toast.makeText(this@EditAccountActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Validasi input pengguna
    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Full name and email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    // Memperbarui akun pengguna
    private fun updateUserAccount() {
        val originalEmail = sharedPreferences.getString("USER_EMAIL", "") ?: ""
        val fullName = etFullName.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (originalEmail.isEmpty()) {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser = currentUser.copy(fullname = fullName, email = newEmail, password = password)

        APIClient.api.updateUserAccount(originalEmail, fullName, newEmail, password).enqueue(object : Callback<ResponseBody> {
            // Callback untuk respons dari server
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditAccountActivity, "Account updated successfully", Toast.LENGTH_SHORT).show()
                    // Memperbarui SharedPreferences dengan email baru
                    sharedPreferences.edit().putString("USER_EMAIL", newEmail).apply()

                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_EMAIL", newEmail)
                        putExtra("UPDATED_FULL_NAME", fullName)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Log.e("EditAccountActivity", "Update failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@EditAccountActivity, "Failed to update account", Toast.LENGTH_SHORT).show()
                }
            }

            // Callback untuk kegagalan jaringan
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("EditAccountActivity", "Update network error: ${t.message}")
                Toast.makeText(this@EditAccountActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}