package com.east.buglyhotfix

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.east.buglyhotfix.bugtest.LoadBugClass
import com.east.buglyhotfix.bugtest.NativeCrashJni
import com.east.east_utils.utils.permission.PermissionCheckUtils
import com.east.east_utils.utils.permission.PermissionListener
import com.tencent.bugly.beta.Beta
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentVersion.text = "当前版本：" + getCurrentVersion(this)

        PermissionCheckUtils.checkPermission(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE),object: PermissionListener {
            override fun onCancel() {
                finish()
            }
        })
    }


    fun onClick(v: View){
        when(v){
            btnShowToast ->{
                // 测试热更新功能
                testToast()
            }
            btnKillSelfAndRestart ->{
                // 杀死进程
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                ShareTinkerInternals.killAllOtherProcess(applicationContext)
                Process.killProcess(Process.myPid())
            }
            btnLoadPatch ->{
                // 本地加载补丁测试
                Beta.applyTinkerPatch(
                    applicationContext,
                    Environment.getExternalStorageDirectory()
                        .absolutePath + "/patch_signed_7zip.apk"
                )
            }
            btnLoadLibrary -> {
                // 本地加载so库测试
                NativeCrashJni.getInstance().createNativeCrash()
            }
            btnDownloadPatch -> {
                //线上下载补丁包
                Beta.downloadPatch()
            }
            btnPatchDownloaded ->{
                //加载线上下载下来的补丁包
                Beta.applyDownloadedPatch()
            }
            btnCheckUpgrade ->{
                //检查更新
                Beta.checkUpgrade()
            }
        }
    }


    /**
     * 根据应用patch包前后来测试是否应用patch包成功.
     *
     * 应用patch包前，提示"This is a bug class"
     * 应用patch包之后，提示"The bug has fixed"
     */
    fun testToast() {
        Toast.makeText(this, LoadBugClass.getBugString(), Toast.LENGTH_SHORT).show()
    }


    /**
     * 获取当前版本.
     *
     * @param context 上下文对象
     * @return 返回当前版本
     */
    fun getCurrentVersion(context: Context): String? {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(
                    this.packageName,
                    PackageManager.GET_CONFIGURATIONS
                )
            val versionCode = packageInfo.versionCode
            val versionName = packageInfo.versionName
            return "$versionName.$versionCode"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
