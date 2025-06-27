package com.example.rocket_for_iran

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
)

data class UsersResponse(
    @SerializedName("users") val users: List<User>?,
    @SerializedName("status") val status: String?
)

interface UsersService {
    @POST("api/user_list.php") //
    @FormUrlEncoded
    fun get_users(@FieldMap request: Map<String, String>): Call<UsersResponse>
}

interface DeleteUserService {
    @POST("api/delete_user.php")
    @FormUrlEncoded
    fun delete_User(@FieldMap request: Map<String, String>): Call<DeleteUserResponse>
}
data class DeleteUserResponse(
    @SerializedName("status") val status: String?
)

data class ChangePasswordResponse(
    @SerializedName("status") val status: String?
)
interface ChangePasswordService {
    @POST("api/change_password.php")
    @FormUrlEncoded
    fun change_password(@FieldMap request: Map<String, String>): Call<ChangePasswordResponse>
}

data class CreateUserResponse(
    @SerializedName("status") val status: String?
)
interface CreateUserService {
    @POST("api/new_user.php")
    @FormUrlEncoded
    fun create_user(@FieldMap request: Map<String, String>): Call<CreateUserResponse>
}
data class BackupResponse(
    @SerializedName("status") val status: String?
)
interface BackupService {
    @POST("api/make_backup.php")
    @FormUrlEncoded
    fun create_backup(@FieldMap request: Map<String, String>): Call<BackupResponse>
}



class Navi:AppCompatActivity() {

    private val url = "https://filesexchange.ru.tuna.am/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navi_champions)
        val button2 = findViewById<ImageView>(R.id.homeik)
        val btncreate = findViewById<Button>(R.id.button_new_user)
        val btnbackup = findViewById<Button>(R.id.button_backup)
        val intent = Intent(this, MainActivity::class.java)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        makeFiles(token_api)
        button2.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            startActivity(intent)
            finish()
        }
        btncreate.setOnClickListener{
            val usernameInput = EditText(this)
            usernameInput.hint = "Имя пользователя"
            val passwordInput = EditText(this)
            passwordInput.hint = "Пароль"
            passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(usernameInput)
            layout.addView(passwordInput)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            usernameInput.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            passwordInput.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layout.layoutParams = layoutParams
            layout.setPadding(60)
            AlertDialog.Builder(this)
                .setTitle("Создать пользователя")
                .setMessage("Введите данные нового пользователя")
                .setView(layout)
                .setPositiveButton("Создать") { dialog, _ ->
                    val username = usernameInput.text.toString()
                    val password = passwordInput.text.toString()
                    createUser(username, password)
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        btnbackup.setOnClickListener{
            makebackup()
        }
        radio0()
    }

    private val resultat = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        makeFiles(token_api)
    }
    private val filesService = retrofit.create(FilesService::class.java)
    private val usersService = retrofit.create(UsersService::class.java)
    private val deleteUserService = retrofit.create(DeleteUserService::class.java)
    private val changePasswordService = retrofit.create(ChangePasswordService::class.java)
    private val createUserService = retrofit.create(CreateUserService::class.java)
    private val backupService = retrofit.create(BackupService::class.java)

    fun click(file_url : String){
        val intent = Intent(this@Navi, Kartochka::class.java)
        intent.putExtra("file_url", file_url)
        resultat.launch(intent)
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
                    spat.layoutManager = LinearLayoutManager(this@Navi)
                    spat.adapter = Popka(qweList, this@Navi, ::click)
                    val find = findViewById<Button>(R.id.button2)
                    find.setOnClickListener({
                        val findTextEdit = findViewById<EditText>(R.id.editTextText2)
                        val text = findTextEdit.text.toString()
                        qweList = qweListAll.filter {
                            it.name.contains(text, ignoreCase = true)
                        }
                        spat.adapter = Popka(qweList, this@Navi, ::click)
                    })
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Не загрузились файлы")
                        .setPositiveButton("OK", null)
                        .show()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Navi)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Navi, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<FilesResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun makeUsers(token_api : String) {
        val request = mapOf("token_api" to token_api)
        usersService.get_users(request).enqueue(object : retrofit2.Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: retrofit2.Response<UsersResponse>) {
                val usersResponse = response.body()
                val users = usersResponse?.users
                val status = usersResponse?.status
                if (status == "success" && users != null) {
                    val pipliListAll = users.map { user ->
                        Pipli(user.username, user.id)
                    }
                    var piplList = pipliListAll
                    // Заполняем адаптер
                    val spat = findViewById<RecyclerView>(R.id.op)
                    spat.layoutManager = LinearLayoutManager(this@Navi)
                    spat.adapter = Obichni(piplList, { userId ->
                        AlertDialog.Builder(this@Navi)
                            .setTitle("Подтверждение удаления")
                            .setMessage("Вы точно хотите удалить этого пользователя?")
                            .setPositiveButton("Да") { _, _ ->
                                deleteUser(userId)
                            }
                            .setNegativeButton("Нет", null)
                            .show()
                    }, { userId ->
                        val input = EditText(this@Navi)
                        input.hint = "Введите пароль"
                        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        val container = LinearLayout(this@Navi)
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        container.layoutParams = layoutParams
                        container.setPadding(60)
                        input.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        container.addView(input)
                        AlertDialog.Builder(this@Navi)
                            .setTitle("Изменить пароль")
                            .setMessage("Введите новый пароль")
                            .setView(container)
                            .setPositiveButton("Сохранить") { dialog, _ ->
                                val newPassword = input.text.toString()
                                changepassword(userId, newPassword)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                dialog.cancel()
                            }
                            .show()
                    })
                    val find = findViewById<Button>(R.id.button3)
                    find.setOnClickListener({
                        val findTextEdit = findViewById<EditText>(R.id.editTextText3)
                        val text = findTextEdit.text.toString()
                        piplList = pipliListAll.filter {
                            it.name.contains(text, ignoreCase = true)
                        }
                        spat.adapter = Obichni(piplList, { userId ->
                            AlertDialog.Builder(this@Navi)
                                .setTitle("Подтверждение удаления")
                                .setMessage("Вы точно хотите удалить этого пользователя?")
                                .setPositiveButton("Да") { _, _ ->
                                    deleteUser(userId)
                                }
                                .setNegativeButton("Нет", null)
                                .show()
                        }, { userId ->
                            val input = EditText(this@Navi)
                            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            input.hint = "Введите пароль"
                            val container = LinearLayout(this@Navi)
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            container.layoutParams = layoutParams
                            container.setPadding(60)
                            input.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            container.addView(input)
                            AlertDialog.Builder(this@Navi)
                                .setTitle("Изменить пароль")
                                .setMessage("Введите новый пароль")
                                .setView(container)
                                .setPositiveButton("Сохранить") { dialog, _ ->
                                    val newPassword = input.text.toString()
                                    changepassword(userId, newPassword)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Отмена") { dialog, _ ->
                                    dialog.cancel()
                                }
                                .show()
                        })
                    })
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Не загрузились Пользователи")
                        .setPositiveButton("OK", null)
                        .show()
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@Navi)
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(this@Navi, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun deleteUser(user_id : Int){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        val request = mapOf("token_api" to token_api, "user_id" to user_id.toString())
        deleteUserService.delete_User(request).enqueue(object : retrofit2.Callback<DeleteUserResponse> {
            override fun onResponse(call: Call<DeleteUserResponse>, response: retrofit2.Response<DeleteUserResponse>) {
                val deleteResponse = response.body()
                if (deleteResponse?.status == "success") {
                    makeUsers(token_api)
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Пользователя не получилось удалить")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            override fun onFailure(call: Call<DeleteUserResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun createUser(username : String, password: String){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        val request = mapOf("token_api" to token_api, "username" to username, "password" to password)
        createUserService.create_user(request).enqueue(object : retrofit2.Callback<CreateUserResponse> {
            override fun onResponse(call: Call<CreateUserResponse>, response: retrofit2.Response<CreateUserResponse>) {
                val createResponse = response.body()
                if (createResponse?.status == "success") {
                    makeUsers(token_api)
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Пользователя не получилось создать")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }


    private fun changepassword(user_id : Int, password : String){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        val request = mapOf("token_api" to token_api, "user_id" to user_id.toString(), "password" to password)
        changePasswordService.change_password(request).enqueue(object : retrofit2.Callback<ChangePasswordResponse> {
            override fun onResponse(call: Call<ChangePasswordResponse>, response: retrofit2.Response<ChangePasswordResponse>) {
                val changePasswordResponse = response.body()
                if (changePasswordResponse?.status == "success") {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Удачно")
                        .setMessage("Пароль изменён")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Пользователя не получилось удалить")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })

    }

    private fun makebackup(){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        val request = mapOf("token_api" to token_api)
        backupService.create_backup(request).enqueue(object : retrofit2.Callback<BackupResponse> {
            override fun onResponse(call: Call<BackupResponse>, response: retrofit2.Response<BackupResponse>) {
                val backupResponse = response.body()
                if (backupResponse?.status == "success") {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Удачно")
                        .setMessage("Резервное копирование закончено")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    AlertDialog.Builder(this@Navi)
                        .setTitle("Ошибка")
                        .setMessage("Пользователя не получилось удалить")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            override fun onFailure(call: Call<BackupResponse>, t: Throwable) {
                AlertDialog.Builder(this@Navi)
                    .setTitle("Ошибка")
                    .setMessage("Ошибка сети")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    fun radio0() {
        val sabaton = findViewById<RadioGroup>(R.id.daily_weekly_button_view)
        val batonchik = findViewById<RadioButton>(R.id.radio0)
        val batonchik1 = findViewById<RadioButton>(R.id.radio1)
        val users = findViewById<ConstraintLayout>(R.id.window_users)
        val files = findViewById<ConstraintLayout>(R.id.window_files)
        val backup = findViewById<Button>(R.id.button_backup)
        val findfiles = findViewById<EditText>(R.id.editTextText2)
        val findusers = findViewById<EditText>(R.id.editTextText3)
        val buttonfiles = findViewById<Button>(R.id.button2)
        val buttonusers = findViewById<Button>(R.id.button3)
        val create = findViewById<Button>(R.id.button_new_user)
        if(batonchik.isChecked){
            files.visibility = View.VISIBLE
            users.visibility = View.INVISIBLE
            findfiles.visibility = View.VISIBLE
            findusers.visibility = View.INVISIBLE
            buttonfiles.visibility = View.VISIBLE
            buttonusers.visibility = View.INVISIBLE
            backup.visibility = View.VISIBLE
            create.visibility = View.INVISIBLE
        }
        sabaton.setOnCheckedChangeListener { group, checkedId ->
            if(batonchik.isChecked){
                files.visibility = View.VISIBLE
                users.visibility = View.INVISIBLE
                findfiles.visibility = View.VISIBLE
                findusers.visibility = View.INVISIBLE
                buttonfiles.visibility = View.VISIBLE
                buttonusers.visibility = View.INVISIBLE
                backup.visibility = View.VISIBLE
                create.visibility = View.INVISIBLE
            } else {
                files.visibility = View.INVISIBLE
                users.visibility = View.VISIBLE
                findfiles.visibility = View.INVISIBLE
                findusers.visibility = View.VISIBLE
                buttonfiles.visibility = View.INVISIBLE
                buttonusers.visibility = View.VISIBLE
                backup.visibility = View.INVISIBLE
                create.visibility = View.VISIBLE
            }}
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val token_api = sharedPreferences.getString("token_api", null).toString()
        makeUsers(token_api)
    }
}