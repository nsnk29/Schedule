package com.example.schedule

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.schedule.activities.MainActivity
import com.example.schedule.activities.PickerActivity
import com.example.schedule.activities.SettingsActivity
import com.example.schedule.fragments.UpdateDialogFragment
import com.example.schedule.interfaces.GetLessonsInterface
import com.example.schedule.interfaces.OnRegisterListener
import com.example.schedule.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.settings_activity.*
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


object URLRequests {

    private const val REGISTRATION_DEVICE_ERROR = "Ошибка регистрации устройства"
    private const val ALREADY_REGISTERED_ERROR = "Ваше устройство уже зарегистрировано"
    private const val PUBLIC_KEY_ERROR = "Ошибка открытого ключа"
    private const val NEW_VERSION_STATUS = "Установлена последняя версия приложения"
    private const val TAG = "URLRequests"

    var lastDownloadController: DownloadController? = null

    fun getLessonsJson(
        context: Context,
        getLessonsInterface: GetLessonsInterface,
        isUpdate: Boolean = false
    ) {
        fun hideLoading(context: Context) {
            if (isUpdate && context is PickerActivity) {
                context.hideLoading()
            }
        }
        showToast(context, "getLessonsJSON")
        val mEncryptedSharedPreferences = getEncryptedPreferences(context)
        val version = PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.version), 0).toString()
        val token = mEncryptedSharedPreferences.getString("token", "") ?: ""
        val deviceId = mEncryptedSharedPreferences.getString("deviceId", "") ?: ""
        if (token.isBlank() || deviceId.isBlank()) {
            showToast(context, context.getString(R.string.register_api_error))
            getPublicKey(
                context,
                getLessonOnRegisterListener(context, getLessonsInterface, isUpdate)
            )
            return
        }

        val timestamp = System.currentTimeMillis() / 1000L
        val message = GsonBuilder().disableHtmlEscaping().create()
            .toJson(GetLessonJsonPost(version.toLong(), token, deviceId, timestamp))
            .toByteArray()
        val postBody = FormBody.Builder()
            .add("data", encryptRsa(message, getRsaKey(context)))
            .build()
        val request = Request.Builder()
            .url(URL(context.getString(R.string.URL_LESSONS_JSON_TOKEN)))
            .post(postBody)
            .build()

        OkHttpClient().newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    hideLoading(context)
                    showToast(context, context.getString(R.string.connection_problem))
                }

                override fun onResponse(call: Call, response: Response) {
                    val lessonJsonStructure: LessonJsonStructure
                    try {
                        lessonJsonStructure = GsonBuilder().create()
                            .fromJson(response.body?.string(), LessonJsonStructure::class.java)
                    } catch (error: JsonSyntaxException) {
                        showToast(context, context.getString(R.string.api_parse_error))
                        hideLoading(context)
                        return
                    }
                    if (lessonJsonStructure.error != null) {
                        showToast(context, lessonJsonStructure.error!!)
                        hideLoading(context)
                        when (lessonJsonStructure.error) {
                            PUBLIC_KEY_ERROR, REGISTRATION_DEVICE_ERROR, ALREADY_REGISTERED_ERROR -> getPublicKey(
                                context,
                                getLessonOnRegisterListener(context, getLessonsInterface, isUpdate)
                            )
                            else -> showToast(
                                context,
                                context.getString(R.string.unknown_api_error)
                            )
                        }
                        return
                    }

                    if (lessonJsonStructure.pairs.isNotEmpty())
                        getLessonsInterface.onLessonsReady(lessonJsonStructure)
                    hideLoading(context)
                }
            })
    }

    fun checkUpdate(context: Context) {
        showToast(context, "checkUpdate")
        val mEncryptedSharedPreferences = getEncryptedPreferences(context)
        val token = mEncryptedSharedPreferences.getString("token", "") ?: ""
        val deviceId = mEncryptedSharedPreferences.getString("deviceId", "") ?: ""
        if (token.isBlank() || deviceId.isBlank()) {
            showToast(context, context.getString(R.string.register_api_error))
            getPublicKey(context, checkUpdateRegisterListener(context))
            return
        }
        val timestamp = System.currentTimeMillis() / 1000L
        val message: ByteArray = GsonBuilder().disableHtmlEscaping().create().toJson(
            GetLessonJsonPost(
                BuildConfig.VERSION_CODE.toLong(),
                token,
                deviceId,
                timestamp
            )
        )
            .toByteArray()
        val postBody = FormBody.Builder()
            .add("data", encryptRsa(message, getRsaKey(context)))
            .build()
        val request = Request.Builder()
            .url(URL("https://makson-dev.ru/api/getAndroidVersion.php"))
            .post(postBody)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (context is SettingsActivity)
                    context.runOnUiThread {
                        Snackbar.make(
                            context.mainLayout,
                            R.string.connection_problem,
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.try_again) {
                            checkUpdate(context)
                        }.show()
                    }
            }

            override fun onResponse(call: Call, response: Response) {
                val updateJsonScheme: UpdateJsonScheme
                try {
                    updateJsonScheme = GsonBuilder().create()
                        .fromJson(response.body?.string(), UpdateJsonScheme::class.java)
                } catch (error: JsonSyntaxException) {
                    showToast(context, context.getString(R.string.api_parse_error))
                    return
                }
                with(updateJsonScheme) {
                    if (error != null || link == null || size == null || changelog == null) {
                        if (error == null) {
                            showToast(context, context.getString(R.string.api_parse_error))
                            return
                        }
                        when (error) {
                            NEW_VERSION_STATUS -> {
                                if (context is SettingsActivity)
                                    context.runOnUiThread {
                                        Snackbar.make(
                                            context.mainLayout,
                                            updateJsonScheme.error!!,
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                            }
                            PUBLIC_KEY_ERROR, REGISTRATION_DEVICE_ERROR, ALREADY_REGISTERED_ERROR -> getPublicKey(
                                context,
                                checkUpdateRegisterListener(context)
                            )
                            else -> {
                                showToast(
                                    context,
                                    context.getString(R.string.unknown_api_error)
                                )
                            }
                        }
                        return
                    }
                }
                val activity: AppCompatActivity =
                    (if (context is MainActivity) context else if (context is SettingsActivity) context else null)
                        ?: return
                lastDownloadController = DownloadController(activity, updateJsonScheme.link!!)
                Handler(Looper.getMainLooper()).post {
                    UpdateDialogFragment.newInstance(
                        updateJsonScheme.size!!,
                        updateJsonScheme.changelog!!
                    ).show(
                        activity.supportFragmentManager,
                        "update_dialog"
                    )
                }
            }
        })
    }

    private fun register(context: Context, listener: OnRegisterListener?, counter: Int) {
        showToast(context, "register $counter")
        if (counter > 4)
            return
        val mEncryptedSharedPreferences = getEncryptedPreferences(context)
        val aesKey = generateAesKey()
        val aesKeyString = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
        val aesCipherIv = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            .apply { init(Cipher.ENCRYPT_MODE, aesKey) }.iv
        val ivBase64 = Base64.encodeToString(aesCipherIv, Base64.DEFAULT)
        val deviceId = UUID.randomUUID().toString()
        mEncryptedSharedPreferences.edit()
            .putString("deviceId", deviceId)
            .apply()
        val message: ByteArray = GsonBuilder().disableHtmlEscaping().create()
            .toJson(RegisterJsonModel(deviceId, aesKeyString, ivBase64)).toByteArray()
        val postBody = FormBody.Builder()
            .add("data", encryptRsa(message, getRsaKey(context)))
            .build()
        val request = Request.Builder()
            .url(URL("https://makson-dev.ru/api/deviceRegistration.php"))
            .post(postBody)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast(context, context.getString(R.string.connection_problem))
            }

            override fun onResponse(call: Call, response: Response) {
                val tokenClass: TokenClass
                try {
                    tokenClass =
                        GsonBuilder().create()
                            .fromJson(response.body?.string(), TokenClass::class.java)
                } catch (error: JsonSyntaxException) {
                    showToast(context, context.getString(R.string.api_parse_error))
                    return
                }
                if (tokenClass.error != null) {
                    showToast(context, tokenClass.error)
                    when (tokenClass.error) {
                        REGISTRATION_DEVICE_ERROR, ALREADY_REGISTERED_ERROR -> Handler().postDelayed(
                            {
                                register(context, listener, counter + 1)
                            },
                            2000
                        )
                        PUBLIC_KEY_ERROR -> getPublicKey(context, listener)
                        else -> showToast(
                            context,
                            context.getString(R.string.unknown_api_error)
                        )
                    }
                    return
                }
                val aesDecrypt = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
                    init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(aesCipherIv))
                }
                mEncryptedSharedPreferences
                    .edit()
                    .putString(
                        "token",
                        String(aesDecrypt.doFinal(Base64.decode(tokenClass.token, Base64.DEFAULT)))
                    )
                    .apply()
                listener?.onRegister()
            }
        })
    }

    fun getPublicKey(context: Context, listener: OnRegisterListener?) {
        showToast(context, "getPublicKey")
        val request = Request.Builder()
            .url(URL("https://makson-dev.ru/api/getAPIOpenKey.php"))
            .get()
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast(context, context.getString(R.string.connection_problem))
            }

            override fun onResponse(call: Call, response: Response) {
                val publicKeyModel: PublicKeyModel
                try {
                    publicKeyModel = GsonBuilder().create()
                        .fromJson(response.body?.string(), PublicKeyModel::class.java)
                } catch (error: JsonSyntaxException) {
                    showToast(context, context.getString(R.string.api_parse_error))
                    return
                }
                val key = publicKeyModel.publicKey.replace("\n", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
                mPreference.edit().apply {
                    putString("RSA", key)
                    apply()
                }
                register(context, listener, 0)
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        Log.d(TAG, message)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getEncryptedPreferences(context: Context): EncryptedSharedPreferences =
        EncryptedSharedPreferences.create(
            "encrypted_preferences",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences

    private fun getLessonOnRegisterListener(
        context: Context,
        getLessonsInterface: GetLessonsInterface,
        isUpdate: Boolean
    ): OnRegisterListener = object : OnRegisterListener {
        override fun onRegister() {
            getLessonsJson(context, getLessonsInterface, isUpdate)
        }
    }

    private fun checkUpdateRegisterListener(
        context: Context
    ): OnRegisterListener = object : OnRegisterListener {
        override fun onRegister() {
            checkUpdate(context)
        }
    }
}