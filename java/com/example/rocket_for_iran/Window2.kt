package com.example.rocket_for_iran

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.FieldMap
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.annotations.SerializedName
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.FormUrlEncoded
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part
import android.annotation.SuppressLint
import android.provider.OpenableColumns
import android.view.View
import android.widget.ProgressBar


data class File(
    @SerializedName("id") val id: Int,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: String,
    @SerializedName("owner_name") val ownerName: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("upload_date") val uploadDate: String,
    @SerializedName("count_views") val countViews: Int
)

data class FilesResponse(
    @SerializedName("files") val files: List<File>?,
    @SerializedName("status") val status: String?
)

interface FilesService {
    @POST("api/user_files.php") //
    @FormUrlEncoded
    fun get_files(@FieldMap request: Map<String, String>): Call<FilesResponse>
}

interface UploadService {
    @Multipart
    @POST("api/upload_file.php")
    fun uploadFile(
        @Part("token_api") token: String,
        @Part file: MultipartBody.Part
    ): Call<String>
}

class Window2:AppCompatActivity() {

    private val url = "https://filesexchange.ru.tuna.am/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val filesService = retrofit.create(FilesService::class.java)
    private val resultat = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        makeFiles(token_api)
    }

    private fun makeFiles(token_api : String) {
        val request = mapOf("token_api" to token_api)
        filesService.get_files(request).enqueue(object : retrofit2.Callback<FilesResponse> {
            override fun onResponse(call: Call<FilesResponse>, response: retrofit2.Response<FilesResponse>) {
                val filesResponse = response.body()
                val files = filesResponse?.files
                val status = filesResponse?.status
                if (status == "success" && files != null) {
                    val qweListAll = files.map { file ->
                        Qwe(file.fileName, file.uploadDate, file.fileSize, file.fileUrl)
                    }
                    var qweList = qweListAll
                    // Заполняем адаптер
                    val spat = findViewById<RecyclerView>(R.id.recyclerView)
                    spat.layoutManager = LinearLayoutManager(this@Window2)
                    spat.adapter = Popka(qweList, this@Window2, ::click)
                    val find = findViewById<Button>(R.id.button2)
                    find.setOnClickListener({
                        val findTextEdit = findViewById<EditText>(R.id.editTextText2)
                        val text = findTextEdit.text.toString()
                        qweList = qweListAll.filter {
                            it.name.contains(text, ignoreCase = true)
                        }
                        spat.adapter = Popka(qweList, this@Window2, ::click)
                    })
                } else if (status != "invalid token") {
                    AlertDialog.Builder(this@Window2)
                        .setTitle("Ошибка")
                        .setMessage("Не загрузились файлы")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Window2)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Window2, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<FilesResponse>, t: Throwable) {
                AlertDialog.Builder(this@Window2)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            uploadFile(uri)
        }
    }

    @SuppressLint("Range")
    fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    private fun uploadFile(fileUri: Uri) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        progressBar.visibility = View.VISIBLE
        fab.visibility = View.GONE // Скрываем кнопку
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null)
        // Преобразование Uri в MultipartBody.Part
        val fileName = getFileNameFromUri(fileUri) // Получаем имя файла
            ?: fileUri.lastPathSegment // Fallback, если имя не удалось получить
        val file = contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val requestFile = inputStream.readBytes().toRequestBody("*/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", fileName, requestFile) // Используем fileName
        }
        val uploadService = retrofit.create(UploadService::class.java)
        if (file != null) {
            uploadService.uploadFile(token_api.toString(), file).enqueue(object : Callback<String> { // Callback<String> or your specific response type
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    progressBar.visibility = View.GONE
                    fab.visibility = View.VISIBLE // Показываем кнопку
                    if (response.isSuccessful) {
                        AlertDialog.Builder(this@Window2)
                            .setTitle("Успех")
                            .setMessage("Файл загружен")
                            .setPositiveButton("OK", null)
                            .show()
                        makeFiles(token_api.toString())
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        AlertDialog.Builder(this@Window2)
                            .setTitle("Ошибка")
                            .setMessage("Ошибка загрузки файла: $errorMessage")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    fab.visibility = View.VISIBLE // Показываем кнопку
                    AlertDialog.Builder(this@Window2)
                        .setTitle("Ошибка")
                        .setMessage("Ошибка сети: ${t.message}")
                        .setPositiveButton("OK", null)
                        .show()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Window2)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Window2, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.window2)
        button3()
    }
    fun button3() {
        val button2 = findViewById<ImageView>(R.id.homeik)
        val intent = Intent(this, MainActivity::class.java)
        button2.setOnClickListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Window2)
            sharedPreferences.edit().clear().apply()
            startActivity(intent)
            finish()
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        makeFiles(token_api)
        val button_new_file = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        button_new_file.setOnClickListener {
            selectFileLauncher.launch("*/*")
        }
    }
    fun click(file_url : String){
        val intent = Intent(this@Window2, Kartochka::class.java)
        intent.putExtra("file_url", file_url)
        resultat.launch(intent)
    }
}