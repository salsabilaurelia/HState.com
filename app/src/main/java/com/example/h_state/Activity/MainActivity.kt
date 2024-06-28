package com.example.h_state.Activity

// Import library yang diperlukan
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.h_state.R

// Kelas utama untuk aktivitas utama
class MainActivity : AppCompatActivity() {
    // Deklarasi variabel untuk tombol login dan register
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Metode yang dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi tombol
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Menambahkan listener untuk tombol login
        btnLogin.setOnClickListener {
            // Membuat intent untuk berpindah ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Menambahkan listener untuk tombol register
        btnRegister.setOnClickListener {
            // Membuat intent untuk berpindah ke RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}