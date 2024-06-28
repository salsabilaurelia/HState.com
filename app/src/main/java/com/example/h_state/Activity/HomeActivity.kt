package com.example.h_state.Activity

// Import library yang diperlukan
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.h_state.API.APIClient
import com.example.h_state.API.OnRoomImageClickListener
import com.example.h_state.Adapter.RoomAdapter
import com.example.h_state.Modal.RoomHome
import com.example.h_state.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

// Kelas utama untuk aktivitas beranda
class HomeActivity : AppCompatActivity(), OnRoomImageClickListener {

    // Deklarasi variabel untuk RecyclerView dan adapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var roomAdapter: RoomAdapter
    private val roomsList = ArrayList<RoomHome>()
    private val calendar = Calendar.getInstance()

    // Metode yang dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi elemen UI dan menambahkan listener
        val navPesananSaya = findViewById<ImageView>(R.id.nav_pesanan_saya)
        val navProfil = findViewById<ImageView>(R.id.nav_profil)

        navPesananSaya.setOnClickListener {
            val intent = Intent(this@HomeActivity, PesananSayaActivity::class.java)
            startActivity(intent)
        }

        navProfil.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfilActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        roomAdapter = RoomAdapter(roomsList)
        roomAdapter.setOnRoomImageClickListener(this)
        recyclerView.adapter = roomAdapter

        Log.d("HomeActivity", "RecyclerView setup complete")

        // Mengambil data kamar dan setup spinner
        fetchRooms()
        setupSpinner()
    }

    // Setup spinner untuk memilih kategori kamar
    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.your_spinner_id)
        val options = listOf("All", "Deluxe", "Premium", "Regular")

        // Membuat adapter kustom untuk spinner
        val adapter = object : ArrayAdapter<String>(
            this, R.layout.spinner_item, R.id.spinner_text, options
        ) {
            // Override metode untuk mengatur tampilan item spinner
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val imageView: ImageView = view.findViewById(R.id.spinner_icon)
                imageView.visibility = if (position == spinner.selectedItemPosition) View.VISIBLE else View.GONE
                return view
            }

            // Override metode untuk mengatur tampilan dropdown spinner
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val imageView: ImageView = view.findViewById(R.id.spinner_icon)
                imageView.visibility = View.GONE
                return view
            }
        }

        spinner.adapter = adapter

        // Menambahkan listener untuk spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter.notifyDataSetChanged()
                filterRooms(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle situasi ketika tidak ada yang dipilih
            }
        }
    }

    // Metode untuk memfilter kamar berdasarkan kategori
    private fun filterRooms(category: String) {
        val filteredRooms = if (category == "All") {
            roomsList
        } else {
            roomsList.filter { it.kategori == category }
        }
        roomAdapter.updateRooms(filteredRooms)
    }

    // Metode untuk mengambil data kamar dari API
    private fun fetchRooms() {
        Log.d("HomeActivity", "Fetching rooms...")
        APIClient.api.data().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("API_RESPONSE", "Raw response: $rawResponse")

                    when {
                        rawResponse == null -> {
                            Log.e("HomeActivity", "Response body is null")
                        }
                        else -> {
                            try {
                                // Parsing respons JSON
                                val jsonStartIndex = rawResponse.indexOf("{")
                                if (jsonStartIndex != -1) {
                                    val jsonPart = rawResponse.substring(jsonStartIndex)
                                    val jsonObject = JSONObject(jsonPart)
                                    val roomsArray = jsonObject.getJSONArray("rooms")
                                    val rooms = ArrayList<RoomHome>()

                                    // Mengekstrak data kamar dari JSON
                                    for (i in 0 until roomsArray.length()) {
                                        val room = roomsArray.getJSONObject(i)
                                        val status = room.optString("status", "")
                                        if (status != "booked") { // Hanya tambahkan kamar yang tidak di-booking
                                            val roomHome = RoomHome(
                                                id_room = room.getString("id_room"),
                                                gambar = room.getString("gambar"),
                                                kategori = room.getString("kategori"),
                                                harga = room.getString("harga"),
                                                fasilitas = room.getString("fasilitas")
                                            )
                                            rooms.add(roomHome)
                                        }
                                    }

                                    // Memperbarui daftar kamar
                                    roomsList.clear()
                                    roomsList.addAll(rooms)
                                    roomAdapter.updateRooms(rooms)

                                    Log.d("HomeActivity", "Parsed ${rooms.size} available rooms")
                                } else {
                                    Log.e("HomeActivity", "No JSON object found in response")
                                }
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Error parsing response", e)
                            }
                        }
                    }
                } else {
                    Log.e("HomeActivity", "Failed to get response: ${response.code()}, ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HomeActivity", "Error fetching rooms", t)
            }
        })
    }

    // Metode untuk menampilkan dialog pemilih tanggal
    private fun showDatePickerDialog(editText: TextInputEditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)

                val dateFormat = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                editText.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    // Metode yang dipanggil saat gambar kamar diklik
    override fun onRoomImageClick(room: RoomHome) {
        // Menampilkan dialog detail kamar
        val view = LayoutInflater.from(this).inflate(R.layout.detail_pop, null)
        val imageView: ImageView = view.findViewById(R.id.image)
        val textRoomId: TextView = view.findViewById(R.id.lay_id)
        val textKategori: TextView = view.findViewById(R.id.lay_kategori)
        val textHarga: TextView = view.findViewById(R.id.lay_harga)
        val textFasilitas: TextView = view.findViewById(R.id.lay_fasilitas)
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_title, null)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.alertTitle)
        titleTextView.text = "Detail Room"

        // Mengisi detail kamar
        textRoomId.text = "Room ID : ${room.id_room}"
        textKategori.text = "Kategori : " + room.kategori
        textHarga.text = "Harga : " + room.harga
        textFasilitas.text = "Fasilitas : " + room.fasilitas

        // Memuat gambar kamar menggunakan Glide
        Glide.with(this)
            .load(room.gambar)
            .placeholder(R.color.brown)
            .centerCrop()
            .into(imageView)

        // Menampilkan dialog detail kamar
        val alertDialog = MaterialAlertDialogBuilder(this)
            .setCustomTitle(customTitleView)
            .setView(view)
            .setPositiveButton("Booking") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                showBookingForm(room.id_room)
            }
            .setNegativeButton("Close") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // Ekstensi fungsi untuk menampilkan Toast
    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Metode untuk menampilkan form pemesanan
    fun showBookingForm(roomId: String) {
        val view1 = LayoutInflater.from(this).inflate(R.layout.booking_pop, null)
        val editTextFullname = view1.findViewById<TextInputEditText>(R.id.fullname)
        val editTextNoHp = view1.findViewById<TextInputEditText>(R.id.noHp)
        val editTextCheckIn = view1.findViewById<TextInputEditText>(R.id.dateCheckIn)
        val editTextCheckOut = view1.findViewById<TextInputEditText>(R.id.dateCheckOut)
        val roomIdEditText = view1.findViewById<TextInputEditText>(R.id.room_id)

        val customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_title, null)
        val titleTextView = customTitleView.findViewById<TextView>(R.id.alertTitle)
        titleTextView.text = "Booking Form"

        roomIdEditText.setText(roomId)

        // Menambahkan listener untuk pemilihan tanggal
        editTextCheckIn.setOnClickListener {
            showDatePickerDialog(editTextCheckIn)
        }
        editTextCheckOut.setOnClickListener {
            showDatePickerDialog(editTextCheckOut)
        }

        // Menampilkan dialog form pemesanan
        val alertDialogBookingForm = MaterialAlertDialogBuilder(this)
            .setCustomTitle(customTitleView)
            .setView(view1)
            .setPositiveButton("Submit") { dialogInterface: DialogInterface, i: Int ->
                val fullname = editTextFullname.text.toString()
                val noHp = editTextNoHp.text.toString()
                val tglCheckin = editTextCheckIn.text.toString()
                val tglCheckout = editTextCheckOut.text.toString()
                val id_room = roomIdEditText.text.toString()

                if (fullname.isNotEmpty() && noHp.isNotEmpty() && tglCheckin.isNotEmpty() && tglCheckout.isNotEmpty()) {
                    bookReservation(fullname, noHp, tglCheckin, tglCheckout, id_room)
                    showToast("Booking berhasil, Silahkan cek di menu pesanan saya")
                    dialogInterface.dismiss()
                } else {
                    showToast("Harap isi semua kolom yang diperlukan")
                }
            }
            .setNegativeButton("Close") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialogBookingForm.show()
    }

    // Metode untuk melakukan pemesanan kamar
    private fun bookReservation(fullname: String, noHp: String, tglCheckin: String, tglCheckout: String, id_room: String) {
        val apiService = APIClient.api
        Log.d("BookingDebug", "Booking: fullname=$fullname, noHp=$noHp, tglCheckin=$tglCheckin, tglCheckout=$tglCheckout, id_room=$id_room")

        val call = apiService.booking(fullname, noHp, tglCheckin, tglCheckout, id_room)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    updateRoomList() // Perbarui daftar kamar setelah booking berhasil
                } else {
                    showToast("Reservasi gagal: " + response.message())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("Gagal melakukan reservasi: " + t.message)
            }
        })
    }

    // Metode untuk memperbarui daftar kamar
    private fun updateRoomList() {
        fetchRooms()
    }

    // Metode yang dipanggil saat aktivitas dilanjutkan
    override fun onResume() {
        super.onResume()
        fetchRooms()
    }
}