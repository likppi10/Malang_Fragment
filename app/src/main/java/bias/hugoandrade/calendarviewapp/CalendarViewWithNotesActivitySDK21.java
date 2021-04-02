package bias.hugoandrade.calendarviewapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import bias.hugoandrade.calendarviewapp.data.Event;
import bias.hugoandrade.calendarviewapp.data.CALENDAR;
import bias.hugoandrade.calendarviewapp.data.USER;
import bias.hugoandrade.calendarviewapp.helpers.YMDCalendar;
import bias.hugoandrade.calendarviewapp.uihelpers.CalendarDialog;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CalendarViewWithNotesActivitySDK21 extends AppCompatActivity  {

    private final static int CREATE_EVENT_REQUEST_CODE = 100;
    private final static int MOVE_FROM_LOGIN_REQUEST_CODE = 200;

    private String[] mShortMonths;
    private CalendarView mCalendarView;
    private CalendarDialog mCalendarDialog;
    private final List<Event> EventList = new ArrayList<>();
    private final List<CALENDAR> Calendar_List = new ArrayList<>();
    private ListenerRegistration listenerUsers;
    private FirebaseFirestore Firestore= FirebaseFirestore.getInstance();
    private static int shape;

    private USER user = new USER();
    private String CurrentUid;
    private FirebaseUser CurrentUser;
    private int DataYear = 0;
    private int DataMonth = 0;

    FragmentManager manager;
    FragmentB fragmentb;

    String Gender = user.getUSER_Gender() == 0 ? "CALENDAR_GIRL" : "CALENDAR_MAN";
    String O_Gender = user.getUSER_Gender() == 0 ? "CALENDAR_MAN" : "CALENDAR_GIRL";


    public static Intent makeIntent(Context context) { return new Intent(context, CalendarViewWithNotesActivitySDK21.class); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        CurrentUid =CurrentUser.getUid();
        getUserModel(CurrentUid);
    }

    private void initializeUI() {

        setContentView(R.layout.activity_calendar_view_with_notes_sdk_21);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCalendarView = findViewById(R.id.calendarView);

        manager = getSupportFragmentManager();
        fragmentb = new FragmentB();

        DataYear = mCalendarView.getCurrentDate().get(Calendar.YEAR);
        DataMonth = mCalendarView.getCurrentDate().get(Calendar.MONTH)+1;
        AllData(DataYear,DataMonth,0);
        Calendar_Listener(DataYear,DataMonth);

        //월 이동할때의 리스너
        mCalendarView.setOnMonthChangedListener(new CalendarView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int month, int year) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mShortMonths[month]);
                    getSupportActionBar().setSubtitle(Integer.toString(year));

                    //같은게 있을 시 빼야한다.
                    if(DataMonth > (month+1)){
                        AllData(DataYear,DataMonth,-1);
                    }
                    else{
                        AllData(DataYear,DataMonth,1);
                    }
                    DataYear = year;
                    DataMonth = month+1;
                    //내가 지금 보고 있는 곳의 달의 상대방 일정이 들어왔을시 리스너가 감지해 새로운 일정을 Calendar_List에 추가 하고 EventList에도 추가하고 일정바를 만든다.
                    Calendar_Listener(DataYear,DataMonth);
                }
            }
        });

        /* 달력에서 일정의 유무에 다른 날짜 클릭 리스너*/
        mCalendarView.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate,
                                      LinearLayout schedule) {

                Log.d("민규규", "schedule.getResources() = " + schedule.getResources());
                if (calendarObjects.size() != 0) {
                    mCalendarDialog.setSelectedDate(selectedDate);
                    mCalendarDialog.show();


                }
                else {
                    //schedule.setBackgroundResource(R.color.Mint);

                    FragmentTransaction ft = manager.beginTransaction();
                    ft.replace(schedule.getResources(), fragmentb);
                    ft.commit();

//                    if (diffYMD(previousDate, selectedDate) == 0)
//                        createEvent(selectedDate);
                }
            }
        });

        /* 드래그 코드*/
        mCalendarView.setOnItemTouchedListener(new CalendarView.OnItemTouchListener() {
            @Override
            public void onItemTouched(String title, int count, Calendar selectedDate, Calendar startDate, String Uid) {

                CollectionReference collectionReference_man = FirebaseFirestore.getInstance().collection("CALENDAR").document(user.getUSER_CoupleUID()).collection(Gender).document(DataYear + "_" + DataMonth).collection(DataYear + "_" + DataMonth);                // 파이어베이스의 posts에서
                collectionReference_man.whereEqualTo("CALENDAR_UID",Uid).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        CALENDAR old_Calendar = create_event_firebase(document);
                                        Event event = convert_event(old_Calendar);
                                        Drag_remove_Calendar(event,old_Calendar);
                                    }mCalendarDialog.setEventList(EventList);
                                }

                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                final DocumentReference Calendar_Docu =firebaseFirestore.collection("CALENDAR").document(user.getUSER_CoupleUID());
                                final DocumentReference Gender_Docu = Calendar_Docu.collection(Gender).document(DataYear + "_" + DataMonth);
                                final DocumentReference documentReference =Gender_Docu.collection(DataYear + "_" + DataMonth).document(Uid);

                                Calendar endDate = YMDCalendar.toCalendar(new YMDCalendar(selectedDate.get(Calendar.DATE)+count-1,selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.YEAR)));

                                String id = generateID();
                                CALENDAR new_calendar = new CALENDAR(
                                        Uid, id, title,
                                        startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DATE),
                                        endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DATE),
                                        startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DATE),
                                        count
                                );
                                Event new_event = convert_event(new_calendar);
                                storeUpload(documentReference, new_calendar);
                                Event_and_bar_add(new_event,new_calendar);
                                mCalendarDialog.setEventList(EventList);
                            }
                        });
            }
        });

        /* 상단바 */
        if (getSupportActionBar() != null) {
            int month = mCalendarView.getCurrentDate().get(Calendar.MONTH);
            int year = mCalendarView.getCurrentDate().get(Calendar.YEAR);
            getSupportActionBar().setTitle(mShortMonths[month]);
            getSupportActionBar().setSubtitle(Integer.toString(year));
        }

        /* 일정 추가 버튼 -> 캘린더 다이어로그에 추가 하는 사람은 반드시 깔끔하게 지우기*/
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent(mCalendarView.getSelectedDate());
            }
        });

        /* 다이얼로그 내부의 클릭리스너*/
        mCalendarDialog = CalendarDialog.Builder.instance(this)
                .setEventList(EventList)
                .setOnItemClickListener(new CalendarDialog.OnCalendarDialogListener() {
                    @Override
                    public void onEventClick(Event event) {
                        onEventSelected(event);
                    }

                    @Override
                    public void onCreateEvent(Calendar calendar) {
                        createEvent(calendar);
                    }
                    @Override
                    public void onRightClick(Event event,CALENDAR del_Calendar){ Drag_remove_Calendar(event,del_Calendar); mCalendarDialog.setEventList(EventList);}
                })
                .create();
    }

    private static String generateID() {
        return Long.toString(System.currentTimeMillis());
    }

    private void storeUpload(DocumentReference documentReference, final CALENDAR mOriginalEventFirebase) {
        documentReference.set(mOriginalEventFirebase.getScheduleInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void onEventSelected(Event event) {
        Activity context = CalendarViewWithNotesActivitySDK21.this;
        Intent intent = Create_Schadule.Revise_Schadle_Intent(context, event);
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    private void createEvent(Calendar selectedDate) {
        Activity context = CalendarViewWithNotesActivitySDK21.this;
        Intent intent = Create_Schadule.Create_Schadle_Intent(context, selectedDate);
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    //롱클릭 스르륵 기능에 사용할지도 모르니 남겨둠
    private void DragcreateEvent(String title, int count, Calendar selectedDate, String Uid) {
        Activity context = CalendarViewWithNotesActivitySDK21.this;
        Intent intent = DragCreate_Schedule.DragCreate_Schedle_Intent(context, selectedDate);
        intent.putExtra("DragTitle", title);
        intent.putExtra("DragCount", count);
        intent.putExtra("DragEndDate", selectedDate);
        intent.putExtra("DragUid", Uid);
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_calendar_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today: {
                mCalendarView.setSelectedDate(Calendar.getInstance());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 다른 액티비티에서 행동의 결과에 따른 것이며 크리에이트 스케쥴 에서 받아와서 수정및 삭제룰 해야한다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int Create_action = Create_Schadule.extractActionFromIntent(data);
                Event Create_event = Create_Schadule.extractEventFromIntent(data);
                CALENDAR Calendar = Create_Schadule.extractCalendarModelFromIntent(data);

                switch (Create_action) {
                    case Create_Schadule.ACTION_CREATE: {
                        Event_and_bar_add(Create_event,Calendar);
                        mCalendarDialog.setEventList(EventList);
                        break;
                    }

                    case Create_Schadule.ACTION_EDIT: {
//                        Event oldEvent = null;
//                        for (Event e : mEventList) {
//                            if (Objects.equals(event.getID(), e.getID())) {
//                                oldEvent = e;
//                                break;
//                            }
//                        }
//                        if (oldEvent != null) {
//                            mEventList.remove(oldEvent);
//                            mEventList.add(event);
//
//                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));
//                            mCalendarView.addCalendarObject(parseCalendarObject(event));
//                            mCalendarDialog = CalendarDialog.Builder.instance(this)
//                                    .setEventList(mEventList)
//                                    .setOnItemClickListener(new CalendarDialog.OnCalendarDialogListener() {
//                                        @Override
//                                        public void onEventClick(Event event) {
//                                            onEventSelected(event);
//                                        }
//
//                                        @Override
//                                        public void onCreateEvent(Calendar calendar) {
//                                            createEvent(calendar);
//                                        }
//                                    })
//                                    .create();
//                        }
                        break;
                    }
                }
            }
            /*
            롱클릭 드르륵에 사용
            else if (resultCode == DragCreate_Schedule.RESULT_DRAG) {
                int Drag_action = DragCreate_Schedule.extractActionFromIntent(data);
                Event Drag_event = DragCreate_Schedule.extractEventFromIntent(data);
                switch (Drag_action) {
                    case DragCreate_Schedule.ACTION_DRAG: {
                        Event_and_bar_add(Drag_event);
                        mCalendarDialog.setEventList(EventList);
                        break;
                    }
                }
            }
             */
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* 클릭한 날짜와 이전의 날짜를 비교*/
    public static int diffYMD(Calendar date1, Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
            return 0;

        return date1.before(date2) ? -1 : 1;
    }

    /* 여기서 일정의 데이터를 object로 바꿔서
    171번째 줄  mCalendarView.addCalendarObject(parseCalendarObject(event));
    로 달력에 반영*/
    private static CalendarView.CalendarObject parseCalendarObject(Event event) {
        return new CalendarView.CalendarObject(
                event.getCALENDAR_id(),
                event.getCALENDAR_MarkedDate(),
                event.getCALENDAR_StartDate(),
                event.getCALENDAR_EndDate(),
                event.getCALENDAR_UID(),
                shape,
                event.getCALENDAR_DateCount());
    }

    //Event_firebase 를 생성하는 함수
    CALENDAR create_event_firebase(QueryDocumentSnapshot doc){
        CALENDAR e_firebase = new CALENDAR(
                doc.getData().get("CALENDAR_UID").toString(),
                doc.getData().get("CALENDAR_id").toString(),
                doc.getData().get("CALENDAR_Schedule").toString(),
                YMDCalendar.toCalendar(new YMDCalendar(Integer.parseInt(doc.getData().get("CALENDAR_StartD").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_StartM").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_StartY").toString())
                )),
                YMDCalendar.toCalendar(new YMDCalendar(Integer.parseInt(doc.getData().get("CALENDAR_EndD").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_EndM").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_EndY").toString())
                )),
                YMDCalendar.toCalendar(new YMDCalendar(Integer.parseInt(doc.getData().get("CALENDAR_FixD").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_FixM").toString()),
                        Integer.parseInt(doc.getData().get("CALENDAR_FixY").toString())
                )),
                Integer.parseInt(doc.getData().get("CALENDAR_DateCount").toString())
        );
        return e_firebase;
    }

    //코드상에 사용하는 event로 변환하는 함수(Event_firebase -> Event)
    Event convert_event(CALENDAR CALENDAR){
        Event C_event = new Event(CALENDAR.getCALENDAR_UID(), CALENDAR.getCALENDAR_id(), CALENDAR.getCALENDAR_Schedule(),
                CALENDAR.getCALENDAR_StartDate(), CALENDAR.getCALENDAR_StartDate(), CALENDAR.getCALENDAR_EndDate(), CALENDAR.getCALENDAR_DateCount());
        return C_event;
    }


    Event event_convert_event(Event event, Calendar day, Calendar startday){
        Calendar date = YMDCalendar.toCalendar(new YMDCalendar(day.get(Calendar.DATE), day.get(Calendar.MONTH), day.get(Calendar.YEAR)));
        Event f_event=new Event(event.getCALENDAR_UID(),event.getCALENDAR_id(),event.getCALENDAR_Schedule(),
                startday,startday,event.getCALENDAR_EndDate(), event.getCALENDAR_DateCount());
        f_event.setCALENDAR_MarkedDate(date);
        return f_event;
    }

    //캘린더 바 생성 및 Event_List에 추가
    void Event_and_bar_add(Event event,CALENDAR calendar){
        Calendar_List.add(calendar);
        Calendar startday = event.getCALENDAR_StartDate();
        Calendar endday = event.getCALENDAR_EndDate();
        Calendar markedday = event.getCALENDAR_MarkedDate();
        if(startday.get(Calendar.DATE) == (endday.get(Calendar.DATE)) && startday.get(Calendar.MONTH) == (endday.get(Calendar.MONTH))){
            shape = 0;
            EventList.add(event);
            mCalendarView.addCalendarObject(parseCalendarObject(event));
        } else{
            int count = 0;
            while (!startday.after(endday)){
                if(startday.get(Calendar.DATE) == (endday.get(Calendar.DATE))){    //끝에날
                    shape = 2;
                } else{   //중긴
                    if(count == 0){
                        shape = 1;
                    }else{
                        shape = 3;
                    }
                }
                count++;
                Event c_event = new Event(); //초기화
                c_event = event_convert_event(event,startday,markedday); //변경된 day를 넣어주는 함수
                EventList.add(c_event);
                mCalendarView.addCalendarObject(parseCalendarObject(c_event));
                startday.add(Calendar.DATE,1);
            }startday.add(Calendar.DATE,-count);
        }
    }

    //캘린더 바 삭제 및 캘린더 다이어로그에서 삭제 Calendar_List,EventList 삭제
    public void Drag_remove_Calendar(Event event, CALENDAR old_Calendar){
        Calendar_List.remove(old_Calendar);
        Calendar startday = event.getCALENDAR_StartDate();
        Calendar endday = event.getCALENDAR_EndDate();
        if(startday.get(Calendar.DATE) == (endday.get(Calendar.DATE)) && startday.get(Calendar.MONTH) == (endday.get(Calendar.MONTH))){
            shape = 0;
            EventList.remove(event);
            mCalendarView.removeCalendarObjectByID(parseCalendarObject(event));
        } else{
            ArrayList<Event> oldEvent = new ArrayList<>();
            for (Event e : EventList) {
                if (Objects.equals(event.getCALENDAR_UID(), e.getCALENDAR_UID())) {
                    oldEvent.add(e);
                }
            }
            for(int i = 0; i < oldEvent.size() ; i++){
                EventList.remove(oldEvent.get(i));
                mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent.get(i)));
            }
        }
    }

    void AllData(int DataYear , int DataMonth , int check){
        switch (check) {
            case 0: {
                CollectionReference_Data(Gender,DataYear,DataMonth);
                CollectionReference_Data(Gender,DataYear,DataMonth+1);
                CollectionReference_Data(Gender,DataYear,DataMonth-1);
                CollectionReference_Data(O_Gender,DataYear,DataMonth);
                CollectionReference_Data(O_Gender,DataYear,DataMonth+1);
                CollectionReference_Data(O_Gender,DataYear,DataMonth-1);
                break;
            }

            case 1: {
                CollectionReference_Data(Gender,DataYear,DataMonth+1);
                CollectionReference_Data(O_Gender,DataYear,DataMonth+1);
                break;
            }
            case -1: {
                CollectionReference_Data(Gender,DataYear,DataMonth-1);
                CollectionReference_Data(O_Gender,DataYear,DataMonth-1);
                break;
            }
        }

    }

    public void getUserModel(String CurrentUid){
        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("USER").document(CurrentUid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {  //데이터의 존재여부
                            USER test = new USER();
                            test =  document.toObject(USER.class);
                            user = new USER(test.getUSER_Name(),test.getUSER_Gender(),test.getUSER_NickName(),test.getUSER_BirthY()
                                    ,test.getUSER_BirthM(),test.getUSER_BirthD(),test.getUSER_CoupleUID(),test.getUSER_UID(),test.getUSER_Level());
                            mShortMonths = new DateFormatSymbols().getShortMonths();
                            initializeUI();
                        }
                    }
                }
            }
        });
    }

    public void CollectionReference_Data(String Gender,int DataYear,int DataMonth){
        CollectionReference collectionReference_girl = FirebaseFirestore.getInstance().collection("CALENDAR").document(user.getUSER_CoupleUID()).collection(Gender).document(DataYear + "_" + DataMonth).collection(DataYear + "_" + DataMonth);                // 파이어베이스의 posts에서
        collectionReference_girl.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                CALENDAR new_Calendar  = new CALENDAR();
                                new_Calendar = create_event_firebase(doc);
                                if(Calendar_List.size() == 0){
                                    Event_and_bar_add(convert_event(new_Calendar),new_Calendar);  //바를 만들고 eventList에 추가한다.
                                }
                                else{
                                    int i = 0;
                                    for(i = 0 ; i < Calendar_List.size() ; i++) {
                                        if (Calendar_List.get(i).getCALENDAR_UID().equals(new_Calendar.getCALENDAR_UID())) {
                                            break;
                                        }
                                    }
                                    if(i == Calendar_List.size()){
                                        Event_and_bar_add(convert_event(new_Calendar),new_Calendar);  //바를 만들고 eventList에 추가한다.
                                        break;
                                    }
                                }
                            }
                            mCalendarDialog.setEventList(EventList);
                        }
                    }
                });
    }

    public void Calendar_Listener(int DataYear,int DataMonth){
        listenerUsers = Firestore.collection("CALENDAR").document(user.getUSER_CoupleUID()).collection(O_Gender).document(DataYear + "_" + DataMonth).collection(DataYear + "_" + DataMonth)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) { return; }
                        Toast.makeText(getApplicationContext(), "상대방 일정이 들어왔음", Toast.LENGTH_LONG).show();
                        for (QueryDocumentSnapshot doc : value) {
                            CALENDAR new_Calendar = new CALENDAR();
                            new_Calendar = create_event_firebase(doc);//민규가 만든 파이어베이스에서 정보 받아온걸로 이벤트 파이어베이스모델에 넣는 함수
                            if(Calendar_List.size() == 0){
                                Event_and_bar_add(convert_event(new_Calendar),new_Calendar);  //바를 만들고 eventList에 추가한다.
                            }
                            else{
                                int i = 0;
                                for(i = 0 ; i < Calendar_List.size() ; i++) {
                                    if (Calendar_List.get(i).getCALENDAR_UID().equals(new_Calendar.getCALENDAR_UID())) {
                                        break;
                                    }
                                }
                                if(i == Calendar_List.size()){
                                    Event_and_bar_add(convert_event(new_Calendar),new_Calendar);  //바를 만들고 eventList에 추가한다.
                                    break;
                                }
                            }
                        }
                        mCalendarDialog.setEventList(EventList);
                    }
                });
    }

}