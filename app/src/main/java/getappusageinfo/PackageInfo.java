package getappusageinfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.example.phoneusagewatcher.AppsActivity;

import java.util.ArrayList;
import java.util.List;

public class PackageInfo extends android.content.pm.PackageInfo {
    private int mUsedCount;
    private long mUsedTime;
    private String mPackageName;
    private String mAppName;
   // private Drawable icon;


    public PackageInfo(int mUsedCount, long mUsedTime, String mPackageName,String appName) {
        this.mUsedCount = mUsedCount;
        this.mUsedTime = mUsedTime;
        this.mPackageName = mPackageName;
        this.mAppName=appName;
        //this.icon=icon;
    }

    public void addCount() {
        mUsedCount++;
    }

    public int getmUsedCount() {
        return mUsedCount;
    }

    public void setmUsedCount(int mUsedCount) {
        this.mUsedCount = mUsedCount;
    }

    public long getmUsedTime() {
        return mUsedTime;
    }

    public void setmUsedTime(long mUsedTime) {
        this.mUsedTime = mUsedTime;
    }

    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmAppName() {
        return mAppName;
    }

    public void setmAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public Drawable getAppIcon(Context context,String mPackageName)
    {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    mPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable icon = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable

        return icon;

    }

    @Override
    public boolean equals(Object o) {
        //return super.equals(o);
        if (o == null) return false;
        if (this == o) return true;
        PackageInfo standardDetail = (PackageInfo) o;
        if (standardDetail.getmPackageName().equals(this.mPackageName)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //return super.hashCode();
        return (mPackageName + mUsedTime).hashCode();
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "mUsedCount=" + mUsedCount +
                ", mUsedTime=" + mUsedTime +
                ", mPackageName='" + mPackageName + '\'' +
                ", mAppName='" + mAppName + '\'' +
                '}';
    }

}
