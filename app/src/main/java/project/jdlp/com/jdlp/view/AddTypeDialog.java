package project.jdlp.com.jdlp.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import project.jdlp.com.jdlp.R;

/**
 * Created by atsmucha on 15. 7. 10.
 */
public class AddTypeDialog extends Dialog {
    private TextView takePic, getPic, pickPic;
    private TextView ok, no;

    public AddTypeDialog(Context context) {
        super(context);
//        , android.R.style.Theme_Translucent_NoTitleBar
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setAddTypeDialogListener();
    }

    public void init() {
//        WindowManager.LayoutParams windowParam = new WindowManager.LayoutParams();
//        windowParam.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        windowParam.dimAmount = 0.7f;
//        getWindow().setAttributes(windowParam);
        setContentView(R.layout.dialog_add_type);
        setTitle("Set Picture");

        takePic = (TextView)findViewById(R.id.dialog_add_type_take_pic);
        getPic = (TextView)findViewById(R.id.dialog_add_type_get_pic);
        pickPic = (TextView)findViewById(R.id.dialog_add_type_pick_pic);
        ok = (TextView)findViewById(R.id.dialog_add_type_ok);
        no = (TextView)findViewById(R.id.dialog_add_type_no);

    }   //init();

    public void setAddTypeDialogListener() {

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnTackPicClickListener();
            }
        });

        getPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnGetPicClickListener();
            }
        });

        pickPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnPickPicClickListener();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }   //setListener()

    /*
    *   Interface
    *   Send Data AddFragment
    */
    public interface OnAddTypeDialogListener {
        public void OnTackPicClickListener();
        public void OnGetPicClickListener();
        public void OnPickPicClickListener();
    }

    OnAddTypeDialogListener listener;
    public void setOnAddTypeDialogListener(OnAddTypeDialogListener listener) {
        this.listener = listener;
    }

}   //AddTypeDialog
