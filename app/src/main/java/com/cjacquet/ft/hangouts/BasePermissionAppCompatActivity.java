package com.cjacquet.ft.hangouts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class BasePermissionAppCompatActivity extends AppCompatActivity {

    private final static String APP_NAME = "ft_hangouts";
    private final static int REQUEST_READ_SMS_PERMISSION = 3004;
    public final static String READ_SMS_PERMISSION_NOT_GRANTED = "Please allow " + APP_NAME + " to access your SMS from setting";

    RequestPermissionAction onPermissionCallBack;

    private boolean checkReadSMSPermission() {
        System.out.println("checkReadSMSPermission: enter the method ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("checkReadSMSPermission: Permission is already granted ");
                return true;
            } else {
                System.out.println("checkReadSMSPermission: Permission is not granted ");
                return false;
            }
        } else {
            System.out.println("checkReadSMSPermission: Permission is already granted ");
            return true;
        }
    }

    public void getReadSMSPermission(RequestPermissionAction onPermissionCallBack) {
        this.onPermissionCallBack = onPermissionCallBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkReadSMSPermission()) {
                System.out.println("getReadPermission: Permission not granted yet, make a request");
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS_PERMISSION);
                return;
            }
        }
        if (onPermissionCallBack != null) {
            System.out.println("getReadPermission: Callback present, called it for permission granted");
            onPermissionCallBack.permissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("onRequestPermissionsResult: result of permission is bacl");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("onRequestPermissionsResult: permission is granted ");
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                // TODO Request Granted for READ_SMS.
                System.out.println("onRequestPermissionResult: REQUEST_READ_SMS_PERMISSION Permission Granted");
            }
            if (onPermissionCallBack != null) {
                System.out.println("onRequestPermissionsResult: there is a callback so we call it to permission granted");
                onPermissionCallBack.permissionGranted();
            }

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            System.out.println("onRequestPermissionsResult: permission is denied");
            if (REQUEST_READ_SMS_PERMISSION == requestCode) {
                // TODO REQUEST_READ_SMS_PERMISSION Permission is not Granted.
                // TODO Request Not Granted.
                System.out.println("onRequestPermissionsResult: REQUEST_READ_SMS_PERMISSION Permission Denied");
                System.out.println("onRequestPermissionsResult: Will open settings to grant sms from there");

                // This code is for get permission from setting.
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
            }
            if (onPermissionCallBack != null) {
                System.out.println("onRequestPermissionsResult: There is a call back so we call it to permission denied");
                onPermissionCallBack.permissionDenied();
            }
        }
    }

    public interface RequestPermissionAction {
        void permissionDenied();

        void permissionGranted();
    }

}