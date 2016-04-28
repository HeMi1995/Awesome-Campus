package cn.edu.jxnu.awesome_campus.database.dao.about;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Headers;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.edu.jxnu.awesome_campus.api.NotifyApi;
import cn.edu.jxnu.awesome_campus.database.DatabaseHelper;
import cn.edu.jxnu.awesome_campus.database.dao.DAO;
import cn.edu.jxnu.awesome_campus.database.table.about.NotifyTable;
import cn.edu.jxnu.awesome_campus.database.table.home.CourseInfoTable;
import cn.edu.jxnu.awesome_campus.event.EVENT;
import cn.edu.jxnu.awesome_campus.event.EventModel;
import cn.edu.jxnu.awesome_campus.model.about.NotifyBean;
import cn.edu.jxnu.awesome_campus.model.about.NotifyModel;
import cn.edu.jxnu.awesome_campus.support.utils.net.NetManageUtil;
import cn.edu.jxnu.awesome_campus.support.utils.net.callback.JsonEntityCallback;

/**
 * Created by MummyDing on 16-4-23.
 * GitHub: https://github.com/MummyDing
 * Blog: http://blog.csdn.net/mummyding
 */
public class NotifyDAO implements DAO<NotifyModel> {
    public static final String TAG = "NotifyDAO";


    @Override
    public boolean cacheAll(List<NotifyModel> list) {
        if (list == null || list.size() == 0){
            return false;
        }

        clearCache();

        for (int i=0 ;i <list.size(); i++) {
            NotifyModel model = list.get(i);
            ContentValues values = new ContentValues();
            values.put(NotifyTable.NOTIFY_CODE,model.getNotifyCode());
            values.put(NotifyTable.TITLE,model.getTitle());
            values.put(NotifyTable.TYPE,model.getType());
            values.put(NotifyTable.DATA,model.getData());
            DatabaseHelper.insert(NotifyTable.NAME,values);
        }
        return true;
    }

    @Override
    public boolean clearCache() {
        DatabaseHelper.clearTable(NotifyTable.NAME);
        return true;
    }

    @Override
    public void loadFromCache() {

        final Handler handler = new Handler(Looper.getMainLooper());
        Cursor cursor = DatabaseHelper.selectAll(NotifyTable.NAME);
        final List<NotifyModel> list = new ArrayList<>();
        while (cursor.moveToNext()){
            NotifyModel model = new NotifyModel();
            model.setNotifyCode(cursor.getString(NotifyTable.ID_NOTIFY_CODE));
            model.setTitle(cursor.getString(NotifyTable.ID_TITLE));
            model.setType(cursor.getString(NotifyTable.ID_TYPE));
            model.setData(cursor.getString(NotifyTable.ID_DATA));
            list.add(model);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!list.isEmpty()){
                    // 发送成功消息
                    EventBus.getDefault().post(new EventModel<NotifyModel>(EVENT.NOTIFY_LOAD_CACHE_SUCCESS,list));
                }else {
                    //发送失败消息
                    EventBus.getDefault().post(new EventModel<NotifyModel>(EVENT.NOTIFY_LOAD_CACHE_FAILURE));
                }
            }
        });
    }

    public NotifyModel getCache(){
        Cursor cursor = DatabaseHelper.selectAll(NotifyTable.NAME);
        List<NotifyModel> list = new ArrayList<>();
        if (cursor.moveToNext()){
            NotifyModel model = new NotifyModel();
            model.setNotifyCode(cursor.getString(NotifyTable.ID_NOTIFY_CODE));
            model.setTitle(cursor.getString(NotifyTable.ID_TITLE));
            model.setType(cursor.getString(NotifyTable.ID_TYPE));
            model.setData(cursor.getString(NotifyTable.ID_DATA));
            list.add(model);
        }
        if (list.isEmpty()) return null;
        return list.get(list.size()-1);
    }

    @Override
    public void loadFromNet() {
        final Handler handler = new Handler(Looper.getMainLooper());
        NetManageUtil.get(NotifyApi.NotifyListUrl)
                .addTag(TAG)
                .enqueue(new JsonEntityCallback<NotifyBean>() {
                    @Override
                    public void onSuccess(NotifyBean entity, Headers headers) {
                        final List<NotifyModel> list = new ArrayList<NotifyModel>();
                        list.addAll(Arrays.asList(entity.getMsgList()));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new EventModel<NotifyModel>(EVENT.NOTIFY_REFRESH_SUCCESS,list));
                            }
                        });
                    }

                    @Override
                    public void onFailure(IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new EventModel<NotifyModel>(EVENT.NOTIFY_REFRESH_FAILURE));
                            }
                        });

                    }

                });
    }
}
