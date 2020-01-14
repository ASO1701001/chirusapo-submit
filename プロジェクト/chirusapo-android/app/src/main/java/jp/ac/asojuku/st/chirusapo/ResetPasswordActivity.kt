package jp.ac.asojuku.st.chirusapo

import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_reset_password.*
import java.util.regex.Pattern

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        realm = Realm.getDefaultInstance()

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()

        PasswordReset_Button.setOnClickListener { onPasswordReset() }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onPasswordCheck():Boolean{
        val userPass = old_password.editText?.text.toString().trim()
        return when {
            userPass.isEmpty() -> {
                old_password.error = "パスワードが未入力です"
                false
            }
            userPass.count() < 2 -> {
                old_password.error = "パスワードの文字数が不正です"
                false
            }
            userPass.count() > 30 -> {
                old_password.error = "パスワードの文字数が不正です"
                false
            }
            !Pattern.compile("^[a-zA-Z0-9-_]*\$").matcher(userPass).find() -> {
                old_password.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                old_password.error = null
                true
            }
        }
    }

    private fun onNewPasswordCheck():Boolean{
        val userNewPass = new_password.editText?.text.toString().trim()
        return when {
            userNewPass.isEmpty() -> {
                new_password.error = "新しいパスワードが未入力です"
                false
            }
            userNewPass.count() < 2 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
                false
            }
            userNewPass.count() > 30 -> {
                new_password.error = "新しいパスワードの文字数が不正です"
                false
            }
            !Pattern.compile("^[a-zA-Z0-9-_]*\$").matcher(userNewPass).find() -> {
                new_password.error = "使用できない文字が含まれています"
                false
            }
            else -> {
                new_password.error = null
                true
            }
        }
    }

    private fun onPasswordReset(){
        var check = true
        if(!onPasswordCheck())check = false
        if(!onNewPasswordCheck())check = false

        if(!check) return

        val account = realm.where<Account>().findFirst()
        val token = account?.Rtoken.toString()
        val newPassword = new_password.editText?.text.toString()
        val oldPassword = old_password.editText?.text.toString()

        ApiPostTask{
            if(it == null){
                Toast.makeText(applicationContext, "APIとの通信に失敗しました", Toast.LENGTH_SHORT).show()
            }
            else {
                when(it.getString("status")){
                    "200" -> {
                        finish()
                    }
                    "400" -> {
                        val errorArray = it.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.VALIDATION_OLD_PASSWORD -> {
                                    ApiError.showEditTextError(old_password,errorArray.getString(i))
                                }
                                ApiError.VALIDATION_NEW_PASSWORD -> {
                                    ApiError.showEditTextError(new_password,errorArray.getString(i))
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                                ApiError.VERIFY_PASSWORD_FAILED -> {
                                    ApiError.showToast(this,errorArray.getString(i),LENGTH_LONG)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "account/password-change",
                hashMapOf("old_password" to oldPassword,"new_password" to newPassword,"token" to token)
            )
        )
    }
}