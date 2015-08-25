package project.jdlp.com.jdlp.view;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import project.jdlp.com.jdlp.MainActivity;
import project.jdlp.com.jdlp.R;
import project.jdlp.com.jdlp.model.JdlpItem;
import project.jdlp.com.jdlp.model.LogicStatus;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class AddFragment extends Fragment {
    private View view;
    private MainActivity activity;
    public ImageView itemImage;
    public EditText title, pass;
    private TextView ok, no;
    private JdlpItem addFragItem;

    private AddTypeDialog addTypeDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add, container, false);

        init();
        setAddFragmentListener();

        return view;
    }

    public void init() {
        activity = (MainActivity)getActivity();
        title = (EditText)view.findViewById(R.id.fragment_add_titleEdit);
        pass = (EditText)view.findViewById(R.id.fragment_add_passEdit);
        itemImage = (ImageView)view.findViewById(R.id.fragment_add_itemImage);
        ok = (TextView)view.findViewById(R.id.fragment_add_okText);
        no = (TextView)view.findViewById(R.id.fragment_add_noText);
        listener.OnAddFragmentSyncView(title, pass, itemImage);
        activity.setOnMainActivityItemTranseListener(new MainActivity.OnMainActivityItemTranseListener() {
            @Override
            public void OnMainActivityItemTranseListener(JdlpItem item) {
                addFragItem = item;
                title.setText(addFragItem.getItemTitle());
                pass.setText(addFragItem.getItemPassword());
                itemImage.setImageBitmap(addFragItem.getItemBitmap());
            }
        });

        if(addTypeDialog == null) {
            addTypeDialog = new AddTypeDialog(getActivity());
        }
    }   //init()

    public void setAddFragmentListener() {

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemImageClickListener();
                addTypeDialog.show();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnAddFragmentNoClickListener();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("ADDfrag::", addFragItem.getItemPosition()+"");
                if(addFragItem == null) {
                    if(title.getText().toString().length() != 0
                            && pass.getText().toString().length() != 0) {
                        //  JOB_PUSH_ITEM_RAND
//                        addFragItem.setItemTitle(title.getText().toString());
//                        addFragItem.setItemBitmap(((BitmapDrawable)(itemImage.getDrawable())).getBitmap());
//                        listener.OnAddFragmentOkClickListener(addFragItem, LogicStatus.JOB_PUSH_ITEM_RAND);
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "타이틀과 이미지를 추가해 주세요", Toast.LENGTH_SHORT).show();
                        activity.jdlpPager.setCurrentItem(0);
                    }
                } else {
                    if(title.getText().toString().length() != 0) {
                        //  JOB_PUSH_ITEM
                        if (addFragItem.getItemTitle() == null
                                && addFragItem.getItemBitmap() == null
                                && addFragItem.getItemPassword() == null) {
                            addFragItem.setItemTitle(title.getText().toString());
                            addFragItem.setItemPassword(pass.getText().toString());
                            addFragItem.setItemBitmap(((BitmapDrawable)(itemImage.getDrawable())).getBitmap());
                            listener.OnAddFragmentOkClickListener(addFragItem, LogicStatus.JOB_PUSH_ITEM);

                        } else if (!title.getText().equals(addFragItem.getItemTitle())
                                &&!pass.getText().equals(addFragItem.getItemPassword())
                                && ((BitmapDrawable)(itemImage.getDrawable())).getBitmap() != addFragItem.getItemBitmap()) {
                            //  JOB_MOD_ITEM
                            addFragItem.setItemTitle(title.getText().toString());
                            addFragItem.setItemPassword(pass.getText().toString());
                            addFragItem.setItemBitmap(((BitmapDrawable)(itemImage.getDrawable())).getBitmap());
                            listener.OnAddFragmentOkClickListener(addFragItem, LogicStatus.JOB_MOD_ITEM);
                        } else {
                            Toast.makeText(activity.getApplicationContext(), "변경할 타이틀과 이미지를 변경해 주세요", Toast.LENGTH_SHORT).show();
                            activity.jdlpPager.setCurrentItem(0);
                        }
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "타이틀과 이미지를 추가해 주세요", Toast.LENGTH_SHORT).show();
                        activity.jdlpPager.setCurrentItem(0);
                    }

                }
                addFragItem = null;

            }
        });
    }   //setAddFragmentListener()


    public void itemImageClickListener() {

        addTypeDialog.setOnAddTypeDialogListener(new AddTypeDialog.OnAddTypeDialogListener() {
            @Override
            public void OnTackPicClickListener() {
                listener.OnAddFragmentTackPicClickListener(addTypeDialog, addFragItem);
            }

            @Override
            public void OnGetPicClickListener() {
                listener.OnAddFragmentGetPicClickListener(addTypeDialog, addFragItem);
            }

            @Override
            public void OnPickPicClickListener() {
                Toast.makeText(getActivity(), "리소스 가져오기", Toast.LENGTH_SHORT).show();Toast.makeText(getActivity(), "사진 찍기", Toast.LENGTH_SHORT).show();
                listener.OnAddFragmentPickPicClickListener(addTypeDialog, addFragItem);
            }
        });
    }   //itemImageClickListener()

    /*
        *   interface
        *   Send Data MainActivity
        */
    public interface onAddFragmentClickListener {
        void OnAddFragmentNoClickListener();
        void OnAddFragmentOkClickListener(JdlpItem jdlpItem, LogicStatus addFragLogicStatus);
        void OnAddFragmentTackPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem);
        void OnAddFragmentGetPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem);
        void OnAddFragmentPickPicClickListener(AddTypeDialog addTypeDialog, JdlpItem jdlpItem);
        void OnAddFragmentSyncView(EditText title, EditText pass, ImageView itemImage);
    }
    onAddFragmentClickListener listener;

    public void setOnAddFragmentClickListener(onAddFragmentClickListener listener) {
        this.listener = listener;
    }

}
