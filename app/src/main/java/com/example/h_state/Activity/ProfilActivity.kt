package com.example.h_state.Activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.h_state.API.APIClient
import com.example.h_state.R
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProfilActivity : AppCompatActivity() {
    // Deklarasi variabel untuk elemen UI
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnBack: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var tvEditAcc: TextView
    private lateinit var tvLogout: TextView
    private lateinit var sharedPref: SharedPreferences

    // Launcher untuk memilih gambar dari galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val filename = saveImageToInternalStorage(it)
            loadImageFromUri(it)
            uploadImageToServer(it, filename)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        // Inisialisasi SharedPreferences
        sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

        initViews()
        setupListeners()
        loadUserData()
    }

    // Inisialisasi elemen UI dan set onClickListener
    private fun initViews() {
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        btnBack = findViewById(R.id.btnBack)
        profileImage = findViewById(R.id.profileImage)
        tvEditAcc = findViewById(R.id.tvEditAcc)
        tvLogout = findViewById(R.id.logout)

        tvEditAcc.setOnClickListener {
            val intent = Intent(this, EditAccountActivity::class.java)
            startActivityForResult(intent, EditAccountActivity.EDIT_ACCOUNT_REQUEST_CODE)
        }

        profileImage.setOnClickListener {
            openImageChooser()
        }
    }

    // Set up listener untuk tombol kembali dan logout
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        tvLogout.setOnClickListener {
            logout()
        }
    }

    // Fungsi untuk menangani proses logout
    private fun logout() {
        val dialogView = layoutInflater.inflate(R.layout.logout_pop, null)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val buttonYes = dialogView.findViewById<Button>(R.id.buttonYes)
        val buttonNo = dialogView.findViewById<Button>(R.id.buttonNo)

        buttonYes.setOnClickListener {
            // Menghapus data user dari SharedPreferences
            sharedPref.edit().apply {
                remove("USER_EMAIL")
                remove("USER_FULL_NAME")
                remove("CURRENT_USER_EMAIL")
                apply()
            }

            deleteProfileImage()

            // Kembali ke MainActivity dan hapus semua aktivitas sebelumnya
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            dialog.dismiss()
        }

        buttonNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Memuat data pengguna dari SharedPreferences dan API
    private fun loadUserData() {
        val userEmail = sharedPref.getString("USER_EMAIL", "") ?: ""
        tvEmail.text = userEmail

        val currentEmail = sharedPref.getString("CURRENT_USER_EMAIL", "") ?: ""
        if (currentEmail != userEmail) {
            deleteProfileImage()
        }

        sharedPref.edit().putString("CURRENT_USER_EMAIL", userEmail).apply()

        fetchUserDetails(userEmail)
        fetchProfileImage(userEmail)
    }

    // Menghapus gambar profil dari penyimpanan internal
    private fun deleteProfileImage() {
        val file = File(filesDir, "profile_image.jpg")
        if (file.exists()) {
            file.delete()
        }
    }

    // Memperbarui gambar profil dari penyimpanan internal
    private fun updateProfileImage() {
        val file = File(filesDir, "profile_image.jpg")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            profileImage.setImageBitmap(bitmap)
        } else {
            loadDefaultImage()
        }
    }

    // Menyimpan gambar ke penyimpanan internal
    private fun saveImageToInternalStorage(uri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val filename = "profile_image_$timestamp.jpg"
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(filesDir, filename)
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            Log.e("ProfilActivity", "Error menyimpan gambar: ${e.message}", e)
            Toast.makeText(this, "Gagal menyimpan gambar profil", Toast.LENGTH_SHORT).show()
        }
        return filename
    }

    // Membuka pemilih gambar
    private fun openImageChooser() {
        pickImageLauncher.launch("image/*")
    }

    // Mengambil detail pengguna dari API
    private fun fetchUserDetails(email: String) {
        APIClient.api.getUserDetails(email).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("ProfilActivity", "Respon mentah: $rawResponse")

                    rawResponse?.let {
                        handleStringResponse(it)
                    } ?: handleError("Tidak ada data pengguna")
                } else {
                    handleError("Gagal memuat data pengguna: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleError("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    // Menangani respons string dari API
    private fun handleStringResponse(response: String) {
        try {
            val jsonStartIndex = response.indexOf("{")
            if (jsonStartIndex != -1) {
                val cleanedResponse = response.substring(jsonStartIndex)
                val jsonObject = JsonParser.parseString(cleanedResponse).asJsonObject
                val status = jsonObject.get("status").asString
                if (status == "success") {
                    val fullName = jsonObject.get("fullName").asString
                    tvFullName.text = fullName
                    sharedPref.edit().putString("USER_FULL_NAME", fullName).apply()
                    Log.d("ProfilActivity", "FullName diatur ke: $fullName")
                } else {
                    val message = jsonObject.get("message")?.asString ?: "Unknown error"
                    handleError("Gagal memuat data pengguna: $message")
                }
            } else {
                tvFullName.text = response.trim()
                Log.d("ProfilActivity", "Tidak ada JSON valid, menampilkan respon penuh")
            }
        } catch (e: Exception) {
            Log.e("ProfilActivity", "Error parsing respon string: ${e.message}")
            tvFullName.text = response.trim()
        }
        Log.d("ProfilActivity", "Menerima respon string: $response")
    }

    // Menangani error
    private fun handleError(message: String) {
        tvFullName.text = message
        Log.e("ProfilActivity", message)
    }

    // Mengunggah gambar ke server
    private fun uploadImageToServer(imageUri: Uri, filename: String) {
        val file = File(filesDir, filename)
        val requestFile = file.asRequestBody(contentResolver.getType(imageUri)?.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", filename, requestFile)

        val email = sharedPref.getString("USER_EMAIL", "") ?: ""
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())

        APIClient.api.uploadImage(emailBody, body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("ProfilActivity", "Upload berhasil: $rawResponse")
                    Toast.makeText(this@ProfilActivity, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                    fetchProfileImage(email)
                } else {
                    handleUploadError("Gagal mengunggah gambar: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleUploadError("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    // Menangani error saat mengunggah gambar
    private fun handleUploadError(message: String) {
        Toast.makeText(this@ProfilActivity, message, Toast.LENGTH_SHORT).show()
        Log.e("ProfilActivity", message)
    }

    // Mengambil gambar profil dari server
    private fun fetchProfileImage(email: String) {
        APIClient.api.getProfileImage(email).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val responseString = responseBody.string()
                        Log.d("ProfilActivity", "Respon gambar profil: $responseString")

                        if (responseString.startsWith("uploads/")) {
                            loadImageFromUrl(APIClient.BASE_URL + responseString)
                        } else {
                            try {
                                val inputStream = responseBody.byteStream()
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                if (bitmap != null) {
                                    runOnUiThread {
                                        profileImage.setImageBitmap(bitmap)
                                    }
                                    saveImageToInternalStorage(bitmap)
                                } else {
                                    Log.e("ProfilActivity", "Gagal decode bitmap dari respon")
                                    loadDefaultImage()
                                }
                            } catch (e: Exception) {
                                Log.e("ProfilActivity", "Error memproses gambar: ${e.message}")
                                loadDefaultImage()
                            }
                        }
                    } ?: run {
                        Log.e("ProfilActivity", "Body respon null")
                        loadDefaultImage()
                    }
                } else {
                    Log.e("ProfilActivity", "Gagal mengambil gambar profil: ${response.code()}")
                    loadDefaultImage()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ProfilActivity", "Error mengambil gambar profil: ${t.message}")
                loadDefaultImage()
            }
        })
    }

    // Memuat gambar dari URL menggunakan Glide
    private fun loadImageFromUrl(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .error(R.drawable.profile_ic)
            .into(profileImage)
    }

    // Memuat gambar dari URI menggunakan Glide
    private fun loadImageFromUri(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .error(R.drawable.profile_ic)
            .into(profileImage)
    }

    // Memuat gambar default
    private fun loadDefaultImage() {
        runOnUiThread {
            profileImage.setImageResource(R.drawable.profile_ic)
        }
    }

    // Menyimpan gambar bitmap ke penyimpanan internal
    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        try {
            val timestamp = System.currentTimeMillis()
            val filename = "profile_image_$timestamp.jpg"
            val file = File(filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: Exception) {
            Log.e("ProfilActivity", "Error menyimpan gambar: ${e.message}", e)
        }
    }

    // Menangani hasil dari aktivitas EditAccount
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EditAccountActivity.EDIT_ACCOUNT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updatedEmail = data?.getStringExtra("UPDATED_EMAIL")
            val updatedFullName = data?.getStringExtra("UPDATED_FULL_NAME")

            updatedEmail?.let {
                sharedPref.edit().putString("USER_EMAIL", it).apply()
                tvEmail.text = it
            }

            updatedFullName?.let {
                sharedPref.edit().putString("USER_FULL_NAME", it).apply()
                tvFullName.text = it
            }
        }
    }
}