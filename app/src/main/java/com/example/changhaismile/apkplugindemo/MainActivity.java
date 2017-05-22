package com.example.changhaismile.apkplugindemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.changhaismile.apkplugindemo.Bean.PluginBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dalvik.system.PathClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    //所有的插件
    private List<PluginBean> plugins;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.theme_change:
                List<HashMap<String, String>> datas = new ArrayList<>();
                plugins = findAllPlugin();
                if (plugins != null && !plugins.isEmpty()) {
                    for (PluginBean bean : plugins) {
                        HashMap<String ,String> map = new HashMap<>();
                        map.put("label", bean.getLabel());
                        datas.add(map);
                    }
                } else {
                    Toast.makeText(this, "没有找到插件，请先下载", Toast.LENGTH_SHORT).show();
                }
                showEnabledAllPluginPopup(datas);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 查找手机内所有的插件
     * @return
     */
    private List<PluginBean> findAllPlugin() {
        List<PluginBean> plugins = new ArrayList<>();
        PackageManager pm = getPackageManager();
        //通过包管理器查找所有已安装的apk文件
        List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : packageInfos) {
            //获取当前apk的包名
            String pkgName = info.packageName;
            //获取当前apk的sharedUserId
            String shareUserId = info.sharedUserId;
            Log.i(TAG, "pkgName:" + pkgName + "------" + "shareUserId:" + shareUserId);
            /**
             * 判断这个apk是否是我们应用程序的插件
             */
            if (shareUserId != null && shareUserId.equals("com.sunzxyong.myapp")
                    && !pkgName.equals(this.getPackageName())) {
                //获取插件apk的名称
                String label = pm.getApplicationLabel(info.applicationInfo).toString();
                PluginBean bean = new PluginBean(label, pkgName);
                plugins.add(bean);
            }
        }
        return plugins;
    }

    /**
     * 显示所有可用插件列表，查找已安装的apk即在/data/app目录下
     * @param datas
     */
    private void showEnabledAllPluginPopup(List<HashMap<String, String>>  datas){
        View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_popup, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        listView.setAdapter(new SimpleAdapter(this, datas, android.R.layout.simple_list_item_1,
                new String[]{"label"}, new int[]{android.R.id.text1}));
        final PopupWindow popupWindow = new PopupWindow(rootView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(findViewById(R.id.linearlayout), Gravity.CENTER, 0, 0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                try {
                    PluginBean bean = plugins.get(position);
                    String packageName = bean.getPackageName();
                    Context pluginContext = createPackageContext(packageName, CONTEXT_IGNORE_SECURITY | CONTEXT_INCLUDE_CODE);
                    int resourceId = dynamicLoadApk(packageName, pluginContext);
                    findViewById(R.id.linearlayout).setBackgroundDrawable(pluginContext.getResources().getDrawable(resourceId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 加载已安装的插件
     * @param packageName
     * @param mContext
     * @return
     */
    private int dynamicLoadApk(String packageName, Context mContext) throws Exception {
        //第一个参数：包含dex的apk或者jar的路径，第二个参数为父加载器
        PathClassLoader pathClassLoader = new PathClassLoader(mContext.getPackageResourcePath(),
                ClassLoader.getSystemClassLoader());
        Class<?> clazz = Class.forName(packageName + ".R$mipmap", true, pathClassLoader);
        Field field = clazz.getDeclaredField("one");
        int resourceId = field.getInt(R.mipmap.class);
        return resourceId;
    }
}
