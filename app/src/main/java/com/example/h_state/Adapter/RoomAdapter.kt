package com.example.h_state.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.h_state.API.OnButtonClickListener
import com.example.h_state.API.OnRoomImageClickListener
import com.example.h_state.Modal.RoomHome
import com.example.h_state.R

// Adapter untuk menampilkan daftar kamar dalam RecyclerView
class RoomAdapter(private var rooms: List<RoomHome>) : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    // Listener untuk menangani klik pada gambar kamar
    private var roomImagelistener: OnRoomImageClickListener? = null
    // Listener untuk menangani klik pada tombol (tidak digunakan dalam kode ini)
    private var buttonlistener: OnButtonClickListener? = null

    // Fungsi untuk mengatur listener klik gambar kamar
    fun setOnRoomImageClickListener(listener: OnRoomImageClickListener) {
        this.roomImagelistener = listener
    }
    // Fungsi untuk mengatur listener klik tombol
    fun setOnButtonClickListener(listener: OnButtonClickListener) {
        this.buttonlistener = listener
    }

    // Fungsi untuk memperbarui daftar kamar
    fun updateRooms(newRooms: List<RoomHome>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    // Membuat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return ViewHolder(view, roomImagelistener)
    }

    // Mengikat data ke ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Mengambil dua kamar untuk setiap posisi
        val room1 = rooms[position * 2]
        val room2 = if (position * 2 + 1 < rooms.size) rooms[position * 2 + 1] else null
        holder.bind(room1, room2)
    }

    // Menghitung jumlah item yang akan ditampilkan (dua kamar per item)
    override fun getItemCount(): Int {
        return (rooms.size + 1) / 2
    }

    // ViewHolder untuk menyimpan dan mengikat tampilan item
    class ViewHolder(itemView: View, private val listener: OnRoomImageClickListener?) : RecyclerView.ViewHolder(itemView) {
        // Inisialisasi komponen tampilan untuk dua kamar
        private val gambar1: ImageView = itemView.findViewById(R.id.gambar)
        private val idRoom1: TextView = itemView.findViewById(R.id.id_room)
        private val kategori1: TextView = itemView.findViewById(R.id.kategori)
        private val gambar2: ImageView = itemView.findViewById(R.id.gambar2)
        private val idRoom2: TextView = itemView.findViewById(R.id.id_room2)
        private val kategori2: TextView = itemView.findViewById(R.id.kategori2)

        // Fungsi untuk mengikat data ke tampilan
        fun bind(room1: RoomHome, room2: RoomHome?) {
            // Mengatur data untuk kamar pertama
            idRoom1.text = "Room ID : " + room1.id_room
            kategori1.text = room1.kategori
            // Memuat gambar kamar pertama menggunakan Glide
            Glide.with(itemView.context)
                .load(room1.gambar)
                .placeholder(R.color.brown)
                .centerCrop()
                .into(gambar1)

            // Mengatur listener klik untuk gambar kamar pertama
            gambar1.setOnClickListener {
                listener?.onRoomImageClick(room1)
            }

            // Jika ada kamar kedua, atur datanya
            if (room2 != null) {
                idRoom2.visibility = View.VISIBLE
                gambar2.visibility = View.VISIBLE
                kategori2.visibility = View.INVISIBLE
                idRoom2.text = "Room ID : " + room2.id_room
                kategori2.text = room2.kategori
                // Memuat gambar kamar kedua menggunakan Glide
                Glide.with(itemView.context)
                    .load(room2.gambar)
                    .placeholder(R.color.brown)
                    .centerCrop()
                    .into(gambar2)

                // Mengatur listener klik untuk gambar kamar kedua
                gambar2.setOnClickListener {
                    listener?.onRoomImageClick(room2)
                }
            } else {
                // Jika tidak ada kamar kedua, sembunyikan tampilan yang terkait
                idRoom2.visibility = View.INVISIBLE
                gambar2.visibility = View.INVISIBLE
                kategori2.visibility = View.INVISIBLE
            }
        }
    }
}

