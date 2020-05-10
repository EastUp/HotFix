package com.east.tinker

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.east.east_utils.utils.permission.PermissionCheckUtils
import com.east.east_utils.utils.permission.PermissionListener
import com.tencent.tinker.lib.tinker.TinkerInstaller
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

    private lateinit var mCat:Cat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCat = Cat()

        PermissionCheckUtils.checkPermission(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE),object: PermissionListener {
            override fun onGranted() {
                val path = Environment.getExternalStorageDirectory()
                    .absolutePath + "/patch_signed_7zip.apk"
                val patchFile = File(path)
                if (patchFile.exists())
                    TinkerInstaller.onReceiveUpgradePatch(application.applicationContext, path)
            }

            override fun onCancel() {
                finish()
            }
        })

        tv_tip.text = "你好啊"
    }

    fun onClick(v: View){
        Toast.makeText(this,mCat.say(),Toast.LENGTH_SHORT).show()
    }
}
