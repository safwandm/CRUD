package com.example.crud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //buat varieble database refenrence yg akan diisi oleh database firebase
    private lateinit var databaseRef : DatabaseReference

    //untuk cekdata dibuat untk read
    private lateinit var cekData:DatabaseReference

    //untuk memantau perubahan databsae
    private lateinit var readDataListener:ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseRef = FirebaseDatabase.getInstance().reference
        //ketika tombol tambah diklick
        btn_tambah.setOnClickListener {
            //ambil text dari editText input nama
            val nama = input_name.text.toString()
            if (nama.isBlank()){
                toastData("Kolam Nama Harus Difisi")
            }else{
                tambahData(nama)
            }
        }

        btn_hapus.setOnClickListener {
            val nama = input_name.text.toString()

            if (nama.isBlank()){
                toastData("Kolam Kalimat Harus Diisi")
            }else{
                hapusData(nama)
            }
        }

        btn_update.setOnClickListener {
            val kalimatAwal = input_name.text.toString()
            val kalimatUpdate = edit_nam.text.toString()
            if (kalimatAwal.isBlank() or  kalimatUpdate.isBlank()) {
                toastData("Kolam tidak boleh kosong")
            }else{
                updateData(kalimatAwal, kalimatUpdate)
            }
        }

        //untuk get data dari database
        cekDataKalimat()
    }

    private fun updateData(kalimatAwal: String, kalimatUpdate: String) {
        val dataUpdate = HashMap<String, Any>()
        dataUpdate["Nama"] = kalimatUpdate

        val datalistener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount> 0){
                    databaseRef.child("Daftar Nama")
                        .child(kalimatAwal)
                        .updateChildren(dataUpdate)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("Data Berhasil di Update")
                        }
                }else{
                    toastData("Data Yang dituju tidak ada didalam database")
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        }
        val dataAsal = databaseRef.child("Daftar Nama")
            .child(kalimatAwal)
        dataAsal.addListenerForSingleValueEvent(datalistener)
    }

    private fun hapusData(nama: String) {
        //membuat listener data firebase
        val dataListener = object : ValueEventListener{
            //OnDataChange itu untuk mengetahui aktiftas data
            //seperti penambahan,pengurangan dan perubahan data
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot.childrenCount untuk mengehatui banyak data yg telah di ambil
                if (snapshot.childrenCount > 0){
                    //jika data tersebut ada,maka hapus field nama yg ada didalam tabel Daftar Nama
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful)toastData("$nama telah dihapus")
                        }

                }else{
                    toastData("Tidak Ada Data $nama")
                }

            }
            override fun onCancelled(p0: DatabaseError) {
                toastData("tidak bisa menghapus data tersebut")

            }
        }
        //untuk menghapus data,kita perlu ccek data yg ada dialam tabel Daftar Nama
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
        //addValueEventListener itu menjalan kan Listerner terus menerus selama data yg diniputkan sama
        //sedangkan addListenerForSingeValueEvent itu dijalankan sekali saja
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    private fun cekDataKalimat() {
        readDataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //cek apakah data ada di dalam database
                if (snapshot.childrenCount > 0){
                    var textData = ""
                    for (data in snapshot.children){
                        val nilai = data.getValue(ModelNama::class.java) as ModelNama
                        textData += "${nilai.Nama} \n"
                    }

                    txt_nama.text = textData
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }
        //cekdata menuju ke databse firebase di tabel "Daftar Nama"
        cekData = databaseRef.child("Daftar Nama")
        //
        cekData.addValueEventListener(readDataListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cekData.removeEventListener(readDataListener)
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String,Any>()
        data["Nama"] = nama

        //logika penambahan data, yaitu cek terlebih dahulu data
        //kemudian tamnbahkan data jika data belum ada
        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot childrecount ini menghitung jumlah data
                //jika data kurang dari satu maka pasti tidak ada data jadi tambahkan data
                if (snapshot.childrenCount<1){
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            toastData("$nama telah ditambahkan dalam database")
                        }else{
                            toastData("$nama gagal ditambahkan")
                        }
                    }
                }else{
                    toastData("Data tersebut sudah ada di database")
                }
            }
            override fun onCancelled(p0:DatabaseError){
                toastData("Terjadi eror saat menambah data")

            }
        }
        //untuk mengecek tabel daftar nama apakah data yang ingin diinputkan ke table tersebut sudah ada
        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(dataListener)
    }

    private fun toastData(pesan: String) {
        Toast.makeText(this,pesan, Toast.LENGTH_SHORT).show()
    }

}
// 0 false
// 1 true}