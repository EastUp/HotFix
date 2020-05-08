package com.east.hotfix;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.east.east_utils.utils.permission.PermissionCheckUtils;
import com.east.east_utils.utils.permission.PermissionListener;

public class MainActivity extends AppCompatActivity {

    private Cat mCat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionCheckUtils.INSTANCE.checkPermission(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, new PermissionListener() {
            @Override
            public void onCancel() {
                finish();
            }
        });

        mCat = new Cat();
    }

    public void onClick(View v){
        //模拟错误，猫不能是狗叫
        Toast.makeText(this,mCat.say(),Toast.LENGTH_SHORT).show();
    }
}
