package cn.edu.jxnu.awesome_campus.database.dao.home;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Headers;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.edu.jxnu.awesome_campus.InitApp;
import cn.edu.jxnu.awesome_campus.database.DatabaseHelper;
import cn.edu.jxnu.awesome_campus.database.dao.DAO;
import cn.edu.jxnu.awesome_campus.support.htmlparse.education.CourseTableExtraInfo;
import cn.edu.jxnu.awesome_campus.support.spkey.EducationStaticKey;
import cn.edu.jxnu.awesome_campus.database.table.home.CourseTable;
import cn.edu.jxnu.awesome_campus.event.EVENT;
import cn.edu.jxnu.awesome_campus.event.EventModel;
import cn.edu.jxnu.awesome_campus.model.education.CourseScoreModel;
import cn.edu.jxnu.awesome_campus.model.home.CourseTableModel;
import cn.edu.jxnu.awesome_campus.support.htmlparse.education.CourseTableParse;
import cn.edu.jxnu.awesome_campus.support.urlconfig.Urlconfig;
import cn.edu.jxnu.awesome_campus.support.utils.common.SPUtil;
import cn.edu.jxnu.awesome_campus.support.utils.common.TimeUtil;
import cn.edu.jxnu.awesome_campus.support.utils.net.NetManageUtil;
import cn.edu.jxnu.awesome_campus.support.utils.net.callback.StringCallback;

/**
 * Created by MummyDing on 16-2-3.
 * GitHub: https://github.com/MummyDing
 * Blog: http://blog.csdn.net/mummyding
 */
public class CourseTableDAO implements DAO<CourseTableModel> {
    public static final String TAG="CourseTableDAO";
    final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public boolean cacheAll(List<CourseTableModel> list) {

        if (list == null || list.size() == 0){
            return false;
        }

        clearCache();

        for(int i=0 ; i < list.size() ; i++){
            CourseTableModel model = list.get(i);
            ContentValues values = new ContentValues();
            values.put(CourseTable.DAY_OF_WEEK,model.getDayOfWeek());
            values.put(CourseTable.TERM,model.getTerm());
            values.put(CourseTable.ONE_TWO,model.getOneTwo());
            values.put(CourseTable.THREE,model.getThree());
            values.put(CourseTable.FOUR,model.getFour());
            values.put(CourseTable.SIX_SEVEN,model.getSixSeven());
            values.put(CourseTable.EIGHT_NINE,model.getEightNine());
            values.put(CourseTable.NIGHT,model.getNight());
            DatabaseHelper.insert(CourseTable.NAME,values);
        }
        return true;
    }

    @Override
    public boolean clearCache() {
        DatabaseHelper.clearTable(CourseTable.NAME);
        return true;
    }

    @Override
    public void loadFromCache() {
        Cursor cursor = DatabaseHelper.selectAll(CourseTable.NAME);

        final List<CourseTableModel> list = new ArrayList<>();

        while (cursor.moveToNext()){
            CourseTableModel model = new CourseTableModel();
            model.setDayOfWeek(cursor.getInt(CourseTable.ID_DAY_OF_WEEK));
            model.setTerm(cursor.getString(CourseTable.ID_TERM));
            model.setOneTwo(cursor.getString(CourseTable.ID_ONE_TWO));
            model.setThree(cursor.getString(CourseTable.ID_THREE));
            model.setFour(cursor.getString(CourseTable.ID_FOUR));
            model.setFive(cursor.getString(CourseTable.ID_FIVE));
            model.setSixSeven(cursor.getString(CourseTable.ID_SIX_SEVEN));
            model.setEightNine(cursor.getString(CourseTable.ID_EIGHT_NINE));
            model.setNight(cursor.getString(CourseTable.ID_NIGHT));
            list.add(model);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!list.isEmpty()){
                    // 发送成功消息
                    EventBus.getDefault().post(new EventModel<CourseTableModel>(EVENT.COURSE_TABLE_LOAD_CACHE_SUCCESS,list));
                }else{
                    // 发送失败消息
                    EventBus.getDefault().post(new EventModel<CourseTableModel>(EVENT.COURSE_TABLE_LOAD_CACHE_FAILURE));
                }
            }
        });

    }

    @Override
    public void loadFromNet() {

        SPUtil spu = new SPUtil(InitApp.AppContext);
        String cookies = "_ga=GA1.3.609810117.1451115712; ASP.NET_SessionId=" +
                spu.getStringSP(EducationStaticKey.SP_FILE_NAME, EducationStaticKey.BASE_COOKIE) +
                ";JwOAUserSettingNew=" +
                spu.getStringSP(EducationStaticKey.SP_FILE_NAME, EducationStaticKey.SPECIAL_COOKIE);
        NetManageUtil.post(Urlconfig.CourseTable_URL)
                .addHeader("Cookie", cookies)
                .addParams("__EVENTTARGET", CourseTableExtraInfo.__EVENTTARGET)
                .addParams("__EVENTARGUMENT",CourseTableExtraInfo.__EVENTARGUMENT)
                .addParams("__VIEWSTATE",CourseTableExtraInfo.__VIEWSTATE)
                .addParams("__EVENTVALIDATION",CourseTableExtraInfo.__EVENTVALIDATION)
                .addParams("_ctl1:ddlSterm", TimeUtil.getTerm())
//                .addParams("_ctl1:ddlSterm", "2015/3/1 0:00:00")
                .addParams("_ctl1:btnSearch","确定")
                .addTag(TAG).enqueue(new StringCallback() {
            @Override
            public void onSuccess(String result, Headers headers) {
               CourseTableParse myParse = new CourseTableParse(result);
                final List<CourseTableModel> list = myParse.getEndList();
                //for (int i = 0; i < list.size(); i++)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (list != null) {
                            Log.e(TAG,"课程表获取成功");
                            cacheAll(list);
                            EventBus.getDefault().post(new EventModel<CourseTableModel>(EVENT.COURSE_TABLE_REFRESH_SUCCESS, list));
                        } else {
                            Log.e(TAG,"课程表获取失败，解析结果列表为空");
                            EventBus.getDefault().post(new EventModel<CourseTableModel>(EVENT.COURSE_TABLE_REFRESH_FAILURE));
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG,"课程表获取失败");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new EventModel<CourseScoreModel>(EVENT.COURSE_TABLE_REFRESH_FAILURE));

                    }
                });
            }
        });
    }
}
