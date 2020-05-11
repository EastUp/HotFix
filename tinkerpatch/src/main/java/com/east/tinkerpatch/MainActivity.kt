package com.east.tinkerpatch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.east.east_utils.utils.permission.PermissionCheckUtils
import com.east.east_utils.utils.permission.PermissionListener
import com.tencent.tinker.lib.util.TinkerLog
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals
import com.tinkerpatch.sdk.TinkerPatch
import com.tinkerpatch.sdk.server.callback.ConfigRequestCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:  腾讯热修复TinkerPatch的测试
 *  @author: jamin
 *  @date: 2020/5/9 10:20
 * |---------------------------------------------------------------------------------------------------------------|
 */
class MainActivity : AppCompatActivity(){

    private lateinit var mCat:Cat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCat = Cat()

        PermissionCheckUtils.checkPermission(this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),object: PermissionListener {
            override fun onGranted() {
                //使用 fetchPatchUpdate(true) 强制检查更新
                TinkerPatch.with().fetchPatchUpdate(true)
            }

            override fun onCancel() {
                finish()
            }
        })
    }

    fun onClick(v: View){
        when(v){
            requestPatch -> {
                //使用 fetchPatchUpdate(true) 强制检查更新,默认在SampleApplicationLike中会检查一次
                TinkerPatch.with().fetchPatchUpdate(true)
            }
            requestConfig -> {
                //请求配置
                TinkerPatch.with().fetchDynamicConfig(object : ConfigRequestCallback {
                    override fun onSuccess(configs: HashMap<String, String>) {
                        TinkerLog.w(
                            "TAG",
                            "request config success, config:$configs"
                        )
                    }

                    override fun onFail(e: Exception) {
                        TinkerLog.w(
                            "TAG",
                            "request config failed, exception:$e"
                        )
                    }
                }, true)
            }
            cleanPatch -> {
                //请求 patch包
                TinkerPatch.with().cleanAll()
            }
            killSelf -> {
                //关闭并重启
                //you can send service or broadcast intent to restart your process
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                ShareTinkerInternals.killAllOtherProcess(applicationContext)
                Process.killProcess(Process.myPid())
            }
            catsay -> {
                Toast.makeText(this,mCat.say(), Toast.LENGTH_SHORT).show()
            }
        }

    }
}
