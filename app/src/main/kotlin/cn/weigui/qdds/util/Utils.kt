package cn.weigui.qdds.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.forEach
import cn.weigui.qdds.BuildConfig
import cn.weigui.qdds.ui.MainActivity
import cn.weigui.qdds.util.Option.parseNeedShieldList
import cn.weigui.qdds.util.Option.updateOptionEntity
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XposedHelpers
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FilenameFilter
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Serializable
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

val kJson = Json {
    isLenient = true
//    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

object Path {
    lateinit var MY_BASE_URL: String

    lateinit var HEADERS: Map<String, String>

    const val CHECK_RISK = "/argus/api/v1/common/risk/check"
    const val WELFARE_CENTER = "/argus/api/v1/video/adv/mainPage"
    const val WELFARE_REWARD = "/argus/api/v1/video/adv/finishWatch"
    const val RECEIVE_WELFARE_REWARD = "/argus/api/v1/video/adv/receiveTaskReward"
    const val CHECK_IN_DETAIL = "/argus/api/v2/checkin/detail"
    const val AUTO_CHECK_IN = "/argus/api/v1/checkin/autocheckin"
    const val NORMAL_CHECK_IN = "/argus/api/v2/checkin/checkin"
    const val LOTTERY_CHANCE = "/argus/api/v2/video/callback"
    const val LOTTERY = "/argus/api/v2/checkin/lottery"
    const val EXCHANGE_CHAPTER_CARD = "/argus/api/v1/readtime/scoremall/checkinrewardpage"
    const val BUY_CHAPTER_CARD = "/argus/api/v1/readtime/scoremall/buygood"
    const val GAME_TIME = "/home/log/heartbeat"
    const val CARD_CALL_PAGE = "/argus/api/v2/bookrole/card/callpage"
    const val CARD_CALL = "/argus/api/v2/bookrole/card/call"
    const val MASCOT_TASK_LIST = "/argus/api/v1/mascot/gettasklist"
    const val MASCOT_CLOCK_IN = "/argus/api/v1/mascot/clockin"
    const val MASCOT_TASK_REWARD = "/argus/api/v1/mascot/getreward"

    const val COLLECT_URL = "https://rank.xihan.website/"
}

/**
 * 时间戳转为时间
 */
fun Long.toTime(): String = runCatching {
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(this))
}.getOrElse { "暂无时间" }

/**
 * 传入一个延迟秒数，等待指定时间，并执行指定操作
 * @param time Int
 * @param block suspend CoroutineScope.() -> Unit
 * @return Unit
 * @since 7.9.354-1336 ~ 1499
 */
suspend fun wait(time: Int, block: suspend () -> Unit) {
    if (time != 0) {
        kotlinx.coroutines.delay(time.seconds)
    }
    block()
}

/**
 * 判断今天是不是周日
 */
fun isSunday(): Boolean = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

/**
 * 记住可变状态
 * @param [value] 价值
 * @return [MutableState<T>]
 * @suppress Generate Documentation
 */
@Composable
fun <T> rememberMutableStateOf(value: T): MutableState<T> = remember { mutableStateOf(value) }

/**
 * 记住可保存可变状态
 * @param [value] 价值
 * @return [MutableState<T>]
 * @suppress Generate Documentation
 */
@Composable
fun <T> rememberSavableMutableStateOf(value: T): MutableState<T> =
    rememberSaveable { mutableStateOf(value) }

/**
 * 记住可变交互源
 * @since 7.9.354-1336
 * @suppress Generate Documentation
 */
@Composable
fun rememberMutableInteractionSource() = remember { MutableInteractionSource() }

/**
 * 记住可变状态列表
 * @return [MutableList<T>]
 * @suppress Generate Documentation
 */
@Composable
fun <T> rememberMutableStateListOf() = remember { mutableStateListOf<T>() }

/**
 * 记住可变状态列表
 * @param [value] 价值
 * @return [MutableList<T>]
 * @suppress Generate Documentation
 */
@Composable
inline fun <reified T> rememberMutableStateListOf(value: Collection<T>) =
    remember { mutableStateListOf(*value.toTypedArray()) }

/**
 * 获取方位无线电
 * @since 7.9.354-1336
 * @return [Float]
 * @suppress Generate Documentation
 */
@Composable
fun getAspectRadio(): Float {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.screenHeightDp.toFloat() / configuration.screenWidthDp.toFloat()
    }
}

/**
 * 是平板电脑
 * @since 7.9.354-1336
 * @return [Boolean]
 * @suppress Generate Documentation
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        configuration.screenHeightDp > 600
    } else {
        configuration.screenWidthDp > 600
    }
}

/**
 * 打印错误日志
 * @suppress Generate Documentation
 */
fun String.loge() {
    if (BuildConfig.DEBUG) {
        YLog.error(msg = this, tag = YLog.Configs.tag)
    }
}

/**
 * 打印错误日志
 * @suppress Generate Documentation
 */
fun Throwable.loge() {
    if (BuildConfig.DEBUG) {
        YLog.error(msg = this.message ?: "未知错误", tag = YLog.Configs.tag)
    }
}

/**
 * 安全转换
 * @see [T?]
 * @return [T?]
 * @suppress Generate Documentation
 */
inline fun <reified T> Any?.safeCast(): T? = this as? T

/**
 * 按 ID 查找视图
 * @param [id] 编号
 * @see [T?]
 * @return [T?]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.findViewById(id: Int): T? {
    return XposedHelpers.callMethod(this, "findViewById", id).safeCast<T>()
}

/**
 * 获取视图
 * @param [name] 名称
 * @param [isSuperClass] 是超一流
 * @return [T?]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getView(name: String, isSuperClass: Boolean = false): T? =
    getParam<T>(name, isSuperClass)

/**
 * 获取视图
 * @param [pairs] 对
 * @return [List<View>]
 * @suppress Generate Documentation
 */
fun Any.getViews(vararg pairs: Pair<String, Boolean> = arrayOf("name" to false)): List<View> =
    if (pairs.isEmpty()) emptyList()
    else pairs.mapNotNull { (name, isSuperClass) -> getParam<View>(name, isSuperClass) }

/**
 * 获取视图
 * @param [isSuperClass] 是超一流
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getViews(isSuperClass: Boolean = false) =
    getParamList<T>(isSuperClass)

/**
 * 获取视图
 * @param [type] 类型
 * @param [isSuperClass] 是超一流
 * @return [ArrayList<Any>]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
fun Any.getViews(type: Class<*>, isSuperClass: Boolean = false): ArrayList<Any> {
    // 使用缓存来存储已经反射过的类信息
    val classCache = mutableMapOf<Class<*>, List<Field>>()

    fun getDeclaredFields(clazz: Class<*>): List<Field> {
        return classCache.getOrPut(clazz) { clazz.declaredFields.toList() }
    }

    val results = ArrayList<Any>()
    val classes = if (isSuperClass) generateSequence(javaClass) { it.superclass }.toList() else listOf(javaClass)

    for (clazz in classes) {
        val fields = getDeclaredFields(clazz)
        for (field in fields) {
            // 如果类型匹配，则添加到结果列表
            if (type.isAssignableFrom(field.type)) {
                field.isAccessible = true
                val value = field.get(this)
                // 直接使用add方法，避免创建新的ArrayList
                value?.let { results.add(it) }
            }
        }
    }
    return results
}

/**
 * 获取参数
 * @param [name] 名称
 * @param [isSuperClass] 是超一流
 * @return [T?]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> Any.getParam(name: String, isSuperClass: Boolean = false): T? {
    val queue = ArrayDeque<Class<*>>()
    var clazz: Class<*>? = if (isSuperClass) javaClass.superclass else javaClass
    while (clazz != null) {
        queue.add(clazz)
        clazz = clazz.superclass
    }
    while (queue.isNotEmpty()) {
        val currentClass = queue.removeFirst()
        try {
            val field = currentClass.getDeclaredField(name).apply { isAccessible = true }
            return field[this].safeCast<T>()
        } catch (_: NoSuchFieldException) {
            // Ignore and continue searching
        }
    }
    return null
}

/**
 * 获取参数列表
 * @param [isSuperClass] 是超一流
 * @return [ArrayList<T>]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> Any.getParamList(isSuperClass: Boolean = false): ArrayList<T> {
    val results = ArrayList<T>()
    val classes =
        if (isSuperClass) generateSequence(javaClass) { it.superclass }.toList() else listOf(
            javaClass
        )
    val type = T::class.java
    for (clazz in classes) {
        clazz.declaredFields.filter { type.isAssignableFrom(it.type) }.forEach { field ->
            field.isAccessible = true
            val value = field.get(this)
            if (type.isInstance(value)) {
                results += value as T
            }
        }
    }
    return results
}

/**
 * 设置可见性（如果不相等）
 * @param [status] 地位
 * @suppress Generate Documentation
 */
fun View.setVisibilityIfNotEqual(status: Int = View.GONE) {
    this.takeIf { it.visibility != status }?.also { visibility = status }
}

/**
 * 为子控件设置可见性
 * @param [visibility] 能见度
 * @suppress Generate Documentation
 */
fun View.setVisibilityWithChildren(visibility: Int = View.GONE) {
    setVisibilityIfNotEqual(visibility)
    if (this is ViewGroup) {
        this.forEach { child ->
            child.setVisibilityWithChildren(visibility)
        }
    }
}

/**
 * 设置参数
 * @param [name] 名字
 * @param [value] 值
 * @suppress Generate Documentation
 */
fun Any.setParam(name: String, value: Any?) {
    when (value) {
        is Int -> XposedHelpers.setIntField(this, name, value)
        is Boolean -> XposedHelpers.setBooleanField(this, name, value)
        is String -> XposedHelpers.setObjectField(this, name, value)
        is Long -> XposedHelpers.setLongField(this, name, value)
        is Float -> XposedHelpers.setFloatField(this, name, value)
        is Double -> XposedHelpers.setDoubleField(this, name, value)
        is Short -> XposedHelpers.setShortField(this, name, value)
        is Byte -> XposedHelpers.setByteField(this, name, value)
        is Char -> XposedHelpers.setCharField(this, name, value)
        else -> XposedHelpers.setObjectField(this, name, value)
    }
}

/**
 * 设置参数
 * @param [params] 参数
 * @suppress Generate Documentation
 */
fun Any.setParams(vararg params: Pair<String, Any?>) {
    params.forEach {
        setParam(it.first, it.second)
    }
}

/**
 * 获取系统上下文
 * @see [Context]
 * @return [Context]
 * @suppress Generate Documentation
 */
fun getSystemContext(): Context {
    val activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null)
    val activityThread =
        XposedHelpers.callStaticMethod(activityThreadClass, "currentActivityThread")
    val context = XposedHelpers.callMethod(activityThread, "getSystemContext").safeCast<Context>()
    return context ?: throw Error("Failed to get system context.")
}

/**
 * 重新启动应用程序
 * @suppress Generate Documentation
 */
fun Activity.restartApplication() = packageManager.getLaunchIntentForPackage(packageName)?.let {
    finishAffinity()
    startActivity(intent)
    exitProcess(0)
}

/**
 * 获取版本代码
 * @param [packageName] 包名
 * @see [Int]
 * @return [Int]
 * @suppress Generate Documentation
 */
fun Context.getVersionCode(packageName: String): Int = try {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNING_CERTIFICATES
            ).longVersionCode.toInt()
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
        }

        else -> {
            packageManager.getPackageInfo(packageName, 0).versionCode
        }
    }
} catch (e: Throwable) {
    e.loge()
    0
}

/**
 * 打印调用堆栈
 * @suppress Generate Documentation
 */
fun String.printCallStack() {
    val stringBuilder = StringBuilder()
    stringBuilder.appendLine("----className: $this ----")
    stringBuilder.appendLine("Dump Stack: ---------------start----------------")
    val ex = Throwable()
    val stackElements = ex.stackTrace
    stackElements.forEachIndexed { index, stackTraceElement ->
        stringBuilder.appendLine("Dump Stack: $index: $stackTraceElement")
    }
    stringBuilder.appendLine("Dump Stack: ---------------end----------------")
    stringBuilder.toString().loge()
}

/**
 *  执行并捕获异常
 * @param [block] 块
 * @suppress Generate Documentation
 */
inline fun runAndCatch(block: () -> Unit) = runCatching {
    block()
}.onFailure {
    it.loge()
}

/**
 * 打印调用堆栈
 * @suppress Generate Documentation
 */
fun Any.printCallStack() {
    this.javaClass.name.printCallStack()
}

/**
 * 复制到剪贴板
 * @param [text] 发短信
 * @suppress Generate Documentation
 */
fun Context.copyToClipboard(text: String) {
    val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
}

/**
 * 加入QQ群
 * @param [key] 钥匙
 * @see [Boolean]
 * @return [Boolean]
 * @suppress Generate Documentation
 */
fun Context.joinQQGroup(key: String): Boolean {
    val uri = Uri.Builder().scheme("mqqopensdkapi").authority("bizAgent").appendPath("qm")
        .appendPath("qr").appendQueryParameter(
            "url", "http://qm.qq.com/cgi-bin/qm/qr?from=app&p=android&jump_from=webapi&k=$key"
        ).build()
    val intent = Intent().apply {
        data = uri
    }
    return try {
        startActivity(intent)
        true
    } catch (e: Exception) {
        // 未安装手Q或安装的版本不支持
        Toast.makeText(this, "未安装手Q或安装的版本不支持", Toast.LENGTH_SHORT).show()
        false
    }
}

/**
 * 隐藏应用图标
 * @suppress Generate Documentation
 */
fun Context.hideAppIcon() {
    val componentName = ComponentName(this, MainActivity::class.java.name)
    if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

/**
 * “显示应用”图标
 * @suppress Generate Documentation
 */
fun Context.showAppIcon() {
    val componentName = ComponentName(this, MainActivity::class.java.name)
    if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

/**
 * 打开url
 * @param [url] url
 * @suppress Generate Documentation
 */
fun Context.openUrl(url: String) = runCatching {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

/**
 * 吐司
 * @param [msg] 消息
 * @suppress Generate Documentation
 */
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

/**
 * 打印不支持版本
 * @param [versionCode] 版本代码
 * @suppress Generate Documentation
 */
fun String.printlnNotSupportVersion(versionCode: Int = 0) =
    YLog.error(msg = "${this}不支持的版本号为: $versionCode", tag = YLog.Configs.tag)

/**
 * 合并
 * @param [newConfigurations] 新配置
 * @see [List<SelectedModel>]
 * @return [List<SelectedModel>]
 * @suppress Generate Documentation
 */
infix fun List<SelectedModel>.merge(newConfigurations: List<SelectedModel>): List<SelectedModel> {
    val result = this.toMutableList()
    result -= this.filter { it.title !in newConfigurations.map { it1 -> it1.title } }.toSet()
    result += newConfigurations.filter { it.title !in this.map { it1 -> it1.title } }
    return result.distinct().sortedBy { it.title }
}

/**
 * 按标题选择
 * @param [title] 标题
 * @see [Boolean]
 * @return [Boolean]
 * @suppress Generate Documentation
 */
fun List<SelectedModel>.isSelectedByTitle(title: String): Boolean =
    firstOrNull { it.title == title }?.selected ?: false

/**
 * 解析关键字选项
 * @param [it] 它
 * @see [MutableSet<String>]
 * @return [MutableSet<String>]
 * @suppress Generate Documentation
 */
fun parseKeyWordOption(it: String = ""): MutableSet<String> =
    it.split(";").filter(String::isNotBlank).map(String::trim).toMutableSet()

/**
 * 寻找或添加
 * @param [title] 标题
 * @param [iterator] 迭代器
 * @suppress Generate Documentation
 */
fun MutableList<SelectedModel>.findOrPlus(
    title: String,
    iterator: MutableIterator<Any?>,
) = runAndCatch {
    firstOrNull { it.title == title }?.let { config ->
        if (config.selected) {
            iterator.remove()
        }
    } ?: SelectedModel(title = title).also { plusAssign(it) }
    updateOptionEntity()
}

/**
 * 寻找或添加
 * @param [title] 标题
 * @param [actionUnit] 行动单位
 * @suppress Generate Documentation
 */
fun MutableList<SelectedModel>.findOrPlus(
    title: String,
    actionUnit: () -> Unit = {},
) = runAndCatch {
    firstOrNull { it.title == title }?.let { config ->
        if (config.selected) {
            actionUnit()
        }
    } ?: SelectedModel(title = title).also { plusAssign(it) }
    updateOptionEntity()
}

/**
 * 多选选择器
 * @param [list] 列表
 * @suppress Generate Documentation
 */
fun Context.multiChoiceSelector(
    list: List<SelectedModel>,
) = runAndCatch {
    if (list.isEmpty()) {
        toast("没有可用的选项")
        return@runAndCatch
    }

    val checkedItems = list.map { it.selected }.toBooleanArray()
    multiChoiceSelector(
        list.map { it.title }, checkedItems, "选项列表"
    ) { _, i, isChecked ->
        checkedItems[i] = isChecked
    }.doOnDismiss {
        checkedItems.forEachIndexed { index, b ->
            list[index].selected = b
        }
        updateOptionEntity()
    }
}

/**
 * 获取应用程序apk路径
 * @param [packageName] 程序包名称
 * @return [String]
 * @suppress Generate Documentation
 */
fun Context.getApplicationApkPath(packageName: String): String {
    val pm = this.packageManager
    val apkPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        pm.getApplicationInfo(
            packageName, PackageManager.GET_SIGNING_CERTIFICATES
        ).publicSourceDir
    } else {
        pm.getApplicationInfo(packageName, 0).publicSourceDir
    }
    return apkPath ?: throw Error("Failed to get the APK path of $packageName")
}

/**
 * 查找方法和打印
 * @param [className] 类名
 * @param [printCallStack] 打印调用堆栈
 * @param [printType] 打印类型
 * @suppress Generate Documentation
 */
fun PackageParam.findMethodAndPrint(
    className: String,
    printCallStack: Boolean = false,
    printType: MembersType = MembersType.METHOD,
    classLoader: ClassLoader? = appClassLoader
) {
    when (printType) {
        MembersType.METHOD -> {
            className.toClass(classLoader).method().hookAll().after {
                print(printCallStack)
            }
        }

        MembersType.CONSTRUCTOR -> {
            className.toClass(classLoader).constructor().hookAll().after {
                print(printCallStack)
            }
        }

        else -> {
            with(className.toClass(classLoader)) {
                (method().giveAll() + constructor().giveAll()).hookAll {
                    after {
                        print(printCallStack)
                    }
                }
            }
        }
    }
}

private fun HookParam.print(
    printCallStack: Boolean = false
) {
    val stringBuilder = StringBuilder().apply {
        append("---类名: ${instanceClass?.name} 方法名: ${method.name}\n")
        if (args.isEmpty()) {
            append("无参数\n")
        } else {
            args.forEachIndexed { index, any ->
                append("参数${any?.javaClass?.simpleName} ${index}: ${any.mToString()}\n")
            }
        }
        result?.let { append("---返回值: ${it.mToString()}") }
    }
    stringBuilder.toString().loge()
    if (printCallStack) {
        instance.printCallStack()
    }
}

/**
 * 打印参数
 * @return [String]
 * @suppress Generate Documentation
 */
fun Array<Any?>.printArgs(): String {
    val stringBuilder = StringBuilder()
    this.forEachIndexed { index, any ->
        stringBuilder.append("args[$index]: ${any.mToString()}\n")
    }
    return stringBuilder.toString()
}

/**
 * m到字符串
 * @return [String]
 * @suppress Generate Documentation
 */
fun Any?.mToString(): String = when (this) {
    is String, is Int, is Long, is Float, is Double, is Boolean -> "$this"
    is Array<*> -> this.joinToString(",")
    is ByteArray -> this.toHexString()//this.toString(Charsets.UTF_8)
    is Serializable, is Parcelable -> this.toJSONString()
    else -> {
        val list = listOf(
            "Entity",
            "Model",
            "Bean",
            "Result",
        )
        if (list.any { this?.javaClass?.name?.contains(it) == true }) this.toJSONString()
        else if (this?.javaClass?.name?.contains("QDHttpResp") == true) this.getParamList<String>()
            .toString()
        else this?.toString() ?: "null"
    }
}

/**
 * 字节数组使用hex编码
 */
fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    forEach {
        sb.append(String.format("%02x", it))
    }
    return sb.toString()
}

/**
 * 按类型查找视图
 * @param [viewClass] 视图类
 * @return [ArrayList<View>]
 * @suppress Generate Documentation
 */
fun ViewGroup.findViewsByType(viewClass: Class<*>): ArrayList<View> {
    val result = arrayListOf<View>()
    val queue = ArrayDeque<View>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val view = queue.removeFirst()
        if (viewClass.isInstance(view)) {
            result.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0..<view.childCount) {
                queue.add(view.getChildAt(i))
            }
        }
    }

    return result
}

/**
 * 随机时间
 * @return [Long]
 * @suppress Generate Documentation
 */
fun randomTime(): Long = Random().nextInt(500) + 50L

/**
 * 后随机延迟
 * @param [delayMillis] 延迟毫秒
 * @param [action] 行动
 * @suppress Generate Documentation
 */
fun View.postRandomDelay(delayMillis: Long = randomTime(), action: View.() -> Unit) =
    postDelayed({ this.action() }, delayMillis)

/**
 * 随机延迟执行点击
 * @suppress Generate Documentation
 */
fun View.randomDelayPerformClick() = postRandomDelay { performClick() }

/**
 * 将一个字符串数组转换为一个由字符串和布尔值组成的对数组
 * @param [default] 违约
 * @return [Array<Pair<String, Boolean>>]
 * @suppress Generate Documentation
 */
fun Array<String>.toPairs(default: Boolean = false): Array<Pair<String, Boolean>> =
    this.map { it to default }.toTypedArray()

/**
 * 隐藏视图
 * @suppress Generate Documentation
 */
fun List<View>.hideViews() = forEach { it.setVisibilityIfNotEqual() }

/**
 * 执行点击
 *  @suppress Generate Documentation
 */
fun List<View>.randomDelayPerformClick() = forEach { it.randomDelayPerformClick() }

/**
 * 获取带回退字符串
 * @param [key] 钥匙
 * @return [String?]
 * @suppress Generate Documentation
 */
fun com.alibaba.fastjson2.JSONObject.getStringWithFallback(key: String): String? =
    this.getString(key) ?: this.getString(key.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    })

/**
 * 使用回退获取jsonarray
 * @param [key] 钥匙
 * @return [com.alibaba.fastjson2.JSONArray?]
 * @suppress Generate Documentation
 */
fun com.alibaba.fastjson2.JSONObject.getJSONArrayWithFallback(key: String): com.alibaba.fastjson2.JSONArray? =
    this.getJSONArray(key)
        ?: this.getJSONArray(key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })

/**
 * 获取名称
 * @suppress Generate Documentation
 */
fun View.getName() = toString().substringAfter("/").replace("}", "")

/**
 * 数据处理
 * @since 7.9.354-1336
 * @param [value] 价值
 * @return [Int]
 * @suppress Generate Documentation
 */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

/**
 * x
 * @since 7.9.354-1336
 * @param [other] 另外
 * @return [ViewGroup.LayoutParams]
 * @suppress Generate Documentation
 */
infix fun Int.x(other: Int): ViewGroup.LayoutParams = ViewGroup.LayoutParams(this, other)

/**
 * return false
 * @since 7.9.354-1336
 * @param [methodData] 方法数据
 * @suppress Generate Documentation
 */
fun PackageParam.returnFalse(className: String, methodName: String, paramCount: Int = 0) {
    className.toClass().method {
        name = methodName
        if (paramCount == 0) {
            emptyParam()
        } else {
            paramCount(paramCount)
        }
        returnType = BooleanType
    }.hook().replaceToFalse()
}

/**
 * 拦截
 * @since 7.9.354-1336
 * @param [className] 类名
 * @param [methodName] 方法名称
 * @param [paramCount] 参数计数
 * @suppress Generate Documentation
 */
fun PackageParam.intercept(
    className: String, methodName: String, paramCount: Int = 0
) {
    className.toClass().method {
        name = methodName
        if (paramCount == 0) {
            emptyParam()
        } else {
            paramCount(paramCount)
        }
        returnType = UnitType
    }.hook().intercept()
}

fun PackageParam.shieldResult(
    className: String, methodName: String, paramCount: Int = 0, returnType: Any = ListClass
) {
    className.toClass().method {
        name = methodName
        if (paramCount == 0) {
            emptyParam()
        } else {
            paramCount(paramCount)
        }
        this.returnType = returnType
    }.hook().after {
        result.safeCast<ArrayList<*>>()?.let {
            result = parseNeedShieldList(it)
        }
    }
}

fun PackageParam.shieldUnit(
    className: String, methodName: String, paramCount: Int = 0, index: Int = 0
) {
    className.toClass().method {
        name = methodName
        if (paramCount == 0) {
            emptyParam()
        } else {
            paramCount(paramCount)
        }
        returnType = UnitType
    }.hook().before {
        args[index].safeCast<MutableList<*>>()?.let {
            args(index).set(parseNeedShieldList(it))
        }
    }
}

fun Context.readTextFromUri(uri: Uri): String {
    val inputStream = contentResolver.openInputStream(uri)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    reader.forEachLine { line ->
        stringBuilder.append(line)
    }
    reader.close()
    return stringBuilder.toString()
}

fun Context.writeTextToUri(uri: Uri, text: String) {
    contentResolver.openOutputStream(uri)?.use { outputStream ->
        BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
            writer.write(text)
        }
    }
}

object Utils : KoinComponent {

    val httpClient: HttpClient by inject()

    fun saveImageFromUrl(context: Context, imageTitle: String, imageList: Map<String, String>) =
        thread {
            imageList.forEach { (displayName, imageUrl) ->
                try {
                    val bytes = httpClient.get(imageUrl).body<ByteArray>()

                    val fileExtension = imageUrl.substring(imageUrl.lastIndexOf('.') + 1)
                    val mimeType = when (fileExtension.lowercase()) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "gif" -> "image/gif"
                        else -> "image/*"
                    }
                    // 保存图片到Pictures目录
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.$fileExtension")
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "Pictures/QDReader/${imageTitle}/"
                        )
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    uri?.let {
                        val outputStream: OutputStream? =
                            context.contentResolver.openOutputStream(it)
                        outputStream?.use { stream ->
                            if (fileExtension.lowercase() == "gif") {
                                stream.write(bytes)
                            } else {
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            }
                        } ?: "写入图片失败".loge()
                    } ?: "插入图片失败".loge()
                } catch (e: Exception) {
                    "下载图片失败: ${e.message}".loge()
                }
            }
            withContext(Dispatchers.Main.immediate) {
                context.toast("导出成功")
            }
        }

    fun saveAudioFromFile(context: Context, audioTitle: String, audioFile: File) = thread {

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, audioTitle)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/QDReader/")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                audioFile.inputStream().use { inputStream ->
                    inputStream.copyTo(stream)
                }
                withContext(Dispatchers.Main.immediate) {
                    context.toast("导出成功")
                }
            } ?: "写入音频失败".loge()
        } ?: "插入音频失败".loge()

    }

}
