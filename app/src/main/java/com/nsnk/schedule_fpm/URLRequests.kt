package com.nsnk.schedule_fpm

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.nsnk.schedule_fpm.activities.PickerActivity
import com.nsnk.schedule_fpm.interfaces.GetLessonsInterface
import com.nsnk.schedule_fpm.interfaces.OnRegisterListener
import com.nsnk.schedule_fpm.model.*
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
    private const val REQUEST_TIMEOUT_ERROR = "Ошибка времени жизни запроса"
    private const val TAG = "URLRequests"
    private val mDispatcher = Dispatcher().apply { maxRequests = 1 }
    private val client = OkHttpClient.Builder().dispatcher(mDispatcher).build()
    private var isInRegisterProcess: Boolean = false
    private var lastOnRegisterListener: OnRegisterListener? = null


    fun getLessonsJson(
        context: Context,
        getLessonsInterface: GetLessonsInterface,
        isUpdate: Boolean = false, counter: Int = 0
    ) {
        fun hideLoading(context: Context) {
            if (isUpdate && context is PickerActivity) {
                context.hideLoading()
            }
        }
        showLog("getLessonsJSON $counter", context)
        if (counter > 2) {
            showLog("Проблемы с обращением к серверу, попробуйте позже", context)
            return
        }
        val mEncryptedSharedPreferences = getEncryptedPreferences(context)
        val version = PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.version), 0).toString()
        val token = mEncryptedSharedPreferences.getString("token", "") ?: ""
        val deviceId = mEncryptedSharedPreferences.getString("deviceId", "") ?: ""
        if (token.isBlank() || deviceId.isBlank()) {
            showLog(context.getString(R.string.register_api_error), context)
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
            .url(URL("https://tt.fktpm.kubsu.ru/api/timetable/getContent.php"))
            .post(postBody)
            .build()
        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    hideLoading(context)
                    showLog(context.getString(R.string.connection_problem) + e.message, context)
                }

                override fun onResponse(call: Call, response: Response) {
                    val lessonJsonStructure: LessonJsonStructure
                    try {
                        lessonJsonStructure = GsonBuilder().create()
                            .fromJson(response.body?.string(), LessonJsonStructure::class.java)
                    } catch (error: JsonSyntaxException) {
                        showLog(context.getString(R.string.api_parse_error), context)
                        hideLoading(context)
                        return
                    }
                    if (lessonJsonStructure.error != null) {
                        showLog("ошибка: ${lessonJsonStructure.error!!}", context)
                        hideLoading(context)
                        when (lessonJsonStructure.error) {
                            PUBLIC_KEY_ERROR, REGISTRATION_DEVICE_ERROR, ALREADY_REGISTERED_ERROR ->
                                getPublicKey(
                                    context,
                                    getLessonOnRegisterListener(
                                        context,
                                        getLessonsInterface,
                                        isUpdate
                                    )
                                )
                            REQUEST_TIMEOUT_ERROR -> Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    getLessonsJson(
                                        context,
                                        getLessonsInterface,
                                        isUpdate,
                                        counter + 1
                                    )
                                },
                                2000
                            )
                            else -> showLog(
                                "${context.getString(R.string.unknown_api_error)} ${lessonJsonStructure.error}",
                                context
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

    private fun register(context: Context, listener: OnRegisterListener?, counter: Int) {
        showLog("register $counter", context)
        if (counter > 2)
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
            .url(URL("https://tt.fktpm.kubsu.ru/api/timetable/deviceRegistration.php"))
            .post(postBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showLog(context.getString(R.string.connection_problem) + e.message, context)
                isInRegisterProcess = false
                lastOnRegisterListener?.onRegister()
            }

            override fun onResponse(call: Call, response: Response) {
                val tokenClass: TokenClass
                try {
                    tokenClass =
                        GsonBuilder().create()
                            .fromJson(response.body?.string(), TokenClass::class.java)
                } catch (error: JsonSyntaxException) {
                    showLog(context.getString(R.string.api_parse_error), context)
                    isInRegisterProcess = false
                    return
                }
                if (tokenClass.error != null) {
                    showLog(tokenClass.error, context)
                    when (tokenClass.error) {
                        REGISTRATION_DEVICE_ERROR, ALREADY_REGISTERED_ERROR -> Handler().postDelayed(
                            {
                                register(context, listener, counter + 1)
                            },
                            2000
                        )
                        PUBLIC_KEY_ERROR -> getPublicKey(context, listener)
                        else -> showLog(
                            context.getString(R.string.unknown_api_error),
                            context
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
                isInRegisterProcess = false
                listener?.onRegister()
                lastOnRegisterListener?.onRegister()
            }
        })
    }

    fun getPublicKey(context: Context, listener: OnRegisterListener?) {
        if (isInRegisterProcess) {
            lastOnRegisterListener = listener
            return
        }
        isInRegisterProcess = true
        showLog("getPublicKey", context)
        val request = Request.Builder()
            .url(URL("https://tt.fktpm.kubsu.ru/api/timetable/getAPIOpenKey.php"))
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showLog(context.getString(R.string.connection_problem), context)
                isInRegisterProcess = false
            }

            override fun onResponse(call: Call, response: Response) {
                val publicKeyModel: PublicKeyModel
                try {
                    publicKeyModel = GsonBuilder().create()
                        .fromJson(response.body?.string(), PublicKeyModel::class.java)
                } catch (error: JsonSyntaxException) {
                    showLog(context.getString(R.string.api_parse_error), context)
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

    private fun showLog(message: String, context: Context) {
//        Log.d(TAG, message)
//        Handler(Looper.getMainLooper()).post {
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//        }
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

}