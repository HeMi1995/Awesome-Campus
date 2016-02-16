package cn.edu.jxnu.awesome_campus.ui.home;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.edu.jxnu.awesome_campus.R;
import cn.edu.jxnu.awesome_campus.event.EVENT;
import cn.edu.jxnu.awesome_campus.event.EventModel;
import cn.edu.jxnu.awesome_campus.model.home.CourseInfoModel;

public class CourseDetailsDialog extends Activity {

    private TextView courseName;
    private TextView courseID;
    private TextView courseTeacher;
    private TextView courseClass;
    private TextView classmateLink;
    private TextView classForumLink;


    private CourseInfoModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_course_info);
        EventBus.getDefault().register(this);
    }

    private void initView(){
        courseName = findViewById(R.id.course_name);
        courseTeacher = findViewById(R.id.course_id);
        courseClass = findViewById(R.id.course_teacher);
        classmateLink = findViewById(R.id.classmate_list_link);
        classForumLink = findViewById(R.id.class_forum_link);

        courseName.setText(model.getCourseName());
        courseID.setText(model.getCourseID());
        courseTeacher.setText(model.getCourseTeacher());
        courseClass.setText(model.getCourseClass());
        classmateLink.setText(model.getClassmateListLink());
        classForumLink.setText(model.getClassForumLink());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onEventMainThread(EventModel eventModel) {
        if(eventModel.getEventCode() == EVENT.SEND_MODEL_DETAIL){
            model = (CourseInfoModel) eventModel.getData();
            initView();
        }
    }
}
