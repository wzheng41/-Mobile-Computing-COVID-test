package permissions;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XXPermissions {

    private Activity mActivity;
    private List<String> mPermissions;
    private boolean mConstant;

    /**
     * 私有化构造函数
     */
    private XXPermissions(Activity activity) {
        mActivity = activity;
    }

    /**
     * 设置请求的对象
     */
    public static XXPermissions with(Activity activity) {
        return new XXPermissions(activity);
    }

    /**
     * 设置权限组
     */
    public XXPermissions permission(String... permissions) {
        if (mPermissions == null) {
            mPermissions = new ArrayList<>(permissions.length);
        }
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * 设置权限组
     */
    public XXPermissions permission(String[]... permissions) {
        if (mPermissions == null) {
            int length = 0;
            for (String[] permission : permissions) {
                length += permission.length;
            }
            mPermissions = new ArrayList<>(length);
        }
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }

    /**
     * 设置权限组
     */
    public XXPermissions permission(List<String> permissions) {
        if (mPermissions == null) {
            mPermissions = permissions;
        } else {
            mPermissions.addAll(permissions);
        }
        return this;
    }

    /**
     * 被拒绝后继续申请，直到授权或者永久拒绝
     */
    public XXPermissions constantRequest() {
        mConstant = true;
        return this;
    }

    /**
     * 请求权限
     */
    public void request(OnPermission callback) {
        // 如果没有指定请求的权限，就使用清单注册的权限进行请求
        if (mPermissions == null || mPermissions.isEmpty()) {
            mPermissions = PermissionUtils.getManifestPermissions(mActivity);
        }
        if (mPermissions == null || mPermissions.isEmpty()) {
            throw new IllegalArgumentException("The requested permission cannot be empty");
        }
        if (mActivity == null) {
            throw new IllegalArgumentException("The activity is empty");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed()) {
                throw new IllegalStateException("The event has been destroyed");
            } else if (mActivity.isFinishing()) {
                throw new IllegalStateException("The event has been finish");
            }
        }
        if (callback == null) {
            throw new IllegalArgumentException("The permission request callback interface must be implemented");
        }

        PermissionUtils.checkTargetSdkVersion(mActivity, mPermissions);

        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(mActivity, mPermissions);

        if (failPermissions == null || failPermissions.isEmpty()) {
            // 证明权限已经全部授予过
            callback.hasPermission(mPermissions, true);
        } else {
            // 检测权限有没有在清单文件中注册
            PermissionUtils.checkPermissions(mActivity, mPermissions);
            // 申请没有授予过的权限
            PermissionFragment.newInstance((new ArrayList<>(mPermissions)), mConstant).prepareRequest(mActivity, callback);
        }
    }

    /**
     * 检查某些权限是否全部授予了
     *
     * @param context     上下文对象
     * @param permissions 需要请求的权限组
     */
    public static boolean isHasPermission(Context context, String... permissions) {
        return isHasPermission(context, Arrays.asList(permissions));
    }

    public static boolean isHasPermission(Context context, List<String> permissions) {
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissions);
        return failPermissions == null || failPermissions.isEmpty();
    }

    /**
     * 检查某些权限是否全部授予了
     *
     * @param context     上下文对象
     * @param permissions 需要请求的权限组
     */
    public static boolean isHasPermission(Context context, String[]... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : permissions) {
            permissionList.addAll(Arrays.asList(group));
        }
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissionList);
        return failPermissions == null || failPermissions.isEmpty();
    }

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     */
    public static void gotoPermissionSettings(Context context) {
        PermissionSettingPage.start(context, false);
    }

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     * @param newTask 是否使用新的任务栈启动
     */
    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}