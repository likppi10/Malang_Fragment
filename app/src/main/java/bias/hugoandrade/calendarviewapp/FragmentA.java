package bias.hugoandrade.calendarviewapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class FragmentA extends Fragment {
    private TextView mMessage;

    public FragmentA() {}
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a, container, false);

        LinearLayout first_schedule = view.findViewById(R.id.first_schedule);
        first_schedule.setBackgroundResource(R.color.Orange);
//        mMessage = (TextView) getActivity().findViewById(R.id.txtMessage);

        //MyView vw = new MyView(getActivity());

//        setContentView(vw);

        return view;
    }



    public void sendMessage(String msg) {
        mMessage.setText(msg);
    }


    class MyView extends View {

        public MyView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
            canvas.drawColor(Color.LTGRAY);
            Paint Pnt = new Paint();
            Pnt.setColor(Color.BLUE);
            canvas.drawCircle(100,100,80,Pnt);
        }
    }
}

