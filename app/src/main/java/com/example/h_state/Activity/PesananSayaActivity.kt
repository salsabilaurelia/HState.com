package com.example.h_state.Activity

// Import library yang diperlukan
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h_state.API.APIClient
import com.example.h_state.Adapter.ReservasiAdapter
import com.example.h_state.Modal.Reservasi
import com.example.h_state.R
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Kelas utama untuk aktivitas Pesanan Saya
class PesananSayaActivity : AppCompatActivity() {
    // Deklarasi variabel untuk RecyclerView dan adapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var reservasiAdapter: ReservasiAdapter
    private val reservasiList = ArrayList<Reservasi>()

    // Metode yang dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pesanan_saya)

        // Inisialisasi elemen UI dan menambahkan listener
        val navHome= findViewById<ImageView>(R.id.nav_home)
        val navProfil= findViewById<ImageView>(R.id.nav_profil)

        navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        navProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        reservasiAdapter = ReservasiAdapter(reservasiList)
        recyclerView.adapter = reservasiAdapter

        // Mengambil data reservasi
        fetchReservasi()
    }

    // Metode untuk mengambil data reservasi dari API
    private fun fetchReservasi() {
        Log.d("PesananSayaActivity", "Mengambil data reservasi...")
        APIClient.api.getReservasi().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("API_RESPONSE", "Respons mentah: $rawResponse")

                    if (rawResponse != null) {
                        try {
                            // Membersihkan respons
                            val cleanResponse = cleanResponse(rawResponse)

                            // Parsing JSON
                            val jsonObject = JSONObject(cleanResponse)

                            // Memeriksa apakah ada kunci "reservasi"
                            if (jsonObject.has("reservasi")) {
                                val reservasiArray = jsonObject.getJSONArray("reservasi")
                                reservasiList.clear()

                                // Mengekstrak data reservasi dari JSON
                                for (i in 0 until reservasiArray.length()) {
                                    val reservasi = reservasiArray.getJSONObject(i)
                                    val reservasiItem = Reservasi(
                                        id_reservasi = reservasi.optString("id_reservasi", ""),
                                        fullname = reservasi.optString("fullname", ""),
                                        noHP = reservasi.optString("noHP", ""),
                                        id_room = reservasi.optString("id_room", ""),
                                        kategori = reservasi.optString("kategori", ""),
                                        harga = reservasi.optString("harga", ""),
                                        tgl_checkin = reservasi.optString("tgl_checkin", ""),
                                        tgl_checkout = reservasi.optString("tgl_checkout", ""),
                                        gambar = reservasi.optString("gambar", "")
                                    )
                                    reservasiList.add(reservasiItem)
                                }

                                reservasiAdapter.notifyDataSetChanged()
                                Log.d("PesananSayaActivity", "Berhasil memuat ${reservasiList.size} reservasi")
                            } else {
                                Log.e("PesananSayaActivity", "Kunci 'reservasi' tidak ditemukan dalam JSON")
                                showToast("Data reservasi tidak tersedia")
                            }
                        } catch (e: JSONException) {
                            Log.e("PesananSayaActivity", "Error saat parsing JSON: ${e.message}", e)
                            showToast("Terjadi kesalahan saat memproses data")
                        }
                    } else {
                        Log.e("PesananSayaActivity", "Respons body kosong")
                        showToast("Data tidak tersedia")
                    }
                } else {
                    Log.e("PesananSayaActivity", "Gagal mendapatkan respons: ${response.code()}, ${response.message()}")
                    showToast("Gagal mengambil data reservasi: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("PesananSayaActivity", "Error saat mengambil reservasi", t)
                showToast("Terjadi kesalahan jaringan")
            }
        })
    }

    // Metode untuk membersihkan respons JSON
    private fun cleanResponse(response: String): String {
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}") + 1
        return if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd)
        } else {
            throw JSONException("Invalid JSON response")
        }
    }

    // Metode untuk menampilkan Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Metode yang dipanggil setelah permintaan izin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}