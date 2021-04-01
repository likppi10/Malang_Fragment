package bias.hugoandrade.calendarviewapp;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;


public class FragmentA extends Fragment {
    private TextView mMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_a, container, false);

//        mMessage = (TextView) getActivity().findViewById(R.id.txtMessage);

        MyView vw = new MyView(getActivity());

//        setContentView(vw);

        return vw;
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

