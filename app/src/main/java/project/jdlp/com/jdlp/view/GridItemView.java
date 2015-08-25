package project.jdlp.com.jdlp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import project.jdlp.com.jdlp.R;
import project.jdlp.com.jdlp.model.JdlpItem;

/**
 * Created by atsmucha on 15. 7. 9.
 */
public class GridItemView extends FrameLayout{
    public TextView title;
    public ImageView itemImage;
    public JdlpItem jdlpItem;

    public GridItemView(Context context) {
        super(context);
        init();
    }
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_fragment_main, this);
        title = (TextView)findViewById(R.id.fragment_main_griditem_title);
        itemImage = (ImageView)findViewById(R.id.fragment_main_griditem_image);
    }

    public void setData(JdlpItem jdlpItem) {
        this.jdlpItem = jdlpItem;
        title.setText(jdlpItem.getItemTitle());
        itemImage.setImageBitmap(jdlpItem.getItemBitmap());
    }
}
