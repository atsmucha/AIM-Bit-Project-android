package project.jdlp.com.jdlp.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import project.jdlp.com.jdlp.R;

/**
 * Created by atsmucha on 15. 7. 10.
 */
public class GetItemDialog extends Dialog{
    private TextView ok, no;

    public GetItemDialog(Context context) {
        super(context);
        init();
        setGetItemDialogListener();
    }

    public void init() {
        setContentView(R.layout.dialog_get_item);
        setTitle("Get Object");

        ok = (TextView)findViewById(R.id.dialog_get_item_ok);
        no = (TextView)findViewById(R.id.dialog_get_item_no);
    }   //init();

    public void setGetItemDialogListener() {
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnOKClickListener();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnNoClickListener();
            }
        });
    }

    /*
    *   interface
    */

    public interface OnGetItemDialogListener {
        public void OnOKClickListener();
        public void OnNoClickListener();
    }

    OnGetItemDialogListener listener;

    public void setOnGetItemDialogListener(OnGetItemDialogListener listener) {
        this.listener = listener;
    }

}
