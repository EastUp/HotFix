package com.east.hotfix.fixbug;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 *  @description: 基于PathClassLoader 进行的热修复
 *  @author: jamin
 *  @date: 2020/5/7
 * |---------------------------------------------------------------------------------------------------------------|
 */
public class FixDexManager {

    private Context mApplicationContext;
    private File mOwnDexDir; // 应 用可以访问的dex目录


    public FixDexManager(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        // 获取应用可以访问的dex目录
        mOwnDexDir = mApplicationContext.getDir("odex",Context.MODE_PRIVATE);
    }


    /**
     *  加载应用补丁包目录下的所有补丁
     * @throws Exception
     */
    public void loadAllFixDex() throws Exception{
        File[] files = mOwnDexDir.listFiles();

        List<File> fixDexFiles = new ArrayList<>();
        for (File file : files) {
            if(file.getName().endsWith(".dex"))
                fixDexFiles.add(file);
        }
        fixDexFiles(fixDexFiles);
    }


    /**
     * 修复Dex包
     * @param fixDexPath
     */
    public void fixDex(String fixDexPath) throws Exception{
        // 1. 将下载好的补丁文件拷贝到系统可以访问的dex目录下

        File srcDexFile = new File(fixDexPath);
        if(!srcDexFile.exists()){
            throw new RuntimeException("patch [" + fixDexPath + "] has be loaded.");
        }
        File destDexFile = new File(mOwnDexDir,srcDexFile.getName());
        copyFile(srcDexFile,destDexFile);

        // 2. ClassLoader读取FixDex获得它的dexElements 为什么加入到集合  一启动可能就要修复 BaseApplication
        List<File> fixDexFiles = new ArrayList<>();
        fixDexFiles.add(destDexFile);
        fixDexFiles(fixDexFiles);
    }

    /**
     * 修复加载 所有的补丁包
     */
    private void fixDexFiles(List<File> fixDexFiles) throws Exception{

        // 1. 获取应用自己的dexElements
        ClassLoader applicationClassLoader = mApplicationContext.getClassLoader();
        Object applicationDexElements = getDexElementsByClassLoader(applicationClassLoader); //获取的应用DexElements


        // 2. 获取所有补丁包的dexElements
        File optimizedDirectory = new File(mOwnDexDir,"odex");
        for (File fixDexFile : fixDexFiles) {
            BaseDexClassLoader baseDexClassLoader = new BaseDexClassLoader(
                    fixDexFile.getAbsolutePath(),// dex路径  必须要在应用目录下的odex文件中
                    optimizedDirectory,// 解压路径
                    null,// so文件的路径
                    applicationClassLoader// 父ClassLoader
            );
            Object elements = getDexElementsByClassLoader(baseDexClassLoader);
            // 3. 合并应用的dexElements 和 补丁的dexElements
            applicationDexElements  = combineArray(elements,applicationDexElements);
        }

        // 4.反射注入新的dexElements
        injectDexElements(applicationClassLoader,applicationDexElements);

    }

    /**
     * 反射注入新的dexElements
     */
    private void injectDexElements(ClassLoader classLoader, Object applicationDexElements) throws Exception{
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);
        Field dexElementsFiled = pathList.getClass().getDeclaredField("dexElements");
        dexElementsFiled.setAccessible(true);
        dexElementsFiled.set(pathList,applicationDexElements);
    }


    /**
     * 通过classLoader获取dexElements
     */
    private Object getDexElementsByClassLoader(ClassLoader classLoader) throws Exception{
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);
        Field dexElementsFiled = pathList.getClass().getDeclaredField("dexElements");
        dexElementsFiled.setAccessible(true);
        return dexElementsFiled.get(pathList); //获取DexElements
    }


    /**
     * copy file
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * 合并两个数组
     *
     * @param arrayLhs 放在最左边的
     * @param arrayRhs 放在最右边的
     * @return
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

}
