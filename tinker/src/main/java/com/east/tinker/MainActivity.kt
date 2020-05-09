package com.east.tinker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

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

        tv_tip.text = "你好啊"
    }

    fun onClick(v: View){
        Toast.makeText(this,mCat.say(),Toast.LENGTH_SHORT).show()
    }
}
