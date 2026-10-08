package com.example.kitchenpalapp.network

import android.util.Log
import com.example.kitchenpalapp.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okio.Buffer

/**
 * 登录态持有者：AppPreferences 在保存/清除 token 时同步更新此处，
 * auth 拦截器据此为请求自动附加 Authorization 头，使「已登录」真正生效。
 */
object TokenHolder {
    @Volatile
    var token: String = ""
}

/**
 * 服务器地址持有者：支持运行时切换，无需重新编译。
 *
 * 优先级：用户在「设置 → 服务器地址」里手填的值 > 编译期注入的 BuildConfig.API_BASE_URL。
 * 用户清空时回退到编译期默认值；地址一定保证以 `/` 结尾，否则 Retrofit 会丢掉路径段。
 */
object BaseUrlHolder {
    /** 编译期默认地址（local.properties 的 api.baseUrl，模拟器默认为 10.0.2.2） */
    val DEFAULT: String = BuildConfig.API_BASE_URL

    @Volatile
    var current: String = DEFAULT
        private set

    /** 当前是否用了自定义地址（用于设置页展示） */
    val isCustom: Boolean get() = current != DEFAULT

    fun set(url: String?) {
        val cleaned = url?.trim().orEmpty()
        current = if (cleaned.isBlank()) DEFAULT else normalize(cleaned)
    }

    fun reset() {
        current = DEFAULT
    }

    /** 补全协议头与结尾斜杠，避免用户少写 `http://` 或末尾漏 `/` */
    fun normalize(raw: String): String {
        var u = raw.trim()
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://$u"
        if (!u.endsWith("/")) u = "$u/"
        return u
    }
}

/**
 * 选择性 HTTP 日志拦截器（debug 专用）。
 *
 * 规则：
 * - 请求体：multipart / 二进制 / >256KB 一律只打「大小」摘要，不打印内容；
 * - 响应体：只有 JSON / XML / 文本 且 ≤512KB 才打印正文，并截断到 1500 字符；
 * - 其余（图片等二进制）只打 code / method / 耗时 / 字节数 / content-type；
 * - 不打任何请求头，避免 Authorization token 明文进 logcat。
 *
 * 这样识图（multipart 图片上传）时日志仍然是干净的一行摘要，而不是刷屏乱码。
 */
private object SelectiveHttpLogInterceptor : Interceptor {

    private const val TAG = "HttpLog"
    private const val MAX_BODY_CHARS = 1500
    private const val MAX_PRINT_BYTES = 512 * 1024
    private const val MAX_REQUEST_PRINT_BYTES = 256 * 1024

    /** 认为是「文本响应」的 Content-Type 前缀 */
    private val TEXT_TYPE_PREFIXES = arrayOf(
        "application/json",
        "application/problem+json",
        "application/xml",
        "application/javascript",
        "text/"
    )

    /** 请求体属于二进制 / 大块，只打摘要 */
    private fun isBinaryRequest(contentType: String?, size: Long): Boolean {
        val t = contentType.orEmpty()
        if (t.isBlank()) return size > MAX_REQUEST_PRINT_BYTES
        if (t.contains("multipart/form-data") || t.contains("application/octet-stream")) return true
        if (t.startsWith("image/") || t.startsWith("video/") || t.startsWith("audio/")) return true
        return size > MAX_REQUEST_PRINT_BYTES
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val contentType = request.body?.contentType()?.toString()
        val size = request.body?.contentLength() ?: -1
        val byteInfo = if (size >= 0) "$size B" else "unknown"

        if (isBinaryRequest(contentType, size)) {
            Log.d(TAG, "--> ${request.method} ${request.url.encodedPath} <$byteInfo, $contentType, 二进制已跳过>")
        }

        val start = System.nanoTime()
        val response = try {
            chain.proceed(request)
        } catch (e: java.io.IOException) {
            Log.w(TAG, "网络异常 ${request.method} ${request.url.encodedPath}: ${e.javaClass.simpleName} ${e.message}")
            throw e
        }
        val costMs = (System.nanoTime() - start) / 1_000_000

        // okhttp4 里 Response.body 是可空的（204 / HEAD 等场景为 null）
        val body = response.body ?: return response
        val bodySize = body.contentLength()
        val respType = body.contentType()?.toString().orEmpty()
        val isText = TEXT_TYPE_PREFIXES.any { respType.startsWith(it) }
        val head = "<-- ${response.code} ${request.method} ${request.url.encodedPath} (${costMs}ms, " +
            (if (bodySize >= 0) "$bodySize B" else "chunked") + ")"

        if (!isText || bodySize > MAX_PRINT_BYTES) {
            Log.d(TAG, "$head :: <二进制/大响应，contentType=$respType，未打印正文>")
            return response
        }

        val buffer = Buffer()
        val data = try {
            // Buffer 同时是 Source/Sink，readAll(Source) 会解析到 BufferedSink.readAll(Sink)，必须用 writeAll
            buffer.writeAll(body.source())
            buffer.readByteString()
        } catch (e: Exception) {
            Log.w(TAG, "$head :: 响应体读取失败 ${e.javaClass.simpleName} ${e.message}")
            return response
        }

        if (data.size > 0) {
            val text = data.string(Charsets.UTF_8)
            val snippet = if (text.length > MAX_BODY_CHARS) {
                text.take(MAX_BODY_CHARS) + "\n…(共 ${text.length} 字符，已截断)"
            } else {
                text
            }
            Log.d(TAG, "$head\n$snippet")
            // 重新包一层 Body，否则上面的读取会把 original body 消费掉，Retrofit 拿到空响应
            return response.newBuilder()
                .body(data.toResponseBody(body.contentType()))
                .build()
        }
        return response
    }
}

object RetrofitClient {

    // BASE_URL 不再固定：实际请求地址由 BaseUrlHolder 在拦截器里动态改写，
    // 这样用户在设置页改地址后立即生效，无需重建 Retrofit。
    val BASE_URL: String get() = BaseUrlHolder.current

    /**
     * 仅在 debug 构建下挂日志拦截器；release 完全不打日志。
     *
     * 注意：不要用 okhttp3.logging.HttpLoggingInterceptor 的 BODY 级别 —— 它会把
     * 请求体/响应体原样写进 logcat，而识图走的是 `multipart/form-data` 图片流，
     * 一发照片就刷出上万字符的十六进制乱码（tag 为 okhttp.OkHttpClient）。
     * 这里改成选择性打印：文本（JSON/XML）才打正文并截断，二进制只给一行摘要。
     */
    private val loggingInterceptor: Interceptor? = if (BuildConfig.DEBUG) {
        SelectiveHttpLogInterceptor
    } else {
        null
    }

    /** 自动附加登录 token，让后端可在未来开启接口鉴权时直接生效 */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = TokenHolder.token
        val request = if (token.isNotBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    /**
     * 动态改写请求目标地址：Retrofit 的 baseUrl 在构建时固定，
     * 这里把每个请求的 scheme/host/port 换成 BaseUrlHolder 里的当前值，
     * 从而实现「改设置 → 立即生效」。
     */
    private val hostRewriteInterceptor = Interceptor { chain ->
        val original = chain.request()
        val target = BaseUrlHolder.current.toHttpUrlOrNull() ?: return@Interceptor chain.proceed(original)
        val newUrl = original.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()
        chain.proceed(original.newBuilder().url(newUrl).build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        // AI 识图 / 助手对话 / 食谱生成走本地大模型（CPU 推理），单次识图约 30~90s，故放宽到 180s
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(hostRewriteInterceptor)
        .addInterceptor(authInterceptor)
        .apply { loggingInterceptor?.let { addInterceptor(it) } }
        .build()

    /** baseUrl 用一个合法占位值即可，真实地址每次请求由 hostRewriteInterceptor 改写 */
    private const val PLACEHOLDER_BASE = "http://localhost:18080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
