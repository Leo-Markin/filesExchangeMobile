package com.example.rocket_for_iran

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import android.widget.TextView
import java.net.URLDecoder

data class FileResponse(
    @SerializedName("file_id") val fileid: String?,
    @SerializedName("file_name") val filename: String?,
    @SerializedName("file_size") val filesize: String?,
    @SerializedName("count_views") val countviews: String?,
    @SerializedName("owner_name") val ownername: String?,
    @SerializedName("upload_date") val uploaddate: String?,
    @SerializedName("count_downloads") val countdownloads: String?,
    @SerializedName("access_to_del") val access: Int?,
    @SerializedName("status") val status: String?
)

interface FileService {
    @POST("api/file_info.php") //
    @FormUrlEncoded
    fun get_file_info(@FieldMap request: Map<String, String>): Call<FileResponse>
}

data class DeleteResponse(
    @SerializedName("status") val status: String?
)


interface DeleteService {
    @POST("api/delete_file.php") //
    @FormUrlEncoded
    fun delete_file(@FieldMap request: Map<String, String>): Call<DeleteResponse>
}


class Kartochka: AppCompatActivity() {
    private val url = "https://direct-capital-scorpion.ngrok-free.app/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val fileService = retrofit.create(FileService::class.java)
    private val deleteService = retrofit.create(DeleteService::class.java)

    private fun make_file(token_api : String, file_url : String) {
        val request = mapOf("token_api" to token_api, "file_url" to file_url)
        fileService.get_file_info(request).enqueue(object : retrofit2.Callback<FileResponse> {
            override fun onResponse(call: Call<FileResponse>, response: retrofit2.Response<FileResponse>) {
                val fileResponse = response.body()
                if (fileResponse?.status.toString() != "success") {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Kartochka)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Kartochka, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                findViewById<TextView>(R.id.string_name).text = fileResponse?.filename
                findViewById<TextView>(R.id.string_size).text = fileResponse?.filesize
                findViewById<TextView>(R.id.string_author).text = fileResponse?.ownername
                findViewById<TextView>(R.id.string_downloads).text = fileResponse?.countdownloads
                findViewById<TextView>(R.id.string_date).text = fileResponse?.uploaddate
                val delete = findViewById<Button>(R.id.button_delete)
                if (fileResponse?.access == 0) delete.visibility = View.INVISIBLE
                else delete.setOnClickListener {
                    if (fileResponse != null) {
                        AlertDialog.Builder(this@Kartochka)
                            .setTitle("Подтверждение удаления")
                            .setMessage("Вы точно хотите удалить этот файл?")
                            .setPositiveButton("Да") { _, _ ->
                                delete_file(token_api, fileResponse.fileid.toString())
                            }
                            .setNegativeButton("Нет", null)
                            .show()
                    }
                }
                findViewById<Button>(R.id.button_load).setOnClickListener{
                    download_file(token_api, fileResponse?.fileid.toString(), fileResponse?.filename.toString())
                }
            }
            override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                AlertDialog.Builder(this@Kartochka)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun delete_file(token_api : String, file_id : String) {
        val request = mapOf("token_api" to token_api, "file_id" to file_id)
        deleteService.delete_file(request).enqueue(object : retrofit2.Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: retrofit2.Response<DeleteResponse>) {
                val deleteResponse = response.body()
                if (deleteResponse?.status == "success") {
                    finish()
                } else if (deleteResponse?.status != "invalid token") {
                    AlertDialog.Builder(this@Kartochka)
                        .setTitle("Ошибка")
                        .setMessage("Файл не получилось удалить")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Kartochka)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Kartochka, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                AlertDialog.Builder(this@Kartochka)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun download_file(token_api: String, file_id: String, file_name: String) {
        val url = url + "api/download_file.php"
        val request = mapOf("token_api" to token_api, "file_id" to file_id)
        val requestBody = request.map { (key, value) -> "$key=${Uri.encode(value)}" }
            .joinToString("&")
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val decodedFileName = URLDecoder.decode(file_name, "UTF-8")

        try {
            val downloadUri = Uri.parse(url)
                .buildUpon()
                .encodedQuery(requestBody)
                .build()


            val requestd = DownloadManager.Request(downloadUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // уведомление о завершении загрузки
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, decodedFileName) // сохранение в папку "Downloads"
                .setTitle(decodedFileName) // название файла в уведомлении

            downloadManager.enqueue(requestd)



        } catch (e: Exception) {
            AlertDialog.Builder(this@Kartochka)
                .setTitle("Ошибка загрузки")
                .setMessage("Произошла ошибка при загрузке файла.\n${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kartochka)
        val fileurl = intent?.data?.pathSegments?.lastOrNull()
        var link = intent.getStringExtra("file_url").toString()
        if (fileurl != null) link = fileurl
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        make_file(token_api, link)
    }

    override fun onDestroy() {
        setResult(Activity.RESULT_OK)
        super.onDestroy()
    }
}