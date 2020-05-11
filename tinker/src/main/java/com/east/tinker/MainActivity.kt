package com.east.tinker

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.east.east_utils.utils.permission.PermissionCheckUtils
import com.east.east_utils.utils.permission.PermissionListener
import com.east.tinker.app.BaseBuildInfo
import com.east.tinker.app.BuildInfo
import com.tencent.tinker.lib.library.TinkerLoadLibrary
import com.tencent.tinker.lib.tinker.Tinker
import com.tencent.tinker.lib.tinker.TinkerInstaller
import com.tencent.tinker.loader.shareutil.ShareConstants
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:  腾讯热修复Tinker的测试
 *  @author: jamin
 *  @date: 2020/5/9 10:20
 * |---------------------------------------------------------------------------------------------------------------|
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mCat: Cat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCat = Cat()

        PermissionCheckUtils.checkPermission(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ), object : PermissionListener {
            override fun onGranted() {
                val path = Environment.getExternalStorageDirectory()
                    .absolutePath + "/patch_signed_7zip.apk"
                val patchFile = File(path)
                if (patchFile.exists())
                    TinkerInstaller.onReceiveUpgradePatch(application.applicationContext, path)
                else
                    Toast.makeText(this@MainActivity,"没有补丁文件，可能安装成功被删除了",Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                finish()
            }
        })

        tv_tip.text = "仿照了demo的常用功能"


        tv_tip1.text = "Flavor1修改的第十次哦!!"
    }

    fun onClick(v: View) {
        when (v) {
            loadPatch -> {
                val path = Environment.getExternalStorageDirectory()
                    .absolutePath + "/patch_signed_7zip.apk"
                val patchFile = File(path)
                if (patchFile.exists())
                    TinkerInstaller.onReceiveUpgradePatch(application.applicationContext, path)
                else
                    Toast.makeText(this@MainActivity,"没有补丁文件，可能安装成功被删除了",Toast.LENGTH_SHORT).show()
            }
            loadLibrary -> {

                // #method 1, hack classloader library path
                TinkerLoadLibrary.installNavitveLibraryABI(applicationContext, "armeabi")
                System.loadLibrary("stlport_shared")

                // #method 2, for lib/armeabi, just use TinkerInstaller.loadLibrary
//                TinkerLoadLibrary.loadArmLibrary(getApplicationContext(), "stlport_shared");

                // #method 3, load tinker patch library directly
//                TinkerInstaller.loadLibraryFromTinker(getApplicationContext(), "assets/x86", "stlport_shared");
            }
            cleanPatch -> {
                Tinker.with(applicationContext).cleanPatch()
            }
            killSelfAndRestart -> {
                //关闭并重启
                //you can send service or broadcast intent to restart your process
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                ShareTinkerInternals.killAllOtherProcess(applicationContext)
                Process.killProcess(Process.myPid())
            }

            showInfo -> {
                showInfo(this)
            }
            catsay -> {
                Toast.makeText(this, mCat.say(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showInfo(context: Context?): Boolean {
        // add more Build Info
        val sb = StringBuilder()
        val tinker = Tinker.with(applicationContext)
        if (tinker.isTinkerLoaded) {
            sb.append(String.format("[patch is loaded] \n"))
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID))
            sb.append(
                String.format(
                    "[buildConfig BASE_TINKER_ID] %s \n",
                    BuildInfo.TINKER_ID
                )
            )
            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE))
            sb.append(
                String.format(
                    "[TINKER_ID] %s \n",
                    tinker.tinkerLoadResultIfPresent
                        .getPackageConfigByName(ShareConstants.TINKER_ID)
                )
            )
            sb.append(
                String.format(
                    "[packageConfig patchMessage] %s \n",
                    tinker.tinkerLoadResultIfPresent.getPackageConfigByName("patchMessage")
                )
            )
            sb.append(
                String.format(
                    "[TINKER_ID Rom Space] %d k \n",
                    tinker.tinkerRomSpace
                )
            )
        } else {
            sb.append(String.format("[patch is not loaded] \n"))
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID))
            sb.append(
                String.format(
                    "[buildConfig BASE_TINKER_ID] %s \n",
                    BuildInfo.TINKER_ID
                )
            )
            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE))
            sb.append(
                String.format(
                    "[TINKER_ID] %s \n",
                    ShareTinkerInternals.getManifestTinkerID(applicationContext)
                )
            )
        }
        sb.append(
            String.format(
                "[BaseBuildInfo Message] %s \n",
                "I won't change with tinker patch!"
            )
        )
        val v = TextView(context)
        v.text = sb
        v.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
        v.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        v.setTextColor(-0x1000000)
        v.setTypeface(Typeface.MONOSPACE)
        val padding = 16
        v.setPadding(padding, padding, padding, padding)
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(v)
        val alert = builder.create()
        alert.show()
        return true
    }
}
