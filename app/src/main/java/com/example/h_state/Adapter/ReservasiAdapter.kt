package com.example.h_state.Adapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.h_state.Modal.Reservasi
import com.example.h_state.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Adapter untuk menampilkan daftar reservasi dalam RecyclerView
class ReservasiAdapter(private val reservasiList: MutableList<Reservasi>) :
    RecyclerView.Adapter<ReservasiAdapter.ViewHolder>() {

    private lateinit var context: Context

    // ViewHolder untuk menyimpan referensi ke elemen UI untuk setiap item
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gambar: ImageView = view.findViewById(R.id.gambar)
        val idRoom: TextView = view.findViewById(R.id.id_room)
        val nameOrder: TextView = view.findViewById(R.id.name_order)
        val kategoriOrder: TextView = view.findViewById(R.id.kategori_order)
        val hargaOrder: TextView = view.findViewById(R.id.harga_order)
        val checkinOrder: TextView = view.findViewById(R.id.checkin_order)
        val checkoutOrder: TextView = view.findViewById(R.id.checkout_order)
        val noHp: TextView = view.findViewById(R.id.no_hp)
        val deleteButton: ImageButton = view.findViewById(R.id.img_delete_btn)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val downloadButton: ImageButton = view.findViewById(R.id.download_button)
    }

    // Membuat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.order_room, parent, false)
        return ViewHolder(view)
    }

    // Mengikat data ke ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservasi = reservasiList[position]

        // Memuat gambar menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(reservasi.gambar)
            .into(holder.gambar)

        // Mengatur teks untuk setiap elemen UI
        holder.idRoom.text = "Room ID: ${reservasi.id_room}"
        holder.nameOrder.text = "Fullname: ${reservasi.fullname}"
        holder.noHp.text = "Nomor HP: ${reservasi.noHP}"
        holder.kategoriOrder.text = "Kategori: ${reservasi.kategori}"
        holder.hargaOrder.text = "Harga: ${reservasi.harga}"
        holder.checkinOrder.text = "Check In: ${reservasi.tgl_checkin}"
        holder.checkoutOrder.text = "Check Out: ${reservasi.tgl_checkout}"

        // Mengatur listener untuk tombol hapus
        holder.deleteButton.setOnClickListener {
            showDeletePopup(reservasi.id_reservasi)
        }

        // Mengatur listener untuk tombol edit
        holder.editButton.setOnClickListener {
            showEditPopup(reservasi, holder.itemView.context)
        }

        // Mengatur listener untuk tombol unduh
        holder.downloadButton.setOnClickListener {
            downloadReservasiDetails(reservasi, holder.itemView.context)
        }
    }

    // Mengembalikan jumlah item dalam list
    override fun getItemCount(): Int {
        return reservasiList.size
    }

    // Fungsi untuk menghapus reservasi
    private fun deleteReservasi(id: String) {
        val url = "http://192.168.255.82/HSTATE_MOB/api/api-delete-reservasi.php?id_reservasi=$id"

        val request = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                try {
                    val success = response.optBoolean("status", false)
                    val message = response.optString("message", "")

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    // Menghapus item dari list lokal dan memperbarui UI
                    val position = reservasiList.indexOfFirst { it.id_reservasi == id }
                    if (position != -1) {
                        reservasiList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                } catch (e: JSONException) {
                    Log.e("ReservasiAdapter", "Error parsing JSON: ${e.message}")
                    Toast.makeText(context, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ReservasiAdapter", "Error deleting reservasi: ${error.message}")
                Toast.makeText(context, "Error deleting reservasi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Mengatur timeout jika diperlukan
        val socketTimeout = 30000 // 30 detik
        request.retryPolicy = DefaultRetryPolicy(
            socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(request)
    }

    // Fungsi untuk menampilkan DatePicker dialog
    private fun showDatePickerDialog(editText: TextInputEditText, context: Context) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)

                val dateFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                editText.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    // Fungsi untuk menampilkan popup edit
    private fun showEditPopup(reservasi: Reservasi, context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_pop, null)

        // Inisialisasi view dari layout edit_pop.xml
        val roomIDEditText = dialogView.findViewById<TextInputEditText>(R.id.room_id)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val kategoriEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_kategori)
        val hargaEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_harga)
        val fullnameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_fullname)
        val noHpEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_noHp)
        val checkinEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_DateCheckIn)
        val checkoutEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_DateCheckOut)

        // Mengisi nilai awal ke dalam EditText berdasarkan data reservasi yang ada
        roomIDEditText.setText(reservasi.id_room)
        fullnameEditText.setText(reservasi.fullname)
        noHpEditText.setText(reservasi.noHP)
        checkinEditText.setText(reservasi.tgl_checkin)
        checkoutEditText.setText(reservasi.tgl_checkout)

        checkinEditText.setOnClickListener {
            showDatePickerDialog(checkinEditText, context)
        }

        checkoutEditText.setOnClickListener {
            showDatePickerDialog(checkoutEditText, context)
        }

        val alertDialog = MaterialAlertDialogBuilder(context)
            .setView(dialogTitle)
            .setCancelable(true)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                // Implementasi untuk menyimpan perubahan
                val newFullname =
                    if (fullnameEditText.text.isNullOrEmpty()) reservasi.fullname else fullnameEditText.text.toString()
                val newNoHp =
                    if (noHpEditText.text.isNullOrEmpty()) reservasi.noHP else noHpEditText.text.toString()
                val newCheckin =
                    if (checkinEditText.text.isNullOrEmpty()) reservasi.tgl_checkin else checkinEditText.text.toString()
                val newCheckout =
                    if (checkoutEditText.text.isNullOrEmpty()) reservasi.tgl_checkout else checkoutEditText.text.toString()

                // Log data yang akan dikirim
                Log.d(
                    "ReservasiAdapter",
                    "Update data: id_reservasi=${reservasi.id_reservasi}, fullname=$newFullname, noHP=$newNoHp, tgl_checkin=$newCheckin, tgl_checkout=$newCheckout"
                )

                // Lakukan update data ke server atau sesuai kebutuhan aplikasi
                updateReservasi(
                    reservasi.id_reservasi,
                    newFullname,
                    newNoHp,
                    newCheckin,
                    newCheckout
                )
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // Fungsi untuk mengupdate reservasi
    private fun updateReservasi(
        id: String,
        fullname: String,
        noHp: String,
        tglCheckin: String,
        tglCheckout: String
    ) {
        val url = "http://192.168.213.82/HSTATE_MOB/api/api-update-reservasi.php"
        val jsonBody = JSONObject().apply {
            put("id_reservasi", id)
            put("fullname", fullname)
            put("noHP", noHp)
            put("tgl_checkin", tglCheckin)
            put("tgl_checkout", tglCheckout)
        }

        Log.d("ReservasiAdapter", "JSON Body: $jsonBody")

        val request = JsonObjectRequest(
            Request.Method.PUT, url, jsonBody,
            { response ->
                try {
                    val success = response.optBoolean("status", false)
                    val message = response.optString("message", "")

                    Toast.makeText(context, "Reservasi berhasil diperbarui", Toast.LENGTH_SHORT).show()

                    // Update data lokal atau UI di sini
                    val position = reservasiList.indexOfFirst { it.id_reservasi == id }
                    if (position != -1) {
                        reservasiList[position].apply {
                            this.fullname = fullname
                            this.noHP = noHp
                            this.tgl_checkin = tglCheckin
                            this.tgl_checkout = tglCheckout
                        }
                        // Memberitahu adapter bahwa item pada posisi tersebut telah berubah
                        notifyItemChanged(position)
                    }

                } catch (e: JSONException) {
                    Log.e("ReservasiAdapter", "Error parsing JSON: ${e.message}")
                    Toast.makeText(context, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ReservasiAdapter", "Error updating reservasi: ${error.message}")
                Toast.makeText(context, "Error updating reservasi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        // Mengatur timeout jika diperlukan
        val socketTimeout = 30000 // 30 detik
        request.retryPolicy = DefaultRetryPolicy(
            socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(request)
    }

    // Fungsi untuk mengunduh detail reservasi sebagai PDF
    private fun downloadReservasiDetails(reservasi: Reservasi, context: Context) {
        val fileName = "reservasi_${reservasi.id_reservasi}.pdf"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        // Membuat instance file baru di direktori yang ditentukan
        val file = File(fileDir, fileName)

        try {
            // Membuat file baru jika belum ada
            if (!file.exists()) {
                file.createNewFile()
            } else {
                // Opsional: menghapus file yang ada atau menangani logika penulisan ulang
                file.delete()
                file.createNewFile()
            }

            // Inisialisasi dokumen PDF
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Menggambar konten ke halaman PDF
            val paint = Paint()
            paint.textSize = 12f
            canvas.drawText("Room ID: ${reservasi.id_room}", 10f, 50f, paint)
            canvas.drawText("Fullname: ${reservasi.fullname}", 10f, 70f, paint)
            canvas.drawText("Nomor HP: ${reservasi.noHP}", 10f, 90f, paint)
            canvas.drawText("Kategori: ${reservasi.kategori}", 10f, 110f, paint)
            canvas.drawText("Harga: ${reservasi.harga}", 10f, 130f, paint)
            canvas.drawText("Check In: ${reservasi.tgl_checkin}", 10f, 150f, paint)
            canvas.drawText("Check Out: ${reservasi.tgl_checkout}", 10f, 170f, paint)

            // Menyelesaikan penulisan ke dokumen PDF
            pdfDocument.finishPage(page)

            // Menulis konten PDF ke FileOutputStream
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)

            // Menutup FileOutputStream dan dokumen PDF
            fos.close()
            pdfDocument.close()

            // Menampilkan pesan toast dengan path file
            Toast.makeText(context, "PDF berhasil di-download: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Membuka file PDF menggunakan intent
            val pdfUri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(pdfUri, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION

            // Memulai activity untuk melihat PDF
            context.startActivity(intent)

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan atau membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menampilkan popup konfirmasi penghapusan
    private fun showDeletePopup(id: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_delete_popup, null)

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            deleteReservasi(id)
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
