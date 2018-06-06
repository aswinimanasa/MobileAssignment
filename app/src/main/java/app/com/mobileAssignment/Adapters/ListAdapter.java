package app.com.mobileAssignment.Adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import app.com.mobileAssignment.R;

/**
 * Created by aswinimanasa
 */

public class ListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<File> imageId;

    public ListAdapter(Activity context, ArrayList<File> imageId) {
        super(context, R.layout.list_item);
        this.context = context;
        this.imageId = imageId;
    }

    @Override
    public int getCount() {
        return imageId.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item, null, true);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageListItem);
        imageView.setImageURI(Uri.fromFile(imageId.get(position)));
        return rowView;
    }
}
