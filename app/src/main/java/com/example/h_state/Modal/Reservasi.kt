package com.example.h_state.Modal

data class Reservasi(
    var id_reservasi: String,
    var fullname: String,
    var id_room: String,
    var kategori: String,
    var harga: String,
    var tgl_checkin: String,
    var tgl_checkout: String,
    var gambar: String,
    var noHP: String
)