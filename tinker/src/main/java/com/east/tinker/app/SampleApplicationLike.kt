package com.east.tinker.app

import android.annotation.TargetApi
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.multidex.MultiDex
import com.east.tinker.Log.MyLogImp
import com.east.tinker.util.SampleApplicationContext
import com.east.tinker.util.TinkerManager
import com.tencent.tinker.anno.DefaultLifeCycle
import com.tencent.tinker.entry.DefaultApplicationLike
import com.tencent.tinker.lib.tinker.Tinker
import com.tencent.tinker.lib.tinker.TinkerInstaller
import com.tencent.tinker.loader.shareutil.ShareConstants
import java.io.File

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:
 *  @author: jamin
 *  @date: 2020/5/9
 * |---------------------------------------------------------------------------------------------------------------|
 */
@DefaultLifeCycle(
    application = "com.east.tinker.app.SampleApplication",
    flags = ShareConstants.TINKER_ENABLE_ALL,
    loadVerifyFlag = false
)
class SampleApplicationLike(
    application: Application?,
    tinkerFlags: Int,
    tinkerLoadVerifyFlag: Boolean,
    applicationStartElapsedTime: Long,
    applicationStartMillisTime: Long,
    tinkerResultIntent: Intent?
) : DefaultApplicationLike(
    application,
    tinkerFlags,
    tinkerLoadVerifyFlag,
    applicationStartElapsedTime,
    applicationStartMillisTime,
    tinkerResultIntent
) {

    /**
     * install multiDex before install tinker
     * so we don't need to put the tinker lib classes in the main dex
     *
     * @param base
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onBaseContextAttached(base: Context?) {
        super.onBaseContextAttached(base)
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base)

        //这几个可以暂时不要
//        SampleApplicationContext.application = application
//        SampleApplicationContext.context = application
        TinkerManager.setTinkerApplicationLike(this)
//        TinkerManager.initFastCrashProtect()

        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true)

        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(MyLogImp())

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this)
        val tinker = Tinker.with(application)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        application.registerActivityLifecycleCallbacks(callback)
    }

}