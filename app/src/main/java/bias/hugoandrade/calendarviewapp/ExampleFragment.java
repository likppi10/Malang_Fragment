 package bias.hugoandrade.calendarviewapp;

 import android.annotation.SuppressLint;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.widget.TextView;

 import androidx.annotation.IntDef;
 import androidx.annotation.Nullable;
 import androidx.fragment.app.Fragment;
 import androidx.fragment.app.FragmentTransaction;

 import com.google.android.material.snackbar.Snackbar;

 import butterknife.BindView;
 import butterknife.ButterKnife;
 import butterknife.OnClick;


 /**
  * @author kakajika
  * @since 2015/11/27
  */
 public class ExampleFragment extends Fragment {

     @IntDef({NONE, CUBE})
     public @interface AnimationStyle {}
     public static final int NONE     = 0;
     public static final int CUBE     = 2;

     @IntDef({NODIR, UP, DOWN, LEFT, RIGHT})
     public @interface AnimationDirection {}
     public static final int NODIR = 0;
     public static final int UP    = 1;
     public static final int DOWN  = 2;
     public static final int LEFT  = 3;
     public static final int RIGHT = 4;

     private static final long DURATION = 500;

     //시작은 큐브로 세팅함
     @AnimationStyle
     private static int sAnimationStyle = CUBE;

//     @BindView(R.id.textAnimationStyle)
//     TextView mTextAnimationStyle;

     public static ExampleFragment newInstance(@AnimationDirection int direction) {
         ExampleFragment f = new ExampleFragment();
         f.setArguments(new Bundle());
         f.getArguments().putInt("direction", direction);
         return f;
     }

     @Nullable
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_a, container, false);
         int color = Color.rgb((int) Math.floor(Math.random() * 128) + 64,
                               (int) Math.floor(Math.random() * 128) + 64,
                               (int) Math.floor(Math.random() * 128) + 64);
         view.setBackgroundColor(color);
         ButterKnife.bind(this, view);
         //setAnimationStyleText();
         return view;
     }

     @Override
     public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
         switch (sAnimationStyle) {
             case CUBE:
                 Log.d("파이어어어", " 확인2 : " + getArguments().getInt("direction"));
                 switch (getArguments().getInt("direction")) {
                     case UP:
                         return CubeAnimation.create(CubeAnimation.UP, enter, DURATION);
                     case DOWN:
                         return CubeAnimation.create(CubeAnimation.DOWN, enter, DURATION);
                     case LEFT:
                         return CubeAnimation.create(CubeAnimation.LEFT, enter, DURATION);
                     case RIGHT:
                         return CubeAnimation.create(CubeAnimation.RIGHT, enter, DURATION);
                 }
                 break;
             case NONE:
                 break;
         }
         return null;
     }

     /*
     @SuppressWarnings("unused")
     @OnClick(R.id.buttonUp)
     void onButtonUp() {
         getArguments().putInt("direction", UP);
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.replace(R.id.layout_fragment, ExampleFragment.newInstance(UP));
         ft.commit();
     }

     @SuppressWarnings("unused")
     @OnClick(R.id.buttonDown)
     void onButtonDown() {
         getArguments().putInt("direction", DOWN);
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.replace(R.id.layout_fragment,ExampleFragment.newInstance(DOWN));
         ft.commit();
     }

     @SuppressWarnings("unused")
     @OnClick(R.id.buttonLeft)
     void onButtonLeft() {
         getArguments().putInt("direction", LEFT);
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.replace(R.id.layout_fragment, ExampleFragment.newInstance(LEFT));
         ft.commit();
     }

     @SuppressWarnings("unused")
     @OnClick(R.id.buttonRight)
     void onButtonRight() {
         getArguments().putInt("direction", RIGHT);
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.replace(R.id.layout_fragment, ExampleFragment.newInstance(RIGHT));
         ft.commit();
     }
     * */



//     @SuppressWarnings("unused")
//     @OnClick(R.id.textAnimationStyle)
//     public void switchAnimationStyle(View view) {
//         @AnimationStyle int[] styles;
//         styles = new int[]{CUBE};
//         for (int i = 0; i<styles.length-1; ++i) {
//             if (styles[i] == sAnimationStyle) {
//                 setAnimationStyle(styles[i+1]);
//                 return;
//             }
//         }
//         setAnimationStyle(CUBE);
//     }

//     @SuppressWarnings("unused")
//     @OnClick(R.id.buttonLeft)
//     void onButtonLeft() {
//         getArguments().putInt("direction", LEFT);
//         FragmentTransaction ft = getFragmentManager().beginTransaction();
//         ft.replace(R.id.layout_fragment, ExampleFragment.newInstance(LEFT));
//         ft.commit();
//     }

     public void setAnimationStyle(@AnimationStyle int style) {
         if (sAnimationStyle != style) {
             sAnimationStyle = style;
             //setAnimationStyleText();  //이름 적기
             Snackbar.make(getView(), "Animation Style is Changed", Snackbar.LENGTH_SHORT).show();
         }
     }

     /*@SuppressLint("SetTextI18n")
     private void setAnimationStyleText() {
         switch (sAnimationStyle) {
             case NONE:
                 mTextAnimationStyle.setText("None");
                 break;
             case CUBE:
                 mTextAnimationStyle.setText("Cube");
                 break;
         }
     }
     * */


 }
